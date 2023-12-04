package org.checkplagiarism;


import org.checkplagiarism.options.CPOptions;
import org.language_api.SharedTokenType;
import org.language_api.Token;
import org.language_api.TokenType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class GreedyStringTiling {

    private final int minimumMatchLength;
    private ConcurrentMap<TokenType, Integer> tokenTypeValues;
    private final Map<Submission, Set<Token>> baseCodeMarkings = new IdentityHashMap<>();

    private final Map<Submission, int[]> cachedTokenValueLists = new IdentityHashMap<>();
    private final Map<Submission, SubsequenceHashLookupTable> cachedHashLookupTables = new IdentityHashMap<>();

    public GreedyStringTiling(CPOptions options) {
        this.minimumMatchLength = options.minimumTokenMatch();
        this.tokenTypeValues = new ConcurrentHashMap<>();
        this.tokenTypeValues.put(SharedTokenType.FILE_END, 0);
    }

    public final CPComparison generateBaseCodeMarking(Submission submission, Submission baseCodeSubmission) {
        CPComparison comparison = compare(submission, baseCodeSubmission);

        List<Token> submissionTokenList = submission.getTokenList();
        Set<Token> baseCodeMarking = new HashSet<>();
        for (Match match : comparison.matches()) {
            int startIndex = comparison.firstSubmission() == submission ? match.startOfFirst() : match.startOfSecond();
            baseCodeMarking.addAll(submissionTokenList.subList(startIndex, startIndex + match.length()));
        }
        baseCodeMarkings.put(submission, baseCodeMarking);
        cachedHashLookupTables.remove(submission);

        return comparison;
    }
    public final CPComparison compare(Submission firstSubmission, Submission secondSubmission) {
        Submission smallerSubmission;
        Submission largerSubmission;
        if (firstSubmission.getTokenList().size() > secondSubmission.getTokenList().size()) {
            smallerSubmission = secondSubmission;
            largerSubmission = firstSubmission;
        } else {
            smallerSubmission = firstSubmission;
            largerSubmission = secondSubmission;
        }
        return compareInternal(smallerSubmission, largerSubmission);
    }

    private CPComparison compareInternal(Submission leftSubmission, Submission rightSubmission) {
        List<Token> leftTokens = leftSubmission.getTokenList();
        List<Token> rightTokens = rightSubmission.getTokenList();

        int[] leftValues = tokenValueListFromSubmission(leftSubmission);
        int[] rightValues = tokenValueListFromSubmission(rightSubmission);

        // comparison uses <= because it is assumed that the last token is a pivot (FILE_END)
        if (leftTokens.size() <= minimumMatchLength || rightTokens.size() <= minimumMatchLength) {
            return new CPComparison(leftSubmission, rightSubmission, List.of());
        }

        Set<Integer> leftMarkedIndexes = initiallyMarkedTokenIndexes(leftSubmission);
        Set<Integer> rightMarkedIndexes = initiallyMarkedTokenIndexes(rightSubmission);

        SubsequenceHashLookupTable leftLookupTable = subsequenceHashLookupTableForSubmission(leftSubmission, leftMarkedIndexes);
        SubsequenceHashLookupTable rightLookupTable = subsequenceHashLookupTableForSubmission(rightSubmission, rightMarkedIndexes);

        int maximumMatchLength;
        List<Match> globalMatches = new ArrayList<>();
        do {
            maximumMatchLength = minimumMatchLength;
            List<Match> iterationMatches = new ArrayList<>();
            for (int leftStartIndex = 0; leftStartIndex < leftValues.length - maximumMatchLength; leftStartIndex++) {
                int leftSubsequenceHash = leftLookupTable.subsequenceHashForStartIndex(leftStartIndex);
                if (leftMarkedIndexes.contains(leftStartIndex) || leftSubsequenceHash == SubsequenceHashLookupTable.NO_HASH) {
                    continue;
                }
                List<Integer> possiblyMatchingRightStartIndexes = rightLookupTable
                        .startIndexesOfPossiblyMatchingSubsequencesForSubsequenceHash(leftSubsequenceHash);
                for (Integer rightStartIndex : possiblyMatchingRightStartIndexes) {
                    // comparison uses >= because it is assumed that the last token is a pivot (FILE_END)
                    if (rightMarkedIndexes.contains(rightStartIndex) || maximumMatchLength >= rightValues.length - rightStartIndex) {
                        continue;
                    }

                    int subsequenceMatchLength = maximalMatchingSubsequenceLengthNotMarked(leftValues, leftStartIndex, leftMarkedIndexes, rightValues,
                            rightStartIndex, rightMarkedIndexes, maximumMatchLength);
                    if (subsequenceMatchLength >= maximumMatchLength) {
                        if (subsequenceMatchLength > maximumMatchLength) {
                            iterationMatches.clear();
                            maximumMatchLength = subsequenceMatchLength;
                        }
                        Match match = new Match(leftStartIndex, rightStartIndex, subsequenceMatchLength);
                        addMatchIfNotOverlapping(iterationMatches, match);
                    }
                }
            }
            for (Match match : iterationMatches) {
                addMatchIfNotOverlapping(globalMatches, match);
                int leftStartIndex = match.startOfFirst();
                int rightStartIndex = match.startOfSecond();
                for (int offset = 0; offset < match.length(); offset++) {
                    leftMarkedIndexes.add(leftStartIndex + offset);
                    rightMarkedIndexes.add(rightStartIndex + offset);
                }
            }
        } while (maximumMatchLength != minimumMatchLength);
        return new CPComparison(leftSubmission, rightSubmission, globalMatches);
    }


    private int maximalMatchingSubsequenceLengthNotMarked(int[] leftValues, int leftStartIndex, Set<Integer> leftMarkedIndexes, int[] rightValues,
                                                          int rightStartIndex, Set<Integer> rightMarkedIndexes, int minimumSequenceLength) {
        for (int offset = minimumSequenceLength - 1; offset >= 0; offset--) {
            int leftIndex = leftStartIndex + offset;
            int rightIndex = rightStartIndex + offset;
            if (leftValues[leftIndex] != rightValues[rightIndex] || leftMarkedIndexes.contains(leftIndex)
                    || rightMarkedIndexes.contains(rightIndex)) {
                return 0;
            }
        }
        int offset = minimumSequenceLength;
        while (leftValues[leftStartIndex + offset] == rightValues[rightStartIndex + offset] && !leftMarkedIndexes.contains(leftStartIndex + offset)
                && !rightMarkedIndexes.contains(rightStartIndex + offset)) {
            offset++;
        }
        return offset;
    }

    private void addMatchIfNotOverlapping(List<Match> matches, Match match) {
        for (int i = matches.size() - 1; i >= 0; i--) {
            if (matches.get(i).overlaps(match)) {
                return;
            }
        }
        matches.add(match);
    }

    private Set<Integer> initiallyMarkedTokenIndexes(Submission submission) {
        Set<Token> baseCodeTokens = baseCodeMarkings.get(submission);
        List<Token> tokens = submission.getTokenList();
        return IntStream.range(0, tokens.size())
                .filter(i -> tokens.get(i).getType().isExcludedFromMatching() || (baseCodeTokens != null && baseCodeTokens.contains(tokens.get(i))))
                .boxed().collect(Collectors.toSet());
    }

    private SubsequenceHashLookupTable subsequenceHashLookupTableForSubmission(Submission submission, Set<Integer> markedIndexes) {
        return cachedHashLookupTables.computeIfAbsent(submission,
                (key -> new SubsequenceHashLookupTable(minimumMatchLength, tokenValueListFromSubmission(key), markedIndexes)));
    }

    private int[] tokenValueListFromSubmission(Submission submission) {
        return cachedTokenValueLists.computeIfAbsent(submission, (key -> {
            List<Token> tokens = key.getTokenList();
            int[] tokenValueList = new int[tokens.size()];
            for (int i = 0; i < tokens.size(); i++) {
                TokenType type = tokens.get(i).getType();
                synchronized (tokenTypeValues) {
                    tokenTypeValues.putIfAbsent(type, tokenTypeValues.size());
                }
                tokenValueList[i] = tokenTypeValues.get(type);
            }
            return tokenValueList;
        }));
    }
}
