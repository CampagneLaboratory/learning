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

/**
 * @author Fabien Campagne Date: Oct 26, 2007 Time: 2:11:19 PM
 */
public class ContingencyTable {
    double gfpp;  // ++  positiveClass predictedPositive     TP
    double gfpm;  // +-  positiveClass predictedNegative     FN
    double gfmp;  // -+  negativeClass predictedPositive     FP
    double gfmm;  // --  negativeClass predictedNegative     TN
    int count;

    public void observeDecision(final double trueLabel, final double decision) {

        gfpp += (trueLabel == 1 && decision == 1) ? 1 : 0;
        gfmp += (trueLabel == 1 && decision == -1) ? 1 : 0;
        gfpm += (trueLabel == -1 && decision == 1) ? 1 : 0;
        gfmm += (trueLabel == -1 && decision == -1) ? 1 : 0;
        count++;
    }

    public void average() {
        assert getTP() + getFP() + getFN() + getTN() == count :
                "Values in contingency table must add up to number of test cases.";

// average the loo contingency table:
        gfpp /= (double) count;
        gfmp /= (double) count;
        gfpm /= (double) count;
        gfmm /= (double) count;

    }

    public double getErrorRate() {

        return (getFN() + getFP()) / (getTP() + getFN() + getFP() + getTN()) * 100;
    }

    public double getPrecision() {
        return getTP() / (getTP() + getFP()) * 100f;
    }

    public double getFN() {
        return gfpm;
    }

    public void setFN(final double gfpm) {
        this.gfpm = gfpm;
    }

    public void setTP(final double gfpp) {
        this.gfpp = gfpp;
    }


    public double getRecall() {
        return getTP() / (getTP() + getFN()) * 100f;    // TP/(TP+FN)
    }

    public double getTP() {
        return gfpp;
    }

    public double getSpecificity() {
        return getTN() / (getFP() + getTN()) * 100d;    // TN/(FP+TN)
    }

    public double getSensitivity() {
        return getTP() / (getTP() + getFN()) * 100d;    // TP/(TP+FN)
    }

    public double getFalsePositiveRate() {
        return 100d - getSpecificity();
    }

    public double getFalseNegativeRate() {
        return 100d - getSensitivity();
    }


    public double getPositivePredictiveValue() {
        return getTP() / (getTP() + getFP()) * 100d;
    }

    public double getNegativePredictiveValue() {
        return getTN() / (getTN() + getFN()) * 100d;
    }

    public void setFP(final double gfmp) {
        this.gfmp = gfmp;
    }

    public double getFP() {
        return gfmp;
    }

    public double getTN() {
        return gfmm;
    }

    public void setTN(final double gfmm) {
        this.gfmm = gfmm;
    }

    public double getF1Measure() {
        if (getPrecision() == 0 && getRecall() == 0) {
            return 0;
        } else {
            return (2 * getPrecision() * getRecall()) / (getPrecision()
                    + getRecall());
        }
    }
}
