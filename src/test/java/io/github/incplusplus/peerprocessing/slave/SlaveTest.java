package io.github.incplusplus.peerprocessing.slave;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.enable;
import static io.github.incplusplus.peerprocessing.slave.PersistentSlaveRunner.reInitConnection;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.incplusplus.peerprocessing.server.Server;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
class SlaveTest {
  private volatile AtomicBoolean slaveCallbackRan = new AtomicBoolean();

  @Test
  void failIfNoServer() {
    Slave mySlave = new Slave("localhost", 9999);
    assertThrows(java.net.ConnectException.class, mySlave::begin);
  }

  @Test
  void dontUseNullReadersAndWriters() throws IOException, InterruptedException {
    if (VERBOSE_TEST_OUTPUT) enable();
    Slave slave = new Slave("localhost", 9999);
    Thread mainSlaveThread = new Thread(() -> reInitConnection(slave).run());
    mainSlaveThread.setName("Testing PersistentSlave Thread");
    mainSlaveThread.setDaemon(true);
    mainSlaveThread.start();
    Thread.sleep(5000);
    slave.close();
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
    // I don't know why this takes so long to update on Travis but this hack fixes it
    while (!slaveCallbackRan.get()) {
      Thread.sleep(50);
    }
    assertTrue(slaveCallbackRan.get());
  }
}
