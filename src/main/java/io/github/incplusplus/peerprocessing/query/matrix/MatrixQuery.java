package io.github.incplusplus.peerprocessing.query.matrix;

import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.Query;

public class MatrixQuery extends Query {
  private Operation operation;
  private BigDecimalMatrix matrix1;
  private BigDecimalMatrix matrix2;

  public MatrixQuery(Operation operation, BigDecimalMatrix bigDecimalMatrix) {}

  public MatrixQuery(
      Operation operation, BigDecimalMatrix bigDecimalMatrix1, BigDecimalMatrix bigDecimalMatrix2) {
    this.operation = operation;
    this.matrix1 = bigDecimalMatrix1;
    this.matrix2 = bigDecimalMatrix2;
  }
	
	public Operation getOperation() {
		return operation;
	}
	
	public void setOperation(Operation operation) {
		this.operation = operation;
	}
	
	public BigDecimalMatrix getMatrix1() {
		return matrix1;
	}
	
	public void setMatrix1(BigDecimalMatrix matrix1) {
		this.matrix1 = matrix1;
	}
	
	public BigDecimalMatrix getMatrix2() {
		return matrix2;
	}
	
	public void setMatrix2(BigDecimalMatrix matrix2) {
		this.matrix2 = matrix2;
	}
	
	@Override
  public void complete() {}
}
