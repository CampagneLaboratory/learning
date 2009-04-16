package edu.cornell.med.icb.learning;

import edu.cornell.med.icb.learning.libsvm.LibSvmProblem;
import edu.cornell.med.icb.learning.weka.WekaProblem;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import junit.framework.TestCase;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Fabien Campagne
 *         Date: Mar 30, 2008
 *         Time: 3:04:32 PM
 */
public class TestSVMLibAdapter extends TestCase {
    @Test
    public void testCreateProblem() {
        final LibSvmProblem problem = new LibSvmProblem();
        final int instanceIndex = problem.addInstance(10);
        final double label = 0;
        final double[] features = new double[10];
        Arrays.fill(features, 1);
        problem.setInstance(instanceIndex, label, features);
        final double[] newFeatures = problem.getFeatures(instanceIndex);
        assertEquals(features, newFeatures);

        final FeatureScaler scaler = new MultiplyScalingProcessor(3.6);
        final ClassificationProblem scaledProblem = problem.scaleTraining(scaler);
        final double[] expectedFeatures = new double[10];
        Arrays.fill(expectedFeatures, 3.6);
        final double[] scaledFeatures = ((LibSvmProblem) scaledProblem).getFeatures(instanceIndex);
        assertEquals(scaledFeatures, expectedFeatures);

    }

    @Test
    public void testCreateProblemMultiInstances() {
        final LibSvmProblem problem = new LibSvmProblem();
        final double[] features = new double[10];
        final double[] features2 = new double[10];

        final int instanceIndex = problem.addInstance(10);
        final double label = 0;
        Arrays.fill(features, 1);
        problem.setInstance(instanceIndex, label, features);
        final double[] newFeatures = problem.getFeatures(instanceIndex);
        assertEquals(features, newFeatures);

        final int instance2Index = problem.addInstance(10);
        final double label2 = 1;
        Arrays.fill(features2, 2);
        problem.setInstance(instance2Index, label2, features2);
        final double[] newFeatures2 = problem.getFeatures(instance2Index);
        assertEquals(features2, newFeatures2);

        final FeatureScaler scaler = new MultiplyScalingProcessor(3.6);
        final ClassificationProblem scaledProblem = problem.scaleTraining(scaler);
        final double[] expectedFeatures = new double[10];
        Arrays.fill(expectedFeatures, 3.6);
        final double[] expectedFeatures2 = new double[10];
        Arrays.fill(expectedFeatures2, 2 * 3.6);
        final double[] scaledFeatures = ((LibSvmProblem) scaledProblem).getFeatures(instanceIndex);
        assertEquals(scaledFeatures, expectedFeatures);
        final double[] scaledFeatures2 = ((LibSvmProblem) scaledProblem).getFeatures(instance2Index);
        assertEquals(scaledFeatures2, expectedFeatures2);

    }
    @Test
       public void testCreateProblemMultiInstancesWeka() {
           final WekaProblem problem = new WekaProblem();
           final double[] features = new double[10];
           final double[] features2 = new double[10];

           final int instanceIndex = problem.addInstance(10);
           final double label = 0;
           Arrays.fill(features, 1);
           problem.setInstance(instanceIndex, label, features);
           final double[] newFeatures = problem.getFeatures(instanceIndex);
           assertEquals(features, newFeatures);

           final int instance2Index = problem.addInstance(10);
           final double label2 = 1;
           Arrays.fill(features2, 2);
           problem.setInstance(instance2Index, label2, features2);
           final double[] newFeatures2 = problem.getFeatures(instance2Index);
           assertEquals(features2, newFeatures2);

           final FeatureScaler scaler = new MultiplyScalingProcessor(3.6);
           final ClassificationProblem scaledProblem = problem.scaleTraining(scaler);
           final double[] expectedFeatures = new double[10];
           Arrays.fill(expectedFeatures, 3.6);
           final double[] expectedFeatures2 = new double[10];
           Arrays.fill(expectedFeatures2, 2 * 3.6);
           final double[] scaledFeatures = ((WekaProblem) scaledProblem).getFeatures(instanceIndex);
           assertEquals(scaledFeatures, expectedFeatures);
           final double[] scaledFeatures2 = ((WekaProblem) scaledProblem).getFeatures(instance2Index);
           assertEquals(scaledFeatures2, expectedFeatures2);

       }

    public void assertEquals(final double[] array1, final double[] array2) {
        assertEquals(array1.length, array2.length);
        for (int i = 0; i < array1.length; i++) {
            assertEquals(array1[i], array2[i]);
        }
    }

    @Test
    public void testScaleMinMaxMultiInstances() {
        final LibSvmProblem problem = new LibSvmProblem();
        // featureIndex                           0     1    2   3
        final double[] trainingInstanceFeatures1 = new double[]{30, 140, 130, 45};
        final double[] trainingInstanceFeatures2 = new double[]{35, 140, 145, 46};
        final double[] trainingInstanceFeatures3 = new double[]{40, 140, 140, 47};
        final double[] trainingInstanceFeatures4 = new double[]{45, 140, 135, 48};
        final double[] trainingInstanceFeatures5 = new double[]{50, 140, 150, 49};

        // mean                                   40  140  140  47
        // min                                    30  140  130  45
        // max                                    50  140  145  49
        // range                                  20    0   15   4

        final double[] testInstanceFeatures6 = new double[]{20, 120, 120, 42};
        final double[] testInstanceFeatures7 = new double[]{40, 140, 140, 47};
        final double[] testInstanceFeatures8 = new double[]{60, 150, 160, 55};

        final int instanceIndex1 = problem.addInstance(4);
        final int instanceIndex2 = problem.addInstance(4);
        final int instanceIndex3 = problem.addInstance(4);
        final int instanceIndex4 = problem.addInstance(4);
        final int instanceIndex5 = problem.addInstance(4);
        final int instanceIndex6 = problem.addInstance(4);
        final int instanceIndex7 = problem.addInstance(4);
        final int instanceIndex8 = problem.addInstance(4);

        final double label = 0;

        problem.setInstance(instanceIndex1, label, trainingInstanceFeatures1);
        problem.setInstance(instanceIndex2, label, trainingInstanceFeatures2);
        problem.setInstance(instanceIndex3, label, trainingInstanceFeatures3);
        problem.setInstance(instanceIndex4, label, trainingInstanceFeatures4);
        problem.setInstance(instanceIndex5, label, trainingInstanceFeatures5);

        problem.setInstance(instanceIndex6, label, testInstanceFeatures6);
        problem.setInstance(instanceIndex7, label, testInstanceFeatures7);
        problem.setInstance(instanceIndex8, label, testInstanceFeatures8);


        final IntSet trainingSetIndices = new IntArraySet();
        trainingSetIndices.add(0);
        trainingSetIndices.add(1);
        trainingSetIndices.add(2);
        trainingSetIndices.add(3);
        trainingSetIndices.add(4);


        final IntSet testSetIndices = new IntArraySet();
        testSetIndices.add(5);
        testSetIndices.add(6);
        testSetIndices.add(7);


        final ClassificationProblem trainingSet = problem.filter(trainingSetIndices);

        final FeatureScaler scaler = new MinMaxScalingRowProcessor();
        final ClassificationProblem scaledTraining = trainingSet.scaleTraining(scaler);
        final LibSvmProblem lsvmScaledTrainingProblem = (LibSvmProblem) scaledTraining;

        for (int trainInstanceIndex = 0; trainInstanceIndex < lsvmScaledTrainingProblem.getSize(); trainInstanceIndex++)
        {
            final double[] trainFeatures = lsvmScaledTrainingProblem.getFeatures(trainInstanceIndex);
            System.out.println("scaledTrainFeatures" + trainInstanceIndex + ": " + ArrayUtils.toString(trainFeatures));
        }

        assertNotSame("scaled problem must be a different instance", scaledTraining, problem);
        for (final int testInstanceIndex : testSetIndices) {
            final ClassificationProblem scaledTestingProblem = problem.scaleTestSet(scaler, testInstanceIndex);
            final LibSvmProblem lsvmScaledTestingProblem = (LibSvmProblem) scaledTestingProblem;
            assertNotSame("scaled problem must be a different instance", scaledTestingProblem, problem);
            final double[] scaledTestFeatures6 = lsvmScaledTestingProblem.getFeatures(0);
            System.out.println("scaledTestFeatures" + testInstanceIndex + ": " + ArrayUtils.toString(scaledTestFeatures6));
        }


    }

