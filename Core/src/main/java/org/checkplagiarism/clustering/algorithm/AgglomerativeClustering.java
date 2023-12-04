package org.checkplagiarism.clustering.algorithm;

import org.apache.commons.math3.linear.RealMatrix;
import org.checkplagiarism.clustering.ClusteringOptions;

import java.util.*;
import java.util.stream.Collectors;

public class AgglomerativeClustering implements GenericClusteringAlgorithm {

    private final ClusteringOptions options;

    public AgglomerativeClustering(ClusteringOptions options) {
        this.options = options;
    }

    @Override
    public Collection<Collection<Integer>> cluster(RealMatrix similarityMatrix) {
        int size = similarityMatrix.getRowDimension();
        Set<Cluster> clusters = new HashSet<>(size);
        PriorityQueue<ClusterConnection> similarities;
        List<Cluster> initialClusters = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            List<Integer> members = new ArrayList<>();
            members.add(i);
            Cluster cluster = new Cluster(members);
            initialClusters.add(cluster);
            clusters.add(cluster);
        }

        List<ClusterConnection> initialSimilarities = new ArrayList<>(size * (size - 1) / 2);

        for (int leftIndex = 0; leftIndex < initialClusters.size(); leftIndex++) {
            Cluster leftCluster = initialClusters.get(leftIndex);
            for (int rightIndex = leftIndex + 1; rightIndex < initialClusters.size(); rightIndex++) {
                Cluster rightCluster = initialClusters.get(rightIndex);
                initialSimilarities.add(new ClusterConnection(leftCluster, rightCluster, similarityMatrix.getEntry(leftIndex, rightIndex)));
            }
        }

        similarities = new PriorityQueue<>(initialSimilarities);

        while (clusters.size() > 1) {
            ClusterConnection nearest = similarities.poll();

            if (!(clusters.contains(nearest.left) && clusters.contains(nearest.right))) {
                // One cluster already part of another cluster
                continue;
            }
            if (nearest.similarity < options.agglomerativeThreshold()) {
                break;
            }
            clusters.remove(nearest.left);
            clusters.remove(nearest.right);
            nearest.left.submissions().addAll(nearest.right.submissions());
            Cluster combined = new Cluster(nearest.left.submissions());
            for (Cluster otherCluster : clusters) {
                double similarity = options.agglomerativeInterClusterSimilarity().clusterSimilarity(combined.submissions, otherCluster.submissions,
                        similarityMatrix);
                similarities.add(new ClusterConnection(combined, otherCluster, similarity));
            }
            clusters.add(combined);
        }

        return clusters.stream().map(Cluster::submissions).collect(Collectors.toList());
    }

    private record ClusterConnection(Cluster left, Cluster right, double similarity) implements Comparable<ClusterConnection> {
        @Override
        public int compareTo(ClusterConnection other) {
            return (int) Math.signum(other.similarity - similarity);
        }

    }

    private record Cluster(List<Integer> submissions) {
    }

}

