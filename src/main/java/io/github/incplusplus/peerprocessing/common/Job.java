package io.github.incplusplus.peerprocessing.common;

import io.github.incplusplus.peerprocessing.server.JobState;

import java.util.UUID;

import static io.github.incplusplus.peerprocessing.server.JobState.WAITING_FOR_AVAILABLE_SLAVES;

public class Job {
	private final MathQuery mathQuery;
	private final UUID requestingClientUUID;
	private final UUID jobId;
	private UUID solvingSlaveUUID;
	private JobState jobState;
	
	public Job(MathQuery mathQuery, UUID requestingClientUUID) {
		this.mathQuery = mathQuery;
		this.requestingClientUUID = requestingClientUUID;
		this.jobState = WAITING_FOR_AVAILABLE_SLAVES;
		this.jobId = UUID.randomUUID();
	}
	
	public MathQuery getMathQuery() {
		return mathQuery;
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
	
	public JobState getJobState() {
		return jobState;
	}
	
	public void setJobState(JobState jobState) {
		this.jobState = jobState;
	}
}
