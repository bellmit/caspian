#!/bin/bash

ADMIN_PORT=35357
PUBLIC_PORT=6100
OS_TOKEN=123456
KEYSTONE_HOST=keystone
MYSQL_HOST=mysql

#---- ensure all source/default config files are available ----
WORKSPACE_HOME=/workspace/caspian
SOURCE_DIR=${WORKSPACE_HOME}/caspian-common-services/identity/src/main/install
DEST_DIR=/workspace/vol-map/keystone.$$
FILE_LIST=""
DIR_LIST=""
VOL_ARGS=""
for ITEM in ${FILE_LIST}
do
	mkdir -p ${DEST_DIR}/`dirname ${ITEM}`
	cp ${SOURCE_DIR}${ITEM} ${DEST_DIR}${ITEM}
	VOL_ARGS="${VOL_ARGS} -v ${DEST_DIR}${ITEM}:${ITEM}"
done
for ITEM in ${DIR_LIST}
do
	mkdir -p ${DEST_DIR}${ITEM}
	VOL_ARGS="${VOL_ARGS} -v ${DEST_DIR}${ITEM}:${ITEM}"
	chown keystone:keystone ${DEST_DIR}${ITEM}
        chmod -R 777 ${DEST_DIR}${ITEM}
done

#---- start the docker container ----
DOCKER_OPTS=${DOCKER_OPTS:=--rm -it}
docker rm -f keystone 2>&1 > /dev/null
docker run ${DOCKER_OPTS} --name keystone --hostname ${KEYSTONE_HOST} \
	-p ${ADMIN_PORT}:${ADMIN_PORT} -p ${PUBLIC_PORT}:${PUBLIC_PORT} ${VOL_ARGS} \
	--link ${MYSQL_HOST}:${MYSQL_HOST} caspian/ccs-keystone:latest

#---- clean-up ----
#rm -fr ${DEST_DIR}
