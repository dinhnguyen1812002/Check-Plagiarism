package org.checkplagiarism;



import java.util.Collections;
import java.util.List;

public record CPComparison(Submission firstSubmission, Submission secondSubmission, List<Match> matches) {

    public CPComparison(Submission firstSubmission, Submission secondSubmission, List<Match> matches) {
        this.firstSubmission = firstSubmission;
        this.secondSubmission = secondSubmission;
        this.matches = Collections.unmodifiableList(matches);
    }

    public int getNumberOfMatchedTokens() {
        return matches.stream().mapToInt(Match::length).sum();
    }

    public final double maximalSimilarity() {
        return Math.max(similarityOfFirst(), similarityOfSecond());
    }

    public final double minimalSimilarity() {
        return Math.min(similarityOfFirst(), similarityOfSecond());
    }

    public final double similarity() {
        boolean subtractBaseCode = firstSubmission.hasBaseCodeMatches() && secondSubmission.hasBaseCodeMatches();
        int divisorA = firstSubmission.getSimilarityDivisor(subtractBaseCode);
        int divisorB = secondSubmission.getSimilarityDivisor(subtractBaseCode);
        return 2 * similarity(divisorA + divisorB);
    }

    public final double similarityOfFirst() {
        int divisor = firstSubmission.getSimilarityDivisor(true);
        return similarity(divisor);
    }


    public final double similarityOfSecond() {
        int divisor = secondSubmission.getSimilarityDivisor(true);
        return similarity(divisor);
    }

    @Override
    public String toString() {
        return firstSubmission.getName() + " <-> " + secondSubmission.getName();
    }

    private double similarity(int divisor) {
        return (divisor == 0 ? 0.0 : (getNumberOfMatchedTokens() / (double) divisor));
    }
}
