package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    BigDecimal[][] out = new BigDecimal[m][n];
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
    for (int i = 0; i < n; i++) {
      out[i][i] = BigDecimal.ONE;
    }
    return new BigDecimalMatrix(out);
  }

  @Override
  public RealMatrix<BigDecimal> multiply(RealMatrix<BigDecimal> other) {
    if (this.getNumCols() != other.getNumRows())
      throw new IllegalArgumentException(
          "AB is not defined where A is "
              + getNumRows()
              + "x"
              + getNumCols()
              + " and B is "
              + other.getNumRows()
              + "x"
              + other.getNumCols());
    BigDecimal[][] result = initZero(new BigDecimal[this.getNumRows()][other.getNumCols()]);
    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < other.getNumCols(); j++) {
        result[i][j] = dot(getRow(i), other.getCol(j));
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
      for (int j = 0; j < numCols; j++) {
        out[i] = out[i].add(matrix[i][j].multiply(vector[j]));
      }
    }
    return out;
  }

  @Override
  public BigDecimal[] getCol(int colIndex) {
    // adapted from https://stackoverflow.com/a/30426991/1687436
    BigDecimal[] column = new BigDecimal[matrix.length];
    for (int i = 0; i < matrix.length; i++) {
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
    return zipped(rowVector, columnVector, BigDecimal::multiply)
        .reduce(BigDecimal::add)
        .orElseThrow(RuntimeException::new);
  }

  @Override
  public RealMatrix<BigDecimal> add(RealMatrix<BigDecimal> addend) {
    if (getNumRows() != addend.getNumRows() && getNumCols() != addend.getNumCols()) {
      throw new IllegalArgumentException(
          "A+B is not defined where A is "
              + getNumRows()
              + "x"
              + getNumCols()
              + " and B is "
              + addend.getNumRows()
              + "x"
              + addend.getNumCols());
    }
    BigDecimal[][] newMatrix = new BigDecimal[getNumRows()][getNumCols()];
    for (int i = 0; i < getNumRows(); i++) {
      for (int j = 0; j < getNumCols(); j++) {
        newMatrix[i][j] = getEntry(i, j).add(addend.getEntry(i, j));
      }
    }
    return new BigDecimalMatrix(newMatrix);
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

  // a private zipping method. https://stackoverflow.com/a/42787326/1687436
  private <A, B, C> Stream<C> zipped(A[] lista, B[] listb, BiFunction<A, B, C> zipper) {
    int shortestLength = Math.min(lista.length, listb.length);
    return IntStream.range(0, shortestLength).mapToObj(i -> zipper.apply(lista[i], listb[i]));
  }
}
