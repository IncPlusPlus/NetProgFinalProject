package io.github.incplusplus.peerprocessing.slave;

import org.javatuples.Pair;

import java.io.IOException;
import java.util.Scanner;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.enable;

/**
 * This class is just like {@linkplain} except is attempts to reconnect
 * even if the server abandons it.
 */
public class PersistentSlaveRunner {
  public static void main(String[] args) throws IOException {
    enable();
    Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
    Slave mainSlave = new Slave(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
    mainSlave.init();
    mainSlave.begin();
    new Scanner(System.in).nextLine();
    mainSlave.close();
  }
}
