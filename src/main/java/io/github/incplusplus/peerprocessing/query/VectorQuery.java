package io.github.incplusplus.peerprocessing.query;

import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import org.javatuples.Quartet;

import java.math.BigDecimal;

public class VectorQuery extends Query {
  private BigDecimal[] firstVector;
  private BigDecimal[] secondVector;
  private Integer rowIndex;
  private Integer columnIndex;
  private BigDecimal product;

  public VectorQuery(
      BigDecimal[] firstVector, BigDecimal[] secondVector, Integer rowIndex, Integer columnIndex) {
    this.firstVector = firstVector;
    this.secondVector = secondVector;
    this.rowIndex = rowIndex;
    this.columnIndex = columnIndex;
  }

  @SuppressWarnings("unused")
  public VectorQuery() {}

  public static VectorQuery from(Quartet<BigDecimal[], BigDecimal[], Integer, Integer> vectorPair) {
    return new VectorQuery(vectorPair.getValue0(), vectorPair.getValue1(), vectorPair.getValue2(), vectorPair.getValue3());
  }

  @Override
  public void complete() {
    setCompleted(true);
    this.product = new BigDecimalMatrix().dot(firstVector, secondVector);
  }

  public Integer getRowIndex() {
    return rowIndex;
  }

  public Integer getColumnIndex() {
    return columnIndex;
  }

  @Override
  public Object getResult() {
    return this.product;
  }
}
