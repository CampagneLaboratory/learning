/*
 * Copyright (C) 2001-2002 Mount Sinai School of Medicine
 * Copyright (C) 2003-2008 Institute for Computational Biomedicine,
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
import edu.cornell.med.icb.R.RConnectionPool;
import it.unimi.dsi.fastutil.doubles.*;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import java.util.Collections;
import java.io.File;

/**
 * Performs cross-validation for a configurable classifier.
 *
 * @author Fabien Campagne Date: Feb 28, 2006 Time: 3:33:37 PM
 */
public class CrossValidation {
    private static final Logger LOG = Logger.getLogger(CrossValidation.class);

    private ClassificationModel model;
    Classifier classifier;
    ClassificationProblem problem;

    public CrossValidation(final Classifier classifier, final ClassificationProblem problem,
                           final RandomEngine randomEngine) {
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
        final ContingencyTable ctable = new ContingencyTable();

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
        final ContingencyTable ctable = new ContingencyTable();
        final double[] decisionValues = new double[problem.getSize()];
        final double[] labels = new double[problem.getSize()];

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

        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
        measure.setRocAuc(areaUnderRocCurveLOO(decisionValues, labels));
        return measure;
    }

    /**
     * Report the area under the Receiver Operating Characteristic (ROC) curve.
     * See http://pages.cs.wisc.edu/~richm/programs/AUC/
     *
     * @param decisionValues Larger values indicate better confidence that the instance belongs to class 1.
     * @param labels         Values of -1 or 0 indicate that the instance belongs to class 0, values of 1 indicate that the
     *                       instance belongs to class 1.
     * @return ROC AUC
     */
    public static double areaUnderRocCurveLOO(final double[] decisionValues, final double[] labels) {

        assert decisionValues.length == labels.length
                : "number of predictions must match number of labels.";

        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
            if (decisionValues[i] < 0) {
                decisionValues[i] = 0;
            }
            if (labels[i] < 0) {
                labels[i] = 0;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("decisions: " + ArrayUtils.toString(decisionValues));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("labels: " + ArrayUtils.toString(labels));
        }
        Double shortCircuitValue = areaUnderRocCurvShortCircuit(decisionValues, labels);
        if (shortCircuitValue != null) {
            return shortCircuitValue;
        }

        final RConnectionPool connectionPool = RConnectionPool.getInstance();
        RConnection connection = null;

        try {
            // CALL R ROC
            connection = connectionPool.borrowConnection();
            connection.assign("predictions", decisionValues);
            connection.assign("labels", labels);

            // library(ROCR)
            // predictions <- c(1,1,0,1,1,1,1)
            // labels <- c(1,1,1,1,1,0,1)
            // flabels <- factor(labels,c(0,1))
            // pred.svm <- prediction(predictions, flabels)
            // perf.svm <- performance(pred.svm, 'auc')
            // attr(perf.svm,"y.values")[[1]]

            StringBuffer rCommand = new StringBuffer();
            rCommand.append("library(ROCR)\n");
            rCommand.append("flabels <- factor(labels,c(0,1))\n");
            rCommand.append("pred.svm <- prediction(predictions, labels)\n");
            rCommand.append("perf.svm <- performance(pred.svm, 'auc')\n");
            rCommand.append("attr(perf.svm,\"y.values\")[[1]]");  // attr(perf.rocOutAUC,"y.values")[[1]]\
            final REXP expression = connection.eval(rCommand.toString());

            final double valueROC_AUC = expression.asDouble();
            LOG.debug("result from R: " + valueROC_AUC);
            return valueROC_AUC;
        } catch (Exception e) {
            // connection error or otherwise me
            LOG.warn(
                    "Cannot calculate area under the ROC curve. Make sure Rserve (R server) is configured and running.",
                    e);
            return Double.NaN;
        } finally {
            if (connection != null) {
                try {
                    connectionPool.returnConnection(connection);
                } catch (RserveException e) {
                    LOG.warn("Couldn't return connection to the pool", e);
                }
            }
        }
    }

