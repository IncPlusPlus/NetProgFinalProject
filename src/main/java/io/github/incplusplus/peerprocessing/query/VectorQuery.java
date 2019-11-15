package io.github.incplusplus.peerprocessing.query;

import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import org.javatuples.Pair;

import java.math.BigDecimal;

public class VectorQuery extends Query {
  private BigDecimal[] firstVector;
  private BigDecimal[] secondVector;

  public VectorQuery(BigDecimal[] firstVector, BigDecimal[] secondVector) {
    this.firstVector = firstVector;
    this.secondVector = secondVector;
  }

  public static VectorQuery from(Pair<BigDecimal[], BigDecimal[]> vectorPair) {
    return new VectorQuery(vectorPair.getValue0(), vectorPair.getValue1());
  }

  @Override
  public void complete() {
    setCompleted(true);
    new BigDecimalMatrix().dot(firstVector, secondVector);
  }
}
