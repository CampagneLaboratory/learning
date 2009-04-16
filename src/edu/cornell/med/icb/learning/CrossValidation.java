/*
 * Copyright (C) 2006-2009 Institute for Computational Biomedicine,
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
import edu.cornell.med.icb.R.RConnectionPool;
import edu.cornell.med.icb.stat.MatthewsCorrelationCalculator;
import edu.cornell.med.icb.tools.svmlight.EvaluationMeasure;
import edu.cornell.med.icb.util.RandomAdapter;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleArraySet;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import java.io.File;
import java.util.Collections;

/**
 * Performs cross-validation for a configurable classifier.
 *
 * @author Fabien Campagne Date: Feb 28, 2006 Time: 3:33:37 PM
 */
public class CrossValidation {
    private static final Log LOG = LogFactory.getLog(CrossValidation.class);

    private ClassificationModel model;
    private final Classifier classifier;
    private final ClassificationProblem problem;
    private final ObjectSet<CharSequence> evaluationMeasureNames =
            new ObjectArraySet<CharSequence>();
    private int repeatNumber = 1;
    private RandomAdapter randomAdapter;
    private boolean useRServer;

    private Class<? extends FeatureScaler> featureScalerClass;

    /**
     * Request evaluation of the given performance measure.
     *
     * @param measureName Name of a performance measure supported by ROCR.
     *                    Valid names include:
     *                    acc, err, fpr, fall, tpr, rec, sens, fnr, miss, tnr, spec, ppv, prec, npv, pcfall, pcmiss,
     *                    rpp,  rnp, phi, mat, mi, chisq, odds, lift, f, rch,  auc, prbe, cal,  mxe, rmse, sar, ecost, cost
     *                    See the ROCR documentation for definition of these measures.
     */
    public void evaluateMeasure(final CharSequence measureName) {
        evaluationMeasureNames.add(measureName);
    }

    /**
     * Set the number of cross-validation repeats. When more than 1, repeats are done with different folds and results reported
     * averaged over all the fold repeats.
     *
     * @param repeatNumber
     */
    public void setRepeatNumber(final int repeatNumber) {
        assert repeatNumber >= 1 : "Number of repeats must be at least one.";

        this.repeatNumber = repeatNumber;
    }

