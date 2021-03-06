/*
 * Copyright (C) 2008-2010 Institute for Computational Biomedicine,
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

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import edu.cornell.med.icb.learning.tools.svmlight.EvaluationMeasure;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * Validate functionality of {@link edu.cornell.med.icb.learning.CrossValidation} methods.
 *
 * @author Kevin Dorff
 */
public class TestCrossValidation {
    /**
     * Test roc short circuit.
     * label = 1, decisions positive
     */
    @Test
    public void testRocShortCircuit1() {
        assertEquals(1.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, 1),
                        c(1, 1)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 1, decisions positive
     */
    @Test
    public void testRocShortCircuit2() {
        assertEquals(1.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(0, 0),
                        c(1, 1)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 1, decisions negative
     */
    @Test
    public void testRocShortCircuit3() {
        assertEquals(0.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(-1, -1),
                        c(1, 1)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 0, decisions positive
     */
    @Test
    public void testRocShortCircuit4() {
        assertEquals(0.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, 1),
                        c(0, 0)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 0, decisions positive
     */
    @Test
    public void testRocShortCircuit5() {
        assertEquals(0.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(0, 0),
                        c(0, 0)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 0, decisions negative
     */
    @Test
    public void testRocShortCircuit6() {
        assertEquals(1.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(-1, -1),
                        c(0, 0)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 1, decisions positive
     */
    @Test
    public void testRocShortCircuit7() {
        assertEquals(1.0d,
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, 0),
                        c(1, 1)), 0.0d);
    }

    /**
     * Test roc short circuit.
     * label = 1, decisions vary
     */
    @Test
    public void testRocShortCircuit8() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, -1),
                        c(1, 1)));
    }

    /**
     * Test roc short circuit.
     * label = 0, decisions vary
     */
    @Test
    public void testRocShortCircuit9() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, -1),
                        c(0, 0)));
    }

