#!/bin/sh

WORKING_DIR=/working
export GRADLE_USER_HOME=${WORKING_DIR}/.gradle

GRADLE="${WORKING_DIR}/gradlew"

# Load the build environment
ENVFILE="${WORKING_DIR}/jenkins/build.env"
if [ -f "${ENVFILE}" ]; then
    source "${ENVFILE}"
fi

# Default tasks/switches for jenkins build
DEFAULT_TASKS="clean build"
DEFAULT_SWITCHES="--info --no-color"

GRADLE_TASKS=${GRADLE_TASKS:-${DEFAULT_TASKS} ${EXTRA_GRADLE_TASKS}}
GRADLE_SWITCHES=${GRADLE_SWITCHES:-${DEFAULT_SWITCHES} ${EXTRA_GRADLE_SWITCHES}}

set -ex
cd ${WORKING_DIR}
${GRADLE} ${GRADLE_SWITCHES} ${GRADLE_TASKS}

