package io.github.incplusplus.peerprocessing.slave;

import io.github.incplusplus.peerprocessing.server.Server;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

@Timeout(10)
class SlaveTest {
  private volatile AtomicBoolean slaveCallbackRan = new AtomicBoolean();

  @Test
  void failIfNoServer() {
    Slave mySlave = new Slave("localhost", 9999);
    assertThrows(java.net.ConnectException.class, mySlave::begin);
  }

  @Test
  void testCallbackAction() throws IOException, InterruptedException {
    Server server = new Server();
    int serverPort = server.start(0, false);
    Slave slave = new Slave("localhost", serverPort);
    slave.setDisconnectCallback(() -> slaveCallbackRan.compareAndSet(false, true));
    slave.begin();
    //noinspection StatementWithEmptyBody
    while (!slave.isPolite()) {}
    assertFalse(slaveCallbackRan.get());
    server.stop();
    //noinspection StatementWithEmptyBody
    while (!slave.isClosed()) {}
    //noinspection StatementWithEmptyBody
    while (slave.isDisconnectCallbackAlive()) {}
    //I don't know why this takes so long to update on Travis but this hack fixes it
    while(!slaveCallbackRan.get()) {
      Thread.sleep(50);
    }
    assertTrue(slaveCallbackRan.get());
  }
}
