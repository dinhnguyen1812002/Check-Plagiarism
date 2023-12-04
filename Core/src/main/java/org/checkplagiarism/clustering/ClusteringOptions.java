package org.checkplagiarism.clustering;


import org.checkplagiarism.clustering.algorithm.InterClusterSimilarity;
import org.checkplagiarism.options.SimilarityMetric;

import java.util.Objects;




public record ClusteringOptions(SimilarityMetric similarityMetric, double spectralKernelBandwidth, double spectralGaussianProcessVariance,
                                int spectralMinRuns, int spectralMaxRuns, int spectralMaxKMeansIterationPerRun, double agglomerativeThreshold, Preprocessing preprocessor,
                                boolean enabled, ClusteringAlgorithm algorithm, InterClusterSimilarity agglomerativeInterClusterSimilarity, double preprocessorThreshold,
                                double preprocessorPercentile) {

    public ClusteringOptions(SimilarityMetric similarityMetric, double spectralKernelBandwidth, double spectralGaussianProcessVariance,
                             int spectralMinRuns, int spectralMaxRuns, int spectralMaxKMeansIterationPerRun, double agglomerativeThreshold, Preprocessing preprocessor,
                             boolean enabled, ClusteringAlgorithm algorithm, InterClusterSimilarity agglomerativeInterClusterSimilarity, double preprocessorThreshold,
                             double preprocessorPercentile) {
        this.similarityMetric = Objects.requireNonNull(similarityMetric);
        this.spectralKernelBandwidth = spectralKernelBandwidth;
        this.spectralGaussianProcessVariance = spectralGaussianProcessVariance;
        this.spectralMinRuns = spectralMinRuns;
        this.spectralMaxRuns = spectralMaxRuns;
        this.spectralMaxKMeansIterationPerRun = spectralMaxKMeansIterationPerRun;
        this.agglomerativeThreshold = agglomerativeThreshold;
        this.preprocessor = Objects.requireNonNull(preprocessor);
        this.enabled = enabled;
        this.algorithm = Objects.requireNonNull(algorithm);
        this.agglomerativeInterClusterSimilarity = Objects.requireNonNull(agglomerativeInterClusterSimilarity);
        this.preprocessorThreshold = preprocessorThreshold;
        this.preprocessorPercentile = preprocessorPercentile;
    }

    public ClusteringOptions() {
        this(SimilarityMetric.AVG, 20.f, 0.05 * 0.05, 5, 50, 200, 0.2, Preprocessing.CUMULATIVE_DISTRIBUTION_FUNCTION, true,
                ClusteringAlgorithm.SPECTRAL, InterClusterSimilarity.AVERAGE, 0.2, 0.5);
    }

    public ClusteringOptions withSimilarityMetric(SimilarityMetric similarityMetric) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withSpectralKernelBandwidth(double spectralKernelBandwidth) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withSpectralGaussianProcessVariance(double spectralGaussianProcessVariance) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withSpectralMinRuns(int spectralMinRuns) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withSpectralMaxRuns(int spectralMaxRuns) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withSpectralMaxKMeansIterationPerRun(int spectralMaxKMeansIterationPerRun) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withAgglomerativeThreshold(double agglomerativeThreshold) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withPreprocessor(Preprocessing preprocessor) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withEnabled(boolean enabled) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withAlgorithm(ClusteringAlgorithm algorithm) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withAgglomerativeInterClusterSimilarity(InterClusterSimilarity agglomerativeInterClusterSimilarity) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withPreprocessorThreshold(double preprocessorThreshold) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }

    public ClusteringOptions withPreprocessorPercentile(double preprocessorPercentile) {
        return new ClusteringOptions(similarityMetric, spectralKernelBandwidth, spectralGaussianProcessVariance, spectralMinRuns, spectralMaxRuns,
                spectralMaxKMeansIterationPerRun, agglomerativeThreshold, preprocessor, enabled, algorithm, agglomerativeInterClusterSimilarity,
                preprocessorThreshold, preprocessorPercentile);
    }
}

