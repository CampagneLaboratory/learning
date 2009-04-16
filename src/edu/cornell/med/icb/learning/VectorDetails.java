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

/**
 * Describe class here.
 *
 * @author Kevin Dorff
 */
public class VectorDetails {

    /** True if all values are positive. */
    private Boolean isAllPositive;

    /** True if all values are negative. */
    private Boolean isAllNegative;

    /** True if all values are zeros. */
    private Boolean isAllZeros;

    /** True if all values are ones. */
    private Boolean isAllOnes;

    /** True if all values are ones. */
    private Boolean isSingleValue;

    /** True if all values are ones. */
    private Double theSingleValue;

    /** True if all values are ones. */
    private boolean isEmpty = true;

    /**
     * Construct the object and calculate
     * all the values.
     * @param vector the vector to get the details on
     */
    public VectorDetails(final double[] vector) {
        super();
        if (vector != null && vector.length > 0) {
            isEmpty = false;
            theSingleValue = vector[0];
            isSingleValue = true;
            for (final double v : vector) {
                if (isSingleValue && (theSingleValue != v)) {
                    isSingleValue = false;
                    theSingleValue = null;
                }
                if (v >= 0.0d) {
                    isAllNegative = false;
                    if (isAllPositive == null) {
                        isAllPositive = true;
                    }
                }
                if (v < 0) {
                    isAllPositive = false;
                    if (isAllNegative == null) {
                        isAllNegative = true;
                    }
                }
                if (v != 0) {
                    isAllZeros = false;
                }
                if (v != 1) {
                    isAllOnes = false;
                }
                if ((v == 1) && (isAllOnes == null)) {
                    isAllOnes = true;
                }
                if ((v == 0) && (isAllZeros == null)) {
                    isAllZeros = true;
                }
            }
        }
        if (isSingleValue == null) {
            theSingleValue = null;
            isSingleValue = false;
        }
        if (isAllNegative == null) {
            isAllNegative = false;
        }
        if (isAllPositive == null) {
            isAllPositive = false;
        }
        if (isAllZeros == null) {
            isAllZeros = false;
        }
        if (isAllOnes == null) {
            isAllOnes = false;
        }
    }

    /**
     * Return if all the values in the vector are positive.
     * @return true if all the values in the vector are positive
     */
    public boolean isAllPositive() {
        return isAllPositive;
    }

    /**
     * Return if all the values in the vector are negative.
     * @return true if all the values in the vector are negative
     */
    public boolean isAllNegative() {
        return isAllNegative;
    }

    /**
     * Return if all the values in the vector are zeros (0.0d).
     * @return true if all the values in the vector are zeros
     */
    public boolean isAllZeros() {
        return isAllZeros;
    }

    /**
     * Return if all the values in the vector are ones (1.0d).
     * @return true if all the values in the vector are ones
     */
    public boolean isAllOnes() {
        return isAllOnes;
    }

    /**
     * Return if the vector is empty (null or 0 length).
     * @return true if the vector is empty (null or 0 length)
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Return if all the values in the vector have a single value.
     * @return true if all the values in the vector have a single value
     */
    public boolean isSingleValue() {
        return isSingleValue;
    }

    /**
     * Return the single value (if isSingleValue() is true) for this
     * vector. If isSingleValue() is false this will return
     * Double.NaN, so you really should check the  isSingleValue()
     * before calling this.
     * @return the single value of the vector
     */
    public double getTheSingleValue() {
        if (isSingleValue) {
            return theSingleValue;
        } else {
            return Double.NaN;
        }
    }
}
