package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;
import java.util.Vector;
import java.util.stream.Collectors;

public class BigDecimalMatrix extends RealMatrix<BigDecimal> {
	private Vector<Vector<BigDecimal>> thisMatrix;
	
	BigDecimalMatrix(Vector<Vector<BigDecimal>> vector) {
		this.thisMatrix = vector;
	}
	
	BigDecimalMatrix() {
		this.thisMatrix = null;
	}
	
	@Override
	public RealMatrix<BigDecimal> random(int m, int n) {
		return null;
	}
	
	@Override
	public RealMatrix<BigDecimal> identity(int n) {
		Vector<Vector<BigDecimal>> rows = new Vector<>(n);
		Vector<BigDecimal> allZeroes = allZeroes(n);
		for (int i = 0; i < n; i++) {
			Vector<BigDecimal> row = new Vector<>(allZeroes);
			row.set(i, BigDecimal.ONE);
			rows.add(row);
		}
		return new BigDecimalMatrix(rows);
	}
	
	@Override
	public RealMatrix<BigDecimal> multiply(RealMatrix<BigDecimal> other) {
		return null;
	}
	
	public Vector<BigDecimal> getCol(int colIndex) {
		return thisMatrix.stream().map(x -> x.get(colIndex)).collect(Collectors.toCollection(Vector::new));
	}
	
	public Vector<BigDecimal> getRow(int rowIndex) {
		return thisMatrix.get(rowIndex);
	}
	
	@Override
	public int getNumRows() {
		return thisMatrix.size();
	}
	
	@Override
	public int getNumCols() {
		return thisMatrix.get(0).size();
	}
	
	private static Vector<BigDecimal> allZeroes(int n) {
		Vector<BigDecimal> allZeroes = new Vector<>(n);
		for (int i = 0; i < n; i++) {allZeroes.add(BigDecimal.ZERO);}
		return allZeroes;
	}
}
