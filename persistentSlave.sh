#!/usr/bin/env bash
./mvnw -q compile exec:java -Dexec.mainClass="io.github.incplusplus.peerprocessing.slave.PersistentSlaveRunner" -Dexec.cleanupDaemonThreads=false