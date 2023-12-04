package org.checkplagiarism.reporting.reportobject.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Cluster(@JsonProperty("average_similarity") double averageSimilarity, @JsonProperty("strength") double strength,
                      @JsonProperty("members") List<String> members) {
}