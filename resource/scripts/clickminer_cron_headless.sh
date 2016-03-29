#!/bin/bash
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TS=$(date +%s)

Xvfb :1 -screen 0 1024x768x24 &
SCREEN_PID=$!

export DISPLAY=:1 && $SCRIPTDIR/clickminer_automate.sh > $SCRIPTDIR/../log/cron_$TS.log 2>&1

kill -9 $SCREEN_PID &> /dev/null
