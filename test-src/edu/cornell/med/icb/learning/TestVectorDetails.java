/*
 * Copyright (C) 2008-2009 Institute for Computational Biomedicine,
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Describe class here.
 *
 * @author Kevin Dorff
 */
public class TestVectorDetails {

    /**
     * Test sending null.
     */
    @Test
    public void testVectorDetailsNull() {
        final VectorDetails d = new VectorDetails(null);

        assertTrue(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending null.
     */
    @Test
    public void testVectorDetailsEmpty() {
        final double[] data = new double[0];
        final VectorDetails d = new VectorDetails(data);

        assertTrue(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending single positive.
     */
    @Test
    public void testVectorDetailsSinglePositive() {
        final double[] data = makeArray(3);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(3.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending single negative.
     */
    @Test
    public void testVectorDetailsSingleNegative() {
        final double[] data = makeArray(-5);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertTrue(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(-5.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending single positive "1".
     */
    @Test
    public void testVectorDetailsSinglePositiveOne() {
        final double[] data = makeArray(1);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertTrue(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(1.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending single negative "1".
     */
    @Test
    public void testVectorDetailsSingleNegativeOne() {
        final double[] data = makeArray(-1);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertTrue(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(-1.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending single positive "0".
     */
    @Test
    public void testVectorDetailsSinglePositiveZero() {
        final double[] data = makeArray(0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertTrue(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(0.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending single "-0".
     */
    @Test
    public void testVectorDetailsSingleNegativeZero() {
        final double[] data = makeArray(-0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertTrue(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(0.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending multiple "0"s.
     */
    @Test
    public void testVectorDetailsMultiZeros() {
        final double[] data = makeArray(0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertTrue(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(0.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending multiple "1"s.
     */
    @Test
    public void testVectorDetailsMultiOnes() {
        final double[] data = makeArray(1.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertTrue(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertTrue(d.isSingleValue());
        assertEquals(1.0d, d.getTheSingleValue(), 0.0d);
    }

    /**
     * Test sending mostly "1"s but a zero in middle.
     */
    @Test
    public void testVectorDetailsMultiOnesWithZeroMid() {
        final double[] data = makeArray(1.0d, 1.0d, 0.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but a zero at start.
     */
    @Test
    public void testVectorDetailsMultiOnesWithZeroStart() {
        final double[] data = makeArray(0.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but a zero at end.
     */
    @Test
    public void testVectorDetailsMultiOnesWithZeroEnd() {
        final double[] data = makeArray(1.0d, 1.0d, 1.0d, 1.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithPositive() {
        final double[] data = makeArray(1.0d, 1.0d, 5.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithPositive2() {
        final double[] data = makeArray(5.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithPositive3() {
        final double[] data = makeArray(1.0d, 1.0d, 1.0d, 1.0d, 5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithNegative() {
        final double[] data = makeArray(1.0d, 1.0d, -5.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithNegative2() {
        final double[] data = makeArray(-5.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "1"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiOnesWithNegative3() {
        final double[] data = makeArray(1.0d, 1.0d, 1.0d, 1.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending zeros "1"s but a 1 in middle.
     */
    @Test
    public void testVectorDetailsMultiZerosWithZeroMid() {
        final double[] data = makeArray(0.0d, 0.0d, 1.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but a 1at start.
     */
    @Test
    public void testVectorDetailsMultiZerosWithZeroStart() {
        final double[] data = makeArray(1.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but a 1 at end.
     */
    @Test
    public void testVectorDetailsMultiZerosWithZeroEnd() {
        final double[] data = makeArray(0.0d, 0.0d, 0.0d, 0.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiZerosWithPositive() {
        final double[] data = makeArray(0.0d, 0.0d, 5.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiZerosWithPositive2() {
        final double[] data = makeArray(5.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a positive.
     */
    @Test
    public void testVectorDetailsMultiZerosWithPositive3() {
        final double[] data = makeArray(0.0d, 0.0d, 0.0d, 0.0d, 5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a negative.
     */
    @Test
    public void testVectorDetailsMultiZerosWithNegative() {
        final double[] data = makeArray(0.0d, 0.0d, -5.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a negative.
     */
    @Test
    public void testVectorDetailsMultiZerosWithNegative2() {
        final double[] data = makeArray(-5.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test sending mostly "0"s but with a negative.
     */
    @Test
    public void testVectorDetailsMultiZerosWithNegative3() {
        final double[] data = makeArray(0.0d, 0.0d, 0.0d, 0.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All positive.
     */
    @Test
    public void testVectorDetailsMultiAllPositive() {
        final double[] data = makeArray(1.0d, 2.0d, 3.0d, 4.0d, 5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All positive.
     */
    @Test
    public void testVectorDetailsMultiAllPositiveSmaller() {
        final double[] data = makeArray(0.02d, 0.002d, 0.0003d, 0.00004d, 0.5d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertTrue(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative.
     */
    @Test
    public void testVectorDetailsMultiAllNegative() {
        final double[] data = makeArray(-1.0d, -2.0d, -3.0d, -4.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertTrue(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeSmaller() {
        final double[] data = makeArray(-0.02d, -0.002d, -0.0003d, -0.00004d, -0.5d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertTrue(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }


    /**
     * Test All negative but a zero.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButZero1() {
        final double[] data = makeArray(0.0d, -2.0d, -3.0d, -4.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative but a zero.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButZero2() {
        final double[] data = makeArray(1.0d, -2.0d, 0.0d, -4.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative but a zero.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButZero3() {
        final double[] data = makeArray(-1.0d, -2.0d, -3.0d, -4.0d, 0.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative but a 1.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButOne() {
        final double[] data = makeArray(1.0d, -2.0d, -3.0d, -4.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative but a 1.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButOne2() {
        final double[] data = makeArray(1.0d, -2.0d, 1.0d, -4.0d, -5.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /**
     * Test All negative but a 1.
     */
    @Test
    public void testVectorDetailsMultiAllNegativeButOne3() {
        final double[] data = makeArray(-1.0d, -2.0d, -3.0d, -4.0d, 1.0d);
        final VectorDetails d = new VectorDetails(data);

        assertFalse(d.isEmpty());
        assertFalse(d.isAllNegative());
        assertFalse(d.isAllPositive());
        assertFalse(d.isAllOnes());
        assertFalse(d.isAllZeros());
        assertFalse(d.isSingleValue());
        assertTrue(Double.isNaN(d.getTheSingleValue()));
    }

    /* ---- SUPPORT ---- */
    /**
     * Make a double array from the incoming double values.
     * @param data the incoming double values
     * @return the double array
     */
    private static double[] makeArray(final double... data) {
        final double[] out = new double[data.length];
        int pos = 0;
        for (final double d : data) {
            out[pos++] = d;
        }
        return out;
    }
}
