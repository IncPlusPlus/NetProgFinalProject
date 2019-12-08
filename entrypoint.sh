#!/bin/sh
export ZEROTIER_NETWORK_ID=9f77fc393e90f6ef

curl -s https://install.zerotier.com | sudo sh
sudo zerotier-cli join $ZEROTIER_NETWORK_ID
sudo zerotier-cli listnetworks
echo running "$@"
sh "$@"
sudo zerotier-cli leave $ZEROTIER_NETWORK_ID
