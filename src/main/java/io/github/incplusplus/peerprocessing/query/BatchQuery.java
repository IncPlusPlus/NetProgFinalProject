package io.github.incplusplus.peerprocessing.query;

import java.util.stream.Stream;

/**
 * A class can implement BatchQuery in several situations.
 * 1. The operation the class represents is already a group of several, parallelizable operations.
 * <br>
 * 2. The implementing class is just a dumb batch of unrelated Queries.
 * <br>
 * 3. The implementing class is itself a {@linkplain Query} and could,
 * depending on the specifics of how the query might be executed, <b><i>sometimes <u>or</u> always</i></b>
 * be able to be split into multiple, parallelizable operations.
 * <br><br>
 */
public abstract class BatchQuery extends Query {
	/**
	 *
	 * @return all the queries that need to be executed from this batch
	 */
	public abstract Query[] getQueries();
	
	/**
	 * Offer this BatchQuery a Query instance. If this BatchQuery is responsible for this
	 * Query, this returns true and updates the internally-stored values to that of the
	 * specified argument.
	 * @param query the now-complete query that this BatchQuery might be responsible for
	 * @return true if this is Responsible for the specified Query; else false
	 */
	public abstract boolean offer(Query query);
	
	/**
	 *
	 * @return whether or not all of the queries managed by this class have been answered yet.
	 */
	@Override
	public boolean isCompleted(){
		return Stream.of(getQueries()).allMatch(Query::isCompleted);
	}
}