    public CrossValidation(final Classifier classifier, final ClassificationProblem problem,
                           final RandomEngine randomEngine) {
        super();
        this.classifier = classifier;
        this.problem = problem;
        this.randomAdapter = new RandomAdapter(randomEngine);
        evaluateMeasure("auc");
        this.useRServer(true);
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
     * transform continuous score into binary labels in int
     * input <0 output -1, input >0, output 1
     * @param decisions  Negative values predict the first class, while positive values predict the second class.
     * @return
     */
    public static IntList convertBinaryLabels(final DoubleList decisions) {
        final IntList binaryDecisions = new IntArrayList ();

        for (int i = 0; i < decisions.size(); i++) {   // for each training example, leave it out:

            final double decision = decisions.getDouble(i);
            final int binaryDecision = decision < 0 ? -1 : 1; //group -1 and group 1
            binaryDecisions.add( binaryDecision);
        }
        return binaryDecisions;
    }
    /**
     * Report evaluation measures for predictions on a test set.
     *
     * @param decisions  Negative values predict the first class, while positive values predict the second class.
     * @param trueLabels label=0 encodes the first class, label=1 the second class.
     * @return
     */
    public static EvaluationMeasure testSetEvaluation(final double[] decisions,
                                                      final double[] trueLabels,
                                                      final ObjectSet<CharSequence> evaluationMeasureNames,
                                                      final boolean useRServer) {
        final ContingencyTable ctable = new ContingencyTable();
        assert decisions.length == trueLabels.length : "decision and label arrays must have the same length.";
        for (int i = 0; i < trueLabels.length; i++) {
            // convert labels to the conventions used by contingency table.
            if (trueLabels[i] == 0) {
                trueLabels[i] = -1;
            }
        }
        final double[] binaryDecisions = new double[decisions.length];
        for (int i = 0; i < decisions.length; i++) {   // for each training example, leave it out:

            final double decision = decisions[i];
            final double trueLabel = trueLabels[i];

            final int binaryDecision = decision < 0 ? -1 : 1;
            binaryDecisions[i] = binaryDecision;
            ctable.observeDecision(trueLabel, binaryDecision);

        }
        ctable.average();
        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
        try {
            evaluate(decisions, trueLabels, evaluationMeasureNames, measure, "", useRServer);
            evaluate(binaryDecisions, trueLabels, evaluationMeasureNames, measure, "binary-", useRServer);
        } catch (Exception e) {
            LOG.warn("cannot evaluate with ROCR");
        }
        return measure;
    }

    /**
     * Report evaluation measures for predictions on a test set.
     *
     * @param decisionList  Negative values predict the first class, while positive values predict the second class.
     * @param trueLabelList label=0 encodes the first class, label=1 the second class.
     * @return
     */
    public static EvaluationMeasure testSetEvaluation(final ObjectList<double[]> decisionList,
                                                      final ObjectList<double[]> trueLabelList,
                                                      final ObjectSet<CharSequence> evaluationMeasureNames,
                                                      final boolean useRServer) {
        final ContingencyTable ctable = new ContingencyTable();

        for (int j = 0; j < decisionList.size(); j++) {
            final double[] decisions = decisionList.get(j);
            final double[] trueLabels = trueLabelList.get(j);

            assert decisions.length == trueLabels.length : "decision and label arrays must have the same length.";
            for (int i = 0; i < trueLabels.length; i++) {
                // convert labels to the conventions used by contingency table.
                if (trueLabels[i] == 0) {
                    trueLabels[i] = -1;
                }
            }
            for (int i = 0; i < decisions.length; i++) {   // for each training example, leave it out:

                final double decision = decisions[i];
                final double trueLabel = trueLabels[i];

                final int binaryDecision = decision < 0 ? -1 : 1;
                ctable.observeDecision(trueLabel, binaryDecision);

            }

        }
        ctable.average();
        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
        try {
            evaluate(decisionList, trueLabelList, evaluationMeasureNames, measure, "", useRServer);
        } catch (Exception e) {
            LOG.warn("cannot evaluate with ROCR");
        }
        return measure;

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

        final FeatureScaler scaler = resetScaler();
        final double[] probs = {0d, 0d};

        for (int testInstanceIndex = 0; testInstanceIndex < problem.getSize(); testInstanceIndex++)
        {   // for each training example, leave it out:

            final ClassificationProblem currentTrainingSet = problem.exclude(testInstanceIndex);

            final ClassificationProblem scaledTrainingSet = currentTrainingSet.scaleTraining(scaler);
            final ClassificationModel looModel = classifier.train(scaledTrainingSet);
            final ClassificationProblem oneScaledTestInstanceProblem = problem.scaleTestSet(scaler, testInstanceIndex);

            final double decision = classifier.predict(looModel, oneScaledTestInstanceProblem, 0, probs);
            final double trueLabel = problem.getLabel(testInstanceIndex);
            decisionValues[testInstanceIndex] = decision;
            labels[testInstanceIndex] = trueLabel;
            final int binaryDecision = decision < 0 ? -1 : 1;
            ctable.observeDecision(trueLabel, binaryDecision);

        }
        ctable.average();
        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
        evaluate(decisionValues, labels, evaluationMeasureNames, measure, "", useRServer);
        return measure;
    }

    /**
     * Setting this flag to false removes the dependency on the R server.
     *
     * @param calculate If True, use an RServer to evaluate area under the roc curve. If False, skip the calculation.
     */
    public void useRServer(final boolean calculate) {
        this.useRServer = calculate;
        if (!useRServer) {
          evaluationMeasureNames.remove("auc");
        }
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
        if (ArrayUtils.isEmpty(decisionValues) || ArrayUtils.isEmpty(labels)) {
            throw new IllegalArgumentException("There must be at least 1 label and predition."
                    + " Predictions are empty: " + ArrayUtils.isEmpty(decisionValues)
                    + " Labels are empty: " + ArrayUtils.isEmpty(labels));
        }

        if (decisionValues.length != labels.length) {
            throw new IllegalArgumentException("number of predictions (" + decisionValues.length
                    + ") must match number of labels (" + labels.length + ").");
        }

        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
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
        final Double shortCircuitValue = areaUnderRocCurvShortCircuit(decisionValues, labels);
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

            final StringBuilder rCommand = new StringBuilder();
            rCommand.append("library(ROCR)\n");
            rCommand.append("flabels <- factor(labels,c(0,1))\n");
            rCommand.append("pred.svm <- prediction(predictions, labels)\n");
            rCommand.append("perf.svm <- performance(pred.svm, 'auc')\n");
            rCommand.append("attr(perf.svm,\"y.values\")[[1]]");  // attr(perf.rocOutAUC,"y.values")[[1]]\
            final REXP expression = connection.eval(rCommand.toString());

            final double valueROC_AUC = expression.asDouble();
            if (LOG.isDebugEnabled()) {
                LOG.debug("result from R: " + valueROC_AUC);
            }
            return valueROC_AUC;
        } catch (Exception e) {
            // connection error or otherwise me
            LOG.warn("Cannot calculate area under the ROC curve. Make sure Rserve (R server) "
                    + "is configured and running.", e);
            return Double.NaN;
        } finally {
            if (connection != null) {
                connectionPool.returnConnection(connection);
            }
        }
    }

