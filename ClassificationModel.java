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

package edu.cornell.med.icb.learning;

import edu.cornell.med.icb.learning.libsvm.LibSvmClassifier;
import edu.cornell.med.icb.learning.libsvm.LibSvmModel;
import edu.cornell.med.icb.learning.libsvm.LibSvmParameters;
import edu.cornell.med.icb.learning.weka.WekaClassifier;
import edu.cornell.med.icb.learning.weka.WekaModel;
import edu.cornell.med.icb.learning.weka.WekaParameters;
import it.unimi.dsi.fastutil.io.BinIO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.ArrayUtils;

import java.io.IOException;
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
     * @param filename Filename where to store the model.
     * @param parameters parameters for the model
     * @throws IOException thrown if an error occurs writing filename.
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
        helper.parseParameters(helper.classifier, parameters == null
                ? extractModelParameters(filename) : splitModelParameters(parameters));
        helper.classifier.setParameters(helper.parameters);
        return helper;
    }

    private static String[] extractModelParameters(final String modelFilename) {
        final String[] tokens = modelFilename.split("[_]");
        if (tokens.length < 2) {
            throw new IllegalArgumentException("Model filename must have more than two "
                    + "fields separated by underscore characters.  Filename provided was: "
                    +  modelFilename);
        }
        return splitModelParameters(tokens[1]);
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
