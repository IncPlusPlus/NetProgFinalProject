package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;
import java.util.Arrays;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.randBigDec;
import static java.math.BigDecimal.ZERO;

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
		BigDecimal[][] out = new BigDecimal[n][m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				out[i][j] = randBigDec(100000);
			}
		}
		return new BigDecimalMatrix(out);
	}
	
	@Override
	public RealMatrix<BigDecimal> identity(int n) {
		BigDecimal[][] out = initZero(new BigDecimal[n][n]);
		for (int i = 0; i < n; i++) { out[i][i] = BigDecimal.ONE; }
		return new BigDecimalMatrix(out);
	}
	
	@Override
	public RealMatrix<BigDecimal> multiply(RealMatrix<BigDecimal> other) {
		if (this.getNumCols() != other.getNumRows())
			throw new IllegalArgumentException("Multiplication is not defined for the specified matrices");
		BigDecimal[][] result = initZero(new BigDecimal[this.getNumRows()][other.getNumCols()]);
		for (int i = 0; i < this.getNumCols(); i++) {
			for (int j = 0; j < other.getNumCols(); j++) {
				for (int k = 0; k < this.getNumCols(); k++) {
					result[i][j] = result[i][j].add(matrix[i][k].multiply(other.getEntry(k, j)));
				}
			}
		}
		return new BigDecimalMatrix(result);
	}
	
	@Override
	public BigDecimal[] multiply(BigDecimal[] vector) {
		int numRows = matrix.length;
		int numCols = matrix[0].length;
		if (vector.length != numCols) throw new RuntimeException("Illegal matrix dimensions.");
		BigDecimal[] out = new BigDecimal[numRows];
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) { out[i] = out[i].add(matrix[i][j].multiply(vector[j])); }
		}
		return out;
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
	protected BigDecimal getEntry(int rowNum, int colNum) {
		return matrix[rowNum][colNum];
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
	
	@Override
	public BigDecimal[][] getContents() {
		return Arrays.copyOf(matrix, matrix.length);
	}
	
	private static BigDecimal[][] initZero(BigDecimal[][] nullArray) {
		for (BigDecimal[] bigDecimals : nullArray) {
			Arrays.fill(bigDecimals, ZERO);
		}
		return nullArray;
	}
}
