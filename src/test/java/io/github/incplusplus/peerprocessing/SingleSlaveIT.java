package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 2,unit = TimeUnit.MINUTES)
public class SingleSlaveIT {
	public static final boolean VERBOSE_TEST_OUTPUT = false;
	private static int serverPort;
	private static final Server server = new Server();
	
	@BeforeAll
	static void setUp() throws IOException {
		serverPort = server.start(0, VERBOSE_TEST_OUTPUT);
		//noinspection StatementWithEmptyBody
		while (!server.started()) {}
	}
	
	@AfterAll
	static void tearDown() throws IOException {
		server.stop();
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