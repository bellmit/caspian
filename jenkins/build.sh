#!/bin/sh

DOCKER_RUN=${DOCKER_RUN:-${WORKSPACE}/jenkins/docker-run}
DOCKER_IMAGE="${DOCKER_IMAGE:-nile-registry.lss.emc.com:5000/emccaspian/devkit-sles12:1.0.0.0.26}"

set -ex

# Docker container name/args
export NAME=${JOB_NAME##*/}
export ARGS="--rm -v /var/run/docker.sock:/var/run/docker.sock ${ARGS}"

cd ${WORKSPACE}
${DOCKER_RUN} ${DOCKER_IMAGE} "./jenkins/build-in-docker.sh"

