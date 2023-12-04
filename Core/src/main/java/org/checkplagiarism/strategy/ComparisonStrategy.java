package org.checkplagiarism.strategy;

import org.checkplagiarism.CPResult;
import org.checkplagiarism.SubmissionSet;

public interface ComparisonStrategy {
    CPResult compareSubmissions(SubmissionSet submissionSet);
}
