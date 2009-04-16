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
import it.unimi.dsi.fastutil.io.BinIO;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Fabien Campagne Date: Nov 23, 2007 Time: 1:29:58 PM
 */
public class WekaModel extends ClassificationModel {
    final WekaClassifier classifier;

    public WekaModel(final WekaClassifier wekaClassifier) {
        super();
        classifier = wekaClassifier;
    }

    /**
     * Store the weka model as a serialized object. Not much we can do with it except load it
     * again, but it is not clear how to save a model into a human-readable format from the
     * Weka API.
     *
     * @param filename Filename to use to save the model.
     * @throws IOException thrown if an error occurs writing file.
     */
    @Override
    public void write(final String filename) throws IOException {
        BinIO.storeObject(classifier.getNative(), filename);
    }

    /**
     * Store the weka model as a serialized object. Not much we can do with it except load it
     * again, but it is not clear how to save a model into a human-readable format from the
     * Weka API.
     *
     * @param stream stream to use to save the model.
     * @throws IOException thrown if an error occurs writing stream.
     */
    @Override
    public void write(final OutputStream stream) throws IOException {
        BinIO.storeObject(classifier.getNative(), stream);
    }
}
