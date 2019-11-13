package io.github.incplusplus.peerprocessing.linear;

public abstract class Matrix<T> {
	/**
	 * @return the number of rows in this matrix
	 */
	public abstract int getNumRows();
	
	/**
	 * @return the number of columns in this matrix
	 */
	public abstract int getNumCols();
	
	public boolean isSquare() {
		return this.getNumRows() == this.getNumCols();
	}
}
