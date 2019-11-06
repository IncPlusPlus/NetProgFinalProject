package io.github.incplusplus.peerprocessing.common;

public enum Responses implements Header {
	/**
	 * Sent from the client as a response to
	 * the {@link Demands#IDENTIFY} query.
	 */
	IDENTITY,
	/**
	 * The result of a {@link Demands#QUERY}
	 */
	RESULT,
	/**
	 * The solution to a particular query.
	 * This is the response to {@link Demands#SOLVE} regardless
	 * of whether or not a solution was found. See {@link Demands#SOLVE}
	 * for the data type restrictions.
	 * @deprecated in favor of {@link #RESULT}
	 */
	@Deprecated
	SOLUTION,
	/**
	 * Sent from the client. Specifies client name.
	 * Response to {@link Demands#PROVIDE_CLIENT_NAME}
	 */
	@Deprecated
	CLIENT_NAME
}
