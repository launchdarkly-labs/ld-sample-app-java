#!/bin/bash

source .env
echo "const clientKey = \"$LD_CLIENT_KEY\";" > ./src/main/resources/static/js/keys.js
mvn package
