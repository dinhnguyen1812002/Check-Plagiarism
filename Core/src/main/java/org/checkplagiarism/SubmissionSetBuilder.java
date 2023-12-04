package org.checkplagiarism;


import org.checkplagiarism.exceptions.BasecodeException;
import org.checkplagiarism.exceptions.ExitException;
import org.checkplagiarism.exceptions.RootDirectoryException;
import org.checkplagiarism.exceptions.SubmissionException;
import org.checkplagiarism.options.CPOptions;
import org.language_api.Language;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class SubmissionSetBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SubmissionSetBuilder.class);

    private final Language language;
    private final CPOptions options;
    private final Set<String> excludedFileNames;

    public SubmissionSetBuilder(Language language, CPOptions options) {
        this.language = language;
        this.options = options;
        this.excludedFileNames = options.excludedFiles();
    }

    public SubmissionSet buildSubmissionSet() throws ExitException {
        Set<File> submissionDirectories = verifyRootDirectories(options.submissionDirectories(), true);
        Set<File> oldSubmissionDirectories = verifyRootDirectories(options.oldSubmissionDirectories(), false);
        checkForNonOverlappingRootDirectories(submissionDirectories, oldSubmissionDirectories);

        int numberOfRootDirectories = submissionDirectories.size() + oldSubmissionDirectories.size();
        boolean multipleRoots = (numberOfRootDirectories > 1);


        Map<File, Submission> foundSubmissions = new HashMap<>();
        for (File directory : submissionDirectories) {
            processRootDirectoryEntries(directory, multipleRoots, foundSubmissions, true);
        }
        for (File oldDirectory : oldSubmissionDirectories) {
            processRootDirectoryEntries(oldDirectory, multipleRoots, foundSubmissions, false);
        }

        Optional<Submission> baseCodeSubmission = loadBaseCode();
        baseCodeSubmission.ifPresent(baseSubmission -> foundSubmissions.remove(baseSubmission.getRoot()));


        List<Submission> submissions = new ArrayList<>(foundSubmissions.values());
        return new SubmissionSet(submissions, baseCodeSubmission.orElse(null), options);
    }

    private Set<File> verifyRootDirectories(Set<File> rootDirectoryNames, boolean areNewDirectories) throws ExitException {
        if (areNewDirectories && rootDirectoryNames.isEmpty()) {
            throw new RootDirectoryException("No root directories specified with submissions to check for plagiarism!");
        }

        Set<File> canonicalRootDirectories = new HashSet<>(rootDirectoryNames.size());
        for (final File rootDirectory : rootDirectoryNames) {
            if (!rootDirectory.exists()) {
                throw new RootDirectoryException(String.format("Root directory \"%s\" does not exist!", rootDirectory));
            }
            if (!rootDirectory.isDirectory()) {
                throw new RootDirectoryException(String.format("Root directory \"%s\" is not a directory!", rootDirectory));
            }

            File canonicalRootDirectory = makeCanonical(rootDirectory,
                    it -> new RootDirectoryException("Cannot read root directory: " + rootDirectory, it));
            if (!canonicalRootDirectories.add(canonicalRootDirectory)) {

                logger.warn("Root directory \"{}\" was specified more than once, duplicates will be ignored.", canonicalRootDirectory);
            }
        }
        return canonicalRootDirectories;
    }

    private void checkForNonOverlappingRootDirectories(Set<File> submissionDirectories, Set<File> oldSubmissionDirectories) {

        Set<File> commonRootdirectories = new HashSet<>(submissionDirectories);
        commonRootdirectories.retainAll(oldSubmissionDirectories);
        if (commonRootdirectories.isEmpty()) {
            return;
        }

        oldSubmissionDirectories.removeAll(commonRootdirectories);
        for (File rootDirectory : commonRootdirectories) {
            logger.warn(
                    "Root directory \"{}\" is specified both for plagiarism checking and for prior submissions, will perform plagiarism checking only.",
                    rootDirectory);
        }
    }

    private Optional<Submission> loadBaseCode() throws ExitException {
        if (!options.hasBaseCode()) {
            return Optional.empty();
        }

        File baseCodeSubmissionDirectory = options.baseCodeSubmissionDirectory();
        if (!baseCodeSubmissionDirectory.exists()) {
            throw new BasecodeException("Basecode directory \"%s\" does not exist".formatted(baseCodeSubmissionDirectory));
        }
        String errorMessage = isExcludedEntry(baseCodeSubmissionDirectory);
        if (errorMessage != null) {
            throw new BasecodeException(errorMessage);
        }

        Submission baseCodeSubmission = processSubmission(baseCodeSubmissionDirectory.getName(), baseCodeSubmissionDirectory, false);
        logger.info("Basecode directory \"{}\" will be used.", baseCodeSubmission.getName());
        return Optional.ofNullable(baseCodeSubmission);
    }

    /**
     * Read entries in the given root directory.
     */
    private String[] listSubmissionFiles(File rootDirectory) throws ExitException {
        if (!rootDirectory.isDirectory()) {
            throw new AssertionError("Given root is not a directory.");
        }

        String[] fileNames;

        try {
            fileNames = rootDirectory.list();
        } catch (SecurityException exception) {
            throw new RootDirectoryException("Cannot list files of the root directory! " + exception.getMessage(), exception);
        }

        if (fileNames == null) {
            throw new RootDirectoryException("Cannot list files of the root directory!");
        }

        Arrays.sort(fileNames);
        return fileNames;
    }

    /**
     * Check that the given submission entry is not invalid due to exclusion names or bad suffix.
     * @param submissionEntry Entry to check.
     * @return Error message if the entry should be ignored.
     */
    private String isExcludedEntry(File submissionEntry) {
        if (isFileExcluded(submissionEntry)) {
            return "Exclude submission: " + submissionEntry.getName();
        }

        if (submissionEntry.isFile() && !hasValidSuffix(submissionEntry)) {
            return "Ignore submission with invalid suffix: " + submissionEntry.getName();
        }
        return null;
    }


    private Submission processSubmission(String submissionName, File submissionFile, boolean isNew) throws ExitException {

        if (submissionFile.isDirectory() && options.subdirectoryName() != null) {

            submissionFile = new File(submissionFile, options.subdirectoryName());

            if (!submissionFile.exists()) {
                throw new SubmissionException(
                        String.format("Submission %s does not contain the given subdirectory '%s'", submissionName, options.subdirectoryName()));
            }

            if (!submissionFile.isDirectory()) {
                throw new SubmissionException(String.format("The given subdirectory '%s' is not a directory!", options.subdirectoryName()));
            }
        }

        submissionFile = makeCanonical(submissionFile, it -> new SubmissionException("Cannot create submission: " + submissionName, it));
        return new Submission(submissionName, submissionFile, isNew, parseFilesRecursively(submissionFile), language);
    }


    private void processRootDirectoryEntries(File rootDirectory, boolean multipleRoots, Map<File, Submission> foundSubmissions, boolean isNew)
            throws ExitException {
        for (String fileName : listSubmissionFiles(rootDirectory)) {
            File submissionFile = new File(rootDirectory, fileName);

            String errorMessage = isExcludedEntry(submissionFile);
            if (errorMessage == null) {
                String rootDirectoryPrefix = multipleRoots ? (rootDirectory.getName() + File.separator) : "";
                String submissionName = rootDirectoryPrefix + fileName;
                Submission submission = processSubmission(submissionName, submissionFile, isNew);
                foundSubmissions.put(submission.getRoot(), submission);
            } else {
                logger.error(errorMessage);
            }
        }
    }

    private boolean hasValidSuffix(File file) {
        List<String> validSuffixes = options.fileSuffixes();

        // This is the case if either the language modules or the CLI did not set the valid suffixes array in options
        if (validSuffixes == null || validSuffixes.isEmpty()) {
            return true;
        }
        return validSuffixes.stream().anyMatch(suffix -> file.getName().endsWith(suffix));
    }

    private boolean isFileExcluded(File file) {
        return excludedFileNames.stream().anyMatch(excludedName -> file.getName().endsWith(excludedName));
    }

    private Collection<File> parseFilesRecursively(File file) {
        if (isFileExcluded(file)) {
            return Collections.emptyList();
        }

        if (file.isFile() && hasValidSuffix(file)) {
            return Collections.singletonList(file);
        }

        String[] nestedFileNames = file.list();

        if (nestedFileNames == null) {
            return Collections.emptyList();
        }

        Collection<File> files = new ArrayList<>();

        for (String fileName : nestedFileNames) {
            files.addAll(parseFilesRecursively(new File(file, fileName)));
        }

        return files;
    }

    /**
     * Computes the canonical file of a file, if an exception is thrown it is wrapped accordingly and re-thrown.
     */
    private File makeCanonical(File file, Function<Exception, ExitException> exceptionWrapper) throws ExitException {
        try {
            return file.getCanonicalFile();
        } catch (IOException exception) {
            throw exceptionWrapper.apply(exception);
        }
    }
}

