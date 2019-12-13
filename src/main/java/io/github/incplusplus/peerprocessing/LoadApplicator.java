package io.github.incplusplus.peerprocessing;

import static io.github.incplusplus.peerprocessing.common.MiscUtils.randInt;
import static org.javatuples.Pair.with;

import io.github.incplusplus.peerprocessing.client.Client;
import io.github.incplusplus.peerprocessing.linear.BigDecimalMatrix;
import io.github.incplusplus.peerprocessing.server.Server;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;
import org.javatuples.Pair;

/** This class is merely a scratch-pad for throwing a load at a cluster of slaves. */
public class LoadApplicator {
  static Server server = new Server();
  private static int numMatrices;

  static {
    numMatrices = 25;
  }

  public static void main(String[] args)
      throws IOException, InterruptedException, ExecutionException {
    List<FutureTask<BigDecimalMatrix>> tasks = new LinkedList<>();
    int port = server.start(1234, true);
    Client client = new Client("localhost", port);
    client.begin();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    AtomicInteger currentMatrix = new AtomicInteger();
    provideMatrices(numMatrices)
        .forEach(
            pair -> {
              FutureTask<BigDecimalMatrix> task =
                  client.multiply(pair.getValue0(), pair.getValue1());
              executor.submit(task);
              tasks.add(task);
            });
    for (FutureTask<BigDecimalMatrix> i : tasks) {
      currentMatrix.getAndIncrement();
      System.out.println(
          "On matrix "
              + currentMatrix
              + "/"
              + numMatrices
              + " ("
              + currentMatrix.doubleValue() / numMatrices * 100
              + "%)");
      i.get();
    }
    executor.shutdown();
    server.stop();
  }

  private static List<Pair<BigDecimalMatrix, BigDecimalMatrix>> provideMatrices(int numMatrices) {
    int aRows = randInt(50, 100);
    int aColsAndBRows = randInt(50, 100);
    int bCols = randInt(50, 100);
    List<Pair<BigDecimalMatrix, BigDecimalMatrix>> matrixPairs = new ArrayList<>(numMatrices);
    for (int i = 0; i < numMatrices; i++) {
      matrixPairs.add(
          with(
              (BigDecimalMatrix) new BigDecimalMatrix().random(aRows, aColsAndBRows),
              (BigDecimalMatrix) new BigDecimalMatrix().random(aColsAndBRows, bCols)));
    }
    return matrixPairs;
  }
}
