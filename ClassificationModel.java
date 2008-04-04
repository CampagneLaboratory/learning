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

import java.io.IOException;

/**
 * Abstracts a classification model.
 *
 * @author Fabien Campagne Date: Nov 19, 2007 Time: 9:29:12 AM
 */
public abstract class ClassificationModel {
    private static final Log LOG = LogFactory.getLog(ClassificationModel.class);

    /**
     * Write the model to a file.
     *
     * @param filename Filename where to store the model.
     * @throws IOException thrown if an error occurs writting filename.
     */
    public abstract void write(String filename) throws IOException;

    /**
     * Load a trained model from a file.
     *
     * @param modelFilename
     * @return
     * @throws IOException
     */
    public static ClassificationHelper load(final String modelFilename) throws IOException {
        return load(modelFilename, null);
    }

    public static ClassificationHelper load(final String modelFilename, String modelParameters) throws IOException {
        final ClassificationHelper helper = new ClassificationHelper();
        if (modelFilename.contains("libSVM")) {

            helper.model = new LibSvmModel(modelFilename);
            helper.classifier = new LibSvmClassifier();
            helper.parameters = new LibSvmParameters();

        } else if (modelFilename.contains("weka!")) {
            try {
                helper.classifier = new WekaClassifier((weka.classifiers.Classifier) BinIO.loadObject(modelFilename));

                helper.model = new WekaModel((WekaClassifier) helper.classifier);
                helper.parameters = new WekaParameters();
            } catch (ClassNotFoundException e) {
                LOG.error("Unable to load serialized weka model.", e);
            }
        } else {
            LOG.error("Classifier model type not recognized. Cannot load.");
            throw new InternalError("Classifier model type not recognized. Cannot load.");
        }
        helper.parseParameters(helper.classifier, modelParameters == null ? extractModelParameters(modelFilename) :
                splitModelParameters(modelParameters));
        helper.classifier.setParameters(helper.parameters);
        return helper;
    }

    private static String[] extractModelParameters(final String modelFilename) {
        final String[] tokens = modelFilename.split("[_]");
        assert tokens.length >= 2 : "model filename must have more than two fields separated by underscore characters";
        return splitModelParameters(tokens[1]);
    }

    public static String[] splitModelParameters(String parameterToken) {
        final String[] result = parameterToken.split("[,]");
        if (result.length == 1 && result[0].length() == 0) {
            return new String[0];
        } else {
            return result;
        }
    }
}
