#!/bin/sh
./mvnw -q compile exec:java -Dexec.mainClass="io.github.incplusplus.peerprocessing.slave.PersistentSlaveRunner" -Dexec.args="$*" -Dexec.cleanupDaemonThreads=false