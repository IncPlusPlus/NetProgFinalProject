package io.github.incplusplus.peerprocessing.query;

public interface BatchQuery {
	Query[] getQueries();
	boolean allQueriesAnswered();
}
