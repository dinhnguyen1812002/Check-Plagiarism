package org.checkplagiarism;


import org.checkplagiarism.clustering.ClusteringFactory;
import org.checkplagiarism.exceptions.ExitException;
import org.checkplagiarism.exceptions.SubmissionException;
import org.checkplagiarism.options.CPOptions;
import org.checkplagiarism.reporting.reportobject.model.Version;
import org.checkplagiarism.strategy.ComparisonStrategy;
import org.checkplagiarism.strategy.ParallelComparisonStrategy;
import org.language_api.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;



public class checkplagiarism {
    private static final Logger logger = LoggerFactory.getLogger(checkplagiarism.class);

    public static final Version CP_VERSION = loadVersion();

    private static Version loadVersion() {
        ResourceBundle versionProperties = ResourceBundle.getBundle("org.checkplagiarism.version");
        String versionString = versionProperties.getString("version");
        Version currentVersion = Version.parseVersion(versionString);
        return currentVersion == null ? Version.DEVELOPMENT : currentVersion;
    }

    private final CPOptions options;

    private final Language language;
    private final ComparisonStrategy comparisonStrategy;

    public checkplagiarism(CPOptions options) {
        this.options = options;
        language = this.options.language();
        GreedyStringTiling coreAlgorithm = new GreedyStringTiling(options);
        comparisonStrategy = new ParallelComparisonStrategy(options, coreAlgorithm);
    }

    public CPResult run() throws ExitException {
        // Parse and validate submissions.
        SubmissionSetBuilder builder = new SubmissionSetBuilder(language, options);
        SubmissionSet submissionSet = builder.buildSubmissionSet();

        int submissionCount = submissionSet.numberOfSubmissions();
        if (submissionCount < 2) {
            throw new SubmissionException("Not enough valid submissions! (found " + submissionCount + " valid submissions)");
        }

        // Compare valid submissions.
        CPResult result = comparisonStrategy.compareSubmissions(submissionSet);
        if (logger.isInfoEnabled())
            logger.info("Total time for comparing submissions: {}", TimeUtil.formatDuration(result.getDuration()));

        result.setClusteringResult(ClusteringFactory.getClusterings(result.getAllComparisons(), options.clusteringOptions()));

        return result;
    }
}
