package org.checkplagiarism.clustering;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;

public class PreprocessorHelper {
    private IntegerMapping<Integer> mapping;

    /**
     * Removes disconnected edges from the input matrix.
     * @param connections similarity matrix
     * @return similarity matrix without zero rows / cols
     */
    public double[][] removeDisconnectedEntries(double[][] connections) {
        List<Integer> rowList = new ArrayList<>();
        mapping = new IntegerMapping<>(connections.length);
        RealMatrix similarity = new Array2DRowRealMatrix(connections, true);
        for (int i = 0; i < similarity.getRowDimension(); i++) {
            if (DoubleStream.of(similarity.getRow(i)).anyMatch(x -> x > 0)) {
                rowList.add(i);
                mapping.map(i);
            }
        }
        int[] preservedRows = rowList.stream().mapToInt(Integer::intValue).toArray();
        if (preservedRows.length == 0) {
            return new double[0][];
        }
        similarity = similarity.getSubMatrix(preservedRows, preservedRows);
        return similarity.getData();
    }

    public int postProcessResult(int index) {
        return mapping.unmap(index);
    }
}
