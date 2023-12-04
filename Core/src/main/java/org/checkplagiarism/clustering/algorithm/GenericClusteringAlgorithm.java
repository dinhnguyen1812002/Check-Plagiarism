package org.checkplagiarism.clustering.algorithm;

import org.apache.commons.math3.linear.RealMatrix;

import java.util.Collection;

public interface GenericClusteringAlgorithm {
    Collection<Collection<Integer>> cluster(RealMatrix similarityMatrix);
}
