#!/bin/sh
export ZEROTIER_NETWORK_ID=9f77fc393e90f6ef
sudo update-ca-certificates -f
curl -s https://install.zerotier.com | sudo sh
zerotier-one -d
service zerotier-one restart
sudo zerotier-cli join $ZEROTIER_NETWORK_ID
echo sleeping for 30s
sleep 30s
sudo zerotier-cli listnetworks
echo running "$@"
sh "$@"
sudo zerotier-cli leave $ZEROTIER_NETWORK_ID
