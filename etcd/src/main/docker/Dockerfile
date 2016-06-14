#--------------------------------------------------------------------------------------------------
# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#
# Caspian Common Service : Etcd
#

FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35

### Pre-requisite - Adding repositories , creation of temp directory
RUN mkdir -p /tmp/downloads

RUN zypper ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common
RUN zypper --no-gpg-checks -n refresh

RUN zypper -n in curl-7.37.0-15.1 tar-1.27.1-7.1

MAINTAINER sandeep, bansai

ENV VERSION 2.1.3

###download and install etcd
RUN curl -sf -k -o /tmp/downloads/etcd-v$VERSION-linux-amd64.tar.gz -L http://asdrepo.isus.emc.com:8081/artifactory/caspian-build/coreos/etcd/$VERSION/etcd-v$VERSION-linux-amd64.tar.gz \
&& mkdir -p /opt \
&& cd /opt \
&& tar xfz /tmp/downloads/etcd-v$VERSION-linux-amd64.tar.gz \

### create log directory
&& mkdir /var/log/etcd-service/ \

#### cleanup
&& rm -rf /tmp \
&& zypper -n rm curl \
&& zypper -n rm tar

COPY bootstrap-script.sh /opt/etcd/bin/bootstrap-script.sh
RUN chmod +x /opt/etcd/bin/bootstrap-script.sh

# Removing zypper repos
RUN zypper rr caspiancs_common

ENV PATH /opt/etcd-v$VERSION-linux-amd64:$PATH
EXPOSE 4001 7001 2379 2380
WORKDIR /data

ENTRYPOINT ["/opt/etcd/bin/bootstrap-script.sh"]
#default option
CMD ["--help"]

