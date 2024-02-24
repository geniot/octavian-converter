#!/bin/bash
set -e
sudo systemctl stop octavian-converter.service
cp -f target/octavian-converter-*.jar /opt/octavian-converter/octavian-converter.jar
sudo systemctl start octavian-converter.service