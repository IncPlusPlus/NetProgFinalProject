package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.*;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.NormalIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests in this class are "bumpy" because they involve the server
 * starting up and shutting down between tests.
 */
@Timeout(value = 2,unit = TimeUnit.MINUTES)
class BumpyIT {
	private int serverPort;
	private final Server server = new Server();
	
	@BeforeEach
	void setUp() throws IOException {
		serverPort = server.start(INITIAL_SERVER_PORT, VERBOSE_TEST_OUTPUT);
		//noinspection StatementWithEmptyBody
		while (!server.started()) {}
	}
	
	@AfterEach
	void tearDown() throws IOException {
		server.stop();
	}
	
	@ParameterizedTest
	@CsvFileSource(resources = "/SimpleMathProblems.csv", numLinesToSkip = 1)
	void mathQuerySingleSlave(String input, String expected) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimal> task;
		try (Client myClient = new Client("localhost", serverPort);
		     Slave mySlave = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.begin();
			mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
			mySlave.begin();
			task = myClient.evaluateExpression(input);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(task);
			assertEquals(task.get().compareTo(new BigDecimal(expected)),0);
		}
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/SimpleMathProblems.csv", numLinesToSkip = 1)
	void mathQueryTwoSlaves(String input, String expected) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimal> task;
		try (Client myClient = new Client("localhost", serverPort);
			 Slave firstSlave = new Slave("localhost", serverPort);
			 Slave secondSlave = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.begin();
			firstSlave.setVerbose(VERBOSE_TEST_OUTPUT);
			secondSlave.setVerbose(VERBOSE_TEST_OUTPUT);
			firstSlave.begin();
			secondSlave.begin();
			task = myClient.evaluateExpression(input);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(task);
			assertEquals(task.get().compareTo(new BigDecimal(expected)),0);
		}
	}

	@ParameterizedTest
	@MethodSource("io.github.incplusplus.peerprocessing.NormalIT#provideMatrices")
	void matrixQuerySingleSlave(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimalMatrix> task;
		try (Client myClient = new Client("localhost", serverPort);
			 Slave mySlave = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.begin();
			mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
			mySlave.begin();
			task = myClient.multiply(matrix1,matrix2);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(task);
			iterateAndAssertEquals(task.get(),matrix1.multiply(matrix2));
		}
	}

	@ParameterizedTest
	@MethodSource("io.github.incplusplus.peerprocessing.NormalIT#provideMatrices")
	void matrixQueryManySlaves(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimalMatrix> task;
		try (Client myClient = new Client("localhost", serverPort);
			 Slave slave1 = new Slave("localhost", serverPort);
			 Slave slave2 = new Slave("localhost", serverPort);
			 Slave slave3 = new Slave("localhost", serverPort);
			 Slave slave4 = new Slave("localhost", serverPort);
			 Slave slave5 = new Slave("localhost", serverPort);
			 Slave slave6 = new Slave("localhost", serverPort);
			 Slave slave7 = new Slave("localhost", serverPort);
			 Slave slave8 = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.begin();
			slave1.setVerbose(VERBOSE_TEST_OUTPUT);
			slave1.begin();
			slave2.setVerbose(VERBOSE_TEST_OUTPUT);
			slave2.begin();
			slave3.setVerbose(VERBOSE_TEST_OUTPUT);
			slave3.begin();
			slave4.setVerbose(VERBOSE_TEST_OUTPUT);
			slave4.begin();
			slave5.setVerbose(VERBOSE_TEST_OUTPUT);
			slave5.begin();
			slave6.setVerbose(VERBOSE_TEST_OUTPUT);
			slave6.begin();
			slave7.setVerbose(VERBOSE_TEST_OUTPUT);
			slave7.begin();
			slave8.setVerbose(VERBOSE_TEST_OUTPUT);
			slave8.begin();
			task = myClient.multiply(matrix1,matrix2);
			ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(task);
			iterateAndAssertEquals(task.get(),matrix1.multiply(matrix2));
		}
	}
}