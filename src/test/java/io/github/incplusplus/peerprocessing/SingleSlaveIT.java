package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.stream.Stream;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

//@Timeout(value = 5,unit = TimeUnit.MINUTES)
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

	@ParameterizedTest
	@MethodSource("provideMatrices")
	void matrixQuerySingleSlave(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) throws IOException, ExecutionException, InterruptedException {
		FutureTask<BigDecimalMatrix> task;
		try (Client myClient = new Client("localhost", serverPort);
			 Slave mySlave = new Slave("localhost", serverPort)) {
			myClient.setVerbose(VERBOSE_TEST_OUTPUT);
			myClient.init();
			myClient.begin();
			mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
			mySlave.init();
			mySlave.begin();
			task = myClient.multiply(matrix1,matrix2);
			ExecutorService executor = Executors.newFixedThreadPool(2);
			executor.submit(task);
			iterateAndAssertEquals(task.get(),matrix1.multiply(matrix2));
		}
	}
}