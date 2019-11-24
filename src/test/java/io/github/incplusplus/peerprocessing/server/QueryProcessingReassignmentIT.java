package io.github.incplusplus.peerprocessing.server;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.concurrent.*;

import static io.github.incplusplus.peerprocessing.SingleSlaveIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.SingleSlaveIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.linear.BigDecimalMatrixTest.iterateAndAssertEquals;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.debug;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(value = 5, unit = TimeUnit.MINUTES)
class QueryProcessingReassignmentIT {
  private static int serverPort;
  private static final Server server = new Server();

  @BeforeAll
  static void setUp() throws IOException {
    serverPort = server.start(9999, VERBOSE_TEST_OUTPUT);
    //noinspection StatementWithEmptyBody
    while (!server.started()) {}
  }

  @AfterAll
  static void tearDown() throws IOException {
    server.stop();
  }

  @ParameterizedTest
  @MethodSource("io.github.incplusplus.peerprocessing.SingleSlaveIT#provideMatrices")
  void whenSlaveDisconnects_IfSlaveHeldJobs_ThenJobsGetReassigned(
      BigDecimalMatrix matrix1, BigDecimalMatrix matrix2)
      throws IOException, ExecutionException, InterruptedException {
    FutureTask<BigDecimalMatrix> task;
    try (Client myClient = new Client("localhost", serverPort)) {
      Slave mySlave = new Slave("localhost", serverPort);
      myClient.setVerbose(VERBOSE_TEST_OUTPUT);
      myClient.begin();
      mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
      mySlave.begin();
      task = myClient.multiply(matrix1, matrix2);
      //noinspection StatementWithEmptyBody
      while (!myClient.isPolite()) {}
      //noinspection StatementWithEmptyBody
      while (!mySlave.isPolite()) {}
      ExecutorService executor = Executors.newFixedThreadPool(2);
      executor.submit(task);
      // Wait until the server starts assigning the slave some jobs
      while (server.slaves.get(mySlave.getConnectionId()).getJobsResponsibleFor().size() < 5) {
        Thread.sleep(100);
      }
      synchronized (server.slaves) {
        assert server.slaves.get(mySlave.getConnectionId()).getJobsResponsibleFor().size() != 0;
        mySlave.disconnect();
      }
      //noinspection StatementWithEmptyBody
      while (!mySlave.isClosed()) {}
      //this is the source of all suffering
      Thread.sleep(100);
      Thread thread =
          new Thread(
              () -> {
                try {
                  task.get();
                  // we should never get here
                  assert false;
                } catch (InterruptedException ignored) {
                  // we intend to interrupt this thread
                  debug("Execution waiting thread interrupted as normal.");
                } catch (ExecutionException e) {
                  // fail
                  assert false;
                }
              });
      thread.start();
      // Let the current thread sleep (not the created thread!)
      Thread.sleep(5000);
      assertTrue(thread.isAlive());
      assertFalse(task.isDone());
      mySlave = new Slave("localhost", serverPort);
      mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
      thread.interrupt();
      mySlave.begin();
      //noinspection StatementWithEmptyBody
      while (!mySlave.isPolite()) {}
      task.get();
      executor.shutdown();
      mySlave.disconnect();
    }
  }
}
