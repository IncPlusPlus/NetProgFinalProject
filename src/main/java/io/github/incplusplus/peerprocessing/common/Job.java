package io.github.incplusplus.peerprocessing.common;

import io.github.incplusplus.peerprocessing.server.QueryState;

import java.beans.ConstructorProperties;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.server.QueryState.WAITING_FOR_AVAILABLE_SLAVES;

public class Job {
	private final MathJob mathJob;
	private final UUID requestingClientUUID;
	private final UUID jobId;
	private UUID solvingSlaveUUID;
	private QueryState queryState;
	
	@ConstructorProperties({"mathJob", "requestingClientUUID"})
	public Job(MathJob mathJob, UUID requestingClientUUID) {
		this.mathJob = mathJob;
		this.requestingClientUUID = requestingClientUUID;
		this.queryState = WAITING_FOR_AVAILABLE_SLAVES;
		this.jobId = UUID.randomUUID();
	}
	
	public MathJob getMathJob() {
		return mathJob;
	}
	
	public UUID getRequestingClientUUID() {
		return requestingClientUUID;
	}
	
	public UUID getJobId() {
		return jobId;
	}
	
	public UUID getSolvingSlaveUUID() {
		return solvingSlaveUUID;
	}
	
	public void setSolvingSlaveUUID(UUID solvingSlaveUUID) {
		this.solvingSlaveUUID = solvingSlaveUUID;
	}
	
	public QueryState getQueryState() {
		return queryState;
	}
	
	public void setQueryState(QueryState queryState) {
		this.queryState = queryState;
	}
}
