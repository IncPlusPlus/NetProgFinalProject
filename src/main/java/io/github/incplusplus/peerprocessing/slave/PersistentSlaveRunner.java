package io.github.incplusplus.peerprocessing.slave;

import org.javatuples.Pair;

import java.io.IOException;
import java.util.Scanner;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.conditionallyPromptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.*;

/**
 * This class is just like {@linkplain} except is attempts to reconnect even if the server abandons
 * it.
 */
public class PersistentSlaveRunner {
  public static void main(String[] args) throws IOException {
    enable();
    Pair<String, Integer> hostAndPortPair = conditionallyPromptForHostPortTuple(args);
    Slave mainSlave = new Slave(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
    Thread mainSlaveThread = new Thread(() -> reInitConnection(mainSlave).run());
    mainSlaveThread.setName("Main PersistentSlave Thread");
    mainSlaveThread.setDaemon(true);
    mainSlaveThread.start();
    new Scanner(System.in).nextLine();
    mainSlave.close();
  }

  private static Runnable reInitConnection(Slave slaveToReset) {
    return () -> {
      Slave newSlave =
          new Slave(slaveToReset.getDestinationHostname(), slaveToReset.getDestinationPort());
      newSlave.setDisconnectCallback(reInitConnection(newSlave));
      boolean initSuccess = false;
      while (!initSuccess) {
        try {
          debug("PersistentSlave attempting to connect...");
          newSlave.begin();
          initSuccess = true;
        } catch (java.net.ConnectException e) {
          info(
              "PersistentSlave couldn't connect to "
                  + newSlave.getDestinationHostname()
                  + ":"
                  + newSlave.getDestinationPort());
          try {
            Thread.sleep(1000);
          } catch (InterruptedException ex) {
            error("reInitConnection callback was slaughtered in its sleep!");
            printStackTrace(ex);
          }
        } catch (IOException ioEx) {
          printStackTrace(ioEx);
          try {
            Thread.sleep(15000);
          } catch (InterruptedException ieEx) {
            printStackTrace(ieEx);
            error("reInitConnection callback was slaughtered in its sleep!");
          }
        }
      }
    };
  }
}
