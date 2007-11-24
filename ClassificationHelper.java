package edu.cornell.med.icb.learning;

import edu.mssm.crover.tables.writers.ClassificationModel;
import edu.mssm.crover.tables.writers.ClassificationParameters;

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

        for (String parameter : classifierParameters) {
            double value = getParameterValue(parameter);
            String key = getParameterKey(parameter);
            System.out.println("Setting parameter " + parameter);
            classifier.getParameters().setParameter(key, value);
            this.parameters=classifier.getParameters();
        }
    }

    private double getParameterValue(final String parameter) {
        String[] tokens = parameter.split("[=]");
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
        String[] tokens = parameter.split("[=]");
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
