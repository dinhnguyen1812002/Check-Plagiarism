package org.checkplagiarism.options;


import org.checkplagiarism.CPComparison;

import java.util.function.ToDoubleFunction;

public enum SimilarityMetric implements ToDoubleFunction<CPComparison> {
    AVG("average similarity", CPComparison::similarity),
    MIN("minimum similarity", CPComparison::minimalSimilarity),
    MAX("maximal similarity", CPComparison::maximalSimilarity),
    INTERSECTION("matched tokens", it -> (double) it.getNumberOfMatchedTokens());

    private final ToDoubleFunction<CPComparison> similarityFunction;
    private final String description;

    SimilarityMetric(String description, ToDoubleFunction<CPComparison> similarityFunction) {
        this.description = description;
        this.similarityFunction = similarityFunction;
    }

    public boolean isAboveThreshold(CPComparison comparison, double similarityThreshold) {
        return similarityFunction.applyAsDouble(comparison) >= similarityThreshold;
    }

    @Override
    public double applyAsDouble(CPComparison comparison) {
        return similarityFunction.applyAsDouble(comparison);
    }

    @Override
    public String toString() {
        return description;
    }
}

