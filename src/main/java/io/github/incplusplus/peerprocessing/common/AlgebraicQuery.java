package io.github.incplusplus.peerprocessing.common;

import java.util.UUID;

public class AlgebraicQuery extends Query {
	
	private String queryString;
	
	@SuppressWarnings("unused")
	public AlgebraicQuery() {
		super();
	}
	
	public AlgebraicQuery(String originalExpression, UUID requestingClientUUID) {
		super();
		this.queryString = originalExpression;
		setRequestingClientUUID(requestingClientUUID);
	}
	
	/**
	 * To reduce the need for external libraries, the actual processing of the math query does
	 * not occur within this class. Instead, it is expected that it happens outside of the class
	 * and the result is set using {@link #setResult(String)}.
	 */
	@Override
	public void complete() {
		throw new IllegalStateException("AlgebraicQuery does not have a complete() implementation. See its JavaDoc");
	}
	
	/**
	 * @return the string that can be acted upon to
	 * complete this query.
	 */
	public String getQueryString() {
		return this.queryString;
	}
}
