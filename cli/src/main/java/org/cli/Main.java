package org.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.checkplagiarism;
import org.checkplagiarism.clustering.ClusteringOptions;
import org.checkplagiarism.clustering.Preprocessing;
import org.checkplagiarism.exceptions.ExitException;
import org.checkplagiarism.options.CPOptions;
import org.checkplagiarism.reporting.reportobject.ReportObjectFactory;
import org.cli.logger.CollectedLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.cli.CommandLineArgument.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final Random RANDOM = new SecureRandom();

    private static final String CREDITS = "Hutech IT";

    private static final String[] DESCRIPTIONS = {"Detecting Software Plagiarism", "Software-Archaeological Playground", "Since 1996",
            "Scientifically Published", "Maintained by SDQ", "RIP Structure and Table", "What else?", "You have been warned!", "Since Java 1.0",
            "More Abstract than Tree", "Students Nightmare", "No, changing variable names does not work", "The tech is out there!"};

    private static final String PROGRAM_NAME = "Plagiarism";
    static final String CLUSTERING_GROUP_NAME = "Clustering";
    static final String ADVANCED_GROUP = "Advanced";

    private final ArgumentParser parser;

    public static void main(String[] args) {
        try {
            logger.debug("Your version {}", checkplagiarism.CP_VERSION);
            Main cli = new Main();
            Namespace arguments = cli.parseArguments(args);
            CPOptions options = cli.buildOptionsFromArguments(arguments);
            checkplagiarism cp = new checkplagiarism(options);
            logger.debug("initialized");
            CPResult result = cp.run();
            ReportObjectFactory reportObjectFactory = new ReportObjectFactory();
            reportObjectFactory.createAndSaveReport(result, arguments.getString(RESULT_FOLDER.flagWithoutDash()));
        } catch (ExitException exception) {
            logger.error(exception.getMessage());
            finalizeLogger();
            System.exit(1);
        }
    }
    private static void finalizeLogger() {
        ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        if (!(factory instanceof CollectedLoggerFactory collectedLoggerFactory))
            return;
        collectedLoggerFactory.finalizeInstances();
    }

    public Main() {
        parser = ArgumentParsers.newFor(PROGRAM_NAME).build().defaultHelp(true).description(generateDescription());
        CliGroupHelper groupHelper = new CliGroupHelper(parser);
        for (CommandLineArgument argument : CommandLineArgument.values()) {
            argument.parseWith(parser, groupHelper);
        }
    }
    public Namespace parseArguments(String[] arguments) {
        try {
            return parser.parseArgs(arguments);
        } catch (ArgumentParserException exception) {
            parser.handleError(exception);
            System.exit(1);
        }
        return null;
    }
    public CPOptions buildOptionsFromArguments(Namespace namespace) {
        String fileSuffixString = SUFFIXES.getFrom(namespace);
        String[] fileSuffixes = new String[] {};
        if (fileSuffixString != null) {
            fileSuffixes = fileSuffixString.replaceAll("\\s+", "").split(",");
        }

        // Collect the root directories.
        List<String> submissionDirectoryPaths = new ArrayList<>();
        List<String> oldSubmissionDirectoryPaths = new ArrayList<>();
        addAllMultiValueArgument(ROOT_DIRECTORY.getListFrom(namespace), submissionDirectoryPaths);
        addAllMultiValueArgument(NEW_DIRECTORY.getListFrom(namespace), submissionDirectoryPaths);
        addAllMultiValueArgument(OLD_DIRECTORY.getListFrom(namespace), oldSubmissionDirectoryPaths);
        var submissionDirectories = submissionDirectoryPaths.stream().map(File::new).collect(Collectors.toSet());
        var oldSubmissionDirectories = oldSubmissionDirectoryPaths.stream().map(File::new).collect(Collectors.toSet());

        var language = LanguageLoader.getLanguage(LANGUAGE.getFrom(namespace)).orElseThrow();
        ClusteringOptions clusteringOptions = getClusteringOptions(namespace);

        CPOptions options = new CPOptions(language, MIN_TOKEN_MATCH.getFrom(namespace), submissionDirectories, oldSubmissionDirectories, null,
                SUBDIRECTORY.getFrom(namespace), Arrays.stream(fileSuffixes).toList(), EXCLUDE_FILE.getFrom(namespace),
                CPOptions.DEFAULT_SIMILARITY_METRIC, SIMILARITY_THRESHOLD.getFrom(namespace), SHOWN_COMPARISONS.getFrom(namespace),
                clusteringOptions, DEBUG.getFrom(namespace));

        String baseCodePath = BASE_CODE.getFrom(namespace);
        File baseCodeDirectory = baseCodePath == null ? null : new File(baseCodePath);
        if (baseCodeDirectory == null || baseCodeDirectory.exists()) {
            return options.withBaseCodeSubmissionDirectory(baseCodeDirectory);
        } else {
            logger.warn("Using legacy partial base code API. Please migrate to new full path base code API.");
            return options.withBaseCodeSubmissionName(baseCodePath);
        }
    }
    private static ClusteringOptions getClusteringOptions(Namespace namespace) {
        ClusteringOptions clusteringOptions = new ClusteringOptions();
        if (CLUSTER_DISABLE.isSet(namespace)) {
            boolean disabled = CLUSTER_DISABLE.getFrom(namespace);
            clusteringOptions = clusteringOptions.withEnabled(!disabled);
        }
        if (CLUSTER_ALGORITHM.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withAlgorithm(CLUSTER_ALGORITHM.getFrom(namespace));
        }
        if (CLUSTER_METRIC.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSimilarityMetric(CLUSTER_METRIC.getFrom(namespace));
        }
        if (CLUSTER_SPECTRAL_BANDWIDTH.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSpectralKernelBandwidth(CLUSTER_SPECTRAL_BANDWIDTH.getFrom(namespace));
        }
        if (CLUSTER_SPECTRAL_NOISE.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSpectralGaussianProcessVariance(CLUSTER_SPECTRAL_NOISE.getFrom(namespace));
        }
        if (CLUSTER_SPECTRAL_MIN_RUNS.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSpectralMinRuns(CLUSTER_SPECTRAL_MIN_RUNS.getFrom(namespace));
        }
        if (CLUSTER_SPECTRAL_MAX_RUNS.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSpectralMaxRuns(CLUSTER_SPECTRAL_MAX_RUNS.getFrom(namespace));
        }
        if (CLUSTER_SPECTRAL_KMEANS_ITERATIONS.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withSpectralMaxKMeansIterationPerRun(CLUSTER_SPECTRAL_KMEANS_ITERATIONS.getFrom(namespace));
        }
        if (CLUSTER_AGGLOMERATIVE_THRESHOLD.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withAgglomerativeThreshold(CLUSTER_AGGLOMERATIVE_THRESHOLD.getFrom(namespace));
        }
        if (CLUSTER_AGGLOMERATIVE_INTER_CLUSTER_SIMILARITY.isSet(namespace)) {
            clusteringOptions = clusteringOptions
                    .withAgglomerativeInterClusterSimilarity(CLUSTER_AGGLOMERATIVE_INTER_CLUSTER_SIMILARITY.getFrom(namespace));
        }
        if (CLUSTER_PREPROCESSING_NONE.isSet(namespace) && Boolean.TRUE.equals(CLUSTER_PREPROCESSING_NONE.getFrom(namespace))) {
            clusteringOptions = clusteringOptions.withPreprocessor(Preprocessing.NONE);
        }
        if (CLUSTER_PREPROCESSING_CDF.isSet(namespace) && Boolean.TRUE.equals(CLUSTER_PREPROCESSING_CDF.getFrom(namespace))) {
            clusteringOptions = clusteringOptions.withPreprocessor(Preprocessing.CUMULATIVE_DISTRIBUTION_FUNCTION);
        }
        if (CLUSTER_PREPROCESSING_PERCENTILE.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withPreprocessor(Preprocessing.PERCENTILE)
                    .withPreprocessorPercentile(CLUSTER_PREPROCESSING_PERCENTILE.getFrom(namespace));
        }
        if (CLUSTER_PREPROCESSING_THRESHOLD.isSet(namespace)) {
            clusteringOptions = clusteringOptions.withPreprocessor(Preprocessing.THRESHOLD)
                    .withPreprocessorPercentile(CLUSTER_PREPROCESSING_THRESHOLD.getFrom(namespace));
        }
        return clusteringOptions;
    }

    private String generateDescription() {
        var randomDescription = DESCRIPTIONS[RANDOM.nextInt(DESCRIPTIONS.length)];
        return String.format("%s%n%n%s", randomDescription, CREDITS);
    }

    private void addAllMultiValueArgument(List<List<String>> argumentValues, List<String> destinationRootDirectories) {
        if (argumentValues == null) {
            return;
        }
        argumentValues.forEach(destinationRootDirectories::addAll);
    }
}