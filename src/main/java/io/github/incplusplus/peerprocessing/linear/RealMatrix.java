package io.github.incplusplus.peerprocessing.linear;

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
}
