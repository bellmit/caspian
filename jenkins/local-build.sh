#!/bin/sh

set -ex

DIR="$( cd "$( dirname "$0" )" && pwd )"

export WORKSPACE="$( cd $DIR/.. && pwd )"
export JOB_NAME=caspian-common-services
${DIR}/build.sh

