package io.github.incplusplus.peerprocessing.common;

public class MathQuery extends Query {
	private String originalExpression;
	private String result;
	
	@Override
	public String getResult() {
		return result;
	}
	
	public void setOriginalExpression(String expression) {
		this.originalExpression = expression;
	}
	
	public String getQueryString() {
		return originalExpression;
	}
	
	/**
	 * To reduce the need for external libraries, the actual processing of the math query does
	 * not occur within this class. Instead, it is expected that it happens outside of the class
	 * and the result is set using {@link #setResult(String)}.
	 */
	@Override
	public void complete() {
		throw new IllegalStateException("MathQuery does not have a complete() implementation. See its JavaDoc");
	}
	
	public void setResult(String result) {
		this.result = result;
	}
}
