package io.github.incplusplus.peerprocessing.server;

import static io.github.incplusplus.peerprocessing.NormalIT.INITIAL_SERVER_PORT;
import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.slave.Slave;
import java.io.IOException;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ServerTest {
  private int serverPort;
  private Server server = new Server();

  @BeforeEach
  void setUp() throws IOException {
    serverPort = server.start(INITIAL_SERVER_PORT, VERBOSE_TEST_OUTPUT);
    // noinspection StatementWithEmptyBody
    while (!server.started()) {}
  }

  @AfterEach
  void tearDown() throws IOException {
    server.stop();
  }

  @Test
  void start() {}

  @Test
  void testStart() {}

  @Test
  void stop() {}

  @Test
  void shutdownInProgress() {}

  @Test
  void started() {}

  @Test
  void register() {}

  @Test
  void testRegister() {}

  @Test
  void deRegister() {}

  @Test
  void testDeRegister() {}

  @Test
  void testIfSlaveIsConnected() {
    assertTimeoutPreemptively(
        ofMinutes(1),
        () -> {
          final UUID slaveId;
          Slave mySlave = new Slave("localhost", serverPort);
          mySlave.begin();
          while (!mySlave.isPolite()) {}
          slaveId = mySlave.getConnectionId();
          assertTrue(server.isConnected(slaveId));
          mySlave.disconnect();
          // make sure the server eventually removes the client as well
          assertTimeoutPreemptively(
              ofSeconds(10),
              () -> {
                while (server.isConnected(slaveId)) {}
              });
        });
  }

  @Test
  void testIfClientIsConnected() {
    assertTimeoutPreemptively(
        ofMinutes(1),
        () -> {
          final UUID clientId;
          Client myClient = new Client("localhost", serverPort);
          myClient.begin();
          while (!myClient.isPolite()) {}
          clientId = myClient.getConnectionId();
          assertTrue(server.isConnected(clientId));
          myClient.disconnect();
          // make sure the server eventually removes the client as well
          assertTimeoutPreemptively(
              ofSeconds(10),
              () -> {
                while (server.isConnected(clientId)) {}
              });
        });
  }

  @Test
  void submitJob() {}

  @Test
  void removeJob() {}

  @Test
  void relayToAppropriateClient() {}
}
