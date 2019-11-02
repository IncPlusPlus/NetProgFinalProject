package io.github.incplusplus.peerprocessing.common;

public enum Demands {
	/**
	 * Sent from the server indicating the
	 * client should identify itself as one
	 * of the known {@link ClientType}s
	 */
	IDENTIFY,
	/**
	 * Sent from the server. Asks client for their name
	 */
	PROVIDE_CLIENT_NAME
}
