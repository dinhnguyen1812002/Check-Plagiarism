package org.checkplagiarism.reporting.reportobject;

import org.checkplagiarism.CPComparison;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.Submission;
import org.checkplagiarism.checkplagiarism;
import org.checkplagiarism.reporting.jsonfactory.ComparisonReportWriter;
import org.checkplagiarism.reporting.jsonfactory.ToDiskWriter;
import org.checkplagiarism.reporting.reportobject.mapper.ClusteringResultMapper;
import org.checkplagiarism.reporting.reportobject.mapper.MetricMapper;
import org.checkplagiarism.reporting.reportobject.model.Metric;
import org.checkplagiarism.reporting.reportobject.model.OverviewReport;
import org.checkplagiarism.reporting.reportobject.model.Version;
import org.language_api.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.checkplagiarism.reporting.jsonfactory.DirectoryManager.*;
import static org.checkplagiarism.reporting.reportobject.mapper.SubmissionNameToIdMapper.buildSubmissionNameToIdMap;

public class ReportObjectFactory {

    private static final Logger logger = LoggerFactory.getLogger(ReportObjectFactory.class);

    private static final ToDiskWriter fileWriter = new ToDiskWriter();
    public static final String OVERVIEW_FILE_NAME = "overview.json";
    public static final String SUBMISSIONS_FOLDER = "files";
    public static final Version REPORT_VIEWER_VERSION = checkplagiarism.CP_VERSION;

    private Map<String, String> submissionNameToIdMap;
    private Function<Submission, String> submissionToIdFunction;
    private Map<String, Map<String, String>> submissionNameToNameToComparisonFileName;

    public void createAndSaveReport(CPResult result, String path) {

        try {
            logger.info("Start writing report files...");
            createDirectory(path);
            buildSubmissionToIdMap(result);

            copySubmissionFilesToReport(path, result);

            writeComparisons(result, path);
            writeOverview(result, path);

            logger.info("Zipping report files...");
            zipAndDelete(path);
        } catch (IOException e) {
            logger.error("Could not create directory " + path + " for report viewer generation", e);
        }

    }

    private void zipAndDelete(String path) {
        boolean zipWasSuccessful = zipDirectory(path);
        if (zipWasSuccessful) {
            deleteDirectory(path);
        } else {
            logger.error("Could not zip results. The results are still available uncompressed at " + path);
        }
    }

    private void buildSubmissionToIdMap(CPResult result) {
        submissionNameToIdMap = buildSubmissionNameToIdMap(result);
        submissionToIdFunction = (Submission submission) -> submissionNameToIdMap.get(submission.getName());
    }
    private void copySubmissionFilesToReport(String path, CPResult result) {
        logger.info("Start copying submission files to the output directory...");
        List<CPComparison> comparisons = result.getComparisons(result.getOptions().maximumNumberOfComparisons());
        Set<Submission> submissions = getSubmissions(comparisons);
        File submissionsPath = createSubmissionsDirectory(path);
        if (submissionsPath == null) {
            return;
        }
        Language language = result.getOptions().language();
        for (Submission submission : submissions) {
            File directory = createSubmissionDirectory(path, submissionsPath, submission);
            File submissionRoot = submission.getRoot();
            if (directory == null) {
                continue;
            }
            for (File file : submission.getFiles()) {
                File fullPath = createSubmissionDirectory(path, submissionsPath, submission, file, submissionRoot);
                File fileToCopy = getFileToCopy(language, file);
                try {
                    if (fullPath != null) {
                        Files.copy(fileToCopy.toPath(), fullPath.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    } else {
                        throw new NullPointerException("Could not create file with full path");
                    }
                } catch (IOException e) {
                    logger.error("Could not save submission file " + fileToCopy, e);
                }
            }
        }
    }
    private File createSubmissionDirectory(String path, File submissionsPath, Submission submission, File file, File submissionRoot) {
        try {
            return createDirectory(submissionsPath.getPath(), submissionToIdFunction.apply(submission), file, submissionRoot);
        } catch (IOException e) {
            logger.error("Could not create directory " + path + " for report viewer generation", e);
            return null;
        }
    }

    private File createSubmissionDirectory(String path, File submissionsPath, Submission submission) {
        try {
            return createDirectory(submissionsPath.getPath(), submissionToIdFunction.apply(submission));
        } catch (IOException e) {
            logger.error("Could not create directory " + path + " for report viewer generation", e);
            return null;
        }
    }
    private File createSubmissionsDirectory(String path) {
        try {
            return createDirectory(path, SUBMISSIONS_FOLDER);
        } catch (IOException e) {
            logger.error("Could not create directory " + path + " for report viewer generation", e);
            return null;
        }
    }

    private File getFileToCopy(Language language, File file) {
        return language.useViewFiles() ? new File(file.getPath() + language.viewFileSuffix()) : file;
    }

    private void writeComparisons(CPResult result, String path) {
        ComparisonReportWriter comparisonReportWriter = new ComparisonReportWriter(submissionToIdFunction, fileWriter);
        submissionNameToNameToComparisonFileName = comparisonReportWriter.writeComparisonReports(result, path);
    }
    private void writeOverview(CPResult result, String path) {

        List<File> folders = new ArrayList<>();
        folders.addAll(result.getOptions().submissionDirectories());
        folders.addAll(result.getOptions().oldSubmissionDirectories());

        String baseCodePath = result.getOptions().hasBaseCode() ? result.getOptions().baseCodeSubmissionDirectory().getName() : "";
        ClusteringResultMapper clusteringResultMapper = new ClusteringResultMapper(submissionToIdFunction);

        int totalComparisons = result.getAllComparisons().size();
        int numberOfMaximumComparisons = result.getOptions().maximumNumberOfComparisons();
        int shownComparisons = totalComparisons > numberOfMaximumComparisons ? numberOfMaximumComparisons : totalComparisons;
        int missingComparisons = totalComparisons > numberOfMaximumComparisons ? (totalComparisons - numberOfMaximumComparisons) : 0;
        logger.info("Total Comparisons: {}. Comparisons in Report: {}. Omitted Comparisons: {}.", totalComparisons, shownComparisons,
                missingComparisons);
        OverviewReport overviewReport = new OverviewReport(REPORT_VIEWER_VERSION, folders.stream().map(File::getPath).toList(), // submissionFolderPath
                baseCodePath, // baseCodeFolderPath
                result.getOptions().language().getName(), // language
                result.getOptions().fileSuffixes(), // fileExtensions
                submissionNameToIdMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey)), // submissionIds
                submissionNameToNameToComparisonFileName,
                List.of(),
                result.getOptions().excludedFiles(),
                result.getOptions().minimumTokenMatch(), //
                getDate(),
                result.getDuration(),
                getMetrics(result),
                clusteringResultMapper.map(result),
                totalComparisons);

        fileWriter.saveAsJSON(overviewReport, path, OVERVIEW_FILE_NAME);

    }
    private Set<Submission> getSubmissions(List<CPComparison> comparisons) {
        Set<Submission> submissions = comparisons.stream().map(CPComparison::firstSubmission).collect(Collectors.toSet());
        Set<Submission> secondSubmissions = comparisons.stream().map(CPComparison::secondSubmission).collect(Collectors.toSet());
        submissions.addAll(secondSubmissions);
        return submissions;
    }
    private List<Metric> getMetrics(CPResult result) {
        MetricMapper metricMapper = new MetricMapper(submissionToIdFunction);
        return List.of(metricMapper.getAverageMetric(result), metricMapper.getMaxMetric(result));
    }

    private String getDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
