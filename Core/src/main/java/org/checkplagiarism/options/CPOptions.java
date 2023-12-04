package org.checkplagiarism.options;


import org.checkplagiarism.clustering.ClusteringOptions;
import org.checkplagiarism.exceptions.BasecodeException;
import org.language_api.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;



public record CPOptions(Language language, Integer minimumTokenMatch, Set<File> submissionDirectories, Set<File> oldSubmissionDirectories,
                           File baseCodeSubmissionDirectory, String subdirectoryName, List<String> fileSuffixes, String exclusionFileName,
                           SimilarityMetric similarityMetric, double similarityThreshold, int maximumNumberOfComparisons, ClusteringOptions clusteringOptions,
                           boolean debugParser) {

    public static final double DEFAULT_SIMILARITY_THRESHOLD = 0;
    public static final int DEFAULT_SHOWN_COMPARISONS = 100;
    public static final int SHOW_ALL_COMPARISONS = 0;
    public static final SimilarityMetric DEFAULT_SIMILARITY_METRIC = SimilarityMetric.AVG;
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private static final Logger logger = LoggerFactory.getLogger(CPOptions.class);

    public CPOptions(Language language, Set<File> submissionDirectories, Set<File> oldSubmissionDirectories) {
        this(language, null, submissionDirectories, oldSubmissionDirectories, null, null, null, null, DEFAULT_SIMILARITY_METRIC,
                DEFAULT_SIMILARITY_THRESHOLD, DEFAULT_SHOWN_COMPARISONS, new ClusteringOptions(), false);
    }

    public CPOptions(Language language, Integer minimumTokenMatch, Set<File> submissionDirectories, Set<File> oldSubmissionDirectories,
                        File baseCodeSubmissionDirectory, String subdirectoryName, List<String> fileSuffixes, String exclusionFileName,
                        SimilarityMetric similarityMetric, double similarityThreshold, int maximumNumberOfComparisons, ClusteringOptions clusteringOptions,
                        boolean debugParser) {
        this.language = language;
        this.debugParser = debugParser;
        this.fileSuffixes = fileSuffixes == null || fileSuffixes.isEmpty() ? null : Collections.unmodifiableList(fileSuffixes);
        this.similarityThreshold = normalizeSimilarityThreshold(similarityThreshold);
        this.maximumNumberOfComparisons = normalizeMaximumNumberOfComparisons(maximumNumberOfComparisons);
        this.similarityMetric = similarityMetric;
        this.minimumTokenMatch = normalizeMinimumTokenMatch(minimumTokenMatch);
        this.exclusionFileName = exclusionFileName;
        this.submissionDirectories = submissionDirectories == null ? null : Collections.unmodifiableSet(submissionDirectories);
        this.oldSubmissionDirectories = oldSubmissionDirectories == null ? null : Collections.unmodifiableSet(oldSubmissionDirectories);
        this.baseCodeSubmissionDirectory = baseCodeSubmissionDirectory;
        this.subdirectoryName = subdirectoryName;
        this.clusteringOptions = clusteringOptions;
    }

    public CPOptions withLanguageOption(Language language) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withDebugParser(boolean debugParser) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withFileSuffixes(List<String> fileSuffixes) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withSimilarityThreshold(double similarityThreshold) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withMaximumNumberOfComparisons(int maximumNumberOfComparisons) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withSimilarityMetric(SimilarityMetric similarityMetric) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withMinimumTokenMatch(Integer minimumTokenMatch) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withExclusionFileName(String exclusionFileName) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withSubmissionDirectories(Set<File> submissionDirectories) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withOldSubmissionDirectories(Set<File> oldSubmissionDirectories) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withBaseCodeSubmissionDirectory(File baseCodeSubmissionDirectory) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withSubdirectoryName(String subdirectoryName) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public CPOptions withClusteringOptions(ClusteringOptions clusteringOptions) {
        return new CPOptions(language, minimumTokenMatch, submissionDirectories, oldSubmissionDirectories, baseCodeSubmissionDirectory,
                subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                clusteringOptions, debugParser);
    }

    public boolean hasBaseCode() {
        return baseCodeSubmissionDirectory != null;
    }

    public Set<String> excludedFiles() {
        return Optional.ofNullable(exclusionFileName()).map(this::readExclusionFile).orElse(Collections.emptySet());
    }

    @Override
    public List<String> fileSuffixes() {
        var language = language();
        if ((fileSuffixes == null || fileSuffixes.isEmpty()) && language != null)
            return Arrays.stream(language.suffixes()).toList();
        return fileSuffixes == null ? null : Collections.unmodifiableList(fileSuffixes);
    }

    @Override
    public Integer minimumTokenMatch() {
        var language = language();
        if (minimumTokenMatch == null && language != null)
            return language.minimumTokenMatch();
        return minimumTokenMatch;
    }

    private Set<String> readExclusionFile(final String exclusionFileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(exclusionFileName, CPOptions.CHARSET))) {
            final var excludedFileNames = reader.lines().collect(Collectors.toSet());
            if (logger.isDebugEnabled()) {
                logger.debug("Excluded files:{}{}", System.lineSeparator(), String.join(System.lineSeparator(), excludedFileNames));
            }
            return excludedFileNames;
        } catch (IOException e) {
            logger.error("Could not read exclusion file: " + e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    private static double normalizeSimilarityThreshold(double similarityThreshold) {
        if (similarityThreshold > 1) {
            logger.warn("Maximum threshold of 1 used instead of {}", similarityThreshold);
            return 1;
        } else if (similarityThreshold < 0) {
            logger.warn("Minimum threshold of 0 used instead of {}", similarityThreshold);
            return 0;
        } else {
            return similarityThreshold;
        }
    }

    private Integer normalizeMaximumNumberOfComparisons(Integer maximumNumberOfComparisons) {
        return Math.max(maximumNumberOfComparisons, SHOW_ALL_COMPARISONS);
    }

    private Integer normalizeMinimumTokenMatch(Integer minimumTokenMatch) {
        return (minimumTokenMatch != null && minimumTokenMatch < 1) ? Integer.valueOf(1) : minimumTokenMatch;
    }


    @Deprecated(since = "4.0.0", forRemoval = true)
    public CPOptions(Language language, Integer minimumTokenMatch, File submissionDirectory, Set<File> oldSubmissionDirectories,
                        String baseCodeSubmissionName, String subdirectoryName, List<String> fileSuffixes, String exclusionFileName,
                        SimilarityMetric similarityMetric, double similarityThreshold, int maximumNumberOfComparisons, ClusteringOptions clusteringOptions,
                        boolean debugParser) throws BasecodeException {
        this(language, minimumTokenMatch, Set.of(submissionDirectory), oldSubmissionDirectories,
                convertLegacyBaseCodeToFile(baseCodeSubmissionName, submissionDirectory), subdirectoryName, fileSuffixes, exclusionFileName,
                similarityMetric, similarityThreshold, maximumNumberOfComparisons, clusteringOptions, debugParser);
    }


    @Deprecated(since = "4.0.0", forRemoval = true)
    public CPOptions withBaseCodeSubmissionName(String baseCodeSubmissionName) {
        File baseCodeDirectory = new File(baseCodeSubmissionName);
        if (baseCodeDirectory.exists()) {
            return this.withBaseCodeSubmissionDirectory(baseCodeDirectory);
        }

        if (submissionDirectories.size() != 1) {
            throw new IllegalArgumentException("Partial path based base code requires exactly one submission directory");
        }
        File submissionDirectory = submissionDirectories.iterator().next();
        try {
            return new CPOptions(language, minimumTokenMatch, submissionDirectory, oldSubmissionDirectories, baseCodeSubmissionName,
                    subdirectoryName, fileSuffixes, exclusionFileName, similarityMetric, similarityThreshold, maximumNumberOfComparisons,
                    clusteringOptions, debugParser);
        } catch (BasecodeException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Converts a legacy base code submission name to a directory path.
     * @deprecated Use the default initializer with @{{@link #baseCodeSubmissionDirectory} instead.
     */
    @Deprecated(since = "4.0.0", forRemoval = true)
    private static File convertLegacyBaseCodeToFile(String baseCodeSubmissionName, File submissionDirectory) throws BasecodeException {
        if (baseCodeSubmissionName == null) {
            return null;
        }
        File baseCodeAsAbsolutePath = new File(baseCodeSubmissionName);
        if (baseCodeAsAbsolutePath.exists()) {
            return baseCodeAsAbsolutePath;
        } else {
            String normalizedName = baseCodeSubmissionName;
            while (normalizedName.startsWith(File.separator)) {
                normalizedName = normalizedName.substring(1);
            }
            while (normalizedName.endsWith(File.separator)) {
                normalizedName = normalizedName.substring(0, normalizedName.length() - 1);
            }
            if (normalizedName.isEmpty() || normalizedName.contains(File.separator)) {
                throw new BasecodeException(
                        "The basecode directory name \"" + normalizedName + "\" cannot contain dots! Please migrate to the path-based API.");
            }
            if (normalizedName.contains(".")) {
                throw new BasecodeException(
                        "The basecode directory name \"" + normalizedName + "\" cannot contain dots! Please migrate to the path-based API.");
            }
            return new File(submissionDirectory, baseCodeSubmissionName);
        }
    }
}

