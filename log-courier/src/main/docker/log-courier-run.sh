#
# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
#
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#

#Author dyamam

#!/bin/bash
set -e

#Replacing environment variables : CONTAINER HOST IP,LOGSTASH SERVER IP, PORT and PATH with actual values in log-courier conf file
sed -i "s/CONTAINER_HOST_ADDRESS/${CONTAINER_HOST_ADDRESS}/" "${LOG_COURIER_CONF_FILE}"
#sed -i "s/ELK_SERVER_IP/${ELK_SERVER_IP}/" "${LOG_COURIER_CONF_FILE}"
#sed -i "s/ELK_SERVER_PORT/${ELK_SERVER_PORT}/" "${LOG_COURIER_CONF_FILE}"
sed -i "s#LOG_COURIER_BASE_PATH#${LOG_COURIER_BASE_PATH}#" "${LOG_COURIER_SERVICE_FILE}"
sed -i "s#LOG_COURIER_CONF_FILE_PATH#${LOG_COURIER_CONF_FILE}#" "${LCREST_CONF_FILE}"
sed -i "s/REST_HOST_ADDRESS/${LC_HOST_ADDRESS}/" "${LCREST_CONF_FILE}"
sed -i "s/REST_PORT/${LC_REST_PORT}/" "${LCREST_CONF_FILE}"
sed -i "s#CONTAINER_HOST_ADDRESS#${CONTAINER_HOST_ADDRESS}#"  "${LCREST_SETTNGS_CONF_FILE}"
sed -i "s#LOGFILE_DIRS#${LOGFILE_DIRS}#"  "${LOG_AGGREGATOR_CONF_FILE}"
sed -i "s#LOGFILE_PATTERNS#${LOGFILE_PATTERNS}#"  "${LOG_AGGREGATOR_CONF_FILE}"
sed -i "s#REFRESH_API_SCRIPT_PATH#${REFRESH_API_SCRIPT}#" "${REFRESH_API_CRON_JOB}"
sed -i "s#REST_PORT#${LC_REST_PORT}#" "${REFRESH_API_SCRIPT}"
sed -i "s#REFRESH_INTERVAL_IN_MIN#${REFRESH_INTERVAL_IN_MIN}#" "${REFRESH_API_CRON_JOB}"
sed -i "s#LOG_COURIER_SERVICE_FILE#${LOG_COURIER_SERVICE_FILE}#" "${LCREST_CONF_FILE}"

#Adding cron job for logfiles scanning
#cron -i
#crontab ${REFRESH_API_CRON_JOB}

#Running logstash substitutor
/bin/bash $LOGSTASH_INFO_SUBSTITUTOR &

#Running nodetype info subsitutor
/bin/bash $NODETYPE_INFO_SUBSTITUTOR

#Running log-courier-rest
exec ${LCREST_EXEC_FILE} -config "${LCREST_CONF_FILE}"  -lcconfig ${LOG_COURIER_CONF_FILE}
