package io.github.incplusplus.peerprocessing.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import java.math.BigDecimal;
import org.javatuples.Quartet;

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
    return new VectorQuery(
        vectorPair.getValue0(),
        vectorPair.getValue1(),
        vectorPair.getValue2(),
        vectorPair.getValue3());
  }

  public BigDecimal[] getFirstVector() {
    return firstVector;
  }

  public void setFirstVector(BigDecimal[] firstVector) {
    this.firstVector = firstVector;
  }

  public BigDecimal[] getSecondVector() {
    return secondVector;
  }

  public void setSecondVector(BigDecimal[] secondVector) {
    this.secondVector = secondVector;
  }

  public void setRowIndex(Integer rowIndex) {
    this.rowIndex = rowIndex;
  }

  public void setColumnIndex(Integer columnIndex) {
    this.columnIndex = columnIndex;
  }

  public BigDecimal getProduct() {
    return product;
  }

  public void setProduct(BigDecimal product) {
    this.product = product;
  }

  @Override
  public void complete() {
    setCompleted(true);
    this.product = new BigDecimalMatrix().dot(firstVector, secondVector);
    setResult(this.product);
  }

  public Integer getRowIndex() {
    return rowIndex;
  }

  public Integer getColumnIndex() {
    return columnIndex;
  }

  @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
  @JsonSubTypes(@JsonSubTypes.Type(BigDecimal.class))
  @Override
  public Object getResult() {
    return this.product;
  }

  @Override
  public void setResult(Object result) {
    this.product = (BigDecimal) result;
  }
}
