#!/usr/bin/env bash
./compile.sh
java -cp target/classes/:lib/* com.circles.rippers.TorRange netherlands.ini
