package io.github.incplusplus.peerprocessing.server;

/**
 * Represents the state of a {@link io.github.incplusplus.peerprocessing.common.Query}.
 */
public enum QueryState {
	/**
	 * This Job is in the queue waiting
	 * for an available slave to take it on
	 */
	WAITING_FOR_AVAILABLE_SLAVES,
	/**
	 * This Job is being solved by a slave
	 * and the server is waiting to hear back
	 * from that slave.
	 */
	WAITING_ON_SLAVE,
	/**
	 * This Job is complete and the answer
	 * is being sent back to the client
	 * that enqueued it.
	 */
	SENDING_TO_CLIENT,
	/**
	 * This Job has been completed and
	 * the result sent back to the client.
	 */
	COMPLETE
}
