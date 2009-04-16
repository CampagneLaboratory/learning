/*
 * Copyright (C) 2007-2009 Institute for Computational Biomedicine,
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
import edu.mssm.crover.tables.RowProcessor;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A class to construct a classification problem from features/labels in a table.
 *
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:55:33 PM
 */
public class LoadClassificationProblem {
    private int currentRowIndex;

    protected int recodeLabel(final Object value) throws InvalidColumnException {
        if (value instanceof String) {
            final String labelValue = (String) value;
            for (final Set<String> labelGroup : labelValueGroups) {
                if (labelGroup.contains(labelValue)) {
                    return groupToCodedLabel.get(labelGroup);
                }
            }
            assert false : "Label value " + labelValue + " must match a label group.";
            return 0;
        } else {
            throw new InvalidColumnException("Label must be encoded with a String type.");
        }
    }

    protected PrintWriter writer;
    protected int labelColumnIndex;

    protected int currentShuffledLabelIndex;
    private List<Set<String>> labelValueGroups;
    private Map<Set<String>, Integer> groupToCodedLabel;
    /**
     * Feature disabled.
     */
    final boolean shuffle = false;
    protected final int[] shuffledLabels = null;


    public void load(final ClassificationProblem problem, final Table table, final String labelColumnIdf,
                     final List<Set<String>> labelValueGroups) throws InvalidColumnException, TypeMismatchException {
        currentRowIndex = 0;
        this.labelValueGroups = labelValueGroups;

        // output all columns
        labelColumnIndex = table.getColumnIndex(labelColumnIdf);
        final boolean oneClass;

        this.labelValueGroups = labelValueGroups;
        groupToCodedLabel = new HashMap<Set<String>, Integer>();
        if (labelValueGroups.size() == 2) {
            //: "Classification requires exactly two label groups.";


            final Iterator<Set<String>> it = labelValueGroups.iterator();
            final Set<String> labelGroup0 = it.next();  // negative class
            final Set<String> labelGroup1 = it.next();  // positive class

            groupToCodedLabel.put(labelGroup0, -1);
            groupToCodedLabel.put(labelGroup1, 1);
        } else if (labelValueGroups.size() == 1 || labelValueGroups.size() > 2) {
            //one class or multi-class problem:
            // recode classes as 1, 2, 3, ..
            for (int classIndex = 0; classIndex < labelValueGroups.size(); classIndex++) {
                final Set<String> labelGroupForClassIndex = labelValueGroups.get(classIndex);
                groupToCodedLabel.put(labelGroupForClassIndex, classIndex + 1);
            }
        }
        oneClass = labelValueGroups.size() == 1;


        final RowProcessor rowProcessor = new RowProcessor(RowProcessor.buildColumnIndices(table, null)) {

            @Override
            public void processRow(final Table table, final Table.RowIterator ri)
                    throws TypeMismatchException, InvalidColumnException {

                // label:
                double label;
                if (shuffle) {
                    label = shuffledLabels[currentShuffledLabelIndex++];
                } else {
                    label = recodeLabel(table.getValue(labelColumnIndex, ri));
                }
                if (label == 0) {
                    label = -1; // recode 0 -> -1 for libsvm.
                }
                if (!oneClass || (oneClass && label != -1)) {
                    // When training a one-class predictor, do not load samples from other classes than the one class.
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
                    throws TypeMismatchException, InvalidColumnException {

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
