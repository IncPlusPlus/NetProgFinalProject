package io.github.incplusplus.peerprocessing.query.matrix;

public enum Operation {
	TRANSPOSE(1),
	ADD(1),
	SUBTRACT(1),
	MULTIPLY(2);
	
	private final int involvedMatrices;
	
	Operation(int involvedMatrices) {this.involvedMatrices = involvedMatrices;}
	
	/**
	 *
	 * @return the number of matrices this operation involves
	 */
	public int getInvolvedMatrices() {
		return involvedMatrices;
	}
}
