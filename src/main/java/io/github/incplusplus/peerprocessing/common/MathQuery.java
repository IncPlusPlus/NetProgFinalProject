package io.github.incplusplus.peerprocessing.common;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Serves as the holder of the bulk of information
 * regarding
 */
public class MathQuery {
	private final String originalExpression;
	private final UUID problemId;
	private boolean solved;
	private BigDecimal result;
	private Throwable reasonUnsolved;
	
	public MathQuery(String originalExpression) {
		this.problemId = UUID.randomUUID();
		this.originalExpression = originalExpression;
	}
	
	public String getOriginalExpression() {
		return originalExpression;
	}
	
	public boolean isSolved() {
		return solved;
	}
	
	public void setSolved(boolean solved) {
		this.solved = solved;
	}
	
	public BigDecimal getResult() {
		return result;
	}
	
	public void setResult(BigDecimal result) {
		this.result = result;
	}
	
	public UUID getProblemId() {
		return problemId;
	}
	
	public Throwable getReasonUnsolved() {
		return reasonUnsolved;
	}
	
	public void setReasonUnsolved(Throwable reasonUnsolved) {
		this.reasonUnsolved = reasonUnsolved;
	}
}