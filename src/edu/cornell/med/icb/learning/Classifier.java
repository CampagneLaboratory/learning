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
    void setParameters(ClassificationParameters parameters);

    /**
     * Create a new classification problem for use with this classifier.
     *
     * @param size Number of instances in the problem.
     * @return a new classification problem with size instances.
     */
    ClassificationProblem newProblem(int size);

    /**
     * Train a classifier with parameters and a given problem.
     *
     * @param problem Set of instances with labels
     * @param parameters Paramaters of classification (i.e., cost parameter for linear SVM)
     * @return A trained model.
     */
    ClassificationModel train(ClassificationProblem problem, ClassificationParameters parameters);

    /**
     * Train a classifier with default parameters and a given problem.
     *
     * @param problem Set of instances with labels
     * @return A trained model.
     */
    ClassificationModel train(ClassificationProblem problem);

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
     * Estimate probabilities that an instance belongs to each class of the model.
     *
     * @param trainingModel Model used for prediction.
     * @param problem Definition of the problem, containing the instance for which to predict
     * @param instanceIndex Index of the instance to predict in the problem.
     * @param probabilities the probability will be written for each label. probs[0]: first class, probs[1] second class,
     * and so on.
     * @return Predicted label
     */
    double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
                   final int instanceIndex, double[] probabilities);

    /**
     * Get parameters of the classification problem.
     *
     * @return Parameters in use by this classifier.
     */
    ClassificationParameters getParameters();

    String getShortName();
}
