package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;
import java.util.Arrays;

public class SplittableMatrix {
	private BigDecimal[][] matrix;
	
	public SplittableMatrix(BigDecimal[][] matrix) {
		this.matrix = matrix;
	}
	
	public BigDecimal[][] getMatrix() {
		return matrix;
	}
	
	public void setMatrix(BigDecimal[][] matrix) {
		this.matrix = matrix;
	}
	
	public BigDecimal[] getRow(int address) {
		return this.matrix[address];
	}
	
	public BigDecimal[] getColumn(int address) {
		return (BigDecimal[]) Arrays.stream(this.matrix).map(x -> x[address]).toArray();
	}
	
	public BigDecimal dot(BigDecimal[] other) {
	return null;
	}
}
