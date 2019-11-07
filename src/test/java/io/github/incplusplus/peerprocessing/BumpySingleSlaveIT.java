package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests in this class are "bumpy" because they involve the server
 * starting up and shutting down between tests.
 */
class BumpySingleSlaveIT {
	private int serverPort;
	
	@BeforeEach
	void setUp() throws IOException {
		serverPort = Server.start(0, VERBOSE_TEST_OUTPUT);
		while (!Server.started()) {}
	}
	
	@AfterEach
	void tearDown() throws IOException {
		Server.stop();
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/SimpleMathProblems.csv", numLinesToSkip = 1)
	void singleMathQuerySingleSlave(String input, String expected) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimal> task;
		try (Client myClient = new Client("localhost", serverPort);
		     Slave mySlave = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.init();
			myClient.begin();
			mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
			mySlave.init();
			mySlave.begin();
			task = myClient.evaluateExpression(input);
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.submit(task);
			assertEquals(task.get().compareTo(new BigDecimal(expected)),0);
		}
	}
}
