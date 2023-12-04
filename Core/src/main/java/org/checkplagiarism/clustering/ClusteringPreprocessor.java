package org.checkplagiarism.clustering;

public interface ClusteringPreprocessor {
    double[][] preprocessSimilarities(double[][] similarityMatrix);

    int originalIndexOf(int index);
}
