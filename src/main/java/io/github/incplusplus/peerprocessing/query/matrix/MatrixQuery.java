package io.github.incplusplus.peerprocessing.query.matrix;

import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.BatchQuery;
import io.github.incplusplus.peerprocessing.query.Query;
import io.github.incplusplus.peerprocessing.query.VectorQuery;
import io.github.incplusplus.peerprocessing.server.QueryState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.github.incplusplus.peerprocessing.query.matrix.Operation.MULTIPLY;

public class MatrixQuery extends BatchQuery {
  private Operation operation;
  private BigDecimalMatrix matrix1;
  private BigDecimalMatrix matrix2;
  /** Used only in the case that VectorQueries are required (i.e. a batch dot product operation) */
  private List<VectorQuery> vectorQueries;

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
  public void complete() {
    // maybe implemented later if necessary
  }

  @Override
  public Query[] getQueries() {
    if (getOperation().equals(MULTIPLY)) {
      if (vectorQueries==null){
        vectorQueries = matrix1.getVectorsForMultiplyingWith(matrix2).parallelStream()
                .map(VectorQuery::from).collect(Collectors.toList());
      }
      //return all vectorQueries that have not yet been solved
      return (Query[]) vectorQueries.stream().filter(vectorQuery -> !vectorQuery.isCompleted()).toArray();
    }
    else {
      // if this is not something that supports splitting into multiple queries
      // return a single element array containing this single query.
      return new MatrixQuery[] {this};
    }
  }

  @Override
  public boolean offer(Query query) {
    Query internallyStoredCorrespondingQuery =
        Arrays.stream(getQueries())
            .filter(query1 -> query1.getQueryId().equals(query.getQueryId()))
            .findFirst()
            .orElse(null);

    if (Objects.isNull(internallyStoredCorrespondingQuery)) {
      return false;
    } else {
      if (internallyStoredCorrespondingQuery.isCompleted())
        throw new IllegalStateException("MatrixQuery was offered a duplicate Query.");
      internallyStoredCorrespondingQuery.setCompleted(true);
      internallyStoredCorrespondingQuery.setQueryState(QueryState.COMPLETE);
      internallyStoredCorrespondingQuery.setResult(query.getResult());
      internallyStoredCorrespondingQuery.setReasonIncomplete(query.getReasonIncomplete());
      return true;
    }
  }
}
