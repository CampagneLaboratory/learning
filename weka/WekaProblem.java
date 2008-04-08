/*
 * Copyright (C) 2007-2008 Institute for Computational Biomedicine,
 *                         Weill Medical College of Cornell University
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package edu.cornell.med.icb.learning.weka;

import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.FeatureScaler;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Fabien Campagne Date: Nov 21, 2007 Time: 6:26:40 PM
 */
public class WekaProblem implements ClassificationProblem {
    Instances dataset;
    private FastVector attributes;
    private int instanceIndex;
    private int labelIndex = 0;

    public WekaProblem() {

    }

    public WekaProblem(final WekaProblem wekaProblem, final IntSet keepInstanceSet) {
        this(wekaProblem, keepInstanceSet, new FeatureScaler());
    }

    public WekaProblem(final WekaProblem wekaProblem, final IntSet keepInstanceSet,
                       FeatureScaler scaler) {
        dataset = new Instances(this.toString(), (FastVector) wekaProblem.attributes.copy(), keepInstanceSet.size());
        for (int instanceIndex = 0; instanceIndex < wekaProblem.getSize(); instanceIndex++) {
            if (keepInstanceSet.contains(instanceIndex)) {
                Instance copyOfInstance = (Instance) wekaProblem.dataset.instance(instanceIndex).copy();

                int numberOfFeatures = copyOfInstance.numAttributes() - 1;
                for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
                    double featureValue = copyOfInstance.value(featureIndex + 1);
                    copyOfInstance.setValue(featureIndex + 1, scaler.scaleFeatureValue(featureValue, featureIndex));
                }
                dataset.add(copyOfInstance);
            }
        }
        this.dataset.setClassIndex(wekaProblem.dataset.classIndex());
    }

    public double getLabel(final int instanceIndex) {
        return dataset.instance(instanceIndex).value(dataset.attribute(labelIndex)) == 0 ? -1 : 1;
    }

    public int getSize() {
        return dataset.numInstances();
    }

    public ClassificationProblem filter(final IntSet keepInstanceSet) {
        return new WekaProblem(this, keepInstanceSet);

    }

    public ClassificationProblem exclude(final int instanceIndex) {
        final IntSet allButOne = new IntArraySet();
        for (int index = 0; index < dataset.numInstances(); index++) {
            if (index != instanceIndex) {
                allButOne.add(instanceIndex);
            }
        }
        return new WekaProblem(this, allButOne);
    }

    public ClassificationProblem filter(int instanceIndex) {
        IntSet set=new IntArraySet();
        set.add(instanceIndex);
        return filter(set);
    }
    public void setInstance(final int instanceIndex, final double label, final double[] features) {
        setLabel(instanceIndex, label);
        for (int featureIndex = 0; featureIndex < features.length; featureIndex++) {
            final double featureValue = features[featureIndex];
            setFeature(instanceIndex, featureIndex, featureValue);
        }

    }

    public void setLabel(final int instanceIndex, final double label) {
        dataset.instance(instanceIndex)
                .setValue(dataset.attribute(labelIndex), label == 1 ? "positiveClass" : "negativeClass");
    }

    public void setFeature(final int instanceIndex, final int featureIndex, final double featureValue) {
        final Instance instance = dataset.instance(instanceIndex);
        instance.setValue(featureIndex + 1, featureValue);
    }

    public int addInstance(final int maxNumberOfFeatures) {
        createDataset(maxNumberOfFeatures);
        final Instance instance = new Instance(maxNumberOfFeatures + 1); // +1 for label as first attribute
        dataset.add(instance);
        instance.setDataset(dataset);
        return instanceIndex++;
    }

    public void prepareNative() {
        // do nothing. Already stored natively.
    }

    public ClassificationProblem scaleTraining(FeatureScaler scaler) {
        IntSet allInstances = new IntLinkedOpenHashSet();
        int numberOfFeatures = 0;
        for (int i = 0; i < this.dataset.numInstances(); i++) {
            allInstances.add(i);
            numberOfFeatures = Math.max(dataset.instance(i).numAttributes() - 1, numberOfFeatures);

        }

        for (int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {

            scaler.observeFeatureForTraining(numberOfFeatures, featureValues(featureIndex, allInstances), featureIndex);
        }
        throw new InternalError("This method has not been tested");
       // return new WekaProblem(this, allInstances, scaler);
    }

    public ClassificationProblem scaleTestSet(FeatureScaler scaler, int testInstanceIndex) {
        IntSet allInstances = new IntLinkedOpenHashSet();
        allInstances.add(testInstanceIndex);
        throw new InternalError("This method has not been tested");
      //        return new WekaProblem(this, allInstances, scaler);
    }

    public double[] featureValues(int featureIndex, IntSet keepInstanceSet) {
        int instanceIndex = 0;
        DoubleList values = new DoubleArrayList();
        for (int InstanceIndex = 0; instanceIndex < dataset.numInstances(); instanceIndex++) {
            if (keepInstanceSet.contains(instanceIndex)) {
                int attributeIndex = featureIndex + 1;
                values.add(dataset.instance(instanceIndex).value(attributeIndex));
            }
            instanceIndex++;
        }
        throw new InternalError("This method has not been tested");
      //        return values.toDoubleArray();
    }

    public ClassificationProblem scaleFeatures(FeatureScaler scaler, IntSet testSetIndices, boolean trainingMode) {
       throw new InternalError("This method has not been implemented");
    }


    private void createDataset(final int maxNumberOfFeatures) {
        if (dataset == null) {
            final FastVector labelValues = new FastVector(2); // two classes
            labelValues.addElement("negativeClass");
            labelValues.addElement("positiveClass");
            final Attribute labelAttribute = new Attribute("label", labelValues, 0);
            attributes = new FastVector();
            attributes.addElement(labelAttribute);
            for (int i = 0; i < maxNumberOfFeatures; i++) {
                final Attribute attribute = new Attribute("feature" + i, i + 1);
                attributes.addElement(attribute);
            }
            dataset = new Instances(this.toString(), (FastVector) attributes.copy(), 0);
            dataset.setClassIndex(0);
        }
    }

    public Instances getNative() {
        return dataset;
    }
}
