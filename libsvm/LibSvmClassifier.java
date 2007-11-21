package edu.cornell.med.icb.learning.libsvm;

import edu.cornell.med.icb.learning.ClassificationProblem;
import edu.cornell.med.icb.learning.Classifier;
import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ClassificationParameters;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_parameter;
import libsvm.svm_problem;
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
 * @author: Fabien Campagne Date: Nov 20, 2007 Time: 5:24:27 PM
 */
public class LibSvmClassifier implements Classifier {
	private svm_parameter nativeParameters;


	public LibSvmClassifier() {
		this.nativeParameters = new LibSvmParameters().getNative();
	}

	public ClassificationModel train(final ClassificationProblem problem) {
		svm_problem nativeProblem = getNativeProblem(problem);
		return new LibSvmModel(svm.svm_train(nativeProblem, nativeParameters));
	}

	private svm_problem getNativeProblem(final ClassificationProblem problem) {
		assert problem instanceof LibSvmProblem;
		return ((LibSvmProblem) problem).getNative();

	}

	public double predict(final ClassificationModel trainingModel, final ClassificationProblem problem,
						  final int instanceIndex) {
		return svm.svm_predict(getNativeModel(trainingModel), getNativeProblem(problem).x[instanceIndex]);
	}

	public ClassificationParameters getParameters() {
		return new LibSvmParameters(nativeParameters);
	}

	private svm_model getNativeModel(final ClassificationModel trainingModel) {
		assert trainingModel instanceof LibSvmModel;
		return ((LibSvmModel) trainingModel).nativeModel;
	}

	public ClassificationProblem newProblem(final int size) {
		return new LibSvmProblem();
	}

	public ClassificationModel train(ClassificationProblem problem, ClassificationParameters parameters) {
		setParameters(parameters);
		return this.train(problem);
	}

	public void setParameters(ClassificationParameters parameters) {
		assert parameters instanceof LibSvmParameters;
		this.nativeParameters = ((LibSvmParameters) parameters).getNative();
	}
}
