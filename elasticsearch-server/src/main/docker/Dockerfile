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

#Pulling SLES12 thin base image
FROM asdrepo.isus.emc.com:8091/neutrino-base/java8:1.0.0.102.6fb02c3

#Author
MAINTAINER sharique


# Pre-requisite - Adding repositories
RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common

RUN zypper --no-gpg-checks -n refresh

#Install required packages and dependencies
RUN zypper -n in cron-4.2-58.3 cronie-1.4.11-58.3 curl-7.37.0-15.1 iproute2-3.12-5.36 \
    libestr0-0.1.9-1.54 libjson-c2-0.11-2.22 liblogging0-1.0.4-1.6 libnl-config-3.2.23-2.21 \
    libnl3-200-3.2.23-2.21 libpython2_7-1_0-2.7.9-14.1 libsqlite3-0-3.8.3.1-1.14 \
    libwicked-0-6-0.6.18-16.1 libxtables10-1.4.21-2.10 logrotate-3.8.7-3.21 nodejs-0.10.35-1.1 \
    python-2.7.9-14.1 python-base-2.7.9-14.1 \
    python-setuptools-15.1-1.9 python-xml-2.7.9-14.1 rsyslog-8.4.0-5.1 sysconfig-0.83.8-7.1 \
    sysconfig-netconfig-0.83.8-7.1 syslog-service-2.0-778.1 tar-1.27.1-7.1 \
    util-linux-systemd-2.25-22.1 wget-1.14-7.1 wicked-0.6.18-16.1 wicked-service-0.6.18-16.1 \
    python-urllib3-1.9-1.1 python-click-6.2 

#Installing compatible version of ES-Curator for ES 1.7 ( building it from source)
RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-v1.0.0-build/common/x86_64/python-elasticsearch-curator/3.3.0/python-elasticsearch-curator-3.3.0.tar.gz --no-check-certificate \
&& tar -xvzf python-elasticsearch-curator-3.3.0.tar.gz \
&& rm -rf python-elasticsearch-curator-3.3.0.tar.gz \
&& cd curator-3.3.0 \
&& python setup.py install

#Downloading elasticsearch executable
ENV ES_VERSION=1.7.0
ENV ES_DIR="//opt//log-management//elasticsearch"
ENV ES_CLUSTER_NAME=ccs-elasticsearch
ENV ES_DATA_PATH="//data"
ENV ES_LOGS_PATH="//var//log"
ENV ES_CONFIG_PATH="${ES_DIR}//config"
ENV ES_REST_PORT=9200
ENV ES_INTERNAL_COM_PORT=9300

WORKDIR /opt/log-management
RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/elasticsearch/elasticsearch/${ES_VERSION}.tar/elasticsearch-${ES_VERSION}.tar.gz --no-check-certificate
RUN tar -xzvf ${ES_DIR}-${ES_VERSION}.tar.gz \
&& rm ${ES_DIR}-${ES_VERSION}.tar.gz \
&& mv ${ES_DIR}-${ES_VERSION} ${ES_DIR} \
&& cp ${ES_DIR}/config/elasticsearch.yml ${ES_CONFIG_PATH}/elasticsearch-default.yml


COPY elasticsearch-config-script ${ES_DIR}/bin/elasticsearch-config-script
RUN chmod +x ${ES_DIR}/bin/elasticsearch-config-script

COPY query-crs-es.sh ${ES_DIR}/bin/query-crs-es.sh
RUN chmod +x ${ES_DIR}/bin/query-crs-es.sh

COPY query-crs-wrapper.py ${ES_DIR}/bin/query-crs-wrapper.py
RUN chmod +x ${ES_DIR}/bin/query-crs-wrapper.py
ENV CRS_PARSER_PYTHON_SCRIPT="${ES_DIR}//bin//query-crs-wrapper.py"

COPY config-es-cluster ${ES_DIR}/bin/config-es-cluster
RUN chmod +x ${ES_DIR}/bin/config-es-cluster

#Index template for explicit mapping
RUN mkdir -p "${ES_DIR}//config//templates"
COPY custom_index_mapping.json "${ES_DIR}//config//templates//"


