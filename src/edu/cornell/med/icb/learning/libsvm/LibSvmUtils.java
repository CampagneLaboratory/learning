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

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

/**
 * @author Fabien Campagne Date: Oct 18, 2007 Time: 6:54:58 PM
 */
public class LibSvmUtils {

    public static double[] calculateWeights(final svm_model model) {
        if (model.param.kernel_type == svm_parameter.LINEAR &&
                svm.svm_get_svm_type(model) == svm_parameter.C_SVC) {
            final int numFeatures = model.SV[0].length;
            final double[] weights = new double[numFeatures];
            int supportVectorIndex = 0;
            for (final svm_node[] supportVector : model.SV) {
                final double alpha_y = model.sv_coef[0][supportVectorIndex++] * model.label[0];  // alpha * y
                for (final svm_node vectorElement : supportVector) {

                    weights[vectorElement.index] += alpha_y * vectorElement.value;
                }
            }
            return weights;
        } else {
            return null;
        }
    }
}
