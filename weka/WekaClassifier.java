package edu.cornell.med.icb.learning.weka;

import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.Classifier;
import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ClassificationParameters;
import org.apache.log4j.Logger;
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
 * @author: Fabien Campagne Date: Nov 23, 2007 Time: 1:25:19 PM
 */
public class WekaClassifier implements Classifier {

	private weka.classifiers.Classifier delegate;
	private WekaParameters defaultParameters;
	public static Logger log = Logger.getLogger(WekaClassifier.class);
	final private double[] labelIndex2LabelValue = {-1d, 1d};

	public void setParameters(ClassificationParameters parameters) {
		assert parameters instanceof WekaParameters : "parameters must be Weka parameters.";
		this.defaultParameters = (WekaParameters) parameters;
	}

	public WekaClassifier() {
		defaultParameters = new WekaParameters();
	}

	public ClassificationProblem newProblem(int size) {
		return new WekaProblem();
	}

	public ClassificationModel train(ClassificationProblem problem, ClassificationParameters parameters) {
		defaultParameters = getWekaParameters(parameters);
		instanciateClassifier();
		try {
			delegate.setOptions(defaultParameters.getNative());
//			System.out.println("weka Problem: "+getWekaProblem(problem));
			delegate.buildClassifier(getWekaProblem(problem));
			return new WekaModel();
		} catch (Exception e) {
			log.error("Weka classifier has thrown exception.", e);
			return null;
		}
	}

	private void instanciateClassifier() {
		final String wekaClassifierClassName = defaultParameters.getWekaClassifierClassName();

		if (delegate == null) {
			try {
				Class clazz = Class.forName(wekaClassifierClassName);

				Object newInstance = clazz.newInstance();
				assert newInstance instanceof weka.classifiers.Classifier : "weka classifier must implement weka.classifiers.Classifier";
				delegate = (weka.classifiers.Classifier) newInstance;
			} catch (ClassNotFoundException e) {
				log.error("Cannot find class for weka classifier classname=" + wekaClassifierClassName, e);
			} catch (IllegalAccessException e) {
				log.error(e);
			} catch (InstantiationException e) {
				log.error(e);
			}

		}
		assert delegate != null : "Could not instance weka classifier for class name=" + wekaClassifierClassName;
	}

	public ClassificationModel train(ClassificationProblem problem) {
		return train(problem, defaultParameters);
	}

	private WekaParameters getWekaParameters(final ClassificationParameters parameters) {
		assert parameters instanceof WekaParameters : "parameters must be weka parameters.";
		return ((WekaParameters) parameters);
	}

	public double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
						  final int instanceIndex) {
		assert trainingModel instanceof WekaModel : "Model must be a weka model.";
		try {
			return labelIndex2LabelValue[(int) getWekaClassifier(this, trainingModel)
					.classifyInstance(getWekaProblem(problem).instance(instanceIndex))];
		} catch (Exception e) {
			log.error("Weka classifier has thrown exception.", e);
			return Double.NaN;
		}
	}

	public double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
						  final int instanceIndex, double[] probabilities) {
		assert trainingModel instanceof WekaModel : "Model must be a weka model.";
		try {
			double[] probs
					= getWekaClassifier(this, trainingModel)
					.distributionForInstance(getWekaProblem(problem).instance(instanceIndex));
			System.arraycopy(probs, 0, probabilities, 0, probs.length);

			double maxProb = Double.NEGATIVE_INFINITY;
			int maxIndex = -1;
			for (int labelIndex = 0; labelIndex < probabilities.length; labelIndex++) {
				if (probabilities[labelIndex] > maxProb) {
					maxProb = probabilities[labelIndex];
					maxIndex = labelIndex;
				}
			}
			return labelIndex2LabelValue[maxIndex];
		} catch (Exception e) {
			log.error("Weka classifier has thrown exception.", e);
			return Double.NaN;
		}
	}

	public ClassificationParameters getParameters() {
		return defaultParameters;
	}

	private Instances getWekaProblem(final ClassificationProblem problem) {
		assert problem instanceof WekaProblem : "problem must be weka problem.";
		return ((WekaProblem) problem).getNative();
	}

	private weka.classifiers.Classifier getWekaClassifier(final WekaClassifier wekaClassifier,
														  final ClassificationModel trainingModel) {
		return wekaClassifier.delegate;

	}
}
