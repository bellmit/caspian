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
# Caspian Common Service : MySql Image
#

FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35
MAINTAINER thomas3

RUN zypper --gpg-auto-import-keys -n ar http://asdrepo.isus.emc.com:8081/artifactory/adg-catalog/sles/12/ sle
RUN zypper --gpg-auto-import-keys -n ar http://asdrepo.isus.emc.com:8081/artifactory/adg-catalog/common/sles/12/ commonsle
RUN zypper --gpg-auto-import-keys -n ar http://asdrepo.isus.emc.com:8081/artifactory/adg-catalog/common/generic/linux/ common
RUN zypper --gpg-auto-import-keys -n ar http://lglob090.lss.emc.com/SLES12/SLE SLE

RUN zypper --gpg-auto-import-keys -n in mariadb

RUN mkdir -p /data/db && chown mysql:mysql -R /data/db
RUN mkdir -p /var/log/mysql && chown mysql:mysql -R /var/log/mysql

COPY my.cnf /etc/my.cnf
RUN chown mysql:mysql -R /etc/my.cnf

COPY bootstrap.sql /etc/bootstrap.sql
RUN chown mysql:mysql -R /etc/bootstrap.sql

COPY startup.sh /etc/startup.sh
RUN chown mysql:mysql -R /etc/startup.sh && chmod +x /etc/startup.sh

# Removing zypper repos
RUN zypper rr sle commonsle common SLE

EXPOSE 3306

ENTRYPOINT ["/etc/startup.sh"]

