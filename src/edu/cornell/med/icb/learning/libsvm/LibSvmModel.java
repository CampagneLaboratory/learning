/*
 * Copyright (C) 2007-2010 Institute for Computational Biomedicine,
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
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:35:52 PM
 */
public class LibSvmModel extends ClassificationModel {
    svm_model nativeModel;
    boolean isRegressionModel;

    public LibSvmModel(final svm_model svm_model) {
        super();
        nativeModel = svm_model;
        this.isRegressionModel = isRegression();
    }

    public LibSvmModel(final InputStream stream) throws IOException {
        super();
        nativeModel = svm.svm_load_model(new BufferedReader(new InputStreamReader(stream)));
        isRegressionModel= isRegression();
    }

    public boolean isRegression() {
        return (nativeModel.param.svm_type== svm_parameter.NU_SVR||nativeModel.param.svm_type== svm_parameter.EPSILON_SVR);
    }

    public LibSvmModel(final String modelFilename) throws IOException {
        super();
        nativeModel = svm.svm_load_model(modelFilename);
        isRegressionModel= isRegression();
    }

    @Override
    public void write(final String filename) throws IOException {
        svm.svm_save_model(filename, nativeModel);
    }

    @Override
    public void write(final OutputStream stream) throws IOException {
        // write to a temporary file, then copy content to the output stream:
        File tmp = File.createTempFile("libsvm-model", "vsm");
        svm.svm_save_model(tmp.getAbsolutePath(), nativeModel);
        IOUtils.copy(new FileInputStream(tmp), stream);
        IOUtils.closeQuietly(stream);
        tmp.delete();
    }

    public svm_model getNativeModel() {
        return nativeModel;
    }
}
