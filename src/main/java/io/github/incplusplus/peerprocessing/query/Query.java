package io.github.incplusplus.peerprocessing.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.query.matrix.MatrixQuery;
import io.github.incplusplus.peerprocessing.server.QueryState;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
@JsonSubTypes({@JsonSubTypes.Type(AlgebraicQuery.class),@JsonSubTypes.Type(MatrixQuery.class),@JsonSubTypes.Type(VectorQuery.class),@JsonSubTypes.Type(BatchQuery.class)})
public abstract class Query {
	private final UUID queryId = UUID.randomUUID();
	private volatile boolean completed;
	private Throwable reasonIncomplete;
	private UUID requestingClientUUID;
	private UUID solvingSlaveUUID;
	private QueryState queryState;
	@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
	@JsonSubTypes({@JsonSubTypes.Type(BigDecimalMatrix.class),@JsonSubTypes.Type(MatrixQuery.class),@JsonSubTypes.Type(VectorQuery.class),@JsonSubTypes.Type(BatchQuery.class)})
	private Object result;
	
	protected Query() {
		this.queryState = QueryState.WAITING_FOR_AVAILABLE_SLAVES;
	}
	
	public QueryState getQueryState() {
		return queryState;
	}
	
	public void setQueryState(QueryState queryState) {
		this.queryState = queryState;
	}
	
	public Object getResult() {
		return this.result;
	}
	
	/**
	 * Perform whatever action will lead to the completed flag
	 * being switched to true.
	 */
	public abstract void complete();
	
	public UUID getQueryId() {
		return queryId;
	}
	
	/**
	 * Whether or not the request is completed.
	 * NOTE THAT THIS CAN MEAN THAT IT WAS COMPLETED EXCEPTIONALLY.
	 * CHECK THAT {@link #getReasonIncomplete()} IS NULL BEFORE
	 * ASSUMING SUCCESS!
	 *
	 * @return whether or not this request is complete
	 */
	public boolean isCompleted() {
		return completed;
	}
	
	public void setResult(Object result) {
		this.result = result;
	}
	
	public synchronized void setCompleted(boolean completed) {
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
