package org.checkplagiarism.reporting.reportobject.mapper;

import org.checkplagiarism.CPComparison;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.Messages;
import org.checkplagiarism.Submission;
import org.checkplagiarism.options.SimilarityMetric;
import org.checkplagiarism.reporting.reportobject.model.Metric;
import org.checkplagiarism.reporting.reportobject.model.TopComparison;

import java.util.*;
import java.util.function.Function;

public class MetricMapper {
    private final Function<Submission, String> submissionToIdFunction;

    public MetricMapper(Function<Submission, String> submissionToIdFunction) {
        this.submissionToIdFunction = submissionToIdFunction;
    }

    public Metric getAverageMetric(CPResult result) {
        return new Metric(SimilarityMetric.AVG.name(), convertDistribution(result.getSimilarityDistribution()),
                getTopComparisons(getComparisons(result)), Messages.getString("SimilarityMetric.Avg.Description"));
    }

    public Metric getMaxMetric(CPResult result) {
        return new Metric(SimilarityMetric.MAX.name(), convertDistribution(result.getMaxSimilarityDistribution()),
                getMaxSimilarityTopComparisons(getComparisons(result)), Messages.getString("SimilarityMetric.Max.Description"));
    }

    private List<CPComparison> getComparisons(CPResult result) {
        int maxNumberOfComparisons = result.getOptions().maximumNumberOfComparisons();
        return result.getComparisons(maxNumberOfComparisons);
    }

    private List<Integer> convertDistribution(int[] array) {
        List<Integer> list = new ArrayList<>(Arrays.stream(array).boxed().toList());
        Collections.reverse(list);
        return list;
    }

    private List<TopComparison> getTopComparisons(List<CPComparison> comparisons, Function<CPComparison, Double> similarityExtractor) {
        return comparisons.stream().sorted(Comparator.comparing(similarityExtractor).reversed())
                .map(comparison -> new TopComparison(submissionToIdFunction.apply(comparison.firstSubmission()),
                        submissionToIdFunction.apply(comparison.secondSubmission()), similarityExtractor.apply(comparison)))
                .toList();
    }

    private List<TopComparison> getTopComparisons(List<CPComparison> comparisons) {
        return getTopComparisons(comparisons, CPComparison::similarity);
    }

    private List<TopComparison> getMaxSimilarityTopComparisons(List<CPComparison> comparisons) {
        return getTopComparisons(comparisons, CPComparison::maximalSimilarity);
    }
}
