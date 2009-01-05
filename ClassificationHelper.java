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

package edu.cornell.med.icb.learning;

/**
 * @author Fabien Campagne
 */
public class ClassificationHelper {
    public Classifier classifier;
    public ClassificationProblem problem;
    public ClassificationParameters parameters;
    /**
     * The model, if loaded from a file. Null otherwise.
     */
    public ClassificationModel model;

    public void parseParameters(final Classifier classifier, final String[] classifierParameters) {

        for (final String parameter : classifierParameters) {
            final double value = getParameterValue(parameter);
            final String key = getParameterKey(parameter);
            
            //  System.out.println("Setting parameter " + parameter);
            classifier.getParameters().setParameter(key, value);
            this.parameters=classifier.getParameters();
        }
    }

    private double getParameterValue(final String parameter) {
        final String[] tokens = parameter.split("[=]");
        double value = Double.NaN;
        if (tokens.length == 2) {
            try {
                value = Double.parseDouble(tokens[1]);
                return value;
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
        return value;
    }

    private String getParameterKey(final String parameter) {
        final String[] tokens = parameter.split("[=]");
        if (tokens.length == 2) {
            try {
                Double.parseDouble(tokens[1]);
                return tokens[0];
            } catch (NumberFormatException e) {

            }
        }
        return parameter;
    }

}
