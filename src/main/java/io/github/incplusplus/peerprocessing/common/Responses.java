package io.github.incplusplus.peerprocessing.common;

public enum Responses {
	/**
	 * Sent from the client as a response to
	 * the {@link Demands#IDENTIFY} query.
	 */
	IDENTITY,
	/**
	 * Sent from the client. Specifies client name.
	 * Response to {@link Demands#PROVIDE_CLIENT_NAME}
	 */
	CLIENT_NAME
}
