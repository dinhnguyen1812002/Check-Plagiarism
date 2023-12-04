package org.checkplagiarism;


import org.language_api.Language;
import org.language_api.ParsingException;
import org.language_api.Token;
import org.language_api.TokenPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Submission implements Comparable<Submission> {
    private static final Logger logger = LoggerFactory.getLogger(Submission.class);

    private static final String ERROR_FOLDER = "errors";

    private final String name;
    private final File submissionRootFile;
    private final boolean isNew;

    private final Collection<File> files;

    private boolean hasErrors;

    private List<Token> tokenList;

    private CPComparison baseCodeComparison;

    private final Language language;

    public Submission(String name, File submissionRootFile, boolean isNew, Collection<File> files, Language language) {
        this.name = name;
        this.submissionRootFile = submissionRootFile;
        this.isNew = isNew;
        this.files = files;
        this.language = language;
    }

    @Override
    public int compareTo(Submission other) {
        return name.compareTo(other.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Submission otherSubmission)) {
            return false;
        }
        return otherSubmission.getName().equals(name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public CPComparison getBaseCodeComparison() {
        return baseCodeComparison;
    }

    public Collection<File> getFiles() {
        return files;
    }

    public String getName() {
        return name;
    }


    public int getNumberOfTokens() {
        if (tokenList == null) {
            return 0;
        }
        return tokenList.size();
    }


    public File getRoot() {
        return submissionRootFile;
    }


    int getSimilarityDivisor(boolean subtractBaseCode) {
        int divisor = getNumberOfTokens() - getFiles().size();
        if (subtractBaseCode && baseCodeComparison != null) {
            divisor -= baseCodeComparison.getNumberOfMatchedTokens();
        }
        return divisor;
    }

    public List<Token> getTokenList() {
        return tokenList == null ? null : Collections.unmodifiableList(tokenList);
    }

    public boolean hasBaseCodeMatches() {
        return baseCodeComparison != null;
    }

    public boolean hasErrors() {
        return hasErrors;
    }


    public boolean isNew() {
        return isNew;
    }


    public void setBaseCodeComparison(CPComparison baseCodeComparison) {
        this.baseCodeComparison = baseCodeComparison;
    }
    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }


    public String getTokenAnnotatedSourceCode() {
        return TokenPrinter.printTokens(tokenList, submissionRootFile);
    }

    @Override
    public String toString() {
        return name;
    }

    private void copySubmission() {
        File errorDirectory = createErrorDirectory(language.getIdentifier(), name);
        logger.info("Copying erroneous submission to {}", errorDirectory.getAbsolutePath());
        for (File file : files) {
            try {
                Files.copy(file.toPath(), new File(errorDirectory, file.getName()).toPath());
            } catch (IOException exception) {
                logger.error("Error copying file: " + exception.getMessage(), exception);
            }
        }
    }

    private static File createErrorDirectory(String... subdirectoryNames) {
        File subdirectory = Path.of(ERROR_FOLDER, subdirectoryNames).toFile();
        if (!subdirectory.exists()) {
            subdirectory.mkdirs();
        }
        return subdirectory;
    }

    /* package-private */ void markAsErroneous() {
        hasErrors = true;
    }


    /* package-private */ boolean parse(boolean debugParser) {
        if (files == null || files.isEmpty()) {
            logger.error("ERROR: nothing to parse for submission \"{}\"", name);
            tokenList = null;
            hasErrors = true; // invalidate submission
            return false;
        }

        try {
            tokenList = language.parse(new HashSet<>(files));
        } catch (ParsingException e) {
            logger.warn("Failed to parse submission {} with error {}", this, e);
            tokenList = null;
            hasErrors = true;
            if (debugParser) {
                copySubmission();
            }
            return false;
        }

        if (tokenList.size() < 3) {
            logger.error("Submission \"{}\" is too short!", name);
            tokenList = null;
            hasErrors = true;
            return false;
        }
        return true;
    }
}

