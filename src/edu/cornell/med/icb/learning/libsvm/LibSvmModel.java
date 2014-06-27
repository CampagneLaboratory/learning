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
import org.apache.commons.io.IOUtils;

import java.io.*;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:35:52 PM
 */
public class LibSvmModel extends ClassificationModel {
    svm_model nativeModel;

    public LibSvmModel(final svm_model svm_model) {
        super();
        nativeModel = svm_model;
    }

    public LibSvmModel(final InputStream stream) throws IOException {
        super();
        nativeModel = svm.svm_load_model(new BufferedReader(new InputStreamReader(stream)));
    }

    public LibSvmModel(final String modelFilename) throws IOException {
        super();
        nativeModel = svm.svm_load_model(modelFilename);
    }

    @Override
    public void write(final String filename) throws IOException {
        svm.svm_save_model(filename, nativeModel);
    }

    @Override
    public void write(final OutputStream stream) throws IOException {

        // write the model to a temporary file, then copy the file content to the stream.
        File tmp;
        FileInputStream fis =null;
        try {
            tmp= File.createTempFile("model", ".svm");
            this.write(tmp.getAbsolutePath());

            fis = new FileInputStream(tmp);
            IOUtils.copy(fis, stream);
            tmp.delete();
        } catch (IOException e) {
            throw new RuntimeException("Unable to write model to output stream.",e);
        } finally {
            IOUtils.closeQuietly(fis);
        }
        //throw new UnsupportedOperationException("Unable to write model to a stream in libSVM 3.18");
        //svm.svm_save_model(stream, nativeModel);
    }

    public svm_model getNativeModel() {
        return nativeModel;
    }
}
