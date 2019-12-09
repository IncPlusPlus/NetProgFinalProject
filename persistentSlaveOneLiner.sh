#!/bin/sh
docker build -t persistentslave:tag -f PersistentSlave.Dockerfile . && docker run --rm -it --cap-add=NET_ADMIN --cap-add=SYS_ADMIN --device=/dev/net/tun persistentslave:tag