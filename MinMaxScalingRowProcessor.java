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

import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;
import it.unimi.dsi.lang.MutableString;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.moment.Mean;

/**
 * Determine scaling factors based on mean and full range (max-min) of the data. In contrast,
 * PercentileScalingRowProcessor uses median and percentile estimators and may be more stable
 * when applied to an independent test set.
 *
 * @author campagne Date: April 4, 2008
 */
public class MinMaxScalingRowProcessor extends FeatureTableScaler {
    private double[] featureIndex2ScaleMean;
    private double[] featureIndex2ScaleRange;
    private static final Log LOG = LogFactory.getLog(MinMaxScalingRowProcessor.class);

    @Override
    public double scaleFeatureValue(final double featureValue, final int featureIndex) {
        final double mean = featureIndex2ScaleMean[featureIndex];
        final double range = featureIndex2ScaleRange[featureIndex];
        //  System.out.println("scaling.. "+featureIndex +" "+featureValue);
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("training, featureIndex/columnId %d/%s  mean %f range %f",
                    featureIndex, null,
                    mean, range));
        }
        double scaledValue = (range < Math.abs(mean*0.001)) ? (featureValue == mean ? 0 : (featureValue < mean) ? -1 : 1 ):
                2 * (featureValue - mean) / range;
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
            featureIndex2ScaleRange = new double[numFeatures];
        }
        observeStatistics(null, featureIndex, featureValues);
    }

    @Override
    public void processTable(final Table table, final int[] columnIndices)
            throws TypeMismatchException, InvalidColumnException {
        final int numFeatures = getNumberOfFeatures(columnIndices);
        if (LOG.isTraceEnabled()) {
            LOG.trace("numFeatures = " + numFeatures);
        }
        featureIndex2ScaleMean = new double[numFeatures];
        featureIndex2ScaleRange = new double[numFeatures];
        for (final int columnIndex : columnIndices) {
            final double[] columnValues = table.getDoubles(table.getIdentifier(columnIndex));
            final MutableString featureId = new MutableString(table.getIdentifier(columnIndex)).compact();
            if (training) {
                observeStatistics(featureId, columnIndex - 1, columnValues);
            } else {
                // statistics were observed on the training set in a different run. Statistics were restored in
                // probesetScaleMedianMap and probesetScaleRangeMap and are keyed by probesetId.
                // We map feature Id to feature index here:
                final int featureIndex = columnIndex - 1;
                if (LOG.isTraceEnabled()) {
                    LOG.trace("probesetScaleMeanMap size = " + probesetScaleMeanMap.size());
                    LOG.trace("probesetScaleRangeMap size = " + probesetScaleRangeMap.size());
                }
                final double mean = featureIndex2ScaleMean[featureIndex] = probesetScaleMeanMap.get(featureId);
                final double range = featureIndex2ScaleRange[featureIndex] = probesetScaleRangeMap.get(featureId);
                if (LOG.isTraceEnabled()) {
                    LOG.trace(String.format(
                            "scaling on test set, featureIndex/columnId %d/%s range: %f median %f ",
                            featureIndex, featureId, range, mean));
                }
            }

            for (int rowIndex = 0; rowIndex < table.getRowNumber(); rowIndex++) {
                final double scaledValue = scaleFeatureValue(columnValues[rowIndex], columnIndex - 1);
                // put back in a the scaled value.
                columnValues[rowIndex] = scaledValue;
            }
            // put the values back into the table:
            table.getColumnValues(columnIndex).replaceDoublesWith(columnValues);

        }
    }

    public void processMatrix(final double[][] matrix, final int numFeatures)            {
        featureIndex2ScaleMean = new double[numFeatures];
        featureIndex2ScaleRange = new double[numFeatures];
        for (int featureIndex = 0; featureIndex < numFeatures; featureIndex++) {
            final int columnIndex = featureIndex;
            final double[] columnValues = matrix[columnIndex];
            final MutableString featureId = new MutableString(getFeatureIdentifier(columnIndex)).compact();
            if (training) {
                observeStatistics(featureId, featureIndex , columnValues);
            } else {
                // statistics were observed on the training set in a different run. Statistics were restored in
                // probesetScaleMedianMap and probesetScaleRangeMap and are keyed by probesetId.
                // We map feature Id to feature index here:

                final double mean = featureIndex2ScaleMean[featureIndex] = probesetScaleMeanMap.get(featureId);
                final double range = featureIndex2ScaleRange[featureIndex] = probesetScaleRangeMap.get(featureId);
                if (LOG.isTraceEnabled()) {
                    LOG.trace(String.format(
                            "scaling on test set, featureIndex/columnId %d/%s range: %f median %f ",
                            featureIndex, featureId, range, mean));
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
     * This method can be overridden to associate a feature index to a persistent feature identifier.
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

    private void observeStatistics(final MutableString featureId, final int featureIndex, final double[] trimmedArray) {
        final double min = getMin(trimmedArray);
        final double max = getMax(trimmedArray);
        final Mean meanCalculator = new Mean();

        final double mean = meanCalculator.evaluate(trimmedArray);
        final double range = max - min;

        featureIndex2ScaleMean[featureIndex] = mean;
        featureIndex2ScaleRange[featureIndex] = range;
        if (featureId != null) {
            probesetScaleMeanMap.put(featureId, mean);
            probesetScaleRangeMap.put(featureId, range);
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace(String.format(
                    "training, featureIndex/columnId %d/%s lower: %f higher %f mean %f ",
                    featureIndex, featureId, min, max, mean));
        }
    }

    private double getMin(final double[] values) {
        double min = Double.POSITIVE_INFINITY;
        for (final double value : values) {
            min = Math.min(value, min);
        }
        return min;
    }

    private double getMax(final double[] values) {
        double max = Double.NEGATIVE_INFINITY;
        for (final double value : values) {
            max = Math.max(value, max);
        }
        return max;
    }
}