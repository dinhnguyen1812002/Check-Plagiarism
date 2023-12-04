package org.checkplagiarism.clustering;

import org.checkplagiarism.clustering.preprocessors.CumulativeDistributionFunctionPreprocessor;
import org.checkplagiarism.clustering.preprocessors.PercentileThresholdProcessor;
import org.checkplagiarism.clustering.preprocessors.ThresholdPreprocessor;

import java.util.Optional;
import java.util.function.Function;



public enum Preprocessing {
    NONE(options -> null),

    CUMULATIVE_DISTRIBUTION_FUNCTION(options -> new CumulativeDistributionFunctionPreprocessor()),
    /** {@link ThresholdPreprocessor} */
    THRESHOLD(options -> new ThresholdPreprocessor(options.preprocessorThreshold())),
    /** {@link PercentileThresholdProcessor} */
    PERCENTILE(options -> new PercentileThresholdProcessor(options.preprocessorPercentile()));

    private final Function<ClusteringOptions, ClusteringPreprocessor> constructor;

    Preprocessing(Function<ClusteringOptions, ClusteringPreprocessor> constructor) {
        this.constructor = constructor;
    }

    public Optional<ClusteringPreprocessor> constructPreprocessor(ClusteringOptions options) {
        return Optional.ofNullable(constructor.apply(options));
    }

    @Override
    public String toString() {
        return super.toString().toLowerCase().replace('_', ' ');
    }
}

