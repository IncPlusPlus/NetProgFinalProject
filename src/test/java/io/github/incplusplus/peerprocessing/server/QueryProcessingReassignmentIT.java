package io.github.incplusplus.peerprocessing.server;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.slave.Slave;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
  @MethodSource("io.github.incplusplus.peerprocessing.NormalIT#provideMatrices")
  void whenSlaveDisconnects_IfSlaveHeldJobs_ThenJobsGetReassigned(
      BigDecimalMatrix matrix1, BigDecimalMatrix matrix2)
      throws IOException, ExecutionException, InterruptedException {
    FutureTask<BigDecimalMatrix> task;
    try (Client myClient = new Client("localhost", serverPort)) {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      SlaveFuzzer slaveFuzzer = new SlaveFuzzer("localhost", serverPort, 10);
      myClient.setVerbose(VERBOSE_TEST_OUTPUT);
      myClient.begin();
      slaveFuzzer.begin();
      task = myClient.multiply(matrix1, matrix2);
      //noinspection StatementWithEmptyBody
      while (!myClient.isPolite()) {}
      executor.submit(task);
      task.get();
      executor.shutdown();
      slaveFuzzer.stop();
    }
  }

  private class SlaveFuzzer {
    private final String hostname;
    private final int portNum;
    final List<Slave> slaveList = Collections.synchronizedList(new ArrayList<>());
    Thread randomSlaveSlayer;
    Thread randomSlaveCreator;

    SlaveFuzzer(String hostname, int portNum, int initNumSlaves) {
      this.hostname = hostname;
      this.portNum = portNum;
      for (int i = 0; i < initNumSlaves; i++) {
        slaveList.add(new Slave(hostname, portNum));
      }
      randomSlaveSlayer =
          new Thread(
              () -> {
                while (true) {
                  // If we've obviously overhunted, wait a while longer
                  if (slaveList.size() < initNumSlaves / 2) {
                    try {
                      Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                      break;
                      // we could get interrupted here. It's fine
                    }
                  }
                  int index = randInt(1, slaveList.size());
                  Slave sacrificialLamb = slaveList.get(index);
                  // if the slave to kill hasn't introduced itself yet
                  // go looking somewhere else
                  if (!sacrificialLamb.isPolite()) continue;
                  try {
                    synchronized (slaveList) {
                      slaveList.remove(sacrificialLamb);
                      sacrificialLamb.close();
                    }
                    // Don't be constantly killing slaves or nothing will ever get done
                    Thread.sleep(500);
                  } catch (IOException e) {
                    e.printStackTrace();
                  } catch (InterruptedException ignored) {
                    break;
                    // we expect to be interrupted. Don't panic
                  }
                }
              });
      randomSlaveSlayer.setDaemon(true);
      randomSlaveSlayer.setName("Random slave slayer");

      randomSlaveCreator =
          new Thread(
              () -> {
                while (true) {
                  Slave newSlave = new Slave(this.hostname, this.portNum);
                  slaveList.add(newSlave);
                  try {
                    newSlave.begin();
                    // don't expand the list infinitely
                    Thread.sleep(500);
                  } catch (IOException e) {
                    System.out.println(
                        "Encountered an error in SlaveFuzzer. "
                            + "This could be expected behavior. "
                            + "Message below:");
                    System.out.println(e.toString());
                  } catch (InterruptedException ignored) {
                    break;
                    // we expect to be interrupted. Don't panic
                  }
                }
              });
      randomSlaveCreator.setDaemon(true);
      randomSlaveCreator.setName("Random slave creator");
    }

    void begin() throws IOException {
      for (Slave i : slaveList) {
        i.begin();
      }
      randomSlaveCreator.start();
      randomSlaveSlayer.start();
    }

    void stop() throws IOException {
      randomSlaveCreator.interrupt();
      randomSlaveSlayer.interrupt();
      synchronized (slaveList) {
        for (Slave i : slaveList) {
          i.close();
        }
      }
    }
  }
}
