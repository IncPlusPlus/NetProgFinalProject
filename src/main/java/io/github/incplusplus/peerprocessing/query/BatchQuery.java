package io.github.incplusplus.peerprocessing.query;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.incplusplus.peerprocessing.query.matrix.MatrixQuery;

import java.util.stream.Stream;

/**
 * A class can implement BatchQuery in several situations. 1. The operation the class represents is
 * already a group of several, parallelizable operations. <br>
 * 2. The implementing class is just a dumb batch of unrelated Queries. <br>
 * 3. The implementing class is itself a {@linkplain Query} and could, depending on the specifics of
 * how the query might be executed, <b><i>sometimes <u>or</u> always</i></b> be able to be split
 * into multiple, parallelizable operations. <br>
 * <br>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.WRAPPER_ARRAY)
@JsonSubTypes({@JsonSubTypes.Type(MatrixQuery.class)})
public abstract class BatchQuery extends Query {
  private boolean isCompletedReturnedTrueAlready = false;
  /** @return all the queries that need to be executed from this batch */
  public abstract Query[] getQueries();
  protected boolean everythingCompleted = false;

  /**
   * Offer this BatchQuery a Query instance. If this BatchQuery is responsible for this Query, this
   * returns true and updates the internally-stored values to that of the specified argument.
   *
   * @param query the now-complete query that this BatchQuery might be responsible for
   * @return true if this is Responsible for the specified Query; else false
   */
  public abstract boolean offer(Query query);

  /** @return whether or not all of the queries managed by this class have been answered yet. */
  @Override
  public boolean isCompleted() {
    if (everythingCompleted)
      if (!isCompletedReturnedTrueAlready) {
        performCompletionAction();
        isCompletedReturnedTrueAlready = true;
      }
    return everythingCompleted;
  }

  /**
   * Subclasses can optionally override this method to be notified when their batch is complete.
   * That way, they can compile the information themselves when it's all available. This will be
   * triggered when {@link #isCompleted()} is called UNDER THE CONDITION THAT THE METHOD CALL
   * RETURNED TRUE. Additionally, the call to performCompletionAction() will only happen once.
   * Subsequent calls to {@link #isCompleted()} will not trigger this.
   */
  public void performCompletionAction() {
    // do nothing
  }
}