    /**
     * Test roc short circuit.
     * label varies, decisions vary
     */
    @Test
    public void testRocShortCircuit10() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, -1),
                        c(0, 1)));
    }

    /**
     * Test roc short circuit.
     * label varies, decisions positive
     */
    @Test
    public void testRocShortCircuit11() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, 0),
                        c(0, 1)));
    }

    /**
     * Test roc short circuit.
     * label varies, decisions positive
     */
    @Test
    public void testRocShortCircuit12() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(0, 0),
                        c(0, 1)));
    }

    /**
     * Test roc short circuit.
     * label varies, decisions positive
     */
    @Test
    public void testRocShortCircuit13() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(1, 1),
                        c(0, 1)));
    }

    /**
     * Test roc short circuit.
     * label varies, decisions negative
     */
    @Test
    public void testRocShortCircuit14() {
        assertNull(
                CrossValidation.areaUnderRocCurvShortCircuit(
                        c(-1, -2),
                        c(0, 1)));
    }

    @Test
    public void testROCR() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        measuresToTest.add("fpr");
        measuresToTest.add("tpr");
        measuresToTest.add("fnr");
        measuresToTest.add("acc");
        measuresToTest.add("prec");
        measuresToTest.add("rec");
        measuresToTest.add("mat");
        measuresToTest.add("MCC");
        measuresToTest.add("auc");
        final EvaluationMeasure measure = CrossValidation.testSetEvaluation(new double[]{-1, 1, 1, 1, 1},
                new double[]{0, 1, 1, 1, 1},
                measuresToTest, true);
        assertEquals(0.0d, measure.getPerformanceValueAverage("fpr"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("tpr"), 0.01);
        assertEquals(0.0d, measure.getPerformanceValueAverage("fnr"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("acc"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("prec"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("rec"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("mat"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("MCC"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("auc"), 0.01);
    }

    @Test
    public void testROCR2() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        measuresToTest.add("fpr");
        measuresToTest.add("tpr");
        measuresToTest.add("fnr");
        measuresToTest.add("acc");
        measuresToTest.add("prec");
        measuresToTest.add("rec");
        measuresToTest.add("mat");
        measuresToTest.add("MCC");
        measuresToTest.add("auc");
        final EvaluationMeasure measure = CrossValidation.testSetEvaluation(new double[]{-1.9, -1.2, -1, 1.1, 0.3, 0.6, -0.1},
                new double[]{0, 1, 0, 1, 1, 1, 0},
                measuresToTest, true);
        assertEquals(0.0d, measure.getPerformanceValueAverage("fpr"), 0.01);
        assertEquals(0.75d, measure.getPerformanceValueAverage("tpr"), 0.01);
        assertEquals(0.25d, measure.getPerformanceValueAverage("fnr"), 0.01);
        assertEquals(6.0d / 7.0d, measure.getPerformanceValueAverage("acc"), 0.01);
        assertEquals(1.0d, measure.getPerformanceValueAverage("prec"), 0.01);
        assertEquals(0.75d, measure.getPerformanceValueAverage("rec"), 0.01);
        assertEquals(0.75d, measure.getPerformanceValueAverage("mat"), 0.01);
        assertEquals(0.75d, measure.getPerformanceValueAverage("MCC"), 0.01);
        assertEquals(0.83d, measure.getPerformanceValueAverage("auc"), 0.01);
    }

    @Test
    public void testROCR3() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        measuresToTest.add("MCC");
        final EvaluationMeasure measure = CrossValidation.testSetEvaluation(new double[]{-1.9, -1.2, -1, 1.1, 0.3, 0.6, -0.1},
                new double[]{0, 1, 0, 1, 1, 1, 0},
                measuresToTest, true);

        assertEquals(0.75d, measure.getPerformanceValueAverage("MCC"), 0.01);
    }

    @Test
    public void testAUCSplits() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        final String javaMeasureName = "AUC";
        final String rocrMeasureName = "auc";
        measuresToTest.add(javaMeasureName);

        measuresToTest.add(rocrMeasureName);
        compareJavaImplementationToRocr(measuresToTest, javaMeasureName, rocrMeasureName);
    }

    @Test
    public void testMCCSplits() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        final String javaMeasureName = "MCC";
        final String rocrMeasureName = "mat";
        measuresToTest.add(javaMeasureName);

        measuresToTest.add(rocrMeasureName);
        compareJavaImplementationToRocr(measuresToTest, javaMeasureName, rocrMeasureName);
    }

    @Test
    public void testAccuracyManySplits() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        final String javaMeasureName = "Accuracy";
        final String rocrMeasureName = "acc";
        measuresToTest.add(javaMeasureName);

        measuresToTest.add(rocrMeasureName);
        final EvaluationMeasure measure = compareJavaImplementationToRocr(measuresToTest, javaMeasureName, rocrMeasureName);
        assertEquals(measure.getAccuracy() / 100.0d, measure.getPerformanceValueAverage("Accuracy-zero"), 0.1);
    }

    @Test
    public void testSensitivityManySplits() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        final String javaMeasureName = "Sensitivity";
        final String rocrMeasureName = "sens";
        measuresToTest.add(javaMeasureName);

        measuresToTest.add(rocrMeasureName);
        final EvaluationMeasure measure = compareJavaImplementationToRocr(measuresToTest, javaMeasureName, rocrMeasureName);
   //     assertEquals(measure.getSensitivity() / 100d, measure.getPerformanceValueAverage("Sensitivity-zero"), 0.1);
    }

    @Test
    public void testSpecificityManySplits() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        final String javaMeasureName = "Specificity";
        final String rocrMeasureName = "spec";
        measuresToTest.add(javaMeasureName);

        measuresToTest.add(rocrMeasureName);
        final EvaluationMeasure measure = compareJavaImplementationToRocr(measuresToTest, javaMeasureName, rocrMeasureName);
      //  assertEquals(measure.getSpecificity() / 100d, measure.getPerformanceValueAverage("Specificity-zero"), 0.1);
    }

    private EvaluationMeasure compareJavaImplementationToRocr(final ObjectSet<CharSequence> measuresToTest, final String javaMeasureName, final String rocrMeasureName) {
        final int numRepeats = 50;
        final ObjectList<double[]> decisionsList = new ObjectArrayList<double[]>();
        final ObjectList<double[]> trueLabelsList = new ObjectArrayList<double[]>();
        for (int i = 0; i < numRepeats; i++) {
            final double[] decisions = generateRandomDecisions(100);
            final double[] trueLabels = generateRandomLabels(100, 0.25);
            decisionsList.add(decisions);
            trueLabelsList.add(trueLabels);
        }

        final EvaluationMeasure measure = CrossValidation.testSetEvaluation(decisionsList,
                trueLabelsList,
                measuresToTest, true);

        assertEquals(measure.getPerformanceValueAverage(rocrMeasureName), measure.getPerformanceValueAverage(javaMeasureName+"-zero"), 0.1);
        assertEquals(measure.getPerformanceValueStd(rocrMeasureName), measure.getPerformanceValueStd(javaMeasureName+"-zero"), 0.1);
        return measure;
    }

    @Test
    public void testMCCOneSplit() {
        final ObjectSet<CharSequence> measuresToTest = new ObjectArraySet<CharSequence>();
        measuresToTest.add("MCC");
        measuresToTest.add("mat");

        final double[] decisions = generateRandomDecisions(200);
        final double[] trueLabels = generateRandomLabels(200, 0.25);
        final EvaluationMeasure measure = CrossValidation.testSetEvaluation(decisions, trueLabels,
                measuresToTest, true);

        assertEquals(measure.getPerformanceValueAverage("mat"), measure.getPerformanceValueAverage("MCC-zero"), 0.1);
        assertEquals(measure.getPerformanceValueStd("mat"), measure.getPerformanceValueStd("MCC-zero"), 0.1);
    }


    private double[] generateRandomLabels(final int dimension, final double ratioOfPositives) {
        final double[] labels = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            labels[i] = randomEngine.nextDouble() >= (1 - ratioOfPositives) ? 1 : 0;
        }
        return labels;
    }

    final RandomEngine randomEngine = new MersenneTwister();

    private double[] generateRandomDecisions(final int dimension) {
        final double[] decisions = new double[dimension];
        for (int i = 0; i < dimension; i++) {
            decisions[i] = randomEngine.nextDouble();
        }
        return decisions;
    }

    /* ---- SUPPORT ---- */
    /**
     * Make a double array from the incoming double values.
     *
     * @param data the incoming double values
     * @return the double array
     */
    private static double[] c(final double... data) {
        final double[] out = new double[data.length];
        int pos = 0;
        for (final double d : data) {
            out[pos++] = d;
        }
        return out;
    }
}
