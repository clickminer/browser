#!/bin/bash

CLICKMINER_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $CLICKMINER_DIR/target
java -jar clickminer-browser-0.1-executable.jar $@