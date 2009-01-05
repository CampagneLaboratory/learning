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
 * This scaler never scales features.
 *
 * @author Fabien Campagne
 * Date: Jun 9, 2008
 * Time: 2:30:29 PM
 */
public class NoScalingFeatureScaler extends FeatureScaler {

    public NoScalingFeatureScaler(final double multiplier) {
        super();

    }

    @Override
    public void observeFeatureForTraining(final int numFeatures,
                                          final double[] featureValues,
                                          final int featureIndex) {
    }

    @Override
    public double scaleFeatureValue(final double featureValue, final int featureIndex) {
        return featureValue;
    }
}
