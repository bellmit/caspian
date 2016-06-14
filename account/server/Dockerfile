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

#
# Caspian Common Service : Account Service Image
#

FROM asdrepo.isus.emc.com:8091/neutrino-base/java8:1.0.0.102.6fb02c3
MAINTAINER thomas3

RUN zypper -n ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common

RUN zypper --no-gpg-checks -n refresh

# Installing the packages by locking their exact versions
RUN zypper --gpg-auto-import-keys --no-gpg-checks -n in binutils-2.24-7.1 \
    cpp-4.8-6.189 cpp48-4.8.5-24.1 cron-4.2-58.3 cronie-1.4.11-58.3 curl-7.37.0-15.1 \
    gcc-4.8-6.189 gcc48-4.8.5-24.1 glibc-devel-2.19-22.7.1 iproute2-3.12-5.36 \
    libasan0-4.8.5-24.1 libatomic1-5.2.1+r226025-4.1 libcloog-isl4-0.18.1-1.124 \
    libestr0-0.1.9-1.54 libffi4-4.8.3+r212056-11.1 libgomp1-5.2.1+r226025-4.1 \
    libisl10-0.12.2-1.121 libitm1-5.2.1+r226025-4.1 libjson-c2-0.11-2.22 liblogging0-1.0.4-1.6 \
    libmpc3-1.0.2-1.123 libmpfr4-3.1.2-7.1 libmysqlclient18-10.0.20-18.1 libnl-config-3.2.23-2.21 \
    libnl3-200-3.2.23-2.21 libpython2_7-1_0-2.7.9-14.1 libsqlite3-0-3.8.3.1-2.3.1 \
    libtsan0-5.2.1+r226025-4.1 libwicked-0-6-0.6.18-16.1 libxtables10-1.4.21-2.10 \
    logrotate-3.8.7-3.21 linux-kernel-headers-2.6.32-1.13.5 mariadb-client-10.0.20-18.1 \
    mariadb-errormessages-10.0.20-18.1 python-2.7.9-14.1 python-base-2.7.9-14.1 \
    python-devel-2.7.9-14.1 python-cffi-0.8.2-1.10 python-cryptography-0.4-1.23 \
    python-mysql-1.2.4-1.38 python-pyOpenSSL-0.14-1.7 python-pyasn1-0.1.7-5.7 \
    python-pycparser-2.10-3.7 python-requests-2.8.1-77.1 python-setuptools-1.1.7-7.1 \
    python-six-1.7.3-1 rsyslog-8.4.0-5.1 sysconfig-0.83.7-2.1 sysconfig-netconfig-0.83.7-2.1 \
    syslog-service-2.0-778.1 which-2.20-3.180 wicked-0.6.18-16.1 wicked-service-0.6.18-16.1

RUN mkdir -p /opt/caspian/bin/ && \
    mkdir -p /opt/caspian/lib/ && \
    mkdir -p /opt/caspian/conf/ && \
    mkdir -p /opt/caspian/scripts/ && \
    mkdir -p /etc/caspian/ssl/certs/ && \
    mkdir -p /var/log/caspian/ && \
    mkdir -p /data

RUN easy_install http://asdrepo.isus.emc.com:8081/artifactory/caspian-build/com/emc/caspian/encryption-python/1.0.0.1.2394.fb26b55/encryption-python-1.0.0.1.2394.fb26b55.tar.gz

# Removing the unnecessary packages
RUN zypper -n remove python-devel gcc gcc48 cpp cpp48 glibc-devel linux-kernel-headers

COPY build/scripts/server /opt/caspian/bin/account
RUN chmod a+x /opt/caspian/bin/account
RUN sed -i "s#^CLASSPATH=\$APP\(.*\)#CLASSPATH=/opt/caspian:\$APP\1#" /opt/caspian/bin/account

COPY conf/ /opt/caspian/conf/
COPY build/dist/libs/*.jar /opt/caspian/lib/
COPY build/libs/*.jar /opt/caspian/lib/

COPY scripts/background-sync.py /opt/caspian/scripts/
COPY scripts/dbcleanup.py /opt/caspian/scripts/
COPY scripts/dbcleanup.conf /opt/caspian/scripts/
COPY scripts/cronHA.sh /opt/caspian/scripts/

COPY build/scripts/worker /opt/caspian/bin/worker
RUN chmod a+x /opt/caspian/bin/worker
RUN sed -i "s#^CLASSPATH=\$APP\(.*\)#CLASSPATH=/opt/caspian:\$APP\1#" /opt/caspian/bin/worker

COPY etc /etc/
COPY opt /opt/

# wrapper to execute account service and workflow service inside the same container
RUN chmod a+x /opt/caspian/bin/run.sh

# cron format: minutes(0-59) hours(0-23) day_of_month(1-31) month(1-12) weekday(0-6) <command>
# running the cron job every 12 hours
RUN chmod 744 /opt/caspian/scripts/background-sync.py
RUN chmod 744 /opt/caspian/scripts/dbcleanup.py
RUN chmod 744 /opt/caspian/scripts/cronHA.sh
RUN echo "0 */12 * * * /opt/caspian/scripts/cronHA.sh" >> /var/spool/cron/tabs/root

# Removing the zypper repos
RUN zypper rr caspiancs_common

EXPOSE 35359

WORKDIR /opt/caspian

ENTRYPOINT ["/opt/caspian/bin/run.sh"]
