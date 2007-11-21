package edu.cornell.med.icb.learning.libsvm;

import edu.mssm.crover.tables.writers.ClassificationParameters;
import libsvm.svm_parameter;
/*
 * Copyright (C) 2001-2002 Mount Sinai School of Medicine
 * Copyright (C) 2003-2007 Institute for Computational Biomedicine,
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

/**
 * @author: Fabien Campagne Date: Nov 20, 2007 Time: 5:29:48 PM
 */
public class LibSvmParameters extends ClassificationParameters {
	svm_parameter nativeParameters;

	public LibSvmParameters(final svm_parameter nativeParameters) {
		this.nativeParameters = nativeParameters;
	}


	public LibSvmParameters() {

		nativeParameters = new svm_parameter();
		nativeParameters.svm_type = svm_parameter.C_SVC;
		nativeParameters.kernel_type = svm_parameter.LINEAR;
		nativeParameters.degree = 3;
		nativeParameters.gamma = 0;	// 1/k
		nativeParameters.coef0 = 0;
		nativeParameters.nu = 0.5;
		nativeParameters.cache_size = 100;
		nativeParameters.C = 1;
		nativeParameters.eps = 1e-3;
		nativeParameters.p = 0.1;
		nativeParameters.shrinking = 1;	 // use shrinking heuristic.
		nativeParameters.probability = 0;
		// set weights according to proportion in the training set:
		nativeParameters.nr_weight = 0;
		nativeParameters.weight_label = new int[nativeParameters.nr_weight];
		nativeParameters.weight = new double[nativeParameters.nr_weight];

	}

	public svm_parameter getNative() {
		return nativeParameters;
	}

	public void setParameter(final String parameterName, final double value) {
		if (parameterName.equals("C")) {
			nativeParameters.C = value;
		}
	}
}
