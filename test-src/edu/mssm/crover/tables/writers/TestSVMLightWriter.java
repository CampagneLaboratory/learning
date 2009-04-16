/*
 * Copyright (C) 2001-2002 Mount Sinai School of Medicine
 * Copyright (C) 2003-2009 Institute for Computational Biomedicine,
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
import edu.mssm.crover.tables.ArrayTable;
import edu.mssm.crover.tables.ColumnTypeException;
import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.TypeMismatchException;
import junit.framework.TestCase;
import junitx.framework.FileAssert;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Campagne
 *         Date: Mar 7, 2006
 *         Time: 3:49:36 PM
 */
public class TestSVMLightWriter extends TestCase {
    private ArrayTable source;

    @Override
    protected void setUp() throws InvalidColumnException, ColumnTypeException {
        this.source = new ArrayTable();
        source.addColumn("label", String.class);
        source.addColumn("value", double.class);
        final int columnA = source.getColumnIndex("label");
        final int columnB = source.getColumnIndex("value");
        source.parseAppend(columnA, "positive");
        source.parseAppend(columnA, "positive");
        source.parseAppend(columnA, "negative");
        source.parseAppend(columnA, "negative");
        source.parseAppend(columnA, "negative");
        source.parseAppend(columnA, "negative");
        source.parseAppend(columnB, "+1");
        source.parseAppend(columnB, "+1");
        source.parseAppend(columnB, "-1");
        source.parseAppend(columnB, "-1");
        source.parseAppend(columnB, "-1");
        source.parseAppend(columnB, "-1");
    }

    public void testShuffleLabels() throws InvalidColumnException,
            TypeMismatchException, IOException {
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("negative");
        final Set<String> group2 = new HashSet<String>();
        group2.add("positive");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final RandomEngine randomEngine = new MersenneTwister(34);
        final SVMLightWriter writer = new SVMLightWriter(source, "label",
                labelValueGroups, randomEngine);

        for (int i = 0; i < 10; i++) {
            writer.shuffleLabels();
            final int[] shuffledLabels = writer.getShuffledLabels();
            int numNegative = 0;
            int numPositive = 0;
            for (final int label : shuffledLabels) {
                assertTrue("Iteration " + i + ": label = " + label,
                    label == 1 || label == -1);
                if (label < 0) {
                    numNegative++;
                } else {
                    numPositive++;
                }

            }
            assertEquals("Iteration " + i, 2, numPositive);
            assertEquals("Iteration " + i, 4, numNegative);
        }
    }

    public void testWriter() throws IOException, TypeMismatchException,
            InvalidColumnException {
        final String svmTrainingSetFileName = "data/test/svm/forsvm.dat";
        final PrintWriter fileWriter = new PrintWriter(svmTrainingSetFileName);
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("negative");
        final Set<String> group2 = new HashSet<String>();
        group2.add("positive");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final RandomEngine randomEngine = new MersenneTwister(34);
        final SVMLightWriter writer = new SVMLightWriter(source, "label",
                labelValueGroups, randomEngine);

        writer.write(source, fileWriter);
        FileAssert.assertEquals(new File("data/test/svm/expectedsvm.dat"),
                new File(svmTrainingSetFileName));
    }
}
