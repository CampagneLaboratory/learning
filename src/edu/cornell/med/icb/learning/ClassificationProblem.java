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

import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstracts a classification problem.
 *
 * @author Fabien Campagne Date: Nov 19, 2007 Time: 9:29:19 AM
 */
public interface ClassificationProblem {
    /**
     * Returns the label of an instance.
     *
     * @param instanceIndex Instance in the problem.
     * @return The training label, or zero if not known.
     */
    double getLabel(final int instanceIndex);

    /**
     * Return the number of instances in this classification problem.
     *
     * @return the number of instances in this classification problem.
     */
    int getSize();

    /**
     * Returns a subproblem with only instances in the keepInstanceSet.
     *
     * @param keepInstanceSet Index of the records to include in the reduced problem.
     * @return Reduced problem.
     */
    ClassificationProblem filter(final IntSet keepInstanceSet);

    /**
     * Returns the problem with one record excluded.
     *
     * @param instanceIndex Index of the record to exclude.
     * @return Reduced problem.
     */
    ClassificationProblem exclude(final int instanceIndex);
     /**
     * Returns the problem with a single instance included.
     *
     * @param instanceIndex Index of the record to include in the filtered problem.
     * @return Reduced problem.
     */
    ClassificationProblem filter(final int instanceIndex);

    /**
     * Set feature values and label for an instance.
     *
     * @param instanceIndex Index of the instance.
     * @param label         Label for the instance.
     * @param features      Features associated with this instance.
     */
    void setInstance(int instanceIndex, double label, double[] features);

    /**
     * Set feature values and label for an instance.
     *
     * @param instanceIndex Index of the instance.
     * @param label         Label for the instance.
     */
    void setLabel(int instanceIndex, double label);

    /**
     * Set feature value for an instance.
     *
     * @param instanceIndex Index of the instance.
     * @param featureIndex  Index of the feature
     * @param featureValue  Value of the feature for the specified instance.
     */
    void setFeature(final int instanceIndex, final int featureIndex, final double featureValue);

    /**
     * Add an instance to this problem. Allocate storage to store label and features of the instance.
     *
     * @param maxNumberOfFeatures The maximum number of features that this instance can have
     * @return index of the instance.
     */
    int addInstance(final int maxNumberOfFeatures);

    /**
     * Prepare the native representation of this problem. Adding instances is not permitted after this method has been
     * called. Feature values and labels can be changed directly however.
     */
    void prepareNative();

    /**
     * Returns a problem where features have been scaled. This method should be called to prepare a training set.
     * Typically, the scaler may inspect the training set, determine and cache statistics useful to scale the test set.
     *
     * @param scaler The feature scaler engine.
     * @return A copy of training set with scaled features.
     */
    ClassificationProblem scaleTraining(FeatureScaler scaler);

    ClassificationProblem scaleTestSet(FeatureScaler scaler, int testInstanceIndex);

    /**
     * Returns the values of the feature for a subset of instances of this problem.
     * @param featureIndex   Index of the feature to collect values for.
     * @param keepInstanceSet Set of instances to collect over.
     * @return
     */
    double[] featureValues(int featureIndex,final IntSet keepInstanceSet);

    ClassificationProblem scaleFeatures(FeatureScaler scaler, IntSet testSetIndices, boolean trainingMode);
}
