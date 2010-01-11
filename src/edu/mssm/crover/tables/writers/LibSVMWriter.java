/*
 * Copyright (C) 2001-2002 Mount Sinai School of Medicine
 * Copyright (C) 2003-2010 Institute for Computational Biomedicine,
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

package edu.mssm.crover.tables.writers;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import edu.cornell.med.icb.R.RConnectionPool;
import edu.cornell.med.icb.learning.ContingencyTable;
import edu.cornell.med.icb.learning.tools.svmlight.EvaluationMeasure;
import edu.cornell.med.icb.util.RandomAdapter;
import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Converts a table to LibSVM representation.
 *
 * @author Fabien Campagne Date: Feb 28, 2006 Time: 3:33:37 PM
 * @deprecated Use the classifier framework in edu.cornell.med.icb.learning instead.
 */
@Deprecated
public class LibSVMWriter extends SVMLightWriter {
    private svm_parameter parameters;
    private svm_model model;
    private int numberOfTrainingExamples;
    private boolean tableScanned;
    private static final Log LOG = LogFactory.getLog(LibSVMWriter.class);

    public LibSVMWriter(final Table table, final String labelColumnIdf,
                        final List<Set<String>> labelValueGroups)
            throws InvalidColumnException {
        super(table, labelColumnIdf, labelValueGroups);
        init(table);
        this.randomEngine = new MersenneTwister();
        this.randomAdapter = new RandomAdapter(randomEngine);
    }

    public LibSVMWriter(final Table table, final String labelColumnIdf,
                        final List<Set<String>> labelValueGroups,
                        final RandomEngine randomEngine)
            throws InvalidColumnException, TypeMismatchException {
        super(table, labelColumnIdf, labelValueGroups, randomEngine);
        this.randomAdapter = new RandomAdapter(randomEngine);
        init(table);
    }

    public LibSVMWriter(final Table processedTable, final String idrefColumnName)
            throws TypeMismatchException, InvalidColumnException {
        super(processedTable, idrefColumnName, null, null);
    }

    private void init(final Table table) {
        problem = new svm_problem();
        parameters = new svm_parameter();
        parameters.svm_type = svm_parameter.C_SVC;
        parameters.kernel_type = svm_parameter.LINEAR;
        parameters.degree = 3;
        parameters.gamma = 0;   // 1/k
        parameters.coef0 = 0;
        parameters.nu = 0.5;
        parameters.cache_size = 100;
        parameters.C = 1;
        parameters.eps = 1e-3;
        parameters.p = 0.1;
        parameters.shrinking = 1;   // use shrinking heuristic.
        parameters.probability = 0;
        // set weights according to proportion in the training set:
        parameters.nr_weight = 0;
        parameters.weight_label = new int[parameters.nr_weight];
        parameters.weight = new double[parameters.nr_weight];
        /* parameters.weight_label[0] = 1;
                 parameters.weight[0] = numPositive;
                 parameters.weight_label[1] = -1;
                 parameters.weight[1] = numNegative;
                  */
        numberOfTrainingExamples = table.getRowNumber();
        problem.l = numberOfTrainingExamples;
        problem.y = new double[problem.l];
        problem.x = new svm_node[problem.l][0];

        currentRowIndex = 0;
    }

    public svm_model trainModel() {
        assert tableScanned : " Must process rows of input table first.";
        return svm.svm_train(problem, parameters);
    }

    /**
     * Initialize the model with a previoulsy trained model.
     *
     * @param model The model to use from now on.
     */
    public void setModel(final svm_model model) {
        this.model = model;
    }

