package edu.cornell.med.icb.learning;

/**
 * This default implementation does not scale features at all. Sub-classes must implement the scaling strategy by
 * overriding the scaler methods.
 * @author Fabien Campagne
 *         Date: Mar 30, 2008
 *         Time: 11:26:41 AM
 */
public class FeatureScaler {

    /**
     * Observe the values of a feature over a training set. This method must be called before features
     * can  be scaled. It derives statistics from the feature values that are needed to scale individual
     * features.
     *
     * @param numFeatures
     * @param featureValues Values of the feature over the training set.
     * @param featureIndex
     */
    public void observeFeatureForTraining(int numFeatures, double[] featureValues, int featureIndex) {
        return;
    }

    public double scaleFeatureValue(double featureValue, int featureIndex) {
        return featureValue;
    }


}
