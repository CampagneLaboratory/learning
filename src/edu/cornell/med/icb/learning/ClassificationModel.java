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

import edu.cornell.med.icb.learning.libsvm.LibSvmClassifier;
import edu.cornell.med.icb.learning.libsvm.LibSvmModel;
import edu.cornell.med.icb.learning.libsvm.LibSvmParameters;
import edu.cornell.med.icb.learning.weka.WekaClassifier;
import edu.cornell.med.icb.learning.weka.WekaModel;
import edu.cornell.med.icb.learning.weka.WekaParameters;
import it.unimi.dsi.fastutil.io.BinIO;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstracts a classification model.
 *
 * @author Fabien Campagne Date: Nov 19, 2007 Time: 9:29:12 AM
 */
public abstract class ClassificationModel {
    /**
     * Used to log informational and debug messages.
     */
    private static final Log LOG = LogFactory.getLog(ClassificationModel.class);

    /**
     * Write the model to a file.
     *
     * @param filename Filename where to store the model.
     * @throws IOException thrown if an error occurs writing filename.
     */
    public abstract void write(String filename) throws IOException;

    /**
     * Write the model to a stream.
     *
     * @param stream stream where to store the model.
     * @throws IOException thrown if an error occurs writing stream.
     */
    public abstract void write(OutputStream stream) throws IOException;

    /**
     * Load a trained model from a file.
     *
     * @param filename Filename where to store the model.
     * @throws IOException thrown if an error occurs writing filename.
     * @return helper object for the classification model
     */
    public static ClassificationHelper load(final String filename) throws IOException {
        return load(filename, null);
    }

    /**
     * Load a trained model from a file.
     *
     * @param filename Filename to load the model from
     * @param parameters parameters for the model
     * @throws IOException thrown if an error occurs reading filename.
     * @return helper object for the classification model
     */
    public static ClassificationHelper load(final String filename,
                                            final String parameters) throws IOException {
        final ClassificationHelper helper = new ClassificationHelper();
        if (!parameters.contains("wekaClass")) {
            helper.model = new LibSvmModel(filename);
            helper.classifier = new LibSvmClassifier();
            helper.parameters = new LibSvmParameters();
        } else if (parameters.contains("wekaClass")) {
            try {
                helper.classifier = new WekaClassifier((weka.classifiers.Classifier) BinIO.loadObject(filename));
                helper.model = new WekaModel((WekaClassifier) helper.classifier);
                helper.parameters = new WekaParameters();
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to load serialized weka model.", e);
            }
        } else {
            final String message = "Classifier model type not recognized - Cannot load."
                    + "Parameters were: " + parameters;
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        helper.parseParameters(helper.classifier, splitModelParameters(parameters));
        helper.classifier.setParameters(helper.parameters);
        return helper;
    }

    /**
     * Load a trained model from a stream.
     *
     * @param stream InputStream to load the model from
     * @param parameters parameters for the model
     * @throws IOException thrown if an error occurs reading the stream
     * @return helper object for the classification model
     */
    public static ClassificationHelper load(final InputStream stream,
                                            final String parameters) throws IOException {
        final ClassificationHelper helper = new ClassificationHelper();
        if (!parameters.contains("wekaClass")) {
            helper.model = new LibSvmModel(stream);
            helper.classifier = new LibSvmClassifier();
            helper.parameters = new LibSvmParameters();
        } else if (parameters.contains("wekaClass")) {
            try {
                helper.classifier = new WekaClassifier((weka.classifiers.Classifier) BinIO.loadObject(stream));
                helper.model = new WekaModel((WekaClassifier) helper.classifier);
                helper.parameters = new WekaParameters();
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to load serialized weka model.", e);
            }
        } else {
            final String message = "Classifier model type not recognized - Cannot load."
                    + "Parameters were: " + parameters;
            LOG.error(message);
            throw new IllegalArgumentException(message);
        }
        helper.parseParameters(helper.classifier, splitModelParameters(parameters));
        helper.classifier.setParameters(helper.parameters);
        return helper;
    }

    public static String[] splitModelParameters(final String parameterToken) {
        final String[] result = parameterToken.split("[,]");
        if (result.length == 1 && result[0].length() == 0) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        } else {
            return result;
        }
    }
}
