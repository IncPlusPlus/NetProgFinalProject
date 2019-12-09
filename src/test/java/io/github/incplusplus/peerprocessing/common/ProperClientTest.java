package io.github.incplusplus.peerprocessing.common;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;
import static org.junit.jupiter.api.Assertions.*;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.slave.Slave;
import org.junit.jupiter.api.Test;

class ProperClientTest {

  @Test
  void begin() {}

  @Test
  void isClosed() {}

  @Test
  void isPolite() {}

  @Test
  void setVerbose() {}

  @Test
  void getConnectionId() {}

  @Test
  void disconnect() {}

  @Test
  void getDestinationHostname() {
    int randInt = randInt(200, 9999);
    int randInt2 = randInt(200, 9999);
    ProperClient mySlave = new Slave("localhost", randInt);
    ProperClient myClient = new Client("localhost", randInt2);
    assertEquals("localhost", mySlave.getDestinationHostname());
    assertEquals("localhost", myClient.getDestinationHostname());
  }

  @Test
  void getDestinationPort() {
    int randInt = randInt(200, 9999);
    int randInt2 = randInt(200, 9999);
    ProperClient mySlave = new Slave("localhost", randInt);
    ProperClient myClient = new Client("localhost", randInt2);
    assertEquals(randInt, mySlave.getDestinationPort());
    assertEquals(randInt2, myClient.getDestinationPort());
  }
}
