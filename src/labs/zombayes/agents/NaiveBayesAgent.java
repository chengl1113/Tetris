package src.labs.zombayes.agents;

// SYSTEM IMPORTS
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// JAVA PROJECT IMPORTS
import edu.bu.labs.zombayes.agents.SurvivalAgent;
import edu.bu.labs.zombayes.features.Features.FeatureType;
import edu.bu.labs.zombayes.linalg.Matrix;
import edu.bu.labs.zombayes.utils.Pair;

public class NaiveBayesAgent
        extends SurvivalAgent {

    public static class NaiveBayes
            extends Object {

        public static final FeatureType[] FEATURE_HEADER = { FeatureType.CONTINUOUS,
                FeatureType.CONTINUOUS,
                FeatureType.DISCRETE,
                FeatureType.DISCRETE };

        private Map<Double, Integer> prior_map;
        private Map<Double, Double> probability_map;

        private double featureOne_human_mean;
        private double featureOne_human_variance;
        private double featureOne_zombie_mean;
        private double featureOne_zombie_variance;

        private double featureTwo_human_mean;
        private double featureTwo_human_variance;
        private double featureTwo_zombie_mean;
        private double featureTwo_zombie_variance;

        private Matrix featureThree;
        private Matrix featureFour;

        // TODO: complete me!
        public NaiveBayes() {
            prior_map = new HashMap<>();
            prior_map.put(0.0, 0);
            prior_map.put(1.0, 0);

            probability_map = new HashMap<>();

            featureOne_human_mean = 0;
            featureOne_human_variance = 0;
            featureOne_zombie_mean = 0;
            featureOne_zombie_variance = 0;

            featureTwo_human_mean = 0;
            featureTwo_human_variance = 0;
            featureTwo_zombie_mean = 0;
            featureTwo_zombie_variance = 0;

            featureThree = Matrix.zeros(3, 3);
            featureThree.set(0, 0, 0);
            featureThree.set(1, 0, 0);
            featureThree.set(2, 0, 0);

            featureFour = Matrix.zeros(4, 3);
            featureFour.set(0, 0, 0);
            featureFour.set(1, 0, 0);
            featureFour.set(2, 0, 0);
            featureFour.set(3, 0, 0);
        }

        // TODO: complete me!
        public void fit(Matrix X, Matrix y_gt) {
            // Class prior calculations
            for (int i = 0; i < y_gt.getShape().getNumRows(); i++) {
                if (y_gt.get(i, 0) == 0.0) {
                    prior_map.put(0.0, prior_map.get(0.0) + 1);
                } else {
                    prior_map.put(1.0, prior_map.get(1.0) + 1);
                }
            }

            // New variables to store sums of features 1 and 2
            float totalObservations = y_gt.getShape().getNumRows();

            double prob_human = prior_map.get(0.0) / totalObservations;
            double prob_zombie = prior_map.get(1.0) / totalObservations;

            probability_map.put(new Double(0), prob_human);
            probability_map.put(new Double(1), prob_zombie);

            double featureOne_human_sum = 0;
            double featureOne_zombie_sum = 0;
            double featureTwo_human_sum = 0;
            double featureTwo_zombie_sum = 0;

            for (int i = 0; i < X.getShape().getNumRows(); i++) {
                if (y_gt.get(i, 0) == 0.0) {
                    featureOne_human_sum += X.get(i, 0);
                    featureTwo_human_sum += X.get(i, 1);
                } else {
                    featureOne_zombie_sum += X.get(i, 0);
                    featureTwo_zombie_sum += X.get(i, 1);
                }

                // Counting new occurences
                int gt = (int) y_gt.get(i, 0);
                int featureThree_temp = (int) X.get(i, 2);
                int featureFour_temp = (int) X.get(i, 3);
                int featureThree_value = (int) featureThree.get(featureThree_temp, gt + 1);
                int featureFour_value = (int) featureFour.get(featureFour_temp, gt + 1);
                featureThree.set(featureThree_temp, gt + 1, featureThree_value + 1);
                featureFour.set(featureFour_temp, gt + 1, featureFour_value + 1);
            }

            // Human Calcs
            int num_humans = prior_map.get(0.0);

            featureOne_human_mean = featureOne_human_sum / num_humans;
            featureTwo_human_mean = featureTwo_human_sum / num_humans;

            double One_sum_diff_squared_human = 0;
            double Two_sum_diff_squared_human = 0;

            for (int i = 0; i < X.getShape().getNumRows(); i++) {
                if (y_gt.get(i, 0) == 0.0) {
                    double featureOne_temp = X.get(i, 0);
                    One_sum_diff_squared_human += Math.pow((featureOne_temp - featureOne_human_mean), 2);

                    double featureTwo_temp = X.get(i, 1);
                    Two_sum_diff_squared_human += Math.pow((featureTwo_temp - featureTwo_human_mean), 2);
                }
            }

            featureOne_human_variance = One_sum_diff_squared_human / num_humans;
            featureTwo_human_variance = Two_sum_diff_squared_human / num_humans;

            // Zombie Calcs
            int num_zombies = prior_map.get(1.0);

            featureOne_zombie_mean = featureOne_zombie_sum / num_zombies;
            featureTwo_zombie_mean = featureTwo_zombie_sum / num_zombies;

            double One_sum_diff_squared_zombie = 0;
            double Two_sum_diff_squared_zombie = 0;

            for (int i = 0; i < X.getShape().getNumRows(); i++) {
                if (y_gt.get(i, 0) == 1.0) {
                    double featureOne_temp = X.get(i, 0);
                    One_sum_diff_squared_zombie += Math.pow((featureOne_temp - featureOne_zombie_mean), 2);

                    double featureTwo_temp = X.get(i, 1);
                    Two_sum_diff_squared_zombie += Math.pow((featureTwo_temp - featureTwo_zombie_mean), 2);
                }
            }

            featureOne_zombie_variance = One_sum_diff_squared_zombie / num_zombies;
            featureTwo_zombie_variance = Two_sum_diff_squared_zombie / num_zombies;

            // Fill feautre 3 and 4 matrix
            featureThree = applyConditional(featureThree, prior_map.get(0.0), prior_map.get(1.0));
            featureFour = applyConditional(featureFour, prior_map.get(0.0), prior_map.get(1.0));
            // featureThree = smooth(featureThree, .35);
            // featureFour = smooth(featureFour, .35);

            return;
        }

        // TODO: complete me!
        public int predict(Matrix x) {
            Set<Integer> count = new HashSet<>();
            count.add(0);
            count.add(1);

            double best_log = -Double.MAX_VALUE;
            int best_class = -1;

            double stdDevOne_human = Math.sqrt(featureOne_human_variance);
            double stdDevTwo_human = Math.sqrt(featureTwo_human_variance);

            double stdDevOne_zombie = Math.sqrt(featureOne_zombie_variance);
            double stdDevTwo_zombie = Math.sqrt(featureTwo_zombie_variance);

            Double predict_one = x.get(0, 0);
            Double predict_two = x.get(0, 1);
            Double predict_three = x.get(0, 2);
            Double predict_four = x.get(0, 3);

            System.out.println("featureThree \n" + featureThree);
            System.out.println("featureFour \n" + featureFour);

            for (int i : count) {
                double log = Math.log(probability_map.get(new Double(i)));

                // Calculate likelihood using Gaussian Log
                if (i == 0) {
                    log += Math.log(gaussianLogLikelihood(featureOne_human_mean, stdDevOne_human, predict_one));
                    log += Math.log(gaussianLogLikelihood(featureTwo_human_mean, stdDevTwo_human, predict_two));

                    log += Math.log(featureThree.get(predict_three.intValue(), i + 1));
                    log += Math.log(featureFour.get(predict_four.intValue(), i + 1));
                } else {
                    log += Math.log(gaussianLogLikelihood(featureOne_zombie_mean, stdDevOne_zombie, predict_one));
                    log += Math.log(gaussianLogLikelihood(featureTwo_zombie_mean, stdDevTwo_zombie, predict_two));

                    log += Math.log(featureThree.get(predict_three.intValue(), i + 1));
                    log += Math.log(featureFour.get(predict_four.intValue(), i + 1));
                }

                // Set new best predictions
                if (log > best_log) {
                    best_log = log;
                    best_class = i;
                }
            }

            return best_class;
        }

        private Matrix applyConditional(Matrix feature, double human_prob, double zombie_prob) {
            Matrix new_matrix = feature.copy();

            for (int i = 0; i < new_matrix.getShape().getNumRows(); i++) {
                double current_human = new_matrix.get(i, 1);
                new_matrix.set(i, 1, current_human / human_prob);

                double current_zombie = new_matrix.get(i, 2);
                new_matrix.set(i, 2, current_zombie / zombie_prob);
            }

            return new_matrix;
        }

        // private Matrix smooth(Matrix feature, double tax) {
        // int cols = feature.getShape().getNumCols();
        // int rows = feature.getShape().getNumRows();

        // Matrix taxes_collected = Matrix.zeros(rows, cols);

        // for (int i = 1; i < cols; i++) {
        // for (int j = 1; i < rows; j++) {
        // double current_value = feature.get(j, i);
        // double taxes_to_collect = current_value * tax;
        // taxes_collected.set(j, i, taxes_to_collect);
        // feature.set(j, i, current_value - taxes_to_collect);
        // }
        // }

        // Matrix total_taxes_collected = null;

        // // Distribute taxes
        // for (int i = 0; i < cols; i++) {
        // double distribute = total_taxes_collected.get(0, i) / rows;
        // for (int j = 0; j < rows; j++) {
        // double temp = feature.get(j, i);
        // feature.set(j, i, temp + distribute);
        // }
        // }

        // return feature;
        // }

        private double gaussianLogLikelihood(double mean, double stdDev, double value) {
            double exp = -0.5 * Math.pow((value - mean) / stdDev, 2);
            double res = 1 / (stdDev * Math.sqrt(2 * Math.PI)) * Math.pow(Math.E, exp);

            return res;
        }
    }

    private NaiveBayes model;

    public NaiveBayesAgent(int playerNum, String[] args) {
        super(playerNum, args);
        this.model = new NaiveBayes();
    }

    public NaiveBayes getModel() {
        return this.model;
    }

    @Override
    public void train(Matrix X, Matrix y_gt) {
        System.out.println(X.getShape() + " " + y_gt.getShape());
        this.getModel().fit(X, y_gt);
    }

    @Override
    public int predict(Matrix featureRowVector) {
        return this.getModel().predict(featureRowVector);
    }

}
