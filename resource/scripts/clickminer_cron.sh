#!/bin/bash
SCRIPTDIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
TS=$(date +%s)
export DISPLAY=:0 && $SCRIPTDIR/clickminer_automate.sh > $SCRIPTDIR/../log/cron_$TS.log 2>&1
