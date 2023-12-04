package org.checkplagiarism.reporting.jsonfactory;


import org.checkplagiarism.CPComparison;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.Submission;
import org.checkplagiarism.reporting.reportobject.model.ComparisonReport;
import org.checkplagiarism.reporting.reportobject.model.Match;
import org.language_api.Token;

import java.io.File;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


public class ComparisonReportWriter {

    private final FileWriter fileWriter;
    private final Function<Submission, String> submissionToIdFunction;
    private final Map<String, Map<String, String>> submissionIdToComparisonFileName = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> fileNameCollisions = new ConcurrentHashMap<>();

    public ComparisonReportWriter(Function<Submission, String> submissionToIdFunction, FileWriter fileWriter) {
        this.submissionToIdFunction = submissionToIdFunction;
        this.fileWriter = fileWriter;
    }


    public Map<String, Map<String, String>> writeComparisonReports(CPResult cpResult, String path) {
        int numberOfComparisons = cpResult.getOptions().maximumNumberOfComparisons();
        List<CPComparison> comparisons = cpResult.getComparisons(numberOfComparisons);
        writeComparisons(path, comparisons);
        return submissionIdToComparisonFileName;
    }

    private void writeComparisons(String path, List<CPComparison> comparisons) {
        comparisons.parallelStream().forEach(comparison -> {
            String firstSubmissionId = submissionToIdFunction.apply(comparison.firstSubmission());
            String secondSubmissionId = submissionToIdFunction.apply(comparison.secondSubmission());
            String fileName = generateComparisonName(firstSubmissionId, secondSubmissionId);
            addToLookUp(firstSubmissionId, secondSubmissionId, fileName);
            var comparisonReport = new ComparisonReport(firstSubmissionId, secondSubmissionId, comparison.similarity(),
                    convertMatchesToReportMatches(comparison));
            fileWriter.saveAsJSON(comparisonReport, path, fileName);
        });
    }

    private void addToLookUp(String firstSubmissionId, String secondSubmissionId, String fileName) {
        writeToMap(secondSubmissionId, firstSubmissionId, fileName);
        writeToMap(firstSubmissionId, secondSubmissionId, fileName);
    }

    private void writeToMap(String id1, String id2, String comparisonFileName) {
        submissionIdToComparisonFileName.putIfAbsent(id1, new ConcurrentHashMap<>());
        submissionIdToComparisonFileName.get(id1).put(id2, comparisonFileName);
    }

    private String generateComparisonName(String firstSubmissionId, String secondSubmissionId) {
        String name = concatenate(firstSubmissionId, secondSubmissionId);
        AtomicInteger collisionCounter = fileNameCollisions.putIfAbsent(name, new AtomicInteger(1));
        if (collisionCounter != null) {
            name = concatenate(firstSubmissionId, secondSubmissionId, collisionCounter.incrementAndGet());
        }
        return name;
    }

    private String concatenate(String firstSubmissionId, String secondSubmissionId, long index) {
        return firstSubmissionId.concat("-").concat(secondSubmissionId).concat(index > 0 ? "-" + index : "").concat(".json");
    }

    private String concatenate(String firstSubmissionId, String secondSubmissionId) {
        return concatenate(firstSubmissionId, secondSubmissionId, 0);
    }

    private List<Match> convertMatchesToReportMatches(CPComparison comparison) {
        return comparison.matches().stream().map(match -> convertMatchToReportMatch(comparison, match)).toList();
    }

    private Match convertMatchToReportMatch(CPComparison comparison, org.checkplagiarism.Match match) {
        List<Token> tokensFirst = comparison.firstSubmission().getTokenList().subList(match.startOfFirst(), match.endOfFirst() + 1);
        List<Token> tokensSecond = comparison.secondSubmission().getTokenList().subList(match.startOfSecond(), match.endOfSecond() + 1);

        Comparator<? super Token> lineComparator = (first, second) -> first.getLine() - second.getLine();

        Token startOfFirst = tokensFirst.stream().min(lineComparator).orElseThrow();
        Token endOfFirst = tokensFirst.stream().max(lineComparator).orElseThrow();
        Token startOfSecond = tokensSecond.stream().min(lineComparator).orElseThrow();
        Token endOfSecond = tokensSecond.stream().max(lineComparator).orElseThrow();

        return new Match(relativizedFilePath(startOfFirst.getFile(), comparison.firstSubmission()),
                relativizedFilePath(startOfSecond.getFile(), comparison.secondSubmission()), startOfFirst.getLine(), endOfFirst.getLine(),
                startOfSecond.getLine(), endOfSecond.getLine(), match.length());
    }

    private String relativizedFilePath(File file, Submission submission) {
        if (file.toPath().equals(submission.getRoot().toPath())) {
            return Path.of(submissionToIdFunction.apply(submission), submissionToIdFunction.apply(submission)).toString();
        }
        return Path.of(submissionToIdFunction.apply(submission), submission.getRoot().toPath().relativize(file.toPath()).toString()).toString();
    }

}

