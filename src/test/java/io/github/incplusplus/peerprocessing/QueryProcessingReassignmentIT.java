package io.github.incplusplus.peerprocessing;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;

public class QueryProcessingReassignmentIT {
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
  @MethodSource("io.github.incplusplus.peerprocessing.SingleSlaveIT#provideMatrices")
  void whenSlaveDisconnects_IfSlaveHeldJobs_ThenJobsGetReassigned(BigDecimalMatrix matrix1, BigDecimalMatrix matrix2) throws IOException, ExecutionException, InterruptedException {
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
//      task.get();
    }
  }
}
