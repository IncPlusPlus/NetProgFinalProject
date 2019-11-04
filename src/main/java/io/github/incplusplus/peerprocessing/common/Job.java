package io.github.incplusplus.peerprocessing.common;

import io.github.incplusplus.peerprocessing.server.JobState;

import java.beans.ConstructorProperties;
import java.util.UUID;

import static io.github.incplusplus.peerprocessing.server.JobState.WAITING_FOR_AVAILABLE_SLAVES;

public class Job {
	private final MathJob mathJob;
	private final UUID requestingClientUUID;
	private final UUID jobId;
	private UUID solvingSlaveUUID;
	private JobState jobState;
	
	@ConstructorProperties({"mathJob", "requestingClientUUID"})
	public Job(MathJob mathJob, UUID requestingClientUUID) {
		this.mathJob = mathJob;
		this.requestingClientUUID = requestingClientUUID;
		this.jobState = WAITING_FOR_AVAILABLE_SLAVES;
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
	
	public JobState getJobState() {
		return jobState;
	}
	
	public void setJobState(JobState jobState) {
		this.jobState = jobState;
	}
}