    /**
     * Checks decisionValues and lables and determins if we
     * can short-circuit the value based on pre-defined rules.
     * Returns null if the decision cannot be short-circuited
     * or the value
     *
     * @param decisionValues the decision values
     * @param labels         the label values
     * @return null or a Double value
     */
    public static Double areaUnderRocCurvShortCircuit(
            final double[] decisionValues, final double[] labels) {

        VectorDetails decisionValueDetails = new VectorDetails(decisionValues);
        VectorDetails labelDetails = new VectorDetails(labels);

        Double shortCircuitValue = null;
        String debugStr = null;
        if (labelDetails.isAllZeros()) {
            if (decisionValueDetails.isAllPositive()) {
                shortCircuitValue = 0.0;
                debugStr = "++SHORTCIRCUIT: Label all zeros, decision all positive. Returning 0";
            } else if (decisionValueDetails.isAllNegative()) {
                shortCircuitValue = 1.0;
                debugStr = "++SHORTCIRCUIT: Label all zeros, decision all negative. Returning 1";
            } else {
                debugStr = "++SHORTCIRCUIT: Label all zeros, decisions vary. This will fail ROC.";
            }
        } else if (labelDetails.isAllOnes()) {
            if (decisionValueDetails.isAllPositive()) {
                shortCircuitValue = 1.0;
                debugStr = "++SHORTCIRCUIT: Label all ones, decision all positive. Returning 1";
            } else if (decisionValueDetails.isAllNegative()) {
                shortCircuitValue = 0.0;
                debugStr = "++SHORTCIRCUIT: Label all ones, decision all negative. Returning 0";
            } else {
                debugStr = "++SHORTCIRCUIT: Label all ones, decisions vary. This will fail ROC.";
            }
        }

        if (LOG.isDebugEnabled() && debugStr != null) {
            LOG.debug(debugStr);
        }
        return shortCircuitValue;
    }

    /**
     * Report the area under the Receiver Operating Characteristic (ROC) curve. Estimates are done with a leave one out
     * evaluation.
     *
     * @param decisionValues Decision values output by classifier. Larger values indicate more confidence in prediction
     *                       of a positive label.
     * @param labels         Correct label for item, can be 0 (negative class) or +1 (positive class).
     * @return rocCurvefilename where a PDF of the ROC curve has been written.
     */
    public static void plotRocCurveLOO(final double[] decisionValues, final double[] labels, final String rocCurvefilename) {
        assert decisionValues.length == labels.length : "number of predictions must match number of labels.";
        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
            if (decisionValues[i] < 0) {
                decisionValues[i] = 0;
            }
            if (labels[i] < 0) {
                labels[i] = 0;
            }
        }

        final RConnectionPool connectionPool = RConnectionPool.getInstance();
        RConnection connection = null;

