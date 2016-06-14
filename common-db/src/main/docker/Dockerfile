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

FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35

MAINTAINER moidas

# add repo for devel packages
RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common

# Refresh Repo
RUN zypper --no-gpg-checks -n refresh

# python is needed for some scripts, curl is a requirement for rpm --import
RUN zypper --non-interactive --quiet --no-gpg-checks install curl-7.37.0-15.1 \
    iproute2-4.0-105.1  libjemalloc1-3.6.0-2.2.1 liblua5_2-5.2.2-4.2 \
    libmysqlclient18-10.0.20-18.1 libnl-config-3.2.23-2.21 libnl3-200-3.2.23-2.21 \
    libpcap1-1.5.3-2.18 libpython2_7-1_0-2.7.9-14.1 libsqlite3-0-3.8.3.1-2.3.1 \
    libxtables10-1.4.21-2.10 perl-DBD-mysql-4.021-7.178 perl-DBI-1.628-3.214 \
    perl-Net-Daemon-0.48-11.19 perl-PlRPC-0.2020-23.10 python-2.7.9-14.1 \
    python-base-2.7.9-14.1 tar-1.27.1-7.1 tar-lang-1.27.1-7.1 traceroute-2.0.19-1.17

# install and configure mysql and galera. Xtrabackup, which and socat required for galera sst
RUN zypper --non-interactive --quiet --no-gpg-checks install galera-25.3.9-1.sles12.sle12 \
    mariadb-galera-server-10.0.20-1 mariadb-client-10.0.21-1 mariadb-common-10.0.21-1 \
    lsof-4.84-20.65 socat-1.7.2.4-1.2 nmap-6.46-1.72 which-2.20-3.180 xinetd-2.3.15-7.7 \
    iputils-s20121221-2.19 net-tools-1.60-764.185 xtrabackup-no-rsync-2.2.12-71.1

RUN zypper --non-interactive --no-gpg-checks install http://asdrepo.isus.emc.com:8081/artifactory/caspian-prerelease-local/com/emc/caspian/encryption-python/1.0.0.1.2394.fb26b55/encryption-python-1.0.0.1.2394.fb26b55.rpm

ADD data/my.cnf /etc/my.cnf

# create log directory
RUN mkdir /var/log/mysql/ && chown mysql:mysql /var/log/mysql/

ADD data/boot.sh /opt/mysql/boot.sh
ADD data/monitor_functions.sh /opt/mysql/monitor_functions.sh
ADD data/sqlconnection.sh /opt/mysql/sqlconnection.sh
ADD data/mysql_chek_cluster.sh /opt/mysql/
ADD data/mysqlchk /opt/mysql/

COPY data/galera-backup.sh /usr/bin/galera-backup.sh
RUN chown mysql:mysql -R /usr/bin/galera-backup.sh && chmod +x /usr/bin/galera-backup.sh

COPY data/galera-restore.sh /usr/bin/galera-restore.sh
RUN chown mysql:mysql -R /usr/bin/galera-restore.sh && chmod +x /usr/bin/galera-restore.sh

# Removing the zypper repos
RUN zypper rr caspiancs_common


EXPOSE 3306 4444 4567 4568 9500

#see https://github.com/dockerfile/mariadb/issues/3
ENV XTRABACKUP_PASS sstpass
ENV TERM xterm
ENTRYPOINT ["/opt/mysql/boot.sh"]
