package org.checkplagiarism.strategy;

import org.checkplagiarism.CPComparison;
import org.checkplagiarism.GreedyStringTiling;
import org.checkplagiarism.Submission;
import org.checkplagiarism.SubmissionSet;
import org.checkplagiarism.options.CPOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class AbstractComparisonStrategy  implements ComparisonStrategy{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GreedyStringTiling greedyStringTiling;
    protected final CPOptions options;

    public AbstractComparisonStrategy(CPOptions options, GreedyStringTiling greedyStringTiling) {
        this.greedyStringTiling = greedyStringTiling;
        this.options = options;
        logger.info("Start comparing...");
    }
    protected void compareSubmissionsToBaseCode(SubmissionSet submissionSet) {
        Submission baseCodeSubmission = submissionSet.getBaseCode();
        for (Submission currentSubmission : submissionSet.getSubmissions()) {
            CPComparison baseCodeComparison = greedyStringTiling.generateBaseCodeMarking(currentSubmission, baseCodeSubmission);
            currentSubmission.setBaseCodeComparison(baseCodeComparison);
        }
    }

    protected Optional<CPComparison> compareSubmissions(Submission first, Submission second) {
        CPComparison comparison = greedyStringTiling.compare(first, second);
        logger.info("Comparing {}-{}: {}", first.getName(), second.getName(), comparison.similarity());

        if (options.similarityMetric().isAboveThreshold(comparison, options.similarityThreshold())) {
            return Optional.of(comparison);
        }
        return Optional.empty();
    }
    protected static List<SubmissionTuple> buildComparisonTuples(List<Submission> submissions) {
        List<SubmissionTuple> tuples = new ArrayList<>();
        List<Submission> validSubmissions = submissions.stream().filter(s -> s.getTokenList() != null).toList();

        for (int i = 0; i < (validSubmissions.size() - 1); i++) {
            Submission first = validSubmissions.get(i);
            for (int j = (i + 1); j < validSubmissions.size(); j++) {
                Submission second = validSubmissions.get(j);
                if (first.isNew() || second.isNew()) {
                    tuples.add(new SubmissionTuple(first, second));
                }
            }
        }
        return tuples;
    }

}
