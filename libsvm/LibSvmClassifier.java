/*
 * Copyright (C) 2007-2008 Institute for Computational Biomedicine,
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

import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.Classifier;
import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ClassificationParameters;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_problem;
import libsvm.svm_parameter;
import it.unimi.dsi.fastutil.io.TextIO;
import org.apache.log4j.Logger;
import org.apache.commons.lang.ArrayUtils;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:24:27 PM
 */
public class LibSvmClassifier implements Classifier {
    protected LibSvmParameters parameters;

    private static final Logger LOG = Logger.getLogger(LibSvmClassifier.class);

    public LibSvmClassifier() {
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

        svm_model model = getNativeModel(trainingModel);
        if ((model.param.svm_type == svm_parameter.C_SVC || model.param.svm_type == svm_parameter.NU_SVC) &&
                model.probA != null && model.probB != null) {
            LOG.debug("estimating probabilities");
            // the SVM was trained to estimate probabilities. Return estimated probabilities.
            double decision = svm.svm_predict_probability(getNativeModel(trainingModel),
                    getNativeProblem(problem).x[instanceIndex],
                    probabilities);
            LOG.debug("decision values: " + ArrayUtils.toString(probabilities));
            return decision;
        } else {
            // Regular SVM was not trained to estimate probability. Report the decision function in place of estimated
            // probabilities.
            LOG.debug("substituing decision values for probabilities. The SVM was not trained to estimate probabilities.");
            svm.svm_predict_values(getNativeModel(trainingModel), getNativeProblem(problem).x[instanceIndex], probabilities);
            LOG.debug("decision values: " + ArrayUtils.toString(probabilities));
            return svm.svm_predict(getNativeModel(trainingModel), getNativeProblem(problem).x[instanceIndex]);


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
