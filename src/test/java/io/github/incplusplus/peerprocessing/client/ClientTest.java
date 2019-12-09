package io.github.incplusplus.peerprocessing.client;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ClientTest {

  @Test()
  void failIfNoServer() {
    Client myClient = new Client("localhost", 9999);
    assertThrows(java.net.ConnectException.class, myClient::begin);
  }
}
