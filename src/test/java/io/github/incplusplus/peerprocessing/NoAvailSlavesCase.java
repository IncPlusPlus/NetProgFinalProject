package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Make sure the server doesn't try to do anything
 * funny if there are no slaves available to process a request.
 */
class NoAvailSlavesCase {
	private static int serverPort;
	private static final Server server = new Server();
	private final List<Future<?>> futureList = new ArrayList<>();
	private static ExecutorService executor;
	
	@BeforeAll
	static void setUp() throws IOException {
		serverPort = server.start(0, VERBOSE_TEST_OUTPUT);
		// noinspection StatementWithEmptyBody
		while (!server.started()) {}
		executor = Executors.newFixedThreadPool(20);
	}
	
	@AfterAll
	static void tearDown() throws IOException {
		server.stop();
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/SimpleMathProblems.csv", numLinesToSkip = 1)
	void singleMathQuerySingleSlave(String input,
	                                String expected) throws IOException {
		FutureTask<BigDecimal> task;
		try (Client myClient = new Client("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.init();
			myClient.begin();
			//noinspection StatementWithEmptyBody
			while (!myClient.isPolite()){}
			task = myClient.evaluateExpression(input);
			Future<?> nullFuture = executor.submit(task);
			futureList.add(nullFuture);
			assertFalse(nullFuture.isDone());
			//Also make sure none of the other futures are done yet
			futureList.stream().map(Future::isDone).forEach(Assertions::assertFalse);
		}
	}
}
