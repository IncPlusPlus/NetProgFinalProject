package io.github.incplusplus.peerprocessing.query.matrix;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.BatchQuery;
import io.github.incplusplus.peerprocessing.query.Query;
import io.github.incplusplus.peerprocessing.query.VectorQuery;
import io.github.incplusplus.peerprocessing.server.QueryState;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.incplusplus.peerprocessing.query.matrix.Operation.MULTIPLY;
import static java.util.Objects.isNull;

public class MatrixQuery extends BatchQuery {
  private Operation operation;
  private BigDecimalMatrix matrix1;
  private BigDecimalMatrix matrix2;
  /** Used only in the case that VectorQueries are required (i.e. a batch dot product operation) */
  private List<VectorQuery> vectorQueries;
  private BigDecimalMatrix resultMatrix;

  @SuppressWarnings("unused")
  public MatrixQuery() {}

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
  public void performCompletionAction() {
    if(operation.equals(MULTIPLY)){
      if(isNull(resultMatrix)) {
        BigDecimal[][] productMatrix = new BigDecimal[matrix1.getNumRows()][matrix2.getNumCols()];
        vectorQueries.forEach(vectorQuery ->
            productMatrix[vectorQuery.getRowIndex()][vectorQuery.getColumnIndex()] = (BigDecimal) vectorQuery.getResult());
        resultMatrix = new BigDecimalMatrix(productMatrix);
      }
    }
  }

  @Override
  public void setResult(Object result) {
    this.resultMatrix = (BigDecimalMatrix) result;
  }

  public BigDecimalMatrix getResultMatrix() {
    return resultMatrix;
  }

  public void setResultMatrix(BigDecimalMatrix resultMatrix) {
    this.resultMatrix = resultMatrix;
  }

  @Override
  public Object getResult() {
    return resultMatrix;
  }

  @Override
  public void complete() {
    // maybe implemented later if necessary
  }

  @Override
  @JsonIgnore
  public Query[] getQueries() {
    if (getOperation().equals(MULTIPLY)) {
      if (vectorQueries==null){
        vectorQueries = matrix1.getVectorsForMultiplyingWith(matrix2).parallelStream()
                .map(VectorQuery::from).collect(Collectors.toList());
        vectorQueries.forEach(vectorQuery -> vectorQuery.setRequestingClientUUID(this.getRequestingClientUUID()));
      }
      //return all vectorQueries that have not yet been solved
      return vectorQueries.stream().filter(vectorQuery -> !vectorQuery.isCompleted()).toArray(Query[]::new);
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
        vectorQueries.stream()
            .filter(query1 -> query1.getQueryId().equals(query.getQueryId()))
            .findFirst()
            .orElse(null);

    if (isNull(internallyStoredCorrespondingQuery)) {
      return false;
    } else {
      internallyStoredCorrespondingQuery.setCompleted(true);
      internallyStoredCorrespondingQuery.setQueryState(QueryState.COMPLETE);
      internallyStoredCorrespondingQuery.setResult(query.getResult());
      internallyStoredCorrespondingQuery.setReasonIncomplete(query.getReasonIncomplete());
      super.setCompleted(Stream.of(getQueries()).allMatch(Query::isCompleted));
      return true;
    }
  }
}
