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

package edu.cornell.med.icb.learning.libsvm;

import edu.cornell.med.icb.learning.ClassificationProblem;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import libsvm.svm_node;
import libsvm.svm_problem;

/**
 * Represents a classification problem as a libSVM svm_problem instance.
 *
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:25:40 PM
 */
public class LibSvmProblem implements ClassificationProblem {
    svm_problem problem;
    ObjectList<svm_node[]> instanceList;
    DoubleList labelList;

    public LibSvmProblem() {
        instanceList = new ObjectArrayList<svm_node[]>();
        labelList = new DoubleArrayList();
        problem = null;
    }

    public LibSvmProblem(final svm_problem reducedProblem) {
        this.problem = reducedProblem;
    }

    public double getLabel(final int instanceIndex) {
        assert problem != null : " Native problem must not be null.";
        return problem.y[instanceIndex];
    }

    public int getSize() {
        if (problem != null) {
            return problem.l;
        } else {
            return instanceList.size();
        }
    }

    public ClassificationProblem filter(final IntSet keepInstanceSet) {
        prepareNative();
        final svm_problem reducedProblem = new svm_problem();
        reducedProblem.l = keepInstanceSet.size();                                 // number of records.
        reducedProblem.x =
                new svm_node[keepInstanceSet.size()][0];         // features.
        reducedProblem.y =
                new double[reducedProblem.l];                  // decision/label

        int j = 0;
        for (int i = 0; i < problem.x.length; i++) {
            if (keepInstanceSet.contains(i)) {
                reducedProblem.x[j++] = problem.x[i];
            }
        }

        j = 0;
        for (int i = 0; i < problem.y.length; i++) {
            if (keepInstanceSet.contains(i)) {
                reducedProblem.y[j++] = problem.y[i];
            }
        }

        return new LibSvmProblem(reducedProblem);
    }

    public ClassificationProblem filter(final int instanceIndex) {
        prepareNative();
        final svm_problem reducedProblem = new svm_problem();
        reducedProblem.l = problem.l
                - 1;                                 // number of records.
        reducedProblem.x =
                new svm_node[problem.x.length - 1][0];         // features.
        reducedProblem.y =
                new double[reducedProblem.l];                  // decision/label

        System.arraycopy(problem.x, 0, reducedProblem.x, 0, Math.max(0, instanceIndex));
        System.arraycopy(problem.x, instanceIndex + 1, reducedProblem.x, instanceIndex,
                problem.l - instanceIndex - 1);

        System.arraycopy(problem.y, 0, reducedProblem.y, 0, Math.max(0, instanceIndex));
        System.arraycopy(problem.y, instanceIndex + 1, reducedProblem.y, instanceIndex,
                problem.l - instanceIndex - 1);
        return new LibSvmProblem(reducedProblem);
    }

    public void setInstance(final int instanceIndex, final double label, final double[] features) {
        setLabel(instanceIndex, label);
        for (int featureIndex = 0; featureIndex < features.length; featureIndex++) {

            final svm_node svm_node = getSvmNode(instanceIndex, featureIndex);
            svm_node.index = featureIndex;
            svm_node.value = features[featureIndex];
        }
    }

    public void setLabel(final int instanceIndex, final double label) {
        if (problem != null) {
            problem.y[instanceIndex] = label;
        } else {
            labelList.set(instanceIndex, label);
        }
    }

    public void setFeature(final int instanceIndex,
                           final int featureIndex,
                           final double featureValue) {
        final svm_node svm_node = getSvmNode(instanceIndex, featureIndex);
        svm_node.index = featureIndex;
        svm_node.value = featureValue;
    }

    private svm_node getSvmNode(final int instanceIndex, final int featureIndex) {
        return problem != null ? problem.x[instanceIndex][featureIndex] :
                instanceList.get(instanceIndex)[featureIndex];
    }

    public int addInstance(final int maxNumberOfFeatures) {
        final svm_node[] newInstance = new svm_node[maxNumberOfFeatures];
        for (int i = 0; i < newInstance.length; i++) {
            newInstance[i] = new svm_node();
        }
        instanceList.add(newInstance);
        labelList.add(0);
        return instanceList.size() - 1;
    }

    public void prepareNative() {
        if (problem == null) {
            problem = new svm_problem();
            problem.x = instanceList.toArray(new svm_node[instanceList.size()][]);
            problem.l = instanceList.size();
            problem.y = labelList.toDoubleArray();
        }
    }

    public svm_problem getNative() {
        prepareNative();
        return problem;
    }
}
