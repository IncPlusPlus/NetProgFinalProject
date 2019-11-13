package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;

public class BigDecimalMatrix extends RealMatrix<BigDecimal> {
	private BigDecimal[][] matrix;
	
	BigDecimalMatrix(BigDecimal[][] matrix) {
		this.matrix = matrix;
	}
	
	BigDecimalMatrix() {
		this.matrix = new BigDecimal[0][0];
	}
	
	@Override
	public RealMatrix<BigDecimal> random(int m, int n) {
		return null;
	}
	
	@Override
	public RealMatrix<BigDecimal> identity(int n) {
		BigDecimal[][] a = new BigDecimal[n][n];
		for (int i = 0; i < n; i++) { a[i][i] = BigDecimal.ONE; }
		return new BigDecimalMatrix(a);
	}
	
	@Override
	public RealMatrix<BigDecimal> multiply(RealMatrix<BigDecimal> other) {
		return null;
	}
	
	@Override
	public BigDecimal[] getCol(int colIndex) {
		//adapted from https://stackoverflow.com/a/30426991/1687436
		BigDecimal[] column = new BigDecimal[matrix[0].length];
		for (int i = 0; i < column.length; i++) {
			column[i] = matrix[i][colIndex];
		}
		return column;
	}
	
	@Override
	public BigDecimal[] getRow(int rowIndex) {
		return matrix[rowIndex];
	}
	
	@Override
	public BigDecimal dot(BigDecimal[] rowVector, BigDecimal[] columnVector) {
		return null;
	}
	
	@Override
	public int getNumRows() {
		return matrix.length;
	}
	
	@Override
	public int getNumCols() {
		return matrix[0].length;
	}
}
