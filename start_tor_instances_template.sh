#!/usr/bin/env bash
#rm -rf /tmp/tor
while :
do
    for i in {0..[[threadCount]]}
    do
        mkdir -p /tmp/tor/$socksport
        controlport=$((i + [[controlPortStart]]))
        socksport=$((i + [[controlPortEnd]]))
        tor --RunAsDaemon 0 --CookieAuthentication 0 --HashedControlPassword "" --ControlPort $controlport --SocksPort $socksport --DataDirectory  /tmp/tor/$socksport --PidFile /tmp/tor/$socksport/my.pid &
        sleep 0.3
    done
    sleep 5
done