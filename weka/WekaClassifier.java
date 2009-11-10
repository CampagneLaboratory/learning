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

package edu.cornell.med.icb.learning.weka;

import edu.cornell.med.icb.learning.ClassificationModel;
import edu.cornell.med.icb.learning.ClassificationParameters;
import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.Classifier;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import weka.core.Instances;

/**
 * @author Fabien Campagne Date: Nov 23, 2007 Time: 1:25:19 PM
 */
public class WekaClassifier implements Classifier {
    private weka.classifiers.Classifier delegate;
    private WekaParameters defaultParameters;
    private static final Log LOG = LogFactory.getLog(WekaClassifier.class);
    private final double[] labelIndex2LabelValue = {-1.0d, 1.0d};

    public WekaClassifier(final weka.classifiers.Classifier delegate) {
        super();
        this.delegate = delegate;
        defaultParameters = new WekaParameters();
    }

    public void setParameters(final ClassificationParameters parameters) {
        assert parameters instanceof WekaParameters : "parameters must be Weka parameters.";
        defaultParameters = (WekaParameters) parameters;
    }

    public WekaClassifier() {
        super();
        defaultParameters = new WekaParameters();
    }

    public ClassificationProblem newProblem(final int size) {
        return new WekaProblem();
    }

    public ClassificationModel train(final ClassificationProblem problem,
                                     final ClassificationParameters parameters) {
        defaultParameters = getWekaParameters(parameters);
        instantiateClassifier();
        try {
            delegate.setOptions(defaultParameters.getNative());
            // System.out.println("weka Problem: "+getWekaProblem(problem));
            delegate.buildClassifier(getWekaProblem(problem));
            return new WekaModel(this);
        } catch (Exception e) {
            LOG.error("Weka classifier has thrown exception.", e);
            return null;
        }
    }

    private void instantiateClassifier() {
        final String wekaClassifierClassName = defaultParameters.getWekaClassifierClassName();

        if (delegate == null) {
            try {
                final Class clazz = Class.forName(wekaClassifierClassName);
                final Object newInstance = clazz.newInstance();
                assert newInstance instanceof weka.classifiers.Classifier : "weka classifier must implement weka.classifiers.Classifier";
                delegate = (weka.classifiers.Classifier) newInstance;
            } catch (ClassNotFoundException e) {
                LOG.error("Cannot find class for weka classifier classname=" + wekaClassifierClassName, e);
            } catch (IllegalAccessException e) {
                LOG.error(e);
            } catch (InstantiationException e) {
                LOG.error(e);
            }
        }
        assert delegate != null : "Could not instance weka classifier for class name=" + wekaClassifierClassName;
    }

    public ClassificationModel train(final ClassificationProblem problem) {
        return train(problem, defaultParameters);
    }

    private WekaParameters getWekaParameters(final ClassificationParameters parameters) {
        assert parameters instanceof WekaParameters : "parameters must be weka parameters.";
        return (WekaParameters) parameters;
    }

    public double predict(final ClassificationModel trainingModel,
                          final ClassificationProblem problem, final int instanceIndex) {
        assert trainingModel instanceof WekaModel : "Model must be a weka model.";
        try {
            return labelIndex2LabelValue[(int) getWekaClassifier(this)
                    .classifyInstance(getWekaProblem(problem).instance(instanceIndex))];
        } catch (Exception e) {
            LOG.error("Weka classifier has thrown exception.", e);
            return Double.NaN;
        }
    }

    public double predict(final ClassificationModel trainingModel,
                          final ClassificationProblem problem, final int instanceIndex,
                          final double[] probabilities) {
        assert trainingModel instanceof WekaModel : "Model must be a weka model.";
        final double[] probs;
        try {
            probs = getWekaClassifier(this)
                    .distributionForInstance(getWekaProblem(problem).instance(instanceIndex));
        } catch (Exception e) {
            LOG.error("Weka classifier has thrown exception.", e);
            return Double.NaN;
        }

        System.arraycopy(probs, 0, probabilities, 0, probs.length);
        if (LOG.isDebugEnabled()) {
            LOG.debug("decision values: " + ArrayUtils.toString(probabilities));
        }

        double maxProb = Double.NEGATIVE_INFINITY;
        int maxIndex = -1;
        for (int labelIndex = 0; labelIndex < probabilities.length; labelIndex++) {
            if (probabilities[labelIndex] > maxProb) {
                maxProb = probabilities[labelIndex];
                maxIndex = labelIndex;
            }
        }

        final double decision;
        if (maxIndex == -1) {
            decision = Double.NaN;
        } else {
            decision = labelIndex2LabelValue[maxIndex];
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("decision: " + decision);
        }

        return decision;
    }

    public ClassificationParameters getParameters() {
        return defaultParameters;
    }

    public String getShortName() {
        instantiateClassifier();
        return "weka!" + delegate.getClass().getName();
    }

    private Instances getWekaProblem(final ClassificationProblem problem) {
        assert problem instanceof WekaProblem : "problem must be weka problem.";
        return ((WekaProblem) problem).getNative();
    }

    private weka.classifiers.Classifier getWekaClassifier(final WekaClassifier wekaClassifier
    ) {
        return wekaClassifier.delegate;
    }

    public weka.classifiers.Classifier getNative() {
        return delegate;
    }
}
