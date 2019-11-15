package io.github.incplusplus.peerprocessing.query;

/**
 * This class serves no purpose other than to masquerade
 * as a Query. The job ingestion thread will ingest this
 * as q regular old Query, killing it instantly.
 */
public class PoisonPill extends Query {
	@Override
	public void complete() {
		throw new RuntimeException("PoisonPill's complete() method should never actually be run!");
	}
}
