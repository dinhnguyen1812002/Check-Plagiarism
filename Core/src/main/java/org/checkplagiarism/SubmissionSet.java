package org.checkplagiarism;


import org.checkplagiarism.exceptions.BasecodeException;
import org.checkplagiarism.exceptions.ExitException;
import org.checkplagiarism.exceptions.SubmissionException;
import org.checkplagiarism.options.CPOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class SubmissionSet {
    private static final Logger logger = LoggerFactory.getLogger(SubmissionSet.class);

    private final List<Submission> allSubmissions;
    private final List<Submission> invalidSubmissions;
    private final List<Submission> submissions;

    private final Submission baseCodeSubmission;

    private final CPOptions options;
    private int errors = 0;
    private String currentSubmissionName;


    public SubmissionSet(List<Submission> submissions, Submission baseCode, CPOptions options) throws ExitException {
        this.allSubmissions = submissions;
        this.baseCodeSubmission = baseCode;
        this.options = options;
        parseAllSubmissions();
        this.submissions = filterValidSubmissions();
        invalidSubmissions = filterInvalidSubmissions();
    }

    public boolean hasBaseCode() {
        return baseCodeSubmission != null;
    }


    public Submission getBaseCode() {
        if (baseCodeSubmission == null) {
            throw new AssertionError("Querying a non-existing basecode submission.");
        }
        return baseCodeSubmission;
    }


    public int numberOfSubmissions() {
        return submissions.size();
    }


    public List<Submission> getSubmissions() {
        return submissions;
    }

    public List<Submission> getInvalidSubmissions() {
        return invalidSubmissions;
    }

    private List<Submission> filterValidSubmissions() {
        return allSubmissions.stream().filter(submission -> !submission.hasErrors()).collect(Collectors.toCollection(ArrayList::new));
    }

    private List<Submission> filterInvalidSubmissions() {
        return allSubmissions.stream().filter(Submission::hasErrors).toList();
    }

    private void parseAllSubmissions() throws ExitException {
        try {
            parseSubmissions(allSubmissions);
            if (baseCodeSubmission != null) {
                parseBaseCodeSubmission(baseCodeSubmission);
            }
        } catch (OutOfMemoryError exception) {
            throw new SubmissionException("Out of memory during parsing of submission \"" + currentSubmissionName + "\"", exception);
        }
    }

    /**
     * Parse the given base code submission.
     */
    private void parseBaseCodeSubmission(Submission baseCode) throws BasecodeException {
        long startTime = System.currentTimeMillis();
        logger.trace("----- Parsing basecode submission: " + baseCode.getName());
        if (!baseCode.parse(options.debugParser())) {
            throw new BasecodeException("Could not successfully parse basecode submission!");
        } else if (baseCode.getNumberOfTokens() < options.minimumTokenMatch()) {
            throw new BasecodeException("Basecode submission contains fewer tokens than minimum match length allows!");
        }
        logger.trace("Basecode submission parsed!");
        long duration = System.currentTimeMillis() - startTime;
        logger.trace("Time for parsing Basecode: " + TimeUtil.formatDuration(duration));

    }

    /**
     * Parse all given submissions.
     */
    private void parseSubmissions(List<Submission> submissions) {
        if (submissions.isEmpty()) {
            logger.warn("No submissions to parse!");
            return;
        }

        long startTime = System.currentTimeMillis();

        int tooShort = 0;
        for (Submission submission : submissions) {
            boolean ok;

            logger.trace("------ Parsing submission: " + submission.getName());
            currentSubmissionName = submission.getName();

            if (!(ok = submission.parse(options.debugParser()))) {
                errors++;
            }

            if (submission.getTokenList() != null && submission.getNumberOfTokens() < options.minimumTokenMatch()) {
                logger.error("Submission {} contains fewer tokens than minimum match length allows!", currentSubmissionName);
                submission.setTokenList(null);
                tooShort++;
                ok = false;
                submission.markAsErroneous();
            }

            if (ok) {
                logger.trace("OK");
            } else {
                logger.error("ERROR -> Submission {} removed", currentSubmissionName);
            }
        }

        int validSubmissions = submissions.size() - errors - tooShort;
        logger.trace(validSubmissions + " submissions parsed successfully!");
        logger.trace(errors + " parser error" + (errors != 1 ? "s!" : "!"));
        logger.trace(tooShort + " too short submission" + (tooShort != 1 ? "s!" : "!"));
        printDetails(submissions, startTime, tooShort);
    }

    private void printDetails(List<Submission> submissions, long startTime, int tooShort) {
        if (tooShort == 1) {
            logger.trace(tooShort + " submission is not valid because it contains fewer tokens than minimum match length allows.");
        } else if (tooShort > 1) {
            logger.trace(tooShort + " submissions are not valid because they contain fewer tokens than minimum match length allows.");
        }

        long duration = System.currentTimeMillis() - startTime;
        String timePerSubmission = submissions.isEmpty() ? "n/a" : Long.toString(duration / submissions.size());
        logger.trace("Total time for parsing: " + TimeUtil.formatDuration(duration));
        logger.trace("Time per parsed submission: " + timePerSubmission + " msec");
    }

}

