package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Stream;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Timeout(value = 5,unit = TimeUnit.MINUTES)
public class NormalIT {
	/**
	 * This should always stay at zero. When at zero,
	 * the server will use whatever port is available.
	 *
	 * For local testing, you can make this nonzero and have
	 * {@linkplain io.github.incplusplus.peerprocessing.slave.PersistentSlaveRunner}s
	 * to help when you're running the tests.
	 */
	public static final int INITIAL_SERVER_PORT = 0;
	public static final boolean VERBOSE_TEST_OUTPUT = false;
	private static int serverPort;
	private static final Server server = new Server();
	
	@BeforeAll
	static void setUp() throws IOException {
		serverPort = server.start(INITIAL_SERVER_PORT, VERBOSE_TEST_OUTPUT);
		//noinspection StatementWithEmptyBody
		while (!server.started()) {}
	}
	
	@AfterAll
	static void tearDown() throws IOException {
		server.stop();
	}

	private static Stream<Arguments> provideMatrices() {
		int numMatrices = 10;
		int aRows = randInt(50, 100);
		int aColsAndBRows = randInt(50, 100);
		int bCols = randInt(50, 100);
		List<Arguments> matrixPairs = new ArrayList<>(numMatrices);
		for(int i = 0; i < numMatrices; i++){
			matrixPairs.add(Arguments.of(new BigDecimalMatrix()
							.random(aRows, aColsAndBRows),
					new BigDecimalMatrix().random(aColsAndBRows, bCols)));
		}
		return matrixPairs.stream();
	}

	@ParameterizedTest
	@CsvFileSource(resources = "/SimpleMathProblems.csv", numLinesToSkip = 1)
	void singleMathQuerySingleSlave(String input, String expected) throws IOException, ExecutionException, InterruptedException {
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
	void singleMathQueryTwoSlaves(String input, String expected) throws IOException, ExecutionException, InterruptedException {
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
	@MethodSource("provideMatrices")
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
	@MethodSource("provideMatrices")
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