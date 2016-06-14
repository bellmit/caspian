#!/bin/bash

ACCOUNT_PORT=35359
ACCOUNT_HOST=account
DB_HOST=mysql
ID_HOST=keystone

#files and folders to map
WORKSPACE_HOME=/workspace/caspian
SOURCE_DIR=${WORKSPACE_HOME}/caspian-common-services/account/server
DEST_DIR=/workspace/vol-map/account.$$
DIR_LIST=" \
	/var/log/caspian \
	"
VOL_ARGS=""
for ITEM in ${DIR_LIST}
do
	mkdir -p ${DEST_DIR}${ITEM}
	VOL_ARGS="${VOL_ARGS} -v ${DEST_DIR}${ITEM}:${ITEM}"
	chmod -R 777 ${DEST_DIR}${ITEM}
done

#start container
DOCKER_OPTS=${DOCKER_OPTS:=--rm -it}
docker rm -f account
docker run ${DOCKER_OPTS} --name account --hostname ${ACCOUNT_HOST} \
	--expose ${ACCOUNT_PORT} -p ${ACCOUNT_PORT}:${ACCOUNT_PORT} \
	-e NO_OF_WORKERS=4 \
	${VOL_ARGS} \
	--link ${DB_HOST}:${DB_HOST} --link ${ID_HOST}:${ID_HOST} \
	caspian/ccs-account:latest

#---- clean-up ----
#rm -fr ${DEST_DIR}
