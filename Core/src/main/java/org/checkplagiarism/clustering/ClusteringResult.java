package org.checkplagiarism.clustering;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.DoubleStream;


public class ClusteringResult<T> {

    private final List<Cluster<T>> clusters;
    private final double communityStrength;

    public ClusteringResult(Collection<Cluster<T>> clusters, double communityStrength) {
        this.clusters = List.copyOf(clusters);
        this.communityStrength = communityStrength;
        for (Cluster<T> cluster : clusters) {
            cluster.setClusteringResult(this);
        }
    }

    public Collection<Cluster<T>> getClusters() {
        return Collections.unmodifiableList(clusters);
    }


    public double getCommunityStrength() {
        return communityStrength;
    }


    public double getWorth(BiFunction<T, T, Double> similarity) {
        return getClusters().stream().mapToDouble(c -> c.getWorth(similarity)).map(worth -> Double.isFinite(worth) ? worth : 0).average()
                .getAsDouble();
    }


    public static ClusteringResult<Integer> fromIntegerCollections(List<Collection<Integer>> clustering, RealMatrix similarity) {
        int numberOfSubmissions = similarity.getRowDimension();
        Map<Integer, Integer> clusterIndicesOfSubmissionIndices = new HashMap<>();
        int clusterIdx = 0;
        for (Collection<Integer> cluster : clustering) {
            for (Integer submissionIdx : cluster) {
                clusterIndicesOfSubmissionIndices.put(submissionIdx, clusterIdx);
            }
            clusterIdx++;
        }
        List<Cluster<Integer>> clusters = new ArrayList<>(clustering.size());
        double communityStrength = 0;
        if (!clustering.isEmpty()) {
            RealMatrix percentagesOfSimilaritySums = new Array2DRowRealMatrix(clustering.size(), clustering.size());
            percentagesOfSimilaritySums = percentagesOfSimilaritySums.scalarMultiply(0);
            for (int i = 0; i < numberOfSubmissions; i++) {
                if (!clusterIndicesOfSubmissionIndices.containsKey(i))
                    continue;
                int clusterA = clusterIndicesOfSubmissionIndices.get(i);
                for (int j = i + 1; j < numberOfSubmissions; j++) {
                    if (!clusterIndicesOfSubmissionIndices.containsKey(j))
                        continue;
                    int clusterB = clusterIndicesOfSubmissionIndices.get(j);
                    percentagesOfSimilaritySums.addToEntry(clusterA, clusterB, similarity.getEntry(i, j));
                    percentagesOfSimilaritySums.addToEntry(clusterB, clusterA, similarity.getEntry(i, j));
                }
            }
            percentagesOfSimilaritySums = percentagesOfSimilaritySums
                    .scalarMultiply(1 / Arrays.stream(similarity.getData()).flatMapToDouble(DoubleStream::of).sum());
            for (int i = 0; i < clustering.size(); i++) {
                double outWeightSum = percentagesOfSimilaritySums.getRowVector(i).getL1Norm();
                double clusterCommunityStrength = percentagesOfSimilaritySums.getEntry(i, i) - outWeightSum * outWeightSum;
                double averageSimilarity = calculateAverageSimilarityFor(clustering.get(i), similarity);
                clusters.add(new Cluster<>(clustering.get(i), clusterCommunityStrength, averageSimilarity));
                communityStrength += clusterCommunityStrength;
            }
        }
        return new ClusteringResult<>(clusters, communityStrength);
    }

    private static double calculateAverageSimilarityFor(Collection<Integer> cluster, RealMatrix similarityMatrix) {
        double sumOfSimilarities = 0;
        List<Integer> indices = List.copyOf(cluster);
        for (int i = 1; i < cluster.size(); i++) {
            int indexOfSubmission1 = indices.get(i);
            for (int j = 0; j < i; j++) {
                int indexOfSubmission2 = indices.get(j);
                sumOfSimilarities += similarityMatrix.getEntry(indexOfSubmission1, indexOfSubmission2);
            }
        }
        int nMinusOne = cluster.size() - 1;
        double numberOfComparisons = (nMinusOne * (nMinusOne + 1)) / 2.0;
        return sumOfSimilarities / numberOfComparisons;
    }

}

