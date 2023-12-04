package org.checkplagiarism.reporting.reportobject.mapper;

import org.checkplagiarism.clustering.ClusteringResult;
import org.checkplagiarism.reporting.reportobject.model.Cluster;
import org.checkplagiarism.CPResult;
import org.checkplagiarism.Submission;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;


public class ClusteringResultMapper {
    private final Function<Submission, String> submissionToIdFunction;

    public ClusteringResultMapper(Function<Submission, String> submissionToIdFunction) {
        this.submissionToIdFunction = submissionToIdFunction;
    }

    public List<Cluster> map(CPResult result) {
        var clusteringResult = result.getClusteringResult();
        return clusteringResult.stream().map(ClusteringResult::getClusters).flatMap(Collection::stream).map(this::convertCluster).toList();
    }

    private Cluster convertCluster(org.checkplagiarism.clustering.Cluster<Submission> from) {
        var strength = from.getCommunityStrength();
        var avgSimilarity = from.getAverageSimilarity();
        var member = from.getMembers().stream().map(submissionToIdFunction).toList();
        return new Cluster(avgSimilarity, strength, member);
    }
}
