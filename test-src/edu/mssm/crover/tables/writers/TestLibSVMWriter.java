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
import edu.cornell.med.icb.R.RConnectionPool;
import edu.cornell.med.icb.learning.ContingencyTable;
import edu.cornell.med.icb.tools.svmlight.EvaluationMeasure;
import edu.mssm.crover.tables.ArrayTable;
import edu.mssm.crover.tables.ColumnTypeException;
import edu.mssm.crover.tables.InvalidColumnException;
import edu.mssm.crover.tables.Table;
import edu.mssm.crover.tables.TypeMismatchException;
import edu.mssm.crover.tables.readers.SVMLightReader;
import org.apache.commons.lang.ArrayUtils;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Fabien Campagne Date: Mar 7, 2006 Time: 3:49:36 PM
 */
public final class TestLibSVMWriter {
    private ArrayTable source;
    private ArrayTable sourceFuzzy;
    private final double epsilon = 1e-4;

    @Before
    public void setUp() throws InvalidColumnException, ColumnTypeException {
        this.source = new ArrayTable();
        source.addColumn("label", String.class);
        source.addColumn("value", double.class);
        int columnA = source.getColumnIndex("label");
        int columnB = source.getColumnIndex("value");
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

        this.sourceFuzzy = new ArrayTable();
        sourceFuzzy.addColumn("label", String.class);
        sourceFuzzy.addColumn("value", double.class);
        columnA = sourceFuzzy.getColumnIndex("label");
        columnB = sourceFuzzy.getColumnIndex("value");

        sourceFuzzy.parseAppend(columnA, "positive");
        sourceFuzzy.parseAppend(columnA, "positive");
        sourceFuzzy.parseAppend(columnA, "positive");
        sourceFuzzy.parseAppend(columnA, "negative");
        sourceFuzzy.parseAppend(columnA, "negative");
        sourceFuzzy.parseAppend(columnA, "negative");
        sourceFuzzy.parseAppend(columnB, "+1");
        sourceFuzzy.parseAppend(columnB, "+1");
        sourceFuzzy.parseAppend(columnB, "-1");  // this is not +1
        sourceFuzzy.parseAppend(columnB, "+1");  // this is not -1
        sourceFuzzy.parseAppend(columnB, "-1");
        sourceFuzzy.parseAppend(columnB, "-1");
    }

    /**
     * Ensure that the RConnection pool gets shutdown.  This would typically happen
     * in the {@link edu.cornell.med.icb.R.RConnectionPool#finalize()} method but we
     * can't be guaranteed that this will get called so we explicitly shut it down.
     */
    @AfterClass
    public static void closePool() {
        final RConnectionPool pool = RConnectionPool.getInstance();
        pool.close();
    }

    @Test
    public void testTrain()
            throws InvalidColumnException, TypeMismatchException {
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("positive");
        final Set<String> group2 = new HashSet<String>();
        group2.add("negative");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        LibSVMWriter writer =
                new LibSVMWriter(source, "label", labelValueGroups);
        source.processRows(writer);
        EvaluationMeasure results = writer.trainEvaluate();
        assertNotNull(results);
        assertEquals(100d, results.getAccuracy(), epsilon);
        assertEquals(100d, results.getPrecision(), epsilon);
        assertEquals(100d, results.getRecall(), epsilon);

        writer = new LibSVMWriter(sourceFuzzy, "label", labelValueGroups);
        sourceFuzzy.processRows(writer);
        results = writer.trainEvaluate();
        assertNotNull(results);
        assertTrue((66.66666666666666d - results.getAccuracy()) < epsilon);
        assertTrue(((100d - 66.66666666666666d) - results.getErrorRate())
                < epsilon);
        assertTrue((66.66666666666666d - results.getPrecision()) < epsilon);
        assertTrue((66.66666666666666d - results.getRecall()) < epsilon);
    }

    @Test
    public void testLOO() throws InvalidColumnException, TypeMismatchException {
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("positive");
        final Set<String> group2 = new HashSet<String>();
        group2.add("negative");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final LibSVMWriter writer =
                new LibSVMWriter(source, "label", labelValueGroups);
        source.processRows(writer);
        final EvaluationMeasure results = writer.leaveOneOutEvaluation();
        assertNotNull(results);
        assertEquals(100d, results.getAccuracy(), epsilon);
        assertEquals(100d, results.getPrecision(), epsilon);
        assertEquals(100d, results.getRecall(), epsilon);
    }

    @Test
    public void testCrossValidation() throws InvalidColumnException, TypeMismatchException {
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("positive");
        final Set<String> group2 = new HashSet<String>();
        group2.add("negative");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final LibSVMWriter writer =
                new LibSVMWriter(source, "label", labelValueGroups);
        source.processRows(writer);
        final EvaluationMeasure results = writer.crossValidation(2);
        assertNotNull(results);
        assertEquals(100d, results.getAccuracy(), epsilon);
        assertEquals(100d, results.getPrecision(), epsilon);
        assertEquals(100d, results.getRecall(), epsilon);

        /*   writer = new LibSVMWriter(sourceFuzzy, "label", labelValueGroups);
                  sourceFuzzy.processRows(writer);
                  results = writer.crossValidation(3);
                  assertNotNull(results);
                  assertTrue((66.66666666666666d - results.getAccuracy()) < epsilon);
                  assertTrue(((100d - 66.66666666666666d) - results.getErrorRate())
                          < epsilon);
                  assertTrue((66.66666666666666d - results.getPrecision()) < epsilon);
                  assertTrue((66.66666666666666d - results.getRecall()) < epsilon);*/
    }

