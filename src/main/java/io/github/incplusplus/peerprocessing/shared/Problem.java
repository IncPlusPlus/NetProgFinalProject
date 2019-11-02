package io.github.incplusplus.peerprocessing.shared;

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
	private UUID id;
	
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
	
	public UUID getId() {
		return id;
	}
	
	public void setId(UUID id) {
		this.id = id;
	}
}