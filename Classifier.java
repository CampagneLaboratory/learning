package edu.cornell.med.icb.learning;

import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ClassificationParameters;

/**
 * Abstracts a machine learning classifier.
 *
 * @author Fabien Campagne Date: Nov 19, 2007 Time: 9:19:58 AM
 */
public interface Classifier {
	/**
	 * Set parameters of the classification problem.
	 *
	 * @param parameters Parameters to use in subsequent use of this classifier.
	 */
	public void setParameters(ClassificationParameters parameters);

	/**
	 * Create a new classification problem for use with this classifier.
	 *
	 * @param size Number of instances in the problem.
	 * @return a new classification problem with size instances.
	 */
	public ClassificationProblem newProblem(int size);

	/**
	 * Train a classifier with parameters and a given problem.
	 *
	 * @param problem Set of instances with labels
	 * @param parameters Paramaters of classification (i.e., cost parameter for linear SVM)
	 * @return A trained model.
	 */
	public ClassificationModel train(ClassificationProblem problem, ClassificationParameters parameters);

	/**
	 * Train a classifier with default parameters and a given problem.
	 *
	 * @param problem Set of instances with labels
	 * @return A trained model.
	 */
	public ClassificationModel train(ClassificationProblem problem);

	/**
	 * Predict an instance.
	 *
	 * @param trainingModel Model used for prediction.
	 * @param problem Definition of the problem, containing the instance for which to predict
	 * @param instanceIndex Index of the instance to predict in the problem.
	 * @return Predicted label (interpretation of this value depends on classifier type and problem definition).
	 */
	double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
				   final int instanceIndex);


	/**
	 * Get parameters of the classification problem.
	 *
	 * @return Parameters in use by this classifier.
	 */
	ClassificationParameters getParameters();
}
