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

/**
 * A class to construct a classification problem from features/labels in a table.
 *
 * @author: Fabien Campagne Date: Nov 20, 2007 Time: 5:55:33 PM
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
	final protected int[] shuffledLabels = null;


	public void load(final ClassificationProblem problem, final Table table, final String labelColumnIdf,
					 final List<Set<String>> labelValueGroups) throws InvalidColumnException, TypeMismatchException {
		currentRowIndex = 0;
		this.labelValueGroups = labelValueGroups;

		// output all columns
		labelColumnIndex = table.getColumnIndex(labelColumnIdf);

		this.labelValueGroups = labelValueGroups;
		groupToCodedLabel = new HashMap<Set<String>, Integer>();
		assert labelValueGroups.size() == 2 : "Classification requires exactly two label groups.";
		final Iterator<Set<String>> it = labelValueGroups.iterator();
		final Set<String> labelGroup0 = it.next();  // negative class
		final Set<String> labelGroup1 = it.next();  // positive class

		groupToCodedLabel.put(labelGroup0, -1);
		groupToCodedLabel.put(labelGroup1, 1);

		RowProcessor rowProcessor = new RowProcessor(RowProcessor.buildColumnIndices(table, null)) {

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


				final int numberOfFeatures = columnIndices.length - 1;
				int instanceIndex = problem.addInstance(numberOfFeatures);
				problem.setLabel(instanceIndex, label);

				int featureIndex = 1;
				for (final int columnIndex : columnIndices) {

					if (columnIndex != labelColumnIndex) {

						// features:
						double value = table.getDoubleValue(columnIndex, ri);

						if (value != value) { // NaN case
							value = 0;
						}
						problem.setFeature(instanceIndex, featureIndex - 1, value);
						featureIndex += 1;

					}
				}
				currentRowIndex++;
			}
		};
		table.processRows(rowProcessor);

	}


}
