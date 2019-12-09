package io.github.incplusplus.peerprocessing.linear;

import static io.github.incplusplus.peerprocessing.NormalIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class SmallBigDecimalMatrixCase {
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

  @ParameterizedTest
  @MethodSource(
      "io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest#provideSmallMatrices")
  void matrixQuerySingleSlave(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2)
      throws IOException, ExecutionException, InterruptedException {
    FutureTask<BigDecimalMatrix> task;
    try (Client myClient = new Client("localhost", serverPort);
        Slave mySlave = new Slave("localhost", serverPort)) {
      myClient.setVerbose(VERBOSE_TEST_OUTPUT);
      myClient.begin();
      mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
      mySlave.begin();
      task = myClient.multiply(matrix1, matrix2);
      ExecutorService executor = Executors.newSingleThreadExecutor();
      executor.submit(task);
      iterateAndAssertEquals(task.get(), matrix1.multiply(matrix2));
    }
  }
}
