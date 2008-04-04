package edu.cornell.med.icb.learning;

/**
 *@author Fabien Campagne
 * Date: Mar 30, 2008
 * Time: 3:18:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class MultiplyScalingProcessor extends FeatureScaler {
    private double multiplicationFactor;

    public MultiplyScalingProcessor(double multiplier) {
        super();
        this.multiplicationFactor = multiplier;
    }

    @Override
    public void observeFeatureForTraining(int numFeatures, double[] featureValues, int featureIndex) {
    }

    @Override
    public double scaleFeatureValue(double featureValue, int featureIndex) {
       return featureValue* multiplicationFactor;
    }
}
