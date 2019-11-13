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
	 * Sent from the client. Specifies client name.
	 * Response to {@link Demands#PROVIDE_CLIENT_NAME}
	 */
	@Deprecated
	CLIENT_NAME
}