    /**
     * Evaluate a variety of performance measures with <a href="http://rocr.bioinf.mpi-sb.mpg.de/ROCR.pdf">ROCR</a>.
     *
     * @param decisionValues Larger values indicate better confidence that the instance belongs to class 1.
     * @param labels         Values of -1 or 0 indicate that the instance belongs to class 0, values of 1 indicate that the
     *                       instance belongs to class 1.
     * @param measureNames   Name of performance measures to evaluate.
     * @param measure        Where performance values will be stored.
     * @see #evaluateMeasure
     */
    public static void evaluate(final double[] decisionValues, final double[] labels,
                                ObjectSet<CharSequence> measureNames,
                                final EvaluationMeasure measure,
                                final CharSequence measureNamePrefix, final boolean useRServer) {
        measureNames = evaluateMCC(decisionValues, labels, measureNames, measure);
      //  measureNames = evaluateSensitivityAndSpecificity(decisionValues, labels, measureNames, measure);

        if (measureNames.size() > 0) { // more measures to evaluate, send to ROCR
            if (useRServer) {
                evaluateWithROCR(decisionValues, labels, measureNames, measure, measureNamePrefix);
            }
        }
    }

    private static ObjectSet<CharSequence> evaluateSensitivityAndSpecificity(double[] decisionValues, double[] labels, ObjectSet<CharSequence> measureNames, EvaluationMeasure measure) {
        if (measureNames.contains("sens") || measureNames.contains("spec")) {
            ContingencyTable ctable = new ContingencyTable();
            int index = 0;
            for (double decision : decisionValues) {
                ctable.observeDecision(labels[index], decision >= 0 ? 1 : -1);
                index++;
            }
            ctable.average();
            if (measureNames.contains("sens")) {
                measure.addValue("sens", ctable.getSensitivity());
            }
            if (measureNames.contains("spec")) {
                measure.addValue("spec", ctable.getSpecificity());
            }

            final ObjectSet<CharSequence> measureNamesFiltered = new ObjectArraySet<CharSequence>();
            measureNamesFiltered.addAll(measureNames);
            if (measureNames.contains("sens")) measureNamesFiltered.remove("sens");
            if (measureNames.contains("spec")) measureNamesFiltered.remove("spec");
            return measureNamesFiltered;
        } else {
            return measureNames;
        }
    }

    public static void evaluate(final ObjectList<double[]> decisionList, final ObjectList<double[]> trueLabelList,
                                ObjectSet<CharSequence> evaluationMeasureNames, final EvaluationMeasure measure,
                                final CharSequence measureNamePrefix, final boolean useRServer) {
        evaluationMeasureNames = evaluateMCC(decisionList, trueLabelList, evaluationMeasureNames, measure);
        if (evaluationMeasureNames.size() > 0) { // more measures to evaluate, send to ROCR
            if (useRServer) {
                for (int i = 0; i < decisionList.size(); i++) {
                    evaluateWithROCR(decisionList.get(i), trueLabelList.get(i), evaluationMeasureNames, measure, measureNamePrefix);
                }

            }
        }
    }

