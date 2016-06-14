#!/bin/bash

set -e

#Variable replacement using environment variables
sed -i "s#INDICES_EXIPIRY_IN_DAYS#${INDICES_EXIPIRY_IN_DAYS}#" "${ES_DELETE_INDICES_SCRIPT}"
sed -i "s#SHORTLIVED_INDICES_EXIPIRY_IN_DAYS#${SHORTLIVED_INDICES_EXIPIRY_IN_DAYS}#" "${ES_DELETE_SHORTLIVED_INDICES_SCRIPT}"
sed -i "s#ES_REST_PORT#${ES_REST_PORT}#" "${ES_DELETE_INDICES_SCRIPT}"
sed -i "s#ES_REST_PORT#${ES_REST_PORT}#" "${ES_DELETE_SHORTLIVED_INDICES_SCRIPT}"
sed -i "s#ES_DELETE_INDICES_STATUS_FILE#${ES_DELETE_INDICES_STATUS_FILE}#" "${ES_DELETE_INDICES_SCRIPT}"
sed -i "s#ES_DELETE_INDICES_STATUS_FILE#${ES_DELETE_INDICES_STATUS_FILE}#" "${ES_DELETE_SHORTLIVED_INDICES_SCRIPT}"
sed -i "s#ES_DELETE_INDICES_SCRIPT#${ES_DELETE_INDICES_SCRIPT}#" "${ES_DELETE_INDICES_CRON_JOB}"
sed -i "s#CONTAINER_HOST_ADDRESS#${CONTAINER_HOST_ADDRESS}#" "${ES_DIR}/bin/query-crs-es.sh"
sed -i "s#CONTAINER_HOST_ADDRESS#${CONTAINER_HOST_ADDRESS}#" "${ES_EXPORT_DEFAULT_CONFIG}"
sed -i "s#ES_EXPORT_ARCHIVE_DIRECTORY#${ES_EXPORT_ARCHIVE_DIRECTORY}#" "${ES_EXPORT_DEV_CONFIG}"
sed -i "s#ES_EXPORT_PORT#${ES_EXPORT_PORT}#"                 "${ES_EXPORT_DEV_CONFIG}"
sed -i "s#ES_PORT#${ES_REST_PORT}#"                          "${ES_EXPORT_DEV_CONFIG}"
sed -i "s#ES_EXPORT_FILE_SPLIT_LINE_COUNT#${ES_EXPORT_FILE_SPLIT_LINE_COUNT}#" "${ES_EXPORT_DEV_CONFIG}"
sed -i "s#ES_INDEX_PURGE_SCRIPT#${ES_DELETE_INDICES_SCRIPT}#"  "${ES_SCHEDULER_DEV_CONFIG}"
sed -i "s#ES_SHORTLIVED_INDEX_PURGE_SCRIPT#${ES_DELETE_SHORTLIVED_INDICES_SCRIPT}#"  "${ES_SCHEDULER_DEV_CONFIG}"


#Configure elasticsearch.yml
${ES_DIR}/bin/elasticsearch-config-script

#Get info of other Nodes from CRS(Component registry service)
${ES_DIR}/bin/query-crs-es.sh

#RUN cron in daemon mode
#cron -i
#crontab ${ES_DELETE_INDICES_CRON_JOB}

#Setting Upper limit on OpenFD
#ulimit -n ${LIMIT_OPEN_FD}


#Run ESExportWrapper service
mkdir -p ${ES_EXPORT_ARCHIVE_DIRECTORY}
cd ${ES_EXPORT_BASE_DIR}
npm start &


#Run scheduler for index purging
cd ${ES_SCHEDULER_BASE_DIR}
npm start &

#RUN elastic search binary
exec ${ES_DIR}/bin/elasticsearch -Des.max-open-files=true