    @Test
    public void testTrainErrors()
            throws InvalidColumnException, TypeMismatchException {
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("positive");
        final Set<String> group2 = new HashSet<String>();
        group2.add("negative");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final LibSVMWriter writer =
                new LibSVMWriter(sourceFuzzy, "label", labelValueGroups);
        sourceFuzzy.processRows(writer);
        final EvaluationMeasure results = writer.leaveOneOutEvaluation();
        assertNotNull(results);
        assertEquals(0d, results.getAccuracy(), epsilon);
        assertEquals(0d, results.getPrecision(), epsilon);
        assertEquals(0d, results.getRecall(), epsilon);
    }

    @Test
    public void testStall() throws IOException, InvalidColumnException, TypeMismatchException,
            edu.mssm.crover.tables.readers.SyntaxErrorException {
        final String tabDelimited = "" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.25 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.09 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.14 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.35 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.31 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.51 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.29 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.21 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.63 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.04 10:0 \n" +
                "-1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.09 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.37 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.24 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.14 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.1 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.34 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.25 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.19 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.19 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.31 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.06 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.03 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.25 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:0.09 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.05 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.17 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.09 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.12 10:0 \n" +
                "1 1:0 2:0 3:0 4:0 5:0 6:0 7:0 8:0 9:-0.08 10:0\n";

        final SVMLightReader svmLightReader = new SVMLightReader();
        final Table table = svmLightReader.read(new StringReader(tabDelimited));
        final List<Set<String>> labelValueGroups = new ArrayList<Set<String>>();
        final Set<String> group1 = new HashSet<String>();
        group1.add("1");
        final Set<String> group2 = new HashSet<String>();
        group2.add("-1");
        labelValueGroups.add(group1);
        labelValueGroups.add(group2);
        final LibSVMWriter writer =
                new LibSVMWriter(table, "label", labelValueGroups);
        table.processRows(writer);
        final EvaluationMeasure results = writer.leaveOneOutEvaluation();
        assertNotNull(results);
        assertEquals("error rate", 36.666666666666664d, results.getErrorRate(),
                epsilon);
        assertEquals("accuracy", 63.333333333333336d, results.getAccuracy(),
                epsilon);
        // TODO: what's wrong here?
        // assertEquals("precision", 63.33333333333333d, results.getPrecision(), epsilon);
        //assertEquals(100d, results.getRecall());
    }

    @Test
    public void testContingencyTable() {
        final ContingencyTable c = new ContingencyTable();
        c.setTP(2);
        c.setTN(182);
        c.setFP(18);
        c.setFN(1);
        assertEquals(2d / 3d * 100d, c.getSensitivity(), epsilon);
        assertEquals(91d, c.getSpecificity(), epsilon);
        assertEquals(9d, c.getFalsePositiveRate(), epsilon);
        assertEquals(1d / 3d * 100d, c.getFalseNegativeRate(), epsilon);
    }

    @Test
    public void testAUC() {
        final double[] decisions = new double[2000];
        final double[] labels = new double[2000];
        RandomEngine random = new MersenneTwister(21);
        for (int i = 0; i < decisions.length; i++) {
            decisions[i] = random.nextDouble() * 2 - 1;
            if (decisions[i] < 0) {
                decisions[i] = 0;
            }
            labels[i] = random.nextDouble() > 0.5 ? 1 : 0;
        }
        //	System.out.println("decisions: " + ArrayUtils.toString(decisions));
        //	System.out.println("labels: " + ArrayUtils.toString(labels));
        final double auc = LibSVMWriter.areaUnderRocCurveLOO(decisions, labels);
        assertEquals("random AUC must be about 0.5", 1, Math.round(auc * 2));

        // almost perfect predictions:

        final double[] newDecisions = new double[2000];
        final double[] almostPerfectLabels = new double[2000];
        random = new MersenneTwister(21);
        for (int i = 0; i < newDecisions.length; i++) {
            newDecisions[i] = random.nextDouble() * 2 - 1;
            if (newDecisions[i] < 0) {
                newDecisions[i] = 0;
            }
            almostPerfectLabels[i] = newDecisions[i] > 0 ? 1 : (random.nextDouble() > 0.9 ? 1 : 0);
        }
        System.out.println("decisions: " + ArrayUtils.toString(newDecisions));
        System.out.println("labels: " + ArrayUtils.toString(almostPerfectLabels));
        final double almostPerfectAUC = LibSVMWriter.areaUnderRocCurveLOO(newDecisions, almostPerfectLabels);
        assertEquals("random AUC must be about 0.96", 96, Math.round(almostPerfectAUC * 100));


        for (int i = 0; i < newDecisions.length; i++) {
            almostPerfectLabels[i] = newDecisions[i] > 0 ? 1 : 0; // perfect predictions.
        }
        final double perfectAUC = LibSVMWriter.areaUnderRocCurveLOO(newDecisions, almostPerfectLabels);
        assertEquals("Perfect AUC must be about 1.0", 100, Math.round(perfectAUC * 100));
    }
}
