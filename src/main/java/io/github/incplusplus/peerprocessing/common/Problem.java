package io.github.incplusplus.peerprocessing.common;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Serves as the holder of the bulk of information
 * regarding
 */
public class Problem {
	private String originalExpression;
	private boolean solved;
	private BigDecimal result;
	private UUID problemId;
	
	public String getOriginalExpression() {
		return originalExpression;
	}
	
	public void setOriginalExpression(String originalExpression) {
		this.originalExpression = originalExpression;
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
	
	public void setProblemId(UUID problemId) {
		this.problemId = problemId;
	}
}