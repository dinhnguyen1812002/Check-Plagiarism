package org.checkplagiarism.clustering;

import org.checkplagiarism.clustering.algorithm.GenericClusteringAlgorithm;
import org.checkplagiarism.clustering.algorithm.AgglomerativeClustering;
import org.checkplagiarism.clustering.algorithm.SpectralClustering;
import java.util.Objects;

public enum ClusteringAlgorithm {
    AGGLOMERATIVE(AgglomerativeClustering::new),

    SPECTRAL(SpectralClustering::new);

    private final ClusteringAlgorithmSupplier constructor;

    ClusteringAlgorithm(ClusteringAlgorithmSupplier constructor) {
        this.constructor = constructor;
    }

    public GenericClusteringAlgorithm create(ClusteringOptions options) {
        Objects.requireNonNull(options);
        return this.constructor.create(options);
    }

    @FunctionalInterface
    public interface ClusteringAlgorithmSupplier {
        GenericClusteringAlgorithm create(ClusteringOptions options);
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase();
    }
}
