package io.github.incplusplus.peerprocessing.slave;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.promptForHostPortTuple;
import static io.github.incplusplus.peerprocessing.logger.StupidSimpleLogger.enable;

import java.io.IOException;
import java.util.Scanner;
import org.javatuples.Pair;

class SlaveRunner {
  public static void main(String[] args) throws IOException {
    enable();
    Pair<String, Integer> hostAndPortPair = promptForHostPortTuple();
    Slave mainSlave = new Slave(hostAndPortPair.getValue0(), hostAndPortPair.getValue1());
    mainSlave.begin();
    new Scanner(System.in).nextLine();
    mainSlave.close();
  }
}
