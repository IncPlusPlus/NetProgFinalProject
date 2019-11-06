package io.github.incplusplus.peerprocessing.common;

public class MathQuery extends Query {
	public void setOriginalExpression(String expression) {
		setQueryString(expression);
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
}
