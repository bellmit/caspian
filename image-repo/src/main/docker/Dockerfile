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
# Caspian Common Service : OpenStack Glance Image
#

FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35
MAINTAINER thomas3

# install packages
RUN zypper ar http://pld-imgapprd01.isus.emc.com:8081/artifactory/adg-icc icc
RUN zypper ar http://lglob090.lss.emc.com/SLES12/SLE SLE
RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-staging-local staging

RUN zypper --gpg-auto-import-keys --no-gpg-checks -n update

RUN zypper refresh

RUN zypper -n in openstack-glance
RUN zypper -n in python-mysql

# ADD glance-registry.conf /etc/glance/glance-registry.conf
# ADD glance-api.conf /etc/glance/glance-api.conf

# ADD glance.sql /tmp/glance.sql
# ADD bootstrap_api.sh /tmp/bootstrap_api.sh
# ADD bootstrap_registry.sh /tmp/bootstrap_registry.sh

# RUN chmod +x /tmp/bootstrap_registry.sh
# RUN chmod +x /tmp/bootstrap_api.sh

EXPOSE 9191 9292

ENTRYPOINT ["glance-registry"]

