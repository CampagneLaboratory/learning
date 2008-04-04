/*
 * Copyright (C) 2008 Institute for Computational Biomedicine,
 *                    Weill Medical College of Cornell University
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
 * This default implementation does not scale features at all. Sub-classes must implement the
 * scaling strategy by overriding the scaler methods.
 * @author Fabien Campagne
 *         Date: Mar 30, 2008
 *         Time: 11:26:41 AM
 */
public class FeatureScaler {
    /**
     * Observe the values of a feature over a training set. This method must be called before
     * features can  be scaled. It derives statistics from the feature values that are needed to
     * scale individual features.
     *
     * @param numFeatures
     * @param featureValues Values of the feature over the training set.
     * @param featureIndex
     */
    public void observeFeatureForTraining(final int numFeatures,
                                          final double[] featureValues,
                                          final int featureIndex) {
    }

    public double scaleFeatureValue(double featureValue, int featureIndex) {
        return featureValue;
    }
}
