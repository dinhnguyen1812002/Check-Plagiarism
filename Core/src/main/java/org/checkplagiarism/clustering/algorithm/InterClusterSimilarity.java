package org.checkplagiarism.clustering.algorithm;


import org.apache.commons.math3.linear.RealMatrix;

import java.util.List;
import java.util.function.BinaryOperator;

public enum InterClusterSimilarity {
    MIN(Double.MAX_VALUE, Math::min),
    MAX(Double.MIN_VALUE, Math::max),
    AVERAGE(0, Double::sum);

    private final double neutralElement;
    private final BinaryOperator<Double> accumulator;

    InterClusterSimilarity(double neutralElement, BinaryOperator<Double> accumulator) {
        this.neutralElement = neutralElement;
        this.accumulator = accumulator;
    }


    public double clusterSimilarity(List<Integer> leftCluster, List<Integer> rightCluster, RealMatrix similarityMatrix) {
        double similarity = this.neutralElement;

        for (int leftSubmission : leftCluster) {
            for (int rightSubmission : rightCluster) {
                double submissionSimilarity = similarityMatrix.getEntry(leftSubmission, rightSubmission);
                similarity = this.accumulator.apply(similarity, submissionSimilarity);
            }
        }

        if (this == InterClusterSimilarity.AVERAGE) {
            similarity /= leftCluster.size() * rightCluster.size();
        }
        return similarity;
    }
}
