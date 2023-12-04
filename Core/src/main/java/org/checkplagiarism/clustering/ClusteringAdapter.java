package org.checkplagiarism.clustering;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.checkplagiarism.CPComparison;
import org.checkplagiarism.Submission;
import org.checkplagiarism.clustering.algorithm.GenericClusteringAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.ToDoubleFunction;

public class ClusteringAdapter {

    private final RealMatrix similarityMatrix;
    private final IntegerMapping<Submission> mapping;

    public ClusteringAdapter(Collection<CPComparison> comparisons, ToDoubleFunction<CPComparison> metric) {
        mapping = new IntegerMapping<>(comparisons.size());
        for (CPComparison comparison : comparisons) {
            mapping.map(comparison.firstSubmission());
            mapping.map(comparison.secondSubmission());
        }
        int size = mapping.size();

        similarityMatrix = new Array2DRowRealMatrix(size, size);
        for (CPComparison comparison : comparisons) {
            int firstIndex = mapping.map(comparison.firstSubmission());
            int secondIndex = mapping.map(comparison.secondSubmission());
            double similarity = metric.applyAsDouble(comparison);
            similarityMatrix.setEntry(firstIndex, secondIndex, similarity);
            similarityMatrix.setEntry(secondIndex, firstIndex, similarity);
        }
    }
    public ClusteringResult<Submission> doClustering(GenericClusteringAlgorithm algorithm) {
        Collection<Collection<Integer>> intResult = algorithm.cluster(similarityMatrix);
        ClusteringResult<Integer> modularityClusterResult = ClusteringResult.fromIntegerCollections(new ArrayList<>(intResult), similarityMatrix);
        List<Cluster<Submission>> mappedClusters = modularityClusterResult.getClusters().stream()
                .map(unmappedCluster -> new Cluster<>(unmappedCluster.getMembers().stream().map(mapping::unmap).toList(),
                        unmappedCluster.getCommunityStrength(), unmappedCluster.getAverageSimilarity()))
                .toList();
        return new ClusteringResult<>(mappedClusters, modularityClusterResult.getCommunityStrength());
    }

}
