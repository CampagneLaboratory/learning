/*
 * Copyright (C) 2006-2010 Institute for Computational Biomedicine,
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

package edu.cornell.med.icb.learning.tools.svmlight;

import edu.cornell.med.icb.learning.ContingencyTable;
import edu.cornell.med.icb.stat.ZScoreCalculator;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author campagne Date: Mar 1, 2006 Time: 5:43:14 PM
 */
public class EvaluationMeasure {
    ContingencyTable ctable;
    double rocAuc = Double.NaN;
    double aucStdDev;
    private final Object2ObjectMap<String, DoubleList> name2Values =
            new Object2ObjectOpenHashMap<String, DoubleList>();
    private static final Log LOG = LogFactory.getLog(EvaluationMeasure.class);

    public double getRocAuc() {
        return getPerformanceValueAverage("AUC");
    }

    public double getAucStdDev() {
        return getPerformanceValueStd("AUC");
    }

    /**
     * Area under the roc curve.
     *
     * @param rocAuc
     */
    public void setRocAuc(final double rocAuc) {
        this.rocAuc = rocAuc;
    }

    public EvaluationMeasure(final ContingencyTable ctable) {
        super();
        setContingencyTable(ctable);
    }

    public double getErrorRate() {
        return ctable.getErrorRate();
    }

    public double getFalsePositiveRate() {
        return ctable.getFalsePositiveRate();
    }


    public double getPrecision() {
        return ctable.getPrecision();
    }

    public double getSpecificity() {
        return ctable.getSpecificity();
    }

    public double getRecall() {
        return ctable.getRecall();
    }

    public double getSensitivity() {
        return ctable.getSensitivity();
    }


    public double getFalseNegativeRate() {
        return ctable.getFalseNegativeRate();
    }

    public EvaluationMeasure() {
        super();
        ctable = new ContingencyTable();

    }


    public double getAccuracy() {
        return 100.0d - getErrorRate();
    }

    public double getF1Measure() {
        return ctable.getF1Measure();
    }


    static final NumberFormat FORMATTER;

    static {
        FORMATTER = new DecimalFormat();
        FORMATTER.setMaximumFractionDigits(2);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        buffer.append("accuracy: ");
        buffer.append(FORMATTER.format(getAccuracy()));
        buffer.append(' ');
        buffer.append("precision: ");
        buffer.append(FORMATTER.format(getPrecision()));
        buffer.append(' ');
        buffer.append("recall: ");
        buffer.append(FORMATTER.format(getRecall()));

        buffer.append(" f-1: ");
        buffer.append(FORMATTER.format(getF1Measure()));
        buffer.append(" f-1_STD_DEV: ");
        buffer.append(FORMATTER.format(getF1StdDev()));
        buffer.append(" specificity: ");
        buffer.append(FORMATTER.format(getSpecificity()));
        buffer.append(" sensitivity: ");
        buffer.append(FORMATTER.format(getRecall()));
        buffer.append(" PositivePredictiveValue: ");
        buffer.append(FORMATTER.format(getPositivePredictiveValue()));
        buffer.append(" NegativePredictiveValue: ");
        buffer.append(FORMATTER.format(getNegativePredictiveValue()));
        buffer.append(" ROC_AUC: ");
        buffer.append(FORMATTER.format(this.getRocAuc()));
        buffer.append(" AUC_STD_DEV: ");
        buffer.append(FORMATTER.format(this.getAucStdDev()));
        for (final String measureName : name2Values.keySet()) {
            buffer.append(' ');
            buffer.append(measureName);
            buffer.append(": ");
            buffer.append(FORMATTER.format(this.getPerformanceValueAverage(measureName)));
            buffer.append(' ');
            buffer.append(measureName);
            buffer.append("-std: ");
            buffer.append(FORMATTER.format(this.getPerformanceValueStd(measureName)));
        }
        return buffer.toString();
    }

    /**
     * Returns the maximum number of values recorded for any individual performance measure.
     * If two measures have been evaluated, auc and accuracy, if auc has 10 value and
     * accuracy 20, then 20 is returned.
     *
     * @return
     */
    public int getMaxNumValues() {
        int maxNumValue = 0;
        for (final String key : name2Values.keySet()) {
            maxNumValue = Math.max(name2Values.get(key).size(), maxNumValue);
        }
        return maxNumValue;
    }

