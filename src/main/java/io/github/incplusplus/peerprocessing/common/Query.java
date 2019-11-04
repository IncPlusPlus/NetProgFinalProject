package io.github.incplusplus.peerprocessing.common;

import java.util.UUID;

public abstract class Query {
	private final UUID queryId = UUID.randomUUID();
	private boolean completed;
	private Throwable reasonIncomplete;
	private UUID requestingClientUUID;
	private UUID solvingSlaveUUID;
	
	public abstract String getResult();
	
	/**
	 * @return the string that can be acted upon to
	 * complete this query.
	 */
	public abstract String getQueryString();
	
	/**
	 * Perform whatever action will lead to the completed flag
	 * being switched to true.
	 */
	public abstract void complete();
	
	public UUID getQueryId() {
		return queryId;
	}
	
	public boolean isCompleted() {
		return completed;
	}
	
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public Throwable getReasonIncomplete() {
		return reasonIncomplete;
	}
	
	public void setReasonIncomplete(Throwable reasonIncomplete) {
		this.reasonIncomplete = reasonIncomplete;
	}
	
	public UUID getRequestingClientUUID() {
		return requestingClientUUID;
	}
	
	public void setRequestingClientUUID(UUID requestingClientUUID) {
		this.requestingClientUUID = requestingClientUUID;
	}
	
	public UUID getSolvingSlaveUUID() {
		return solvingSlaveUUID;
	}
	
	public void setSolvingSlaveUUID(UUID solvingSlaveUUID) {
		this.solvingSlaveUUID = solvingSlaveUUID;
	}
}
