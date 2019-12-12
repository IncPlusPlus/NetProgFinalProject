package io.github.incplusplus.peerprocessing.slave;

import static io.github.incplusplus.peerprocessing.NormalIT.VERBOSE_TEST_OUTPUT;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.enable;
import static io.github.incplusplus.peerprocessing.slave.PersistentSlaveRunner.reInitConnection;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import io.github.incplusplus.peerprocessing.query.Query;
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

  /**
   * If the unthinkable happens and a {@link JsonProcessingException} gets thrown when the Slave
   * sends a {@link Query} back to the server, ensure the slave slays itself.
   *
   * @throws IOException if something even dumber happens
   */
  @Test
  void dieIfCantSendAnswer() throws IOException {
    // I would use a mock() here but we need to configure the ObjectMapper
    // instance. Also, it's not just used in this method so it needs to work
    // without interfering with its other uses.
    ObjectMapper mapper =
        spy(
            new ObjectMapper()
                .activateDefaultTyping(BasicPolymorphicTypeValidator.builder().build()));
    Server server = new Server();
    int port = server.start(0, VERBOSE_TEST_OUTPUT);
    Slave mySlave = new Slave("localhost", port, mapper);
    mySlave.setVerbose(VERBOSE_TEST_OUTPUT);
    mySlave.begin();
    //noinspection StatementWithEmptyBody
    while (!mySlave.isPolite()) {}
    when(mapper.writeValueAsString(any()))
        .thenThrow(
            new JsonProcessingException("This exception is expected as a test. Leave it be.") {});
    mySlave.sendEvaluatedQuery(new DisallowedQueryExtension());
    assertTrue(mySlave.isClosed());
    server.stop();
  }

  static final class DisallowedQueryExtension extends Query {
    private DisallowedQueryExtension() {}

    public void complete() {
      throw new RuntimeException();
    }
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
