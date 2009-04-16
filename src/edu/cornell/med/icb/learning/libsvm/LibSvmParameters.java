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

import edu.cornell.med.icb.learning.ClassificationParameters;
import libsvm.svm_parameter;

/**
 * @author Fabien Campagne Date: Nov 20, 2007 Time: 5:29:48 PM
 */
public class LibSvmParameters extends ClassificationParameters {
    final svm_parameter nativeParameters;

    public LibSvmParameters(final svm_parameter nativeParameters) {
        super();
        this.nativeParameters = nativeParameters;
    }

    public LibSvmParameters() {
        super();
        nativeParameters = new svm_parameter();
        nativeParameters.kernel_type = svm_parameter.LINEAR;
        registerExposedParameter("kernel=linear");
        registerExposedParameter("kernel=RBF");
        registerExposedParameter("kernel=polynomial");
        registerExposedParameter("kernel=sigmoid");

        this.nativeParameters.svm_type = svm_parameter.C_SVC;
        registerExposedParameter("machine=SVC");
        registerExposedParameter("machine=EPSILON_SVR");
        registerExposedParameter("machine=ONE_CLASS");
        registerExposedParameter("machine=nuSVC");
        registerExposedParameter("machine=nuSVR");

        nativeParameters.degree = 3;
        registerExposedParameter("degree");
        nativeParameters.gamma = 0;    // 1/k
        registerExposedParameter("gamma");
        nativeParameters.coef0 = 0;
        registerExposedParameter("coef0");
        nativeParameters.nu = 0.5;
        registerExposedParameter("nu");
        nativeParameters.cache_size = 100;
        registerExposedParameter("cache_size");
        nativeParameters.C = 1;
        registerExposedParameter("C");
        nativeParameters.eps = 1e-3;
        registerExposedParameter("eps"); // The stopping criterion
        nativeParameters.p = 0.1;
        registerExposedParameter("p");
        nativeParameters.shrinking = 1;     // use shrinking heuristic.
        registerExposedParameter("shrinking=true");
        registerExposedParameter("shrinking=false");
        nativeParameters.probability = 0;
        registerExposedParameter("probability=true");
        registerExposedParameter("probability=false");
        // set weights according to proportion in the training set:
        nativeParameters.nr_weight = 0;
        nativeParameters.weight_label = new int[nativeParameters.nr_weight];
        nativeParameters.weight = new double[nativeParameters.nr_weight];
    }

    public svm_parameter getNative() {
        return nativeParameters;
    }

    @Override
    public void setParameter(final String parameterName, final double value) {
        checkParameterRegistered(parameterName);
        if (parameterName.equals("C")) {
            nativeParameters.C = value;
        } else if (parameterName.equals("kernel=linear")) {
            nativeParameters.kernel_type = svm_parameter.LINEAR;
        } else if (parameterName.equals("kernel=RBF")) {
            nativeParameters.kernel_type = svm_parameter.RBF;
        } else if (parameterName.equals("kernel=polynomial")) {
            nativeParameters.kernel_type = svm_parameter.POLY;
        } else if (parameterName.equals("kernel=sigmoid")) {
            nativeParameters.kernel_type = svm_parameter.SIGMOID;
        } else if (parameterName.equals("gamma")) {   // RBF 2nd parameter
            nativeParameters.gamma = value;
        } else if (parameterName.equals("nu")) { // What is this paramater for?
            nativeParameters.nu = value;
        } else if (parameterName.equals("coef0")) { // polynomial coefficient
            nativeParameters.coef0 = value;
        } else if (parameterName.equals("degree")) { // polynomial degree
            nativeParameters.degree = value;
        } else if (parameterName.equals("cache_size")) {
            nativeParameters.cache_size = value;
        } else if (parameterName.equals("shrinking=true")) { // shrinking on
            nativeParameters.shrinking = 1;
        } else if (parameterName.equals("shrinking=false")) { // shrinking off
            nativeParameters.shrinking = 0;
        } else if (parameterName.equals("eps")) { // The stopping criterion
            nativeParameters.eps = value;
        } else if (parameterName.equals("probability=true")) { // probability estimates will be estimated
            nativeParameters.probability = 1;
        } else if (parameterName.equals("probability=false")) { // probability estimates will NOT be estimated
            nativeParameters.probability = 0;
        } else if (parameterName.equals("machine=SVC")) {
            nativeParameters.svm_type = svm_parameter.C_SVC;
        } else if (parameterName.equals("machine=EPSILON_SVR")) {
            nativeParameters.svm_type = svm_parameter.EPSILON_SVR;
        } else if (parameterName.equals("machine=ONE_CLASS")) {
            nativeParameters.svm_type = svm_parameter.EPSILON_SVR;
        } else if (parameterName.equals("machine=nuSVC")) {
            nativeParameters.svm_type = svm_parameter.NU_SVC;
        } else if (parameterName.equals("machine=nuSVR")) {
            nativeParameters.svm_type = svm_parameter.NU_SVR;
        } else if (parameterName.equals("p")) {
            nativeParameters.p = value;
        }
    }

    private void checkParameterRegistered(final String parameterName) {
        if (!getExposedParameterNames().contains(parameterName)) {
            System.err.println(" Parameter " + parameterName + " is not defined for libSVM. Parameter ignored.");
        }
    }
}
