package org.checkplagiarism.clustering.preprocessors;


import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.DefaultRealMatrixPreservingVisitor;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.random.EmpiricalDistribution;
import org.checkplagiarism.clustering.ClusteringPreprocessor;
import org.checkplagiarism.clustering.PreprocessorHelper;


public class CumulativeDistributionFunctionPreprocessor implements ClusteringPreprocessor {

    private final PreprocessorHelper helper = new PreprocessorHelper();

    @Override
    public double[][] preprocessSimilarities(double[][] similarityMatrix) {
        RealMatrix similarity = new Array2DRowRealMatrix(similarityMatrix, true);
        int connections = (similarity.getColumnDimension() * (similarity.getColumnDimension() - 1)) / 2;
        EmpiricalDistribution dist = new EmpiricalDistribution(Math.max(100, connections / 100));
        double[] allWeights = new double[connections];
        similarity.walkInOptimizedOrder(new DefaultRealMatrixPreservingVisitor() {

            int index = 0;

            @Override
            public void visit(int row, int column, double value) {
                // collect upper triangle
                if (row > column) {
                    allWeights[this.index++] = value;
                }
            }
        });
        dist.load(allWeights);
        similarity.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            @Override
            public double visit(int row, int column, double value) {
                return dist.cumulativeProbability(value) * value;
            }
        });
        return helper.removeDisconnectedEntries(similarity.getData());
    }

    @Override
    public int originalIndexOf(int result) {
        return helper.postProcessResult(result);
    }

}

