package org.checkplagiarism;

import org.checkplagiarism.clustering.ClusteringResult;
import org.checkplagiarism.options.CPOptions;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class CPResult {
    private List<CPComparison> comparisons;

    private final SubmissionSet submissions;

    private final CPOptions options ;

    private final long durationInMillis;

    private final int[] similarityDistribution;

    private List<ClusteringResult<Submission>> clusteringResult;
    private final int SIMILARITY_DISTRIBUTION_SIZE = 10;

    public CPResult(List<CPComparison> comparisons, SubmissionSet submissions, long durationInMillis, CPOptions options) {
        // sort by similarity (descending)
        this.comparisons = comparisons.stream().sorted((first, second) -> Double.compare(second.similarity(), first.similarity())).toList();
        this.submissions = submissions;
        this.durationInMillis = durationInMillis;
        this.options = options;
        similarityDistribution = calculateSimilarityDistribution(comparisons);
    }


    public void dropComparisons(int limit) {
        this.comparisons = this.getComparisons(limit);
    }

    public void setClusteringResult(List<ClusteringResult<Submission>> clustering) {
        this.clusteringResult = clustering;
    }

    public List<CPComparison> getAllComparisons() {
        return comparisons;
    }

    public List<CPComparison> getComparisons(int numberOfComparisons) {
        if (numberOfComparisons == CPOptions.SHOW_ALL_COMPARISONS) {
            return comparisons;
        }
        return comparisons.subList(0, Math.min(numberOfComparisons, comparisons.size()));
    }

    public long getDuration() {
        return durationInMillis;
    }


    public SubmissionSet getSubmissions() {
        return submissions;
    }

    public int getNumberOfSubmissions() {
        return submissions.numberOfSubmissions();
    }

    public CPOptions getOptions() {
        return options;
    }


    public int[] getSimilarityDistribution() {
        return similarityDistribution;
    }


    public int[] getMaxSimilarityDistribution() {
        return calculateDistributionFor(comparisons, (CPComparison::maximalSimilarity));
    }

    public List<ClusteringResult<Submission>> getClusteringResult() {
        return this.clusteringResult;
    }

    @Override
    public String toString() {
        return String.format("JPlagResult { comparisons: %d, duration: %d ms, language: %s, submissions: %d }", getAllComparisons().size(),
                getDuration(), getOptions().language().getName(), submissions.numberOfSubmissions());
    }


    private int[] calculateSimilarityDistribution(List<CPComparison> comparisons) {
        return calculateDistributionFor(comparisons, CPComparison::similarity);
    }

    private int[] calculateDistributionFor(List<CPComparison> comparisons, ToDoubleFunction<CPComparison> similarityExtractor) {
        int[] similarityDistribution = new int[SIMILARITY_DISTRIBUTION_SIZE];
        for (CPComparison comparison : comparisons) {
            double similarity = similarityExtractor.applyAsDouble(comparison);
            int index = (int) (similarity * SIMILARITY_DISTRIBUTION_SIZE);
            index = Math.min(index, SIMILARITY_DISTRIBUTION_SIZE - 1);
            similarityDistribution[index]++;
        }
        return similarityDistribution;
    }
}