        // CALL R ROC
        try {
            // R server only understands unix style path. Convert windows to unix if needed:
            final String filename = rocCurvefilename.replaceAll("[\\\\]", "/");
            //     System.out.println("filename: "+filename);
            final File deleteThis = new File(filename);
            if (deleteThis.exists()) {
                deleteThis.delete();
            }

            connection = connectionPool.borrowConnection();
            connection.assign("predictions", decisionValues);
            connection.assign("labels", labels);
            final String cmd = " library(ROCR) \n"
                    + "pred.svm <- prediction(predictions, labels)\n"
                    + "pdf(\"" + filename + "\", height=5, width=5)\n"
                    + "perf <- performance(pred.svm, measure = \"tpr\", x.measure = \"fpr\")\n"
                    + "plot(perf)\n"
                    + "dev.off()";

            final REXP expression = connection.eval(
                    cmd);  // attr(perf.rocOutAUC,"y.values")[[1]]

            final double valueROC_AUC = expression.asDouble();
            //System.out.println("result from R: " + valueROC_AUC);
        } catch (Exception e) {
            // connection error or otherwise me
            LOG.warn(
                    "Cannot plot ROC curve. Make sure Rserve (R server) is configured and running.",
                    e);
        } finally {
            if (connection != null) {
                try {
                    connectionPool.returnConnection(connection);
                } catch (RserveException e) {
                    LOG.warn("Couldn't return connection to the pool", e);
                }
            }
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
     * @param k            Number of folds for cross validation. Typical values are 5 or 10.
     * @param randomEngine Random engine to use when splitting the training set into folds.
     * @return Evaluation measures.
     */
    public EvaluationMeasure crossValidation(final int k, final RandomEngine randomEngine) {

        this.randomAdapter = new RandomAdapter(randomEngine);
        return this.crossValidation(k);
    }

    /**
     * Run cross-validation with k folds.
     *
     * @param k Number of folds for cross validation. Typical values are 5 or 10.
     * @return Evaluation measures.
     */
    public EvaluationMeasure crossValidation(final int k) {
        assert k <= problem.getSize() : "Number of folds must be less or equal to number of training examples.";
        final int[] foldIndices = assignFolds(k);

        final DoubleList aucValues = new DoubleArrayList();
        final ContingencyTable ctable = new ContingencyTable();
        for (int f = 0; f < k; ++f) { // use each fold as test set while the others are the training set:
            final IntSet trainingSet = new IntArraySet();
            final IntSet testSet = new IntArraySet();
            for (int i = 0; i < problem.getSize(); i++) {   // assign each training example to a fold:
                if (f == foldIndices[i]) {
                    testSet.add(i);
                } else {
                    trainingSet.add(i);
                }
            }

            final ClassificationProblem currentTrainingSet = problem.filter(trainingSet);
            final ClassificationModel looModel = classifier.train(currentTrainingSet);
            final ContingencyTable ctableMicro = new ContingencyTable();

            final double[] decisionValues = new double[testSet.size()];
            final double[] labels = new double[testSet.size()];
            int index = 0;
            final double[] probs = {0d, 0d};
            for (final int testInstanceIndex : testSet) {  // for each test example:

                final double decision = classifier.predict(looModel, problem, testInstanceIndex, probs);
                final double trueLabel = problem.getLabel(testInstanceIndex);
                double maxProb = 0;
                for (double prob : probs) {
                    maxProb = Math.max(prob, maxProb);
                }
                decisionValues[index] = decision * maxProb;
                labels[index] = trueLabel;
                index++;
                final int binaryDecision = decision < 0 ? -1 : 1;
                ctable.observeDecision(trueLabel, binaryDecision);
                ctableMicro.observeDecision(trueLabel, binaryDecision);
            }

            ctableMicro.average();
            final double aucForOneFold = areaUnderRocCurveLOO(decisionValues, labels);
            aucValues.add(aucForOneFold);
        }
        ctable.average();

        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
        measure.setRocAucValues(aucValues);
        return measure;
    }

    /**
     * Calculates semi-random fold assignments. Ideally fold assignments would be as random as possible. Because prediction
     * results on test folds are evaluated with ROCR (to calculate ROC AUC), and because ROCR cannot handle situations
     * where all the labels are only one category (i.e., all class 1 or all class 2), we force folds generated by this
     * method to exclude this situation.
     *
     * @param k Number of folds
     * @return An array where each element is the index of the fold to which the given instance of the training set belongs.
     */
    private int[] assignFolds(int k) {
        final IntList indices = new IntArrayList();
        do {
            indices.clear();
            for (int i = 0; i < problem.getSize(); ++i) {
                //         System.out.println("Assigning instance "+i+ " to fold "+(i % k));
                indices.add(i % k);
            }
            Collections.shuffle(indices, randomAdapter);

        } while (invalidFold(indices, k));
        final int[] splitIndex = new int[problem.getSize()];
        indices.toArray(splitIndex);
        return splitIndex;
    }

    /**
     * Determines if a fold split is valid. See ROCR comment above.
     *
     * @param indices Training instance fold assignments.
     * @param k  Number of folds in the split
     * @return True if the fold is invalid (does not have at least two labels represented)
     * @see #assignFolds
     */
    private boolean invalidFold(IntList indices, int k) {
        problem.prepareNative();
        for (int currentFoldInspected = 0; currentFoldInspected < k; currentFoldInspected++) {
            DoubleSet labels = new DoubleArraySet();
            int instanceIndex = 0;
            for (int foldAssigment : indices) {
                if (foldAssigment == currentFoldInspected) {
                    labels.add(problem.getLabel(instanceIndex));
                }
                instanceIndex++;
            }
            if (labels.size() < 2) return true;
        }
        return false;
    }

    private EvaluationMeasure convertToEvalMeasure(final ContingencyTable ctable) {
        return new EvaluationMeasure(ctable);
    }


    public ClassificationModel getModel() {
        return model;
    }


}