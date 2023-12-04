package org.checkplagiarism.strategy;

import org.checkplagiarism.CPComparison;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.GreedyStringTiling;
import org.checkplagiarism.SubmissionSet;
import org.checkplagiarism.options.CPOptions;
import java.util.Optional;
import java.util.List;

public class ParallelComparisonStrategy extends AbstractComparisonStrategy {

    public ParallelComparisonStrategy(CPOptions options, GreedyStringTiling greedyStringTiling) {
        super(options, greedyStringTiling);
    }

    @Override
    public CPResult compareSubmissions(SubmissionSet submissionSet) {
        long timeBeforeStartInMillis = System.currentTimeMillis();
        boolean withBaseCode = submissionSet.hasBaseCode();
        if (withBaseCode) {
            compareSubmissionsToBaseCode(submissionSet);
        }

        List<SubmissionTuple> tuples = buildComparisonTuples(submissionSet.getSubmissions());
        List<CPComparison> comparisons = tuples.stream().parallel().map(tuple -> compareSubmissions(tuple.left(), tuple.right()))
                .flatMap(Optional::stream).toList();

        long durationInMillis = System.currentTimeMillis() - timeBeforeStartInMillis;
        return new CPResult(comparisons, submissionSet, durationInMillis, options);
    }
}