    private static ObjectSet<CharSequence> evaluateMCC(final ObjectList<double[]> decisionValueList, final ObjectList<double[]> trueLabelList,
                                                       final ObjectSet<CharSequence> evaluationMeasureNames, final EvaluationMeasure measure) {
        if (evaluationMeasureNames.contains("MCC")) {
            final MatthewsCorrelationCalculator c = new MatthewsCorrelationCalculator();
            // find optimal threshold across all splits:
            c.thresholdIndependentMCC(decisionValueList, trueLabelList);
            final double optimalThreshold = c.optimalThreshold;
            for (int i = 0; i < decisionValueList.size(); i++) {
                final double mcc = c.evaluateMCC(optimalThreshold, decisionValueList.get(i), trueLabelList.get(i));
                measure.addValue("MCC", mcc);

            }
            final ObjectSet<CharSequence> measureNamesFiltered = new ObjectArraySet<CharSequence>();
            measureNamesFiltered.addAll(evaluationMeasureNames);
            measureNamesFiltered.remove("MCC");
            return measureNamesFiltered;
        } else {
            return evaluationMeasureNames;
        }
    }

    private static ObjectSet<CharSequence> evaluateMCC(final double[] decisionValues,
                                                       final double[] labels, final ObjectSet<CharSequence> measureNames,
                                                       final EvaluationMeasure measure) {
        if (measureNames.contains("MCC")) {
            final MatthewsCorrelationCalculator c = new MatthewsCorrelationCalculator();
            final double mcc = c.thresholdIndependentMCC(decisionValues, labels);
            measure.addValue("MCC", mcc);
            final ObjectSet<CharSequence> measureNamesFiltered = new ObjectArraySet<CharSequence>();
            measureNamesFiltered.addAll(measureNames);
            measureNamesFiltered.remove("MCC");
            return measureNamesFiltered;
        } else {
            return measureNames;
        }
    }


    /**
     * Evaluate a variety of performance measures with <a href="http://rocr.bioinf.mpi-sb.mpg.de/ROCR.pdf">ROCR</a>.
     *
     * @param decisionValues Larger values indicate better confidence that the instance belongs to class 1.
     * @param labels         Values of -1 or 0 indicate that the instance belongs to class 0, values of 1 indicate that the
     *                       instance belongs to class 1.
     * @param measureNames   Name of performance measures to evaluate.
     * @param measure        Where performance values will be stored.
     * @see #evaluateMeasure
     */
    public static void evaluateWithROCR(final double[] decisionValues, final double[] labels,
                                        final ObjectSet<CharSequence> measureNames,
                                        final EvaluationMeasure measure, final CharSequence measureNamePrefix) {

        assert decisionValues.length == labels.length
                : "number of predictions must match number of labels.";

        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
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


        final RConnectionPool connectionPool = RConnectionPool.getInstance();
        RConnection connection = null;
        CharSequence performanceValueName = null;
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

            final StringBuilder rCommand = new StringBuilder();
            rCommand.append("library(ROCR)\n");
            rCommand.append("flabels <- labels\n");
            rCommand.append("pred.svm <- prediction(predictions, labels)\n");

            connection.eval(rCommand.toString());

            for (ObjectIterator<CharSequence> charSequenceObjectIterator = measureNames.iterator();
                 charSequenceObjectIterator.hasNext();) {
                final StringBuilder rCommandMeasure = new StringBuilder();
                performanceValueName = charSequenceObjectIterator.next();
                final CharSequence storedPerformanceMeasureName = measureNamePrefix.toString() + performanceValueName.toString();

                rCommandMeasure.append("perf.svm <- performance(pred.svm, '");
                rCommandMeasure.append(performanceValueName);
                rCommandMeasure.append("')\n");
                rCommandMeasure.append("attr(perf.svm,\"y.values\")[[1]]");
                final REXP expressionValue = connection.eval(rCommandMeasure.toString());


                final double[] values = expressionValue.asDoubles();
                if (values.length == 1) {
                    // this performance measure is threshold independent..
                    LOG.debug("result from R (" + performanceValueName + ") : " + values[0]);
                    measure.addValue(storedPerformanceMeasureName, values[0]);
                } else {
                    // we have one performance measure value per decision threshold.
                    final StringBuilder rCommandThresholds = new StringBuilder();
                    rCommandThresholds.append("attr(perf.svm,\"x.values\")[[1]]");
                    final REXP expressionThresholds = connection.eval(rCommandThresholds.toString());
                    final double[] thresholds = expressionThresholds.asDoubles();

                    // find the index of x.value which indicates a threshold more or equal to zero (for the decision value)
                    int thresholdGEZero = -1;
                    for (int index = thresholds.length - 1; index >= 0; index--) {
                        if (values[index] != values[index]) {
                            continue;
                        }
                        if (thresholds[index] >= 0) {
                            thresholdGEZero = index;
                            break;
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("result from R (" + performanceValueName + ") : "
                                + values[thresholdGEZero]);
                    }
                    if (thresholdGEZero != -1) {
                        measure.addValue(storedPerformanceMeasureName, values[thresholdGEZero]);
                    }
                }
            }
        } catch (Exception e) {
            // connection error or otherwise
            LOG.warn(
                    "Cannot evaluate performance measure " + performanceValueName +
                            ". Make sure Rserve (R server) is configured and running.",
                    e);

        } finally {
            if (connection != null) {
                connectionPool.returnConnection(connection);
            }
        }
    }

