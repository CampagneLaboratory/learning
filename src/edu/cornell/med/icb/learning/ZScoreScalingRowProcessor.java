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

package edu.cornell.med.icb.learning;

import edu.cornell.med.icb.stat.ZScoreCalculator;
import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Determine scaling factors based on mean and standard deviation of the data. Scaled features are the z-score of
 * raw features.
 *
 * @author campagne Date: Nov 10 2008
 */
public class ZScoreScalingRowProcessor extends FeatureTableScaler {
    private double[] featureIndex2ScaleMean;
    private double[] featureIndex2ScaleStandardDeviation;
    private static final Log LOG = LogFactory.getLog(PercentileScalingRowProcessor.class);

    @Override
    public double scaleFeatureValue(final double featureValue, final int featureIndex) {
        final double mean = featureIndex2ScaleMean[featureIndex];
        final double stdev = featureIndex2ScaleStandardDeviation[featureIndex];
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("training, featureIndex/columnId %d/%s  median %f range %f",
                    featureIndex, null,
                    mean, stdev));
        }
        double scaledValue = (stdev < Math.abs(mean * 0.001)) ? (featureValue == mean ? 0 : (featureValue < mean) ? -1 : 1) :
                (featureValue - mean) / stdev;
        if (scaledValue != scaledValue) {
            //   System.out.println("NaN");
            scaledValue = 0;
        }
        return scaledValue;
    }

    @Override
    public void observeFeatureForTraining(final int numFeatures, final double[] featureValues, final int featureIndex) {
        if (featureIndex2ScaleMean == null) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(String.format("allocating feature statistics array for %d features, featureIndex: %d",
                        numFeatures, featureIndex));
            }
            featureIndex2ScaleMean = new double[numFeatures];
            featureIndex2ScaleStandardDeviation = new double[numFeatures];
        }
        observeStatistics(null, featureIndex, featureValues);
    }

    private Object2DoubleMap<MutableString> probesetScaleRangeMap;
    private Object2DoubleMap<MutableString> probesetScaleMedianMap;
    private boolean training;

    /**
     * Instruct to scale a training set. While training, mean and range of un-scaled features are collected.  Results are
     * stored in the maps passed as parameters.
     *
     * @param probesetScaleMeanMap
     * @param probesetScaleRangeMap
     */
    @Override
    public void setTrainingMode(final Object2DoubleMap<MutableString> probesetScaleMeanMap,
                                final Object2DoubleMap<MutableString> probesetScaleRangeMap) {
        this.probesetScaleMedianMap = probesetScaleMeanMap;
        this.probesetScaleRangeMap = probesetScaleRangeMap;
        training = true;

    }

    /**
     * Instruct to scale a test set. While testing, mean and range of un-scaled features are read directly from the
     * maps passed as parameters. They are not estimated from the dataset.
     *
     * @param probesetScaleMeanMap
     * @param probesetScaleRangeMap
     */
    @Override
    public void setTestSetMode(final Object2DoubleMap<MutableString> probesetScaleMeanMap,
                               final Object2DoubleMap<MutableString> probesetScaleRangeMap) {
        this.probesetScaleMedianMap = probesetScaleMeanMap;
        this.probesetScaleRangeMap = probesetScaleRangeMap;
        training = false;
    }

    @Override
    public void processTable(final Table table, final int[] columnIndices)
            throws TypeMismatchException, InvalidColumnException {
        final int numFeatures = getNumberOfFeatures(columnIndices);
        this.featureIndex2ScaleMean = new double[numFeatures];
        this.featureIndex2ScaleStandardDeviation = new double[numFeatures];

        for (final int columnIndex : columnIndices) {

            final double[] columnValues = table.getDoubles(table.getIdentifier(columnIndex));

            final double median;
            final double range;
            final MutableString featureId = new MutableString(table.getIdentifier(columnIndex)).compact();
            if (training) {

                observeStatistics(featureId, columnIndex - 1, columnValues);
            } else {
                // statistics were observed on the training set in a different run. Statistics were restored in
                // probesetScaleMedianMap and probesetScaleRangeMap and are keyed by probesetId.
                // We map feature Id to feature index here:
                final int featureIndex = columnIndex - 1;
                median = this.featureIndex2ScaleMean[featureIndex] = probesetScaleMedianMap.get(featureId);
                range = this.featureIndex2ScaleStandardDeviation[featureIndex] = probesetScaleRangeMap.get(featureId);
                if (LOG.isTraceEnabled()) {
                    LOG.trace(String.format(
                            "scaling on test set, featureIndex/columnId %d/%s range: %f median %f ",
                            featureIndex, featureId,
                            range, median));
                }
            }

            for (int rowIndex = 0; rowIndex < table.getRowNumber(); rowIndex++) {
                final double scaledValue = scaleFeatureValue(columnValues[rowIndex], columnIndex - 1);
                // put back in a the scaled value.
                columnValues[rowIndex] = scaledValue;
            }
            // put the values back into the table:
            System.arraycopy(columnValues, 0, table.getColumnValues(columnIndex).getDoubles(), 0, columnValues.length);

        }
    }

    public void processMatrix(final double[][] matrix, final int numFeatures) {


        this.featureIndex2ScaleMean = new double[numFeatures];
        this.featureIndex2ScaleStandardDeviation = new double[numFeatures];
        for (int featureIndex = 0; featureIndex < numFeatures; featureIndex++) {
            final int columnIndex = featureIndex;
            final double[] columnValues = matrix[columnIndex];

            final double mean;
            final double range;
            final MutableString featureId = new MutableString(getFeatureIdentifier(columnIndex)).compact();
            if (training) {

                observeStatistics(featureId, featureIndex, columnValues);
            } else {
                // statistics were observed on the training set in a different run. Statistics were restored in
                // probesetScaleMedianMap and probesetScaleRangeMap and are keyed by probesetId.
                // We map feature Id to feature index here:

                mean = this.featureIndex2ScaleMean[featureIndex] = probesetScaleMeanMap.get(featureId);
                range = this.featureIndex2ScaleStandardDeviation[featureIndex] = probesetScaleRangeMap.get(featureId);
                if (LOG.isTraceEnabled()) {
                    LOG.trace(String.format(
                            "scaling on test set, featureIndex/columnId %d/%s range: %f median %f ",
                            featureIndex, featureId,
                            range, mean));
                }
            }

            for (int rowIndex = 0; rowIndex < columnValues.length; rowIndex++) {
                final double scaledValue = scaleFeatureValue(columnValues[rowIndex], featureIndex);
                // put back in a the scaled value.
                columnValues[rowIndex] = scaledValue;
            }
        }
    }

    /**
     * This method can be overridden to associate a feature index to a persistent feature
     * identifier.
     *
     * @param featureIndex
     * @return a feature id for the provided feature index.
     */
    protected MutableString getFeatureIdentifier(final int featureIndex) {
        return new MutableString(Integer.toString(featureIndex));
    }

    private int getNumberOfFeatures(final int[] columnIndices) {
        int numFeatures = 0;
        for (final int colIndex : columnIndices) {
            numFeatures = Math.max(numFeatures, colIndex);
        }
        return numFeatures;
    }

    private void observeStatistics(final MutableString featureId, final int featureIndex,
                                   final double[] trimmedArray) {
        final ZScoreCalculator calc = new ZScoreCalculator();
        for (final double value : trimmedArray) {
            calc.observe(value);
        }
        calc.calculateStats();

        final double mean = calc.mean();
        final double stdev = calc.stdDev();

        featureIndex2ScaleMean[featureIndex] = mean;
        featureIndex2ScaleStandardDeviation[featureIndex] = stdev;
        if (featureId != null) {

            probesetScaleMedianMap.put(featureId, mean);
            probesetScaleRangeMap.put(featureId, stdev);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format(
                    "training, featureIndex/columnId %d/%s mean %f stdev %f",
                    featureIndex, featureId,
                    mean, stdev));
        }
    }
}
