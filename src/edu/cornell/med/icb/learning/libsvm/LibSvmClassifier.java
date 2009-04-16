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

package edu.cornell.med.icb.learning.libsvm;

import edu.cornell.med.icb.learning.ClassificationModel;
import edu.cornell.med.icb.learning.ClassificationParameters;
import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.Classifier;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_problem;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:24:27 PM
 */
public class LibSvmClassifier implements Classifier {
    protected LibSvmParameters parameters;

    private static final Log LOG = LogFactory.getLog(LibSvmClassifier.class);

    public LibSvmClassifier() {
        super();
        this.parameters = new LibSvmParameters();
    }

    public ClassificationModel train(final ClassificationProblem problem) {
        final svm_problem nativeProblem = getNativeProblem(problem);
        return new LibSvmModel(svm.svm_train(nativeProblem, parameters.getNative()));
    }

    private svm_problem getNativeProblem(final ClassificationProblem problem) {
        assert problem instanceof LibSvmProblem;
        return ((LibSvmProblem) problem).getNative();

    }

    public double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
                          final int instanceIndex) {
        return svm.svm_predict(getNativeModel(trainingModel), getNativeProblem(problem).x[instanceIndex]);
    }

    public double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
                          final int instanceIndex, final double[] probabilities) {

        final svm_model model = getNativeModel(trainingModel);
        if (svm.svm_check_probability_model(model) == 1) {
            LOG.debug("estimating probabilities");
            final svm_problem nativeProblem = getNativeProblem(problem);
            if (LOG.isTraceEnabled()) {
                printNodes(instanceIndex, nativeProblem);
            }
            // the SVM was trained to estimate probabilities. Return estimated probabilities.
            final double decision = svm.svm_predict_probability(getNativeModel(trainingModel),
                    nativeProblem.x[instanceIndex],
                    probabilities);
            if (LOG.isDebugEnabled()) {
                LOG.debug("decision values: " + ArrayUtils.toString(probabilities));
            }
            return decision;
        } else {
            // Regular SVM was not trained to estimate probability. Report the decision function in place of estimated
            // probabilities.
            LOG.debug("substituting decision values for probabilities. The SVM was not trained to estimate probabilities.");
            final svm_problem nativeProblem = getNativeProblem(problem);
            if (LOG.isTraceEnabled()) {
                printNodes(instanceIndex, nativeProblem);
            }
            svm.svm_predict_values(getNativeModel(trainingModel), nativeProblem.x[instanceIndex], probabilities);
            probabilities[0] = Math.abs(probabilities[0]);
            probabilities[1] = Double.NEGATIVE_INFINITY; // make sure probs[0] is max of the two values.
            if (LOG.isDebugEnabled()) {
                LOG.debug("decision values: " + ArrayUtils.toString(probabilities));
            }
            final double decision = svm.svm_predict(getNativeModel(trainingModel), getNativeProblem(problem).x[instanceIndex]);
            if (LOG.isDebugEnabled()) {
                LOG.debug("decision: " + decision);
            }

            return decision;
        }
    }

    private void printNodes(final int instanceIndex, final svm_problem nativeProblem) {
        for (final svm_node node : nativeProblem.x[instanceIndex]) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(String.format("feature index: %d value: %f", node.index, node.value));
            }
        }
    }

    public ClassificationParameters getParameters() {
        return parameters;
    }

    public String getShortName() {
        return "libSVM";
    }

    private svm_model getNativeModel(final ClassificationModel trainingModel) {
        assert trainingModel instanceof LibSvmModel;
        return ((LibSvmModel) trainingModel).nativeModel;
    }

    public ClassificationProblem newProblem(final int size) {
        return new LibSvmProblem();
    }

    public ClassificationModel train(final ClassificationProblem problem, final ClassificationParameters parameters) {
        setParameters(parameters);
        return this.train(problem);
    }

    public void setParameters(final ClassificationParameters parameters) {
        assert parameters instanceof LibSvmParameters;
        this.parameters = (LibSvmParameters) parameters;
    }

}