    /**
     * Checks decisionValues and labels and determines if we
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
        Double shortCircuitValue = null;
        String debugStr = null;

        if (ArrayUtils.isEmpty(decisionValues) || ArrayUtils.isEmpty(labels)) {
            debugStr = "++SHORTCIRCUIT: No labels or decision values. This will fail ROC.";
        } else {
            final VectorDetails decisionValueDetails = new VectorDetails(decisionValues);
            final VectorDetails labelDetails = new VectorDetails(labels);

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
        }

        if (LOG.isDebugEnabled() && debugStr != null) {
            LOG.debug(debugStr);
        }
        return shortCircuitValue;
    }

    /**
     * Report the area under the Receiver Operating Characteristic (ROC) curve. Estimates are
     * done with a leave one out evaluation.
     *
     * @param decisionValues   Decision values output by classifier. Larger values indicate more
     *                         confidence in prediction of a positive label.
     * @param labels           Correct label for item, can be 0 (negative class) or +1 (positive class).
     * @param rocCurvefilename Name of the file to plot the pdf image to
     */
    public static void plotRocCurveLOO(final double[] decisionValues,
                                       final double[] labels,
                                       final String rocCurvefilename) {
        assert decisionValues.length == labels.length : "number of predictions must match number of labels.";
        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
            if (decisionValues[i] < 0) {
                decisionValues[i] = 0;
            }
            if (labels[i] < 0) {
                labels[i] = 0;
            }
        }

        // R server only understands unix style path. Convert windows to unix if needed:
        final String plotFilename = FilenameUtils.separatorsToUnix(rocCurvefilename);
        final File plotFile = new File(plotFilename);

        final RConnectionPool connectionPool = RConnectionPool.getInstance();
        RConnection connection = null;

