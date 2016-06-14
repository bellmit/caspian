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

#Using java8 runtime image
FROM asdrepo.isus.emc.com:8091/neutrino-base/java8:1.0.0.102.6fb02c3
MAINTAINER junejn

# For this drop, the artifacts are being pulled from staging repo. Should be pulled from icc repo once legal approval is received and artifacts are moved there.

RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common
RUN zypper --no-gpg-checks -n refresh

# Install curl to get elasticsearch location from CRS
# Install python for json parsing
RUN zypper -n in curl-7.37.0-15.1 libpython2_7-1_0-2.7.9-14.1 libsqlite3-0-3.8.3.1-2.3.1 \
    python-2.7.9-14.1 python-base-2.7.9-14.1 wget-1.14-7.1 tar-1.27.1-7.1 unzip-6.00-32.1 

RUN mkdir -p /opt/log-management/logstash

WORKDIR /opt/log-management/logstash
ENV LS_VERSION=1.5.4

RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/logstash/logstash-logcourier-updatedMutateFilter/${LS_VERSION}/logstash-logcourier-updatedMutateFilter-${LS_VERSION}.tar.gz --no-check-certificate \
 && tar zxvf logstash-logcourier-updatedMutateFilter-${LS_VERSION}.tar.gz \
 && rm /opt/log-management/logstash/logstash-logcourier-updatedMutateFilter-${LS_VERSION}.tar.gz

ENV LS_HEAP_SIZE=2048m
ENV JAVA_OPTS=-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/data/dumps

# Removing installed packages
RUN zypper -n rm unzip \
 && zypper -n rm tar \
 && zypper -n rm wget 

# Exposing port for listening to log courier
EXPOSE 5040

# Copying conf file and patterns file for parsing logs
COPY logstash.conf /opt/log-management/logstash/logstash-${LS_VERSION}/logstash.conf
COPY custom-patterns /opt/log-management/logstash/logstash-${LS_VERSION}/patterns/custom-patterns


# Copying script for setting elastic search IP address, port and starting the server
COPY config.sh /opt/log-management/logstash/logstash-config.sh
RUN chmod +x /opt/log-management/logstash/logstash-config.sh

# Copying script to get IPs from Component Regsitry
ENV CRS_INTEGRATION_SCRIPT /opt/log-management/logstash/env-from-crs.sh
COPY env-from-crs.sh /opt/log-management/logstash/env-from-crs.sh
RUN chmod +x /opt/log-management/logstash/env-from-crs.sh
COPY json_parser.py /opt/log-management/logstash/json_parser.py

# Removing zypper repos
RUN zypper rr caspiancs_common

WORKDIR /opt/log-management/logstash
ENTRYPOINT /opt/log-management/logstash/logstash-config.sh
