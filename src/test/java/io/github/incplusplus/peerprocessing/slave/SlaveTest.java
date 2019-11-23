package io.github.incplusplus.peerprocessing.slave;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlaveTest {

  @Test
  void failIfNoServer() {
    Slave mySlave = new Slave("localhost", 9999);
    assertThrows(java.net.ConnectException.class, mySlave::begin);
  }
}
