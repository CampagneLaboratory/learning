package edu.cornell.med.icb.learning.weka;

import edu.cornell.med.icb.learning.ClassificationProblem;
import it.unimi.dsi.fastutil.ints.IntSet;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
/*
 * Copyright (C) 2001-2002 Mount Sinai School of Medicine
 * Copyright (C) 2003-2007 Institute for Computational Biomedicine,
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

/**
 * @author: Fabien Campagne Date: Nov 21, 2007 Time: 6:26:40 PM
 */
public class WekaProblem implements ClassificationProblem {
	Instances dataset;
	private FastVector attributes;
	private int instanceIndex;
	private int labelIndex = 0;

	public WekaProblem() {

	}

	public WekaProblem(final WekaProblem wekaProblem, final IntSet keepInstanceSet) {
		//dataset = new Instances(this.toString(), dataset.);
		for (int instanceIndex = 0; instanceIndex < wekaProblem.getSize(); instanceIndex++) {

		}
	}

	public double getLabel(final int instanceIndex) {
		return dataset.instance(instanceIndex).value(dataset.attribute(labelIndex));
	}

	public int getSize() {
		return dataset.numInstances();
	}

	public ClassificationProblem filter(final IntSet keepInstanceSet) {
		return new WekaProblem(this, keepInstanceSet);

	}

	public ClassificationProblem filter(final int instanceIndex) {
		return null;	//
	}

	public void setInstance(int instanceIndex, double label, double features[]) {
		setLabel(instanceIndex, label);
		for (int featureIndex = 0; featureIndex < features.length; featureIndex++) {
			double featureValue = features[featureIndex];
			setFeature(instanceIndex, featureIndex, featureValue);
		}

	}

	public void setLabel(int instanceIndex, double label) {
		dataset.instance(instanceIndex).setValue(dataset.attribute(labelIndex), label);
	}

	public void setFeature(final int instanceIndex, final int featureIndex, final double featureValue) {
		Instance instance = dataset.instance(instanceIndex);
		instance.setValue(featureIndex + 1, featureIndex);
	}

	public int addInstance(final int maxNumberOfFeatures) {
		createDataset(maxNumberOfFeatures);
		final Instance instance = new Instance(maxNumberOfFeatures + 1); // +1 for label as first attribute
		dataset.add(instance);
		instance.setDataset(dataset);
		return instanceIndex++;
	}

	public void prepareNative() {
		// do nothing.
	}

	private void createDataset(final int maxNumberOfFeatures) {
		if (dataset == null) {
			final FastVector labelValues = new FastVector(2); // two classes
			labelValues.addElement("negativeClass");
			labelValues.addElement("positiveClass");
			Attribute labelAttribute = new Attribute("label", labelValues);
			attributes.addElement(labelAttribute);
			for (int i = 0; i < maxNumberOfFeatures; i++) {
				Attribute attribute = new Attribute("feature" + i);
				attributes.addElement(attribute);
			}
			dataset = new Instances(this.toString(), attributes, 0);
		}
	}
}
