package org.checkplagiarism.reporting.reportobject.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public record ComparisonReport(@JsonProperty("id1") String firstSubmissionId, @JsonProperty("id2") String secondSubmissionId,
                               @JsonProperty("similarity") double similarity, @JsonProperty("matches") List<Match> matches) {

}
