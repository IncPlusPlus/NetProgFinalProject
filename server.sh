#!/bin/sh
./mvnw -q compile exec:java -Dexec.mainClass="io.github.incplusplus.peerprocessing.server.ServerRunner" -Dexec.cleanupDaemonThreads=false