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
	RESULT
}
