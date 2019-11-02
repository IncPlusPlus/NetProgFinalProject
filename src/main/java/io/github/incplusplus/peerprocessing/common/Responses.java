package io.github.incplusplus.peerprocessing.common;

public enum Responses implements Header {
	/**
	 * Sent from the client as a response to
	 * the {@link Demands#IDENTIFY} query.
	 */
	IDENTITY,
	/**
	 * The solution to a particular query.
	 * This is the response to {@link Demands#SOLVE} regardless
	 * of whether or not a solution was found.
	 */
	SOLUTION,
	/**
	 * Sent from the client. Specifies client name.
	 * Response to {@link Demands#PROVIDE_CLIENT_NAME}
	 */
	@Deprecated
	CLIENT_NAME
}
