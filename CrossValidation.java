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

package edu.cornell.med.icb.learning;

import cern.jet.random.engine.RandomEngine;
import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ContingencyTable;
import edu.mssm.crover.tables.writers.RandomAdapter;
import edu.mssm.crover.tools.svmlight.EvaluationMeasure;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import java.util.Collections;

/**
 * Performs cross-validation for a configurable classifier.
 *
 * @author Fabien Campagne Date: Feb 28, 2006 Time: 3:33:37 PM
 */
public class CrossValidation {
	public static Logger log = Logger.getLogger(CrossValidation.class);

	private ClassificationModel model;
	Classifier classifier;
	ClassificationProblem problem;

	public CrossValidation(Classifier classifier, ClassificationProblem problem, final RandomEngine randomEngine) {
		this.classifier = classifier;
		this.problem = problem;
		this.randomAdapter = new RandomAdapter(randomEngine);

	}

	public ClassificationModel trainModel() {
		return classifier.train(problem);
	}

	/**
	 * Initialize the ClassificationModel with a previoulsy trained model.
	 *
	 * @param model The ClassificationModel to use from now on.
	 */
	public void setModel(final ClassificationModel model) {
		this.model = model;
	}

	/**
	 * Train SVM on entire training set and report evaluation measures on training set.
	 *
	 * @return
	 */
	public EvaluationMeasure trainEvaluate() {
		final ClassificationModel trainingModel = classifier.train(problem);
		ContingencyTable ctable = new ContingencyTable();

		for (int i = 0; i < problem.getSize(); i++) {
			final double decision =
					classifier.predict(trainingModel, problem, i);

			final double trueLabel = problem.getLabel(i);
			ctable.observeDecision(trueLabel, decision);

		}
		ctable.average();
		return convertToEvalMeasure(ctable);
	}

	/**
	 * Report leave-one out evaluation measures for training set.
	 *
	 * @return
	 */
	public EvaluationMeasure leaveOneOutEvaluation() {
		ContingencyTable ctable = new ContingencyTable();
		final double decisionValues[] = new double[problem.getSize()];
		final double labels[] = new double[problem.getSize()];

		for (int i = 0; i < problem.getSize(); i++) {   // for each training example, leave it out:

			final ClassificationProblem looProblem = problem.filter(i);
			final ClassificationModel looModel = classifier.train(looProblem);
			final double decision = classifier.predict(looModel, problem, i);
			final double trueLabel = problem.getLabel(i);
			decisionValues[i] = decision;
			labels[i] = trueLabel;
			ctable.observeDecision(trueLabel, decision);
		}
		ctable.average();

		EvaluationMeasure measure = convertToEvalMeasure(ctable);
		measure.setRocAuc(areaUnderRocCurveLOO(decisionValues, labels));
		return measure;
	}

	/**
	 * Report the area under the Receiver Operating Characteristic (ROC) curve. Estimates are done with a leave one out
	 * evaluation.
	 *
	 * @param decisionValues
	 * @param labels
	 * @return ROC AUC
	 */
	public static double areaUnderRocCurveLOO(final double[] decisionValues, final double[] labels) {
		assert decisionValues.length == labels.length : "number of predictions must match number of labels.";
		for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
			if (decisionValues[i] < 0) {
				decisionValues[i] = 0;
			}
		}
		// CALL R ROC
		try {
			RConnection connection = new RConnection();
			connection.assign("predictions", decisionValues);
			connection.assign("labels", labels);
			REXP expression = connection.eval(
					" library(ROCR) \n"
							+ "pred.svm <- prediction(predictions, labels)\n" +
							"perf.svm <- performance(pred.svm, 'auc')\n"
							+ "attr(perf.svm,\"y.values\")[[1]]");	 // attr(perf.rocOutAUC,"y.values")[[1]]

			double valueROC_AUC = expression.asDouble();
			//System.out.println("result from R: " + valueROC_AUC);
			connection.close();
			return valueROC_AUC;
		} catch (Exception e) {
			// connection error or otherwise me
			log.warn(
					"Cannot calculate area under the ROC curve. Make sure Rserve (R server) is configured and running.",
					e);
			return Double.NaN;
		}
	}

	/*
			 ContingencyTable ctable = new ContingencyTable();

			for (int i = 0; i < numberOfTrainingExamples; i++) {   // for each training example, leave it out:

				final svm_problem looProblem = splitProblem(problem, i);
				final svm_model looModel = svm.svm_train(looProblem, parameters);
				final double decision = svm.svm_predict(looModel, problem.x[i]);
				final double trueLabel = problem.y[i];
				decisionValues[i] = decision;
				labels[i] = trueLabel;
				ctable.observeDecision(trueLabel, decision);
			}
			ctable.average();
			EvaluationMeasure measure = convertToEvalMeasure(ctable);
			measure.setRocAuc(areaUnderRocCurveLOO(decisionValues, labels));
			return measure;
		  */

	RandomAdapter randomAdapter;

	/**
	 * Run cross-validation with k folds.
	 *
	 * @param k Number of folds for cross validation. Typical values are 5 or 10.
	 * @param randomEngine Random engine to use when splitting the training set into folds.
	 * @return Evaluation measures.
	 */
	public EvaluationMeasure crossValidation(int k, RandomEngine randomEngine) {

		this.randomAdapter = new RandomAdapter(randomEngine);
		return this.crossValidation(k);
	}

	/**
	 * Run cross-validation with k folds.
	 *
	 * @param k Number of folds for cross validation. Typical values are 5 or 10.
	 * @return Evaluation measures.
	 */
	public EvaluationMeasure crossValidation(int k) {
		assert k <= problem.getSize() : "Number of folds must be less or equal to number of training examples.";
		IntList indices = new IntArrayList();
		for (int f = 0; f < k; ++f) {
			for (int i = 0; i < problem.getSize() / k; ++i) {
				indices.add(f);
			}
		}
		Collections.shuffle(indices, randomAdapter);

		int splitIndex[] = new int[problem.getSize()];
		indices.toArray(splitIndex);

		ContingencyTable ctable = new ContingencyTable();
		for (int f = 0; f < k; ++f) { // use each fold as test set while the others are the training set:
			IntSet trainingSet = new IntArraySet();
			IntSet testSet = new IntArraySet();
			for (int i = 0; i < problem.getSize(); i++) {   // assign each training example to a fold:
				if (f == splitIndex[i]) {
					testSet.add(i);
				} else {
					trainingSet.add(i);
				}
			}

			final ClassificationProblem currentTrainingSet = problem.filter(trainingSet);
			final ClassificationModel looModel = classifier.train(currentTrainingSet);
			ContingencyTable ctableMicro = new ContingencyTable();

			for (int testInstanceIndex : testSet) {  // for each test example:
				final double decision = classifier.predict(looModel, problem, testInstanceIndex);
				final double trueLabel = problem.getLabel(testInstanceIndex);
				ctable.observeDecision(trueLabel, decision);
				ctableMicro.observeDecision(trueLabel, decision);
			}
			ctableMicro.average();

		}
		ctable.average();

		return convertToEvalMeasure(ctable);
	}

	private EvaluationMeasure convertToEvalMeasure(ContingencyTable ctable) {
		return new EvaluationMeasure(ctable);
	}


	public ClassificationModel getModel() {
		return model;
	}


}