package io.github.incplusplus.peerprocessing.linear;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.*;

class BigDecimalMatrixTest {
  // <editor-fold desc="Various matrices">
  private final BigDecimalMatrix zero2x2 =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {ZERO, ZERO},
            {ZERO, ZERO}
          });

  private final BigDecimalMatrix identity2x2 =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {ONE, ZERO},
            {ZERO, ONE}
          });

  private final BigDecimalMatrix identity3x3 =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {ONE, ZERO, ZERO},
            {ZERO, ONE, ZERO},
            {ZERO, ZERO, ONE}
          });

  private final BigDecimalMatrix dumb4x3 =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {ONE, ZERO, ZERO},
            {ZERO, ONE, ZERO},
            {ZERO, ZERO, ONE},
            {ZERO, ZERO, ZERO}
          });

  // <editor-fold desc="A, B">
  private final BigDecimalMatrix A =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("-4"), w("5"), w("2"), w("3")},
            {w("4"), w("0"), w("-3"), w("1")},
            {w("6"), w("7"), w("-2"), w("8")}
          });

  private final BigDecimalMatrix B =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("9"), w("5"), w("10")},
            {w("11"), w("4"), w("2")},
            {w("6"), w("7"), w("12")},
            {w("-2"), w("-4"), w("0")}
          });

  private final BigDecimalMatrix AB =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("25"), w("2"), w("-6")},
            {w("16"), w("-5"), w("4")},
            {w("103"), w("12"), w("50")}
          });
  // </editor-fold>
  // <editor-fold desc="C, D">
  private final BigDecimalMatrix C =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("-1"), w("-4"), w("-3"), w("1")},
            {w("-2"), w("0"), w("2"), w("3")},
            {w("4"), w("5"), w("6"), w("7")},
            {w("8"), w("9"), w("10"), w("11")}
          });

  private final BigDecimalMatrix D =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("12"), w("13"), w("14"), w("15")},
            {w("1"), w("-4"), w("-3"), w("0")},
            {w("2"), w("3"), w("4"), w("5")},
            {w("6"), w("7"), w("-1"), w("8")}
          });

  private final BigDecimalMatrix CD =
      new BigDecimalMatrix(
          new BigDecimal[][] {
            {w("-16"), w("1"), w("-15"), w("-22")},
            {w("-2"), w("1"), w("-23"), w("4")},
            {w("107"), w("99"), w("58"), w("146")},
            {w("191"), w("175"), w("114"), w("258")}
          });
  // </editor-fold>
  // </editor-fold>

  @Test
  void isSquare() {
    assertTrue(identity2x2.isSquare());
    assertTrue(identity3x3.isSquare());
    assertFalse(dumb4x3.isSquare());
  }

  @Test
  void random() {}

  @Test
  void identity() {
    iterateAndAssertEquals(new BigDecimalMatrix().identity(2), identity2x2);
    iterateAndAssertEquals(new BigDecimalMatrix().identity(3), identity3x3);
  }

  @Test
  void multiply() {
    // (2-by-2 identity) * (2-by-2 identity) = (2-by-2 identity)
    iterateAndAssertEquals(
        identity2x2.multiply(
            new BigDecimalMatrix(
                new BigDecimal[][] {
                  {ONE, ZERO},
                  {ZERO, ONE}
                })),
        identity2x2);

    // (2-by-2 identity) * (2-by-2 zero matrix) = (2-by-2 zero matrix)
    iterateAndAssertEquals(
        identity2x2.multiply(
            new BigDecimalMatrix(
                new BigDecimal[][] {
                  {ZERO, ZERO},
                  {ZERO, ZERO}
                })),
        zero2x2);
  }

  @Test
  void testMultiply() {
    iterateAndAssertEquals(AB, A.multiply(B));
    iterateAndAssertEquals(CD, C.multiply(D));
  }

  @Test
  void getCol() {
    assertArrayEquals(identity3x3.getCol(0), new BigDecimal[] {w("1"), w("0"), w("0")});
    assertArrayEquals(identity3x3.getCol(1), new BigDecimal[] {w("0"), w("1"), w("0")});
    assertArrayEquals(identity3x3.getCol(2), new BigDecimal[] {w("0"), w("0"), w("1")});
  }

  @Test
  void getRow() {
    assertArrayEquals(identity3x3.getRow(0), new BigDecimal[] {w("1"), w("0"), w("0")});
    assertArrayEquals(identity3x3.getRow(1), new BigDecimal[] {w("0"), w("1"), w("0")});
    assertArrayEquals(identity3x3.getRow(2), new BigDecimal[] {w("0"), w("0"), w("1")});
  }

  @Test
  void getEntry() {
    assertEquals(identity2x2.getEntry(0, 0), ONE);
    assertEquals(identity2x2.getEntry(0, 1), ZERO);
    assertEquals(identity2x2.getEntry(1, 0), ZERO);
    assertEquals(identity2x2.getEntry(1, 1), ONE);
  }

  @Test
  void dot() {}

  @Test
  void getNumRows() {}

  @Test
  void getNumCols() {}

  private static void iterateAndAssertEquals(
      RealMatrix<BigDecimal> firstMatrix, RealMatrix<BigDecimal> secondMatrix) {
    iterateAndDoSomething(firstMatrix.getContents(), secondMatrix.getContents(), true);
  }

  private static void iterateAndAssertCompareToZero(
      RealMatrix<BigDecimal> firstMatrix, RealMatrix<BigDecimal> secondMatrix) {
    iterateAndDoSomething(firstMatrix.getContents(), secondMatrix.getContents(), false);
  }

  /**
   * Test helper method. Iterates over each entry and compare the entries using {@link
   * Object#equals(Object)} if useEquals is true or {@link Comparable#compareTo(Object)} if false.
   * For any exception that may occur during iteration (like an NPE), this is just as bad as an
   * assertion failing. Therefore, it is not caught or otherwise handled.
   *
   * @param firstMatrix the first matrix to compare
   * @param secondMatrix the second matrix to compare
   * @param useEquals use {@link Object#equals(Object)} if true; else {@link
   *     Comparable#compareTo(Object)}
   */
  private static void iterateAndDoSomething(
      BigDecimal[][] firstMatrix, BigDecimal[][] secondMatrix, boolean useEquals) {
    assertEquals(firstMatrix.length, secondMatrix.length);
    for (int i = 0; i < firstMatrix.length; i++) {
      assertEquals(firstMatrix[i].length, secondMatrix[i].length);
      for (int j = 0; j < firstMatrix[0].length; j++) {
        if (useEquals) {
          assertEquals(firstMatrix[i][j], secondMatrix[i][j]);
        } else {
          assertEquals(firstMatrix[i][j].compareTo(secondMatrix[i][j]), 0);
        }
      }
    }
  }

  /**
   * A test helper method with a short name to make matrices visually easier to read.
   *
   * @param number the number to create a {@link BigDecimal} with
   * @return a new BigDecimal from the parsed string
   */
  private static BigDecimal w(String number) {
    return new BigDecimal(number);
  }
}
