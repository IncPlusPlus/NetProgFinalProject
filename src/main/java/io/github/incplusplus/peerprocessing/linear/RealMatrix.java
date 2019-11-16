package io.github.incplusplus.peerprocessing.linear;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.javatuples.Quartet;

import java.math.BigDecimal;
import java.util.List;

/**
 * An immutable matrix containing entries that extend T.
 * Implementing classes define their own implementation of this class.
 *
 * @param <T> the data type of all entries
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
@JsonSubTypes({@JsonSubTypes.Type(BigDecimalMatrix.class)})
public abstract class RealMatrix<T> extends Matrix<T> {
	
	/**
	 * @param m the number of rows
	 * @param n the number of columns
	 * @return an m-by-n matrix with random values
	 */
	public abstract RealMatrix<T> random(int m, int n);
	
	/**
	 * @param n the number of rows and columns
	 * @return an n-by-n identity matrix I
	 */
	public abstract RealMatrix<T> identity(int n);
	
	/**
	 * @param other the second matrix in this operation
	 * @return this matrix multiplied by the specified matrix
	 */
	public abstract RealMatrix<T> multiply(RealMatrix<T> other);

	/**
	 * Get the vectors required to compute a matrix multiplication operation
	 * this * other.
	 * @param other the matrix to multiply this matrix by
	 * @return a Quartet where the first BigDecimal[] is the first vector and
	 * the second BigDecimal[] is the second vector. The third item and fourth items
	 * (Integers) are the row and column index respectively that the result of this operation
	 * would be placed in when creating the product matrix.
	 */
	public abstract List<Quartet<BigDecimal[], BigDecimal[], Integer, Integer>> getVectorsForMultiplyingWith(RealMatrix<T> other);
	
	public abstract T[] multiply(T[] vector);
	
	/**
	 * @param colIndex the index of the desired column
	 * @return the column at the specified index
	 */
	public abstract T[] getCol(int colIndex);
	
	/**
	 * @param rowIndex the index of the desired row
	 * @return the row at the specified index
	 */
	public abstract T[] getRow(int rowIndex);
	
	/**
	 * The fact that this is an instance method and not a static method
	 * is a byproduct of the desire to keep the generic abstract in place.
	 * It doesn't matter what RealMatrix instance you use this method on.
	 * The only things that are relevant to this operation are the two parameters.
	 *
	 * @param rowVector    the row vector to dot with the column vector
	 * @param columnVector the column vector to dot with the row vector
	 * @return the dot product of the specified vectors
	 */
	public abstract T dot(T[] rowVector, T[] columnVector);
	
	/**
	 * Add this matrix and the other specified matrix.
	 * @param addend the matrix to sum with this matrix
	 * @return the sum of this matrix and the specified addend
	 */
	public abstract RealMatrix<T> add(RealMatrix<T> addend);
	
	protected abstract T getEntry(int rowNum, int colNum);
	
	public abstract T[][] getMatrix();
}
