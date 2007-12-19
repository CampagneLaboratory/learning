/*
 * Copyright (C) 2007 Institute for Computational Biomedicine,
 *                    Weill Medical College of Cornell University
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

import edu.mssm.crover.tables.writers.ClassificationModel;
import libsvm.svm;
import libsvm.svm_model;

import java.io.IOException;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:35:52 PM
 */
public class LibSvmModel extends ClassificationModel {
    svm_model nativeModel;

    public LibSvmModel(final svm_model svm_model) {
        this.nativeModel = svm_model;
    }

    public LibSvmModel(final String modelFilename) throws IOException {
        nativeModel = svm.svm_load_model(modelFilename);
    }

    @Override
    public void write(final String filename) throws IOException {
        svm.svm_save_model(filename, nativeModel);
    }
}
