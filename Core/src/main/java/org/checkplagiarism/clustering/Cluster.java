package org.checkplagiarism.clustering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

public class Cluster<T> {
    private final double communityStrength;
    private final Collection<T> members;
    private ClusteringResult<T> clusteringResult = null;
    private final double averageSimilarity;


    public Cluster(Collection<T> members, double communityStrength, double averageSimilarity) {
        this.members = new ArrayList<>(members);
        this.communityStrength = communityStrength;
        this.averageSimilarity = averageSimilarity;
    }

    public Collection<T> getMembers() {
        return new ArrayList<>(members);
    }


    public double getAverageSimilarity() {
        return averageSimilarity;
    }

    public double getCommunityStrength() {
        return communityStrength;
    }

    public void setClusteringResult(ClusteringResult<T> clusteringResult) {
        this.clusteringResult = clusteringResult;
    }


    public double getCommunityStrengthPerConnection() {
        int size = members.size();
        if (size < 2)
            return 0;
        return getCommunityStrength() / connections();
    }

    double getNormalizedCommunityStrengthPerConnection() {
        List<Cluster<T>> goodClusters = clusteringResult.getClusters().stream().filter(cluster -> cluster.getCommunityStrength() > 0).toList();
        double posCommunityStrengthSum = goodClusters.stream().mapToDouble(Cluster::getCommunityStrengthPerConnection).sum();

        int size = clusteringResult.getClusters().size();
        if (size < 2)
            return getCommunityStrengthPerConnection();
        return getCommunityStrengthPerConnection() / posCommunityStrengthSum;
    }

    /**
     * How much this cluster is worth during optimization.
     */
    public double getWorth(BiFunction<T, T, Double> similarity) {
        double communityStrength = getCommunityStrength();
        if (members.size() > 1) {
            communityStrength /= connections();
        }
        double averageSimilarity = averageSimilarity(similarity);
        return communityStrength * averageSimilarity;
    }

    /**
     * Computes the average similarity inside the cluster.
     * @param similarity function that supplies the similarity of two cluster members.
     * @return average similarity
     */
    private double averageSimilarity(BiFunction<T, T, Double> similarity) {
        List<T> members = new ArrayList<>(this.members);
        if (members.size() < 2) {
            return 1;
        }
        double similaritySum = 0;
        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {
                similaritySum += similarity.apply(members.get(i), members.get(j));
            }
        }
        return similaritySum / connections();
    }

    private int connections() {
        int size = members.size();
        return ((size - 1) * size) / 2;
    }

    /**
     * Whether this cluster is very uninformative or wrong and should be pruned as last step of the clustering process.
     * @return is bad
     */
    public boolean isBadCluster() {
        return members.size() < 2 || getCommunityStrength() < 0;
    }

}
