#!/usr/bin/env bash
#rm -rf /tmp/tor
while :
do
    for i in {0..55}
    do
        mkdir -p /tmp/tor/$socksport
        controlport=$((i + 10000))
        socksport=$((i + 20000))
        tor --RunAsDaemon 0 --CookieAuthentication 0 --NewCircuitPeriod 3000  --ControlPort $controlport --SocksPort $socksport --DataDirectory  /tmp/tor/$socksport --PidFile /tmp/tor/$socksport/my.pid &
        sleep 0.3
    done
    sleep 5
done
