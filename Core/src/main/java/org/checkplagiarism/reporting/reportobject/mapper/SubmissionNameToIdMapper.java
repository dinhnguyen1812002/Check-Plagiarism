package org.checkplagiarism.reporting.reportobject.mapper;

import org.checkplagiarism.CPResult;
import org.checkplagiarism.Submission;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SubmissionNameToIdMapper {
    private static final String FILE_SEPARATOR_REPLACEMENT = "_";


    public static Map<String, String> buildSubmissionNameToIdMap(CPResult result) {
        HashMap<String, String> idToName = new HashMap<>();
        result.getSubmissions().getSubmissions().forEach(submission -> idToName.put(submission.getName(), sanitizeNameOf(submission)));
        return idToName;
    }

    private static String sanitizeNameOf(Submission comparison) {
        return comparison.getName().replace(File.separator, FILE_SEPARATOR_REPLACEMENT);
    }
}
