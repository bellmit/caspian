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
FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35

#Author
MAINTAINER dyamam

# Pre-requisite - Adding repositories
RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common
RUN zypper --no-gpg-checks -n refresh

#Install required packages
RUN zypper -n in binutils-2.24-7.1 cpp-4.8-6.189 cpp48-4.8.5-24.1 cron-4.2-58.3 \
    cronie-1.4.11-58.3 curl-7.37.0-15.1 gcc-4.8-6.189 gcc48-4.8.5-24.1 git-core-1.8.5.6-14.3 \
    git-1.8.5.6-14.3 glibc-devel-2.19-22.7.1 go-1.4.2-143.1 iproute2-3.12-5.36 less-458-5.13 \
    libasan0-4.8.5-24.1 libatomic1-5.2.1+r226025-4.1 libcloog-isl4-0.18.1-1.124 \
    libedit0-3.1.snap20140620-1.13 libestr0-0.1.9-1.54 libffi4-4.8.3+r212056-11.1 \
    libgcc_s1-5.2.1+r226025-4.1 \
    libgomp1-5.2.1+r226025-4.1 libisl10-0.12.2-1.121 libitm1-5.2.1+r226025-4.1 \
    libjson-c2-0.11-2.22 liblogging0-1.0.4-1.6 libmpc3-1.0.2-1.123 libmpfr4-3.1.2-7.1 \
    libncurses6-5.9-40.124 libnl-config-3.2.23-2.21 libnl3-200-3.2.23-2.21 \
    libpython2_7-1_0-2.7.9-14.1 libsqlite3-0-3.8.3.1-2.3.1 libudev1-210-55.2 \
    libwicked-0-6-0.6.18-16.1 libxtables10-1.4.21-2.10 logrotate-3.8.7-3.21 \
    linux-kernel-headers-2.6.32-1.13.5 make-4.0-4.1 openslp-2.0.0-2.9 openssh-6.6p1-29.1 \
    perl-Error-0.17021-1.18 python-2.7.9-14.1 python-base-2.7.9-14.1 \
    python-cffi-0.8.2-1.10 python-cryptography-0.4-1.23 python-pyOpenSSL-0.14-1.7 \
    python-pyasn1-0.1.7-5.7 python-pycparser-2.10-3.7 python-requests-2.8.1-77.1 \
    python-six-1.9.0-9.7.1 rsync-3.1.0-2.7 rsyslog-8.4.0-5.1 sysconfig-0.83.8-7.1 \
    sysconfig-netconfig-0.83.8-7.1 syslog-service-2.0-778.1 tar-1.27.1-7.1 \
    tar-lang-1.27.1-7.1 udev-210-55.2 unzip-6.00-32.1 util-linux-systemd-2.25-22.1 \
    wget-1.14-7.1 wicked-0.6.18-16.1 wicked-service-0.6.18-16.1


#Turn off SSL Validation
ENV GIT_SSL_NO_VERIFY TRUE

#Setting log-courier base path
ENV LOG_COURIER_BASE_PATH="//opt//log-management//log-courier"

#Download and unzip log-courier-source
RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/logcourier/develop/1.0/develop-1.0.zip
RUN unzip develop-1.0.zip
RUN mv log-courier-develop log-courier
RUN mkdir -p /opt/log-management/
RUN mv log-courier /opt/log-management/log-courier

#Changing Working Directory to log-courier source and build source code
WORKDIR /opt/log-management/log-courier
RUN make

#Copy conf file and entrypoint script
ENV LOG_COURIER_CONF_FILE="//opt//log-management//log-courier//log-courier.conf"
COPY log-courier.conf /opt/log-management/log-courier/
COPY log-courier-run.sh /
RUN chmod +x /log-courier-run.sh

#COPY logcourier service file into /etc/init.d
ENV LOG_COURIER_SERVICE_FILE="//etc//init.d//logcourier-service"
COPY logcourier-service /etc/init.d/
RUN chmod 755 /etc/init.d/logcourier-service