     @Test
    public void testScaleMinMaxMultiInstancesWeka() {
        final WekaProblem problem = new WekaProblem();
        // featureIndex                           0     1    2   3
        final double[] trainingInstanceFeatures1 = new double[]{30, 140, 130, 45};
        final double[] trainingInstanceFeatures2 = new double[]{35, 140, 145, 46};
        final double[] trainingInstanceFeatures3 = new double[]{40, 140, 140, 47};
        final double[] trainingInstanceFeatures4 = new double[]{45, 140, 135, 48};
        final double[] trainingInstanceFeatures5 = new double[]{50, 140, 150, 49};

        // mean                                   40  140  140  47
        // min                                    30  140  130  45
        // max                                    50  140  145  49
        // range                                  20    0   15   4

        final double[] testInstanceFeatures6 = new double[]{20, 120, 120, 42};
        final double[] testInstanceFeatures7 = new double[]{40, 140, 140, 47};
        final double[] testInstanceFeatures8 = new double[]{60, 150, 160, 55};

        final int instanceIndex1 = problem.addInstance(4);
        final int instanceIndex2 = problem.addInstance(4);
        final int instanceIndex3 = problem.addInstance(4);
        final int instanceIndex4 = problem.addInstance(4);
        final int instanceIndex5 = problem.addInstance(4);
        final int instanceIndex6 = problem.addInstance(4);
        final int instanceIndex7 = problem.addInstance(4);
        final int instanceIndex8 = problem.addInstance(4);

        final double label = 0;

        problem.setInstance(instanceIndex1, label, trainingInstanceFeatures1);
        problem.setInstance(instanceIndex2, label, trainingInstanceFeatures2);
        problem.setInstance(instanceIndex3, label, trainingInstanceFeatures3);
        problem.setInstance(instanceIndex4, label, trainingInstanceFeatures4);
        problem.setInstance(instanceIndex5, label, trainingInstanceFeatures5);

        problem.setInstance(instanceIndex6, label, testInstanceFeatures6);
        problem.setInstance(instanceIndex7, label, testInstanceFeatures7);
        problem.setInstance(instanceIndex8, label, testInstanceFeatures8);


        final IntSet trainingSetIndices = new IntArraySet();
        trainingSetIndices.add(0);
        trainingSetIndices.add(1);
        trainingSetIndices.add(2);
        trainingSetIndices.add(3);
        trainingSetIndices.add(4);


        final IntSet testSetIndices = new IntArraySet();
        testSetIndices.add(5);
        testSetIndices.add(6);
        testSetIndices.add(7);


        final ClassificationProblem trainingSet = problem.filter(trainingSetIndices);

        final FeatureScaler scaler = new MinMaxScalingRowProcessor();
        final ClassificationProblem scaledTraining = trainingSet.scaleTraining(scaler);
        final WekaProblem lsvmScaledTrainingProblem = (WekaProblem) scaledTraining;

        for (int trainInstanceIndex = 0; trainInstanceIndex < lsvmScaledTrainingProblem.getSize(); trainInstanceIndex++)
        {
            final double[] trainFeatures = lsvmScaledTrainingProblem.getFeatures(trainInstanceIndex);
            System.out.println("scaledTrainFeatures" + trainInstanceIndex + ": " + ArrayUtils.toString(trainFeatures));
        }

        assertNotSame("scaled problem must be a different instance", scaledTraining, problem);
        for (final int testInstanceIndex : testSetIndices) {
            final ClassificationProblem scaledTestingProblem = problem.scaleTestSet(scaler, testInstanceIndex);
            final WekaProblem lsvmScaledTestingProblem = (WekaProblem) scaledTestingProblem;
            assertNotSame("scaled problem must be a different instance", scaledTestingProblem, problem);
            final double[] scaledTestFeatures6 = lsvmScaledTestingProblem.getFeatures(0);
            System.out.println("scaledTestFeatures" + testInstanceIndex + ": " + ArrayUtils.toString(scaledTestFeatures6));
        }


    }
}
