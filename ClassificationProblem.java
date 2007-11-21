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
	public double getLabel(final int instanceIndex);

	/**
	 * Return the number of instances in this classification problem.
	 *
	 * @return the number of instances in this classification problem.
	 */
	public int getSize();

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
	ClassificationProblem filter(final int instanceIndex);

	/**
	 * Set feature values and label for an instance.
	 *
	 * @param instanceIndex Index of the instance.
	 * @param label Label for the instance.
	 * @param features Features associated with this instance.
	 */
	public void setInstance(int instanceIndex, double label, double features[]);

	/**
	 * Set feature values and label for an instance.
	 *
	 * @param instanceIndex Index of the instance.
	 * @param label Label for the instance.
	 */
	public void setLabel(int instanceIndex, double label);

	/**
	 * Set feature value for an instance.
	 *
	 * @param instanceIndex Index of the instance.
	 * @param featureIndex Index of the feature
	 * @param featureValue Value of the feature for the specified instance.
	 */
	public void setFeature(final int instanceIndex, final int featureIndex, final double featureValue);

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


}
