package io.github.incplusplus.peerprocessing.linear;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * An immutable matrix containing entries that extend T.
 * Implementing classes define their own implementation of this class.
 *
 * @param <T> the data type of all entries
 */
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
	
	protected abstract T getEntry(int rowNum, int colNum);
	
	public abstract T[][] getContents();
}