#LCRest
ENV LCREST_CONF_FILE="//opt//log-management//LogCourierRestWrapper//src//conf//lcrest-conf.conf"
ENV LCREST_EXEC_FILE="//opt//log-management//LogCourierRestWrapper//bin//log-courier-rest"
ENV LCREST_SETTNGS_CONF_FILE="//opt//log-management//LogCourierRestWrapper//src//conf//lc-settings.conf"
ENV NODETYPE_GETTER_SCRIPT="//opt//log-management//utils//script//node-type-getter.py"
ENV NODETYPE_INFO_SUBSTITUTOR="//opt//log-management//utils//script//node-type-info-substitutor.sh"
ENV LC_HOST_ADDRESS=0.0.0.0
ENV LC_REST_PORT=6555
COPY node-type-getter.py /opt/log-management/utils/script/
COPY node-type-info-substitutor.sh /opt/log-management/utils/script/
RUN  chmod +x ${NODETYPE_GETTER_SCRIPT} \
&&   chmod +x ${NODETYPE_INFO_SUBSTITUTOR}
COPY LogCourierRestWrapper/ /opt/log-management/LogCourierRestWrapper/
RUN wget http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/logcourier/rest-wrapper/3.0/log-courier-rest-dependency-3.0.tar
RUN tar -xvf log-courier-rest-dependency-3.0.tar \
&& rm log-courier-rest-dependency-3.0.tar \
&& mv github.com /opt/log-management/LogCourierRestWrapper/src/github.com
WORKDIR /opt/log-management/LogCourierRestWrapper/
RUN make
RUN chmod +x ${LCREST_EXEC_FILE}

#logaggregator script
ENV LOG_AGGREGATOR_EXEC="//opt//log-management//LogCourierRestWrapper//scripts//logpathaggregator-run.sh"
ENV LOGFILE_DIRS="\/opt\/emc\/caspian"
ENV LOGFILE_PATTERNS="*.log,*exp.[0-5],*trc.[0-5],eventlog.*,messages,*.out,mcelog,*sel.log"
ENV LOG_AGGREGATOR_CONF_FILE="//opt//log-management//LogCourierRestWrapper//scripts//logpath-pattern.conf"
RUN chmod +x ${LOG_AGGREGATOR_EXEC}


#Changing Working Directory to log-courier
WORKDIR /opt/log-management/log-courier

#Refresh API CRON
ENV REFRESH_API_SCRIPT="//opt//log-management//LogCourierRestWrapper//scripts//refresh-api-script.sh"
ENV REFRESH_API_CRON_JOB="//etc//cron.d//refresh-api-job.cron"
ENV REFRESH_INTERVAL_IN_MIN=5
COPY refresh-api-script.sh ${REFRESH_API_SCRIPT}
COPY refresh-api-job.cron  ${REFRESH_API_CRON_JOB}
RUN chmod +x ${REFRESH_API_SCRIPT} \
&&  chmod +x ${REFRESH_API_CRON_JOB}

#Expose REST port
EXPOSE ${LC_REST_PORT}


#Copy crs-integration-script
ENV  CRS_INTEGRATION_SCRIPT="//opt//log-management//crs//env-from-crs.sh"
ENV  LOGSTASH_INFO_SUBSTITUTOR="//opt//log-management//crs//configure-logstash-info.sh"
COPY env-from-crs.sh ${CRS_INTEGRATION_SCRIPT}
COPY configure-logstash-info.sh ${LOGSTASH_INFO_SUBSTITUTOR}
RUN  chmod +x ${CRS_INTEGRATION_SCRIPT}
RUN  chmod +x ${LOGSTASH_INFO_SUBSTITUTOR}


# Removing unnecessary packages
RUN zypper -n rm rsync go gcc gcc48 cpp cpp48 glibc-devel linux-kernel-headers make git git-core

# Removing zypper repos
RUN zypper rr caspiancs_common

#Defining entrypoint script
ENTRYPOINT /log-courier-run.sh
