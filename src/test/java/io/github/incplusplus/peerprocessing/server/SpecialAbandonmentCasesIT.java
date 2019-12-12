package io.github.incplusplus.peerprocessing.server;

import static io.github.incplusplus.peerprocessing.NormalIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.incplusplus.peerprocessing.slave.Slave;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class SpecialAbandonmentCasesIT {

  /**
   * Sometimes, a client or slave will encounter a {@linkplain NullPointerException} in the thread
   * that communicates with the server. This happens when the method {@link
   * io.github.incplusplus.peerprocessing.common.MiscUtils#getHeader(String)} throws this exception.
   * An NPE gets thrown because the incoming line is terminated early resulting in a null line. If
   * this happens, it means the server has likely been violently shut down and the client should
   * gracefully disconnect.
   *
   * @throws IOException if the server fails to start
   */
  @Test
  void ensureClientsAbandonServerIfCutOffMidTransmission()
      throws IOException, InterruptedException {
    final Server server = new Server();
    int serverPort = server.start(INITIAL_SERVER_PORT, VERBOSE_TEST_OUTPUT);
    Slave slave = new Slave("localhost", serverPort);
    slave.setVerbose(VERBOSE_TEST_OUTPUT);
    //noinspection StatementWithEmptyBody
    while (!server.started()) {}
    slave.begin();
    //noinspection StatementWithEmptyBody
    while (!slave.isPolite() && !server.isConnected(slave.getConnectionId())) {}
    server.slaves.get(slave.getConnectionId()).getOutToClient().close();
    server.slay();
    // Give the slave a sec to finish disconnecting
    Thread.sleep(1500);
    assertTrue(slave.isClosed());
  }
}