        // CALL R ROC
        try {
            if (plotFile.exists()) {
                plotFile.delete();
            }

            connection = connectionPool.borrowConnection();
            connection.assign("predictions", decisionValues);
            connection.assign("labels", labels);
            final String cmd = " library(ROCR) \n"
                    + "pred.svm <- prediction(predictions, labels)\n"
                    + "pdf(\"" + plotFilename + "\", height=5, width=5)\n"
                    + "perf <- performance(pred.svm, measure = \"tpr\", x.measure = \"fpr\")\n"
                    + "plot(perf)\n"
                    + "dev.off()";

            final REXP expression = connection.eval(cmd);  // attr(perf.rocOutAUC,"y.values")[[1]]
            final double valueROC_AUC = expression.asDouble();
            // System.out.println("result from R: " + valueROC_AUC);
        } catch (Exception e) {
            // connection error or otherwise
            LOG.warn("Cannot plot ROC curve to " + plotFilename + ".  Make sure Rserve (R server) "
                    + "is configured and running and the owner of the Rserve process has permission "
                    + "to write to the directory \""
                    + FilenameUtils.getFullPath(plotFile.getAbsolutePath()) + "\"", e);
        } finally {
            if (connection != null) {
                connectionPool.returnConnection(connection);
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
        final ContingencyTable ctable = new ContingencyTable();
        final DoubleList aucValues = new DoubleArrayList();
        final DoubleList f1Values = new DoubleArrayList();
        final EvaluationMeasure measure = new EvaluationMeasure();
        for (int r = 0; r < repeatNumber; r++) {
            assert k <= problem.getSize() : "Number of folds must be less or equal to number of training examples.";
            final int[] foldIndices = assignFolds(k);


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
                assert testSet.size() + trainingSet.size() == problem.getSize() : "test set and training set size must add to whole problem size.";
                final IntSet intersection = new IntOpenHashSet();
                intersection.addAll(trainingSet);
                intersection.retainAll(testSet);
                assert intersection.size() == 0 : "test set and training set must never overlap";

                final ClassificationProblem currentTrainingSet = problem.filter(trainingSet);
                assert currentTrainingSet.getSize() == trainingSet.size() : "Problem size must match size of training set";

                final FeatureScaler scaler = resetScaler();      // reset the scaler for each test set..

                final ClassificationProblem scaledTrainingSet = currentTrainingSet.scaleTraining(scaler);

                final ClassificationModel looModel = classifier.train(scaledTrainingSet);
                final ContingencyTable ctableMicro = new ContingencyTable();

                final double[] decisionValues = new double[testSet.size()];
                final double[] labels = new double[testSet.size()];
                int index = 0;
                final double[] probs = {0d, 0d};
                for (final int testInstanceIndex : testSet) {  // for each test example:

                    //  ClassificationProblem oneScaledTestInstanceProblem = problem.filter(testInstanceIndex);
                    final ClassificationProblem oneScaledTestInstanceProblem = problem.scaleTestSet(scaler, testInstanceIndex);
                    assert oneScaledTestInstanceProblem.getSize() == 1 : "filtered test problem must have one instance left (size was " + oneScaledTestInstanceProblem.getSize() + ").";
                    final double decision = classifier.predict(looModel, oneScaledTestInstanceProblem, 0, probs);
                    final double trueLabel = problem.getLabel(testInstanceIndex);
                    final double maxProb;

                    maxProb = Math.max(probs[0], probs[1]);

                    decisionValues[index] = decision * maxProb;
                    labels[index] = trueLabel;
                    index++;
                    final int binaryDecision = decision < 0 ? -1 : 1;
                    ctable.observeDecision(trueLabel, binaryDecision);
                    ctableMicro.observeDecision(trueLabel, binaryDecision);
                }

                ctableMicro.average();
                f1Values.add(ctableMicro.getF1Measure());
                final double aucForOneFold = Double.NaN;

                evaluate(decisionValues, labels, evaluationMeasureNames, measure, "", useRServer);


                aucValues.add(aucForOneFold);
            }
        }
        ctable.average();

        measure.setContingencyTable(ctable);

        // The below line was previousl commented out? KCD 2008-09-29
        measure.setRocAucValues(aucValues);

        measure.setF1Values(f1Values);
        return measure;
    }

    private FeatureScaler resetScaler() {
        try {
            final FeatureScaler scaler = featureScalerClass.newInstance();
            return scaler;

        } catch (InstantiationException e) {
            LOG.error("Cannot instanciate feature scaler", e);
            return null;
        } catch (IllegalAccessException e) {
            LOG.error("Cannot create feature scaler", e);
            return null;
        }
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
    private int[] assignFolds(final int k) {
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
     * @param k       Number of folds in the split
     * @return True if the fold is invalid (does not have at least two labels represented)
     * @see #assignFolds
     */
    private boolean invalidFold(final IntList indices, final int k) {
        problem.prepareNative();
        for (int currentFoldInspected = 0; currentFoldInspected < k; currentFoldInspected++) {
            final DoubleSet labels = new DoubleArraySet();
            int instanceIndex = 0;
            for (final int foldAssigment : indices) {
                if (foldAssigment == currentFoldInspected) {
                    labels.add(problem.getLabel(instanceIndex));
                }
                instanceIndex++;
            }
            if (labels.size() < 2) {
                return true;
            }
        }
        return false;
    }

    private static EvaluationMeasure convertToEvalMeasure(final ContingencyTable ctable) {
        return new EvaluationMeasure(ctable);
    }


    public ClassificationModel getModel() {
        return model;
    }


    public void evaluateMeasures(final CharSequence... names) {
        for (final CharSequence name : names) {
            evaluateMeasure(name);
        }
    }

    public void setScalerClass(final Class<? extends FeatureScaler> featureScalerClass) {
        this.featureScalerClass = featureScalerClass;
    }
}