    /**
     * Train SVM on entire training set and report evaluation measures on training set.
     *
     * @return
     */
    public EvaluationMeasure trainEvaluate() {
        assert tableScanned : " Must process rows of input table first.";
        final svm_model trainingModel = svm.svm_train(problem, parameters);
        final ContingencyTable ctable = new ContingencyTable();

        for (int i = 0; i < numberOfTrainingExamples; i++) {
            final double decision =
                    svm.svm_predict(trainingModel, problem.x[i]);

            final double trueLabel = problem.y[i];
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
        assert tableScanned : " Must process rows of input table first.";
        final ContingencyTable ctable = new ContingencyTable();
        final double[] decisionValues = new double[numberOfTrainingExamples];
        final double[] labels = new double[numberOfTrainingExamples];

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
        final EvaluationMeasure measure = convertToEvalMeasure(ctable);
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
     * @deprecated See Classifier framework.
     */
    @Deprecated
    public static double areaUnderRocCurveLOO(final double[] decisionValues, final double[] labels) {
        assert decisionValues.length == labels.length : "number of predictions must match number of labels.";
        for (int i = 0; i < labels.length; i++) {   // for each training example, leave it out:
            if (decisionValues[i] < 0) {
                decisionValues[i] = 0;
            }
        }
        // CALL R ROC
        final RConnectionPool connectionPool = RConnectionPool.getInstance();
        RConnection connection = null;

        try {
            connection = connectionPool.borrowConnection();
            connection.assign("predictions", decisionValues);
            connection.assign("labels", labels);
            final REXP expression = connection.eval(
                    " library(ROCR) \n"
                            + "pred.svm <- prediction(predictions, labels)\n" +
                            "perf.svm <- performance(pred.svm, 'auc')\n"
                            + "attr(perf.svm,\"y.values\")[[1]]");  // attr(perf.rocOutAUC,"y.values")[[1]]

            final double valueROC_AUC = expression.asDouble();
            // System.out.println("result from R: " + valueROC_AUC);
            return valueROC_AUC;
        } catch (Exception e) {
            // connection error or otherwise me
            LOG.warn(
                    "Cannot calculate area under the ROC curve. Make sure Rserve (R server) is configured and running.",
                    e);
            return Double.NaN;
        } finally {
            if (connection != null) {
                connectionPool.returnConnection(connection);
            }
        }
    }

    RandomAdapter randomAdapter;

    /**
     * Run cross-validation with k folds.
     *
     * @param k Number of folds for cross validation. Typical values are 5 or 10.
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
        assert tableScanned : " Must process rows of input table first.";
        assert k <= numberOfTrainingExamples : "Number of folds must be less or equal to number of training examples.";
        final IntList indices = new IntArrayList();
        for (int f = 0; f < k; ++f) {
            for (int i = 0; i < numberOfTrainingExamples / k; ++i) {
                indices.add(f);
            }
        }
        Collections.shuffle(indices, randomAdapter);

        final int[] splitIndex = new int[numberOfTrainingExamples];
        indices.toArray(splitIndex);

        final ContingencyTable ctable = new ContingencyTable();
        for (int f = 0; f < k; ++f) { // use each fold as test set while the others are the training set:
            final IntSet trainingSet = new IntArraySet();
            final IntSet testSet = new IntArraySet();
            for (int i = 0; i < numberOfTrainingExamples; i++) {   // assign each training example to a fold:
                if (f == splitIndex[i]) {
                    testSet.add(i);
                } else {
                    trainingSet.add(i);
                }
            }
            //   System.out.println("trainingSet: " + trainingSet.toString());
            //  System.out.println("splitSpecificTestSet: " + splitSpecificTestSet.toString());
            final svm_problem currentTrainingSet = splitProblem(problem, trainingSet);
            final svm_model looModel = svm.svm_train(currentTrainingSet, parameters);
            final ContingencyTable ctableMicro = new ContingencyTable();

            for (final int testInstanceIndex : testSet) {  // for each test example:
                final double decision = svm.svm_predict(looModel, problem.x[testInstanceIndex]);
                final double trueLabel = problem.y[testInstanceIndex];
                ctable.observeDecision(trueLabel, decision);
                ctableMicro.observeDecision(trueLabel, decision);
            }
            ctableMicro.average();
            // System.out.println("microaverage: " + convertToEvalMeasure(ctableMicro));
        }
        ctable.average();
        //      System.out.println("macroaverage: "+convertToEvalMeasure(ctable));
        return convertToEvalMeasure(ctable);
    }

    private EvaluationMeasure convertToEvalMeasure(final ContingencyTable ctable) {
        return new EvaluationMeasure(ctable);


    }

    /**
     * Returns the problem with one record excluded.
     *
     * @param problem Full size dataset/problem.
     * @param i Index of the record to exclude.
     * @return Reduced problem.
     */
    private svm_problem splitProblem(final svm_problem problem, final int i) {
        final svm_problem reducedProblem = new svm_problem();
        reducedProblem.l = problem.l - 1; // number of records.
        reducedProblem.x = new svm_node[problem.x.length - 1][0];  // features.
        reducedProblem.y = new double[reducedProblem.l];           // decision/label

        System.arraycopy(problem.x, 0, reducedProblem.x, 0, Math.max(0, i));
        System.arraycopy(problem.x, i + 1, reducedProblem.x, i,
                problem.l - i - 1);

        System.arraycopy(problem.y, 0, reducedProblem.y, 0, Math.max(0, i));
        System.arraycopy(problem.y, i + 1, reducedProblem.y, i,
                problem.l - i - 1);
        return reducedProblem;
    }

    /**
     * Returns the subproblem with only instances in the trainingSet.
     *
     * @param problem Full size dataset/problem.
     * @param trainingSet Index of the records to include in the reduced problem.
     * @return Reduced problem.
     */
    private svm_problem splitProblem(final svm_problem problem, final IntSet trainingSet) {
        final svm_problem reducedProblem = new svm_problem();
        reducedProblem.l = trainingSet.size();                      // number of records.
        reducedProblem.x = new svm_node[trainingSet.size()][0];     // features.
        reducedProblem.y = new double[reducedProblem.l];            // decision/label

        int j = 0;
        for (int i = 0; i < problem.x.length; i++) {
            if (trainingSet.contains(i)) {
                reducedProblem.x[j++] = problem.x[i];
            }
        }

        j = 0;
        for (int i = 0; i < problem.y.length; i++) {
            if (trainingSet.contains(i)) {
                reducedProblem.y[j++] = problem.y[i];
            }
        }

        return reducedProblem;
    }


    private svm_problem problem;
    private int currentRowIndex;

    public svm_model getModel() {
        return model;
    }

    @Override
    public void shuffleLabels() {
        super.shuffleLabels();
        currentRowIndex = 0;
        currentShuffledLabelIndex = 0;
        tableScanned = false;
    }

    /**
     * Assemble the svm_problem description.
     *
     * @param table
     * @param ri
     * @throws TypeMismatchException
     * @throws InvalidColumnException
     */
    @Override
    public void processRow(final Table table, final Table.RowIterator ri)
            throws TypeMismatchException, InvalidColumnException {
        tableScanned = true;
        // label:
        int label;
        if (shuffle) {
            label = shuffledLabels[currentShuffledLabelIndex++];
        } else {
            label = recodeLabel(table.getValue(labelColumnIndex, ri));
        }
        if (label == 0) {
            label = -1; // recode 0 -> -1 for libsvm.
        }
        problem.y[currentRowIndex] = label;
        problem.x[currentRowIndex] = new svm_node[columnIndices.length
                - 1]; // label column is not a feature.

        final int numberOfFeatures = columnIndices.length - 1;
        int featureIndex = 1;
        problem.x[currentRowIndex] = new svm_node[numberOfFeatures];
        for (final int columnIndex : columnIndices) {

            if (columnIndex != labelColumnIndex) {

                // features:
                double value = table.getDoubleValue(columnIndex, ri);
                if (value != value) { // NaN case
                    value = 0;
                }
                final svm_node feature = new svm_node();
                feature.index = featureIndex - 1;
                feature.value = value;
                problem.x[currentRowIndex][featureIndex - 1] = feature;
                featureIndex += 1;

            }
        }
        currentRowIndex++;
    }

    public void setC(final double C) {
        this.parameters.C = C;
    }
}
