package edu.cornell.med.icb.learning;

/**
 * Loads a regression problem into libSVM data structures.
 * Created by fac2003 on 5/26/15.
 */

import edu.cornell.med.icb.geo.tools.RegressionLabels;
import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.RowProcessor;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;

import java.io.PrintWriter;

/**
 * A class to construct a regression problem from features/labels in a table.
 *
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:55:33 PM
 */
public class LoadRegressionProblem {
    private int currentRowIndex;

    protected double recodeLabel(final Object value) throws InvalidColumnException {
        if (value instanceof String) {
            final String sampleId = ((String) value).intern();

            assert labels.hasLabel(sampleId) : "Sample " + sampleId + " must have a label, but does not.";
            return labels.getLabel(sampleId);

        } else {
            throw new InvalidColumnException("Label must be encoded with a String type.");
        }
    }

    protected PrintWriter writer;
    protected int labelColumnIndex;
    private RegressionLabels labels;

    public void load(final ClassificationProblem problem, final Table table, final String labelColumnIdf,
                     final RegressionLabels labels) throws InvalidColumnException, TypeMismatchException {
        currentRowIndex = 0;
        this.labels = labels;
        labelColumnIndex = table.getColumnIndex(labelColumnIdf);

        final RowProcessor rowProcessor = new RowProcessor(RowProcessor.buildColumnIndices(table, null)) {

            @Override
            public void processRow(final Table table, final Table.RowIterator ri)
                    throws TypeMismatchException, InvalidColumnException {

                // label:
                double label;

                label = recodeLabel(table.getValue(labelColumnIndex, ri));


                final int numberOfFeatures = columnIndices.length - 1;
                final int instanceIndex = problem.addInstance(numberOfFeatures);
                problem.setLabel(instanceIndex, label);

                int featureIndex = 1;
                for (final int columnIndex : columnIndices) {

                    if (columnIndex != labelColumnIndex) {

                        // features:
                        double value = table.getDoubleValue(columnIndex, ri);

                        if (value != value) { // NaN case
                            value = 0;
                        }
                        //  System.out.println(String.format("Loading feature index %d probeId %s",featureIndex-1, table.getIdentifier(columnIndex)));
                        problem.setFeature(instanceIndex, featureIndex - 1, value);
                        featureIndex += 1;
                    }
                }
                currentRowIndex++;
            }

        };
        table.processRows(rowProcessor);
    }


    /**
     * Load a problem for use with a pre-trained model. All columns should be double valued and map exactly to the features
     * of the pre-trained model.
     *
     * @param problem
     * @param table
     * @throws InvalidColumnException
     * @throws TypeMismatchException
     */
    public void load(final ClassificationProblem problem, final Table table) throws InvalidColumnException, TypeMismatchException {
        currentRowIndex = 0;
        final RowProcessor rowProcessor = new RowProcessor(RowProcessor.buildColumnIndices(table, null)) {

            @Override
            public void processRow(final Table table, final Table.RowIterator ri)
                    throws TypeMismatchException {

                // label:
                final double label = 0;   // We don't know what the label is.

                final int numberOfFeatures = columnIndices.length;
                final int instanceIndex = problem.addInstance(numberOfFeatures);
                problem.setLabel(instanceIndex, label);

                for (final int columnIndex : columnIndices) {

                    // features:
                    double value = table.getDoubleValue(columnIndex, ri);

                    if (value != value) { // NaN case
                        value = 0;
                    }
                    problem.setFeature(instanceIndex, columnIndex, value);
                }
                currentRowIndex++;
            }
        };
        table.processRows(rowProcessor);

    }
}