#Copy indices delete script: cron job script
ENV  INDICES_EXIPIRY_IN_DAYS=31
ENV  SHORTLIVED_INDICES_EXIPIRY_IN_DAYS=7
ENV  ES_DELETE_INDICES_SCRIPT="${ES_DIR}//delete-es-indices.sh"
ENV  ES_DELETE_SHORTLIVED_INDICES_SCRIPT="${ES_DIR}//delete-shortlived-es-indices.sh"
ENV  ES_DELETE_INDICES_CRON_JOB="//etc//cron.d//delete-es-indices-job.cron"
ENV  ES_DELETE_INDICES_STATUS_FILE="${ES_LOGS_PATH}//cron-delete-es-indices-status.log"
COPY delete-es-indices-job.cron /etc/cron.d/
COPY delete-es-indices.sh                  ${ES_DIR}/
COPY delete-shortlived-es-indices.sh       ${ES_DIR}/
RUN chmod +x ${ES_DELETE_INDICES_SCRIPT} \
&&  chmod +x ${ES_DELETE_INDICES_CRON_JOB}
RUN chmod +x ${ES_DELETE_SHORTLIVED_INDICES_SCRIPT}

#Exposing elasticsearch server container port to the HOST
EXPOSE ${ES_REST_PORT} ${ES_INTERNAL_COM_PORT}

#Set Heap size
ENV ES_HEAP_SIZE=4g

#Copy elastic search bootstrap script
COPY elasticsearch-bootstrap-and-run.sh ${ES_DIR}/
RUN chmod +x ${ES_DIR}/elasticsearch-bootstrap-and-run.sh

#Upper limit of Open Fds
ENV LIMIT_OPEN_FD=64000
ENV ES_LIMITS_CONF_FILE="//etc//security//limits.d//elasticsearch-limits.conf"
COPY elasticsearch-limits.conf /etc/security/limits.d/
COPY elasticsearch-sysctl.conf /etc/sysctl.d/

#ESExportWrapper
ENV  ES_EXPORT_PORT 6556
ENV  ES_EXPORT_BASE_DIR="//opt//log-management//ESExportWrapper"
ENV  ES_EXPORT_DEFAULT_CONFIG="//opt//log-management//ESExportWrapper//config//default.json"
ENV  ES_EXPORT_DEV_CONFIG="//opt//log-management//ESExportWrapper//config//development.json"
ENV  ES_EXPORT_LOCAL_STORE_DIR="//opt//log-management//ESExportWrapper//local-store"
ENV  ES_EXPORT_ARCHIVE_DIRECTORY="//data//logfiles-store"
ENV  ES_EXPORT_FILE_SPLIT_LINE_COUNT="50000"
COPY ESExportWrapper/ ${ES_EXPORT_BASE_DIR}/
RUN  mkdir -p ${ES_EXPORT_LOCAL_STORE_DIR}

RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/elasticsearch/es-export-wrapper/3.0/es-export-wrapper-3.0.tar
RUN tar -xvf es-export-wrapper-3.0.tar \
&& rm es-export-wrapper-3.0.tar \
&& mv node_modules /opt/log-management/ESExportWrapper/


#Scheduler
ENV ES_SCHEDULER_BASE_DIR="//opt//log-management//Scheduler"
ENV ES_SCHEDULER_DEV_CONFIG="//opt//log-management//Scheduler//config//development-cronjob.json"
COPY Scheduler/ ${ES_SCHEDULER_BASE_DIR}/

RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/elasticsearch/es-scheduler/1.0/es-scheduler-1.0.tar
RUN tar -xvf es-scheduler-1.0.tar \
&& rm es-scheduler-1.0.tar \
&& mv node_modules /opt/log-management/Scheduler/

#Removing binary files which are not needed
RUN zypper -n rm wget

# Removing zypper repos
RUN zypper rr caspiancs_common

#Expose ESExportWrapper port
EXPOSE ${ES_EXPORT_PORT}

#Running elasticsearch executable
WORKDIR ${ES_DIR}
ENTRYPOINT ${ES_DIR}/elasticsearch-bootstrap-and-run.sh


