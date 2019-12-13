package io.github.incplusplus.peerprocessing.client;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.incplusplus.peerprocessing.server.Server;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class ClientTest {

  @Test
  void failIfNoServer() {
    Client myClient = new Client("localhost", 9999);
    assertThrows(java.net.ConnectException.class, myClient::begin);
  }

  @Test
  void ensureWarningBranchCovered() throws IOException {
    Server server = new Server();
    int port = server.start(0, VERBOSE_TEST_OUTPUT);
    Client myClient = new Client("localhost", port);
    myClient.begin();
    //noinspection StatementWithEmptyBody
    while (!myClient.isPolite()) {}
    myClient.close();
    // close again to make sure the other branch is covered
    myClient.close();
  }
}
