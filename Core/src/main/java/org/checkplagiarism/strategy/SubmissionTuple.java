package org.checkplagiarism.strategy;

import org.checkplagiarism.Submission;

public record SubmissionTuple(Submission left, Submission right) {

    public SubmissionTuple {
        if (left == null || right == null) {
            throw new IllegalArgumentException("Submissions cannot be null");
        }
    }

    @Override
    public String toString() {
        return "(" + left + " | " + right + ")";
    }
}
