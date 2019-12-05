#!/bin/sh
docker build -t persistentslave:tag -f PersistentSlave.Dockerfile . && docker run --rm -it persistentslave:tag