package io.github.incplusplus.peerprocessing.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.matrix.MatrixQuery;
import io.github.incplusplus.peerprocessing.query.matrix.Operation;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BatchQueryTest {
  @ParameterizedTest
  @MethodSource(
      "io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest#provideSmallMatrices")
  void getQueriesAndNumbers(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) {
    MatrixQuery query = new MatrixQuery(Operation.MULTIPLY, matrix1, matrix2);
    List<Query> queryList = query.getQueries();
    assertEquals(matrix1.getNumRows() * matrix2.getNumCols(), queryList.size());
    assertEquals(matrix1.getNumRows() * matrix2.getNumCols(), query.getTotalNumQueries());
    assertEquals(0, query.getNumCompleteParts());
    assertFalse(query.isCompleted());
    queryList.forEach(
        subQuery -> {
          subQuery.complete();
          query.offer(subQuery);
        });
    assertTrue(query.isCompleted());
    assertEquals(queryList.size(), query.getNumCompleteParts());
    assertEquals(matrix1.getNumRows() * matrix2.getNumCols(), query.getTotalNumQueries());
  }
}