    public double getPerformanceValueAverage(final String measureName) {
        double sum = 0;
        final DoubleList values = name2Values.get(measureName.intern());
        if (values == null) {

            return Double.NaN;
        }
        for (final double value : values) {
            sum += value;
        }

        final double value = sum / values.size();
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("returning average value %f for measure %s, "
                    + "averaged over %d distinct values: %s.",
                    value, measureName, values.size(), ArrayUtils.toString(values)));

        }

        return value;

    }

    public double getPerformanceValueStd(final String measureName) {
        final ZScoreCalculator calc = new ZScoreCalculator();
        calc.reset();
        final DoubleList values = name2Values.get(measureName.intern());
        if (values == null || values.size() < 3) {
            return Double.NaN;
        }
        for (final double value : values) {
            calc.observe(value);
        }
        calc.calculateStats();
        return calc.stdDev();
    }

    public double getPositivePredictiveValue() {
        return ctable.getPositivePredictiveValue();
    }

    public double getNegativePredictiveValue() {
        return ctable.getNegativePredictiveValue();
    }

    public static MutableString getHeaders(final char delimiter, final CharSequence[] measures) {
        final MutableString headers = new MutableString();
        headers.append("accuracy");
        headers.append(delimiter);
        headers.append("precision");
        headers.append(delimiter);
        headers.append("recall");
        headers.append(delimiter);
        headers.append("f-1");
        headers.append(delimiter);
        headers.append("f-1_STD_DEV");
        headers.append(delimiter);
        headers.append("specificity");
        headers.append(delimiter);
        // = also called recall, so velues will match recall values exactly.
        headers.append("sensitivity");
        headers.append(delimiter);
        headers.append("PositivePredictiveValue");
        headers.append(delimiter);
        headers.append("NegativePredictiveValue");
        headers.append(delimiter);
        headers.append("ROC_AUC");
        headers.append(delimiter);
        headers.append("AUC_STD_DEV");
        for (final CharSequence measureName : measures) {
            headers.append(delimiter);
            headers.append(measureName);
            headers.append(delimiter);
            headers.append(String.valueOf(measureName)).append("-std");
        }
        return headers;
    }

    /**
     * Return text with the different performance measures, in the order of getHeaders().
     *
     * @param delimiter The character to delimit mesures.
     * @return character delimited classification task description.
     */
    public MutableString getDataAsText(final char delimiter) {
        final MutableString values = new MutableString();

        printValue(delimiter, values, getAccuracy());
        printValue(delimiter, values, getPrecision());
        printValue(delimiter, values, getRecall());
        printValue(delimiter, values, getF1Measure());
        printValue(delimiter, values, getF1StdDev());
        printValue(delimiter, values, getSpecificity());
        printValue(delimiter, values, getSensitivity());
        printValue(delimiter, values, getPositivePredictiveValue());
        printValue(delimiter, values, getNegativePredictiveValue());
        printValue(delimiter, values, getRocAuc());
        printValue(delimiter, values, getAucStdDev());
        for (final String measureName : name2Values.keySet()) {
            printValue(delimiter, values, getPerformanceValueAverage(measureName));
            printValue(delimiter, values, getPerformanceValueStd(measureName));
        }
        return values;
    }

    private void printValue(final char delimiter, final MutableString values, final double value) {
        if (value == value) { // not true when value=NaN
            values.append(FORMATTER.format(value));
        } else {
            values.append("NaN");
        }
        if (delimiter != '\0') {
            values.append(delimiter);
        }
    }

    /**
     * Discrete values of area under the ROC curve, when calculated by cross-validation.
     * There is one value per cross-validation fold.
     */
    DoubleList aucValues;

    public DoubleList getRocAucValues() {
        return aucValues;
    }

    public void setRocAucValues(final DoubleList aucValues) {
        for (final double value : aucValues) {
            addValue("ROC", value);
        }
        this.aucValues = aucValues;
        double average = 0;
        final ZScoreCalculator calc = new ZScoreCalculator();
        calc.reset();
        for (final double auc : aucValues) {
            calc.observe(auc);
        }
        calc.calculateStats();
        average = calc.mean();
        this.rocAuc = average;
        this.aucStdDev = calc.stdDev();
    }

    DoubleList f1Values;
    double f1Average;
    double f1StdDev;

    public DoubleList getF1Values() {
        return f1Values;
    }

    public double getF1StdDev() {
        return f1StdDev;
    }

    public double getF1Average() {
        return f1Average;
    }

    public void setF1Values(final DoubleList f1Values) {
        for (final double value : f1Values) {
            addValue("F-1", value);
        }
        this.f1Values = f1Values;
        double average = 0;
        final ZScoreCalculator calc = new ZScoreCalculator();
        calc.reset();
        for (final double f1Value : f1Values) {
            calc.observe(f1Value);
        }
        calc.calculateStats();
        average = calc.mean();
        this.f1Average = average;
        this.f1StdDev = calc.stdDev();
    }

    public void addValue(final CharSequence performanceValueName, final double value) {
        DoubleList values = name2Values.get(performanceValueName.toString());
        if (values == null) {
            values = new DoubleArrayList();
            name2Values.put(performanceValueName.toString().intern(), values);
        }

        if (value != value) {
            // ignore NaN contributions..
            LOG.debug(String.format("Ignoring NaN contribution to measure %s",
                    performanceValueName));
            return;
        }

        //System.out.printf("Adding %s = %f%n", performanceValueName, value);
        values.add(value);
    }

    public void setContingencyTable(final ContingencyTable ctable) {
        this.ctable = new ContingencyTable();
        this.ctable.setFN(ctable.getFN());
        this.ctable.setFP(ctable.getFP());
        this.ctable.setTP(ctable.getTP());
        this.ctable.setTN(ctable.getTN());

    }

    /**
     * Return the name pf performance measures stored in this array.
     */
    public CharSequence[] getMeasureNames() {
        final ObjectSet<String> keys = name2Values.keySet();
        return keys.toArray(new String[keys.size()]);
    }
}
