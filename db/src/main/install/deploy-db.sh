#!/bin/bash

MYSQL_HOST=mysql
MYSQL_PORT=3306

WORKSPACE_HOME=/workspace/caspian
SOURCE_DIR=${WORKSPACE_HOME}/caspian-common-services/db/src/main/install
DEST_DIR=/workspace/vol-map/mysql.$$
FILE_LIST=""
DIR_LIST=" \
	/data/db \
	/var/log/mysql \
	"

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
	chmod -R 777 ${DEST_DIR}${ITEM}
done

chmod -R 777 ${DEST_DIR}/data/db

DOCKER_OPTS=${DOCKER_OPTS:=--rm -it}
docker rm -f mysql 2>&1 > /dev/null
docker run ${DOCKER_OPTS} --name mysql --hostname ${MYSQL_HOST} \
	${VOL_ARGS} \
	-p ${MYSQL_PORT}:${MYSQL_PORT} \
	caspian/ccs-mysql:latest

#rm -fr ${DEST_DIR}

