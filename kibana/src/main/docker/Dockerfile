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

# Using sles12 slim image
FROM asdrepo.isus.emc.com:8091/neutrino-base/base-sles12:1.0.0.1.35
MAINTAINER syalp

WORKDIR /opt/log-management/kibana
ENV KIBANA_VERSION 4.1.4-linux-x64

RUN zypper -n ar http://asdrepo.isus.emc.com:8081/artifactory/caspianCS-local/common-master/ caspiancs_common

RUN zypper --no-gpg-checks -n refresh

RUN zypper -n in curl-7.37.0-15.1 libffi4-4.8.3+r212056-11.1 libsqlite3-0-3.8.3.1-2.3.1 \
    libpython2_7-1_0-2.7.9-14.1 python-2.7.9-14.1 python-base-2.7.9-14.1 python-cffi-0.8.2-1.10 \
    python-cryptography-0.4-1.23 python-pyOpenSSL-0.14-1.7 python-pyasn1-0.1.7-5.7 \
    python-pycparser-2.10-3.7 python-pycrypto-2.6.1-1.18 python-requests-2.8.1-77.1 \
    python-setuptools-2.1-1.1 python-six-1.9.0-9.7.1 python-xml-2.7.9-14.1 tar-1.27.1-7.1 \
    util-linux-systemd-2.25-22.1 wget-1.14-7.1

RUN wget  http://asdrepo.isus.emc.com:8081/artifactory/caspian-tps-releases-local/kibana/kibana/4.1.4-linux/kibana-${KIBANA_VERSION}.tar.gz \
&& tar zxvf kibana-${KIBANA_VERSION}.tar.gz \
&& rm /opt/log-management/kibana/kibana-${KIBANA_VERSION}.tar.gz

RUN easy_install http://asdrepo.isus.emc.com:8081/artifactory/caspian-build/com/emc/caspian/encryption-python/1.0.0.1.2394.fb26b55/encryption-python-1.0.0.1.2394.fb26b55.tar.gz

# Remove extra packages
RUN zypper -n rm tar wget

#Copy crs-integration-script
ENV  CRS_INTEGRATION_SCRIPT="\/opt\/log-management\/kibana\/integrate-crs.py"
ENV  KIBANA_INFO_REPLACER="\/opt\/log-management\/kibana\/configure-kibana-info.sh"
COPY integrate-crs.py ${CRS_INTEGRATION_SCRIPT}
COPY configure-kibana-info.sh ${KIBANA_INFO_REPLACER}
RUN  chmod +x ${CRS_INTEGRATION_SCRIPT}
RUN  chmod +x ${KIBANA_INFO_REPLACER}

ADD ./INDEX-MAPPING.json /opt/log-management/kibana/INDEX-MAPPING.json
ADD ./INDEX-PATTERN.json /opt/log-management/kibana/INDEX-PATTERN.json
ADD ./DEFAULT-INDEX.json /opt/log-management/kibana/DEFAULT-INDEX.json
ADD ./LOG-SEVERITY.json  /opt/log-management/kibana/LOG-SEVERITY.json
ADD ./CASPIAN-SERVICES.json /opt/log-management/kibana/CASPIAN-SERVICES.json
ADD ./All-logs.json /opt/log-management/kibana/All-logs.json
ADD ./Parttype-logs.json /opt/log-management/kibana/Parttype-logs.json
ADD ./severity-logs.json /opt/log-management/kibana/severity-logs.json
ADD ./category-logs.json /opt/log-management/kibana/category-logs.json
ADD ./Logs-category.json /opt/log-management/kibana/Logs-category.json
ADD ./Caspian-components.json /opt/log-management/kibana/Caspian-components.json
ADD ./Audit_logs.json /opt/log-management/kibana/Audit_logs.json
ADD ./Troubleshoot.json /opt/log-management/kibana/Troubleshoot.json
ADD ./VM_logs.json /opt/log-management/kibana/VM_logs.json
ADD ./TROUBLESHOOT-DASHBOARD.json /opt/log-management/kibana/TROUBLESHOOT-DASHBOARD.json
ADD ./CC_INSTANCES_DASHBOARD.json /opt/log-management/kibana/CC_INSTANCES_DASHBOARD.json
ADD ./AUDIT_DASHBOARD.json /opt/log-management/kibana/AUDIT_DASHBOARD.json
ADD ./Troubleshoot-logs-over-time.json /opt/log-management/kibana/Troubleshoot-logs-over-time.json
ADD ./CC-logs-over-time.json /opt/log-management/kibana/CC-logs-over-time.json
ADD ./CC-logs-components.json /opt/log-management/kibana/CC-logs-components.json
ADD ./Audit-logs-by-severity.json /opt/log-management/kibana/Audit-logs-by-severity.json
ADD ./Audit-logs-by-components.json /opt/log-management/kibana/Audit-logs-by-components.json
ADD ./Troubleshoot-logs-components.json /opt/log-management/kibana/Troubleshoot-logs-components.json
ADD ./CC-logs-by-severity.json /opt/log-management/kibana/CC-logs-by-severity.json
ADD ./VM-logs-by-verb.json /opt/log-management/kibana/VM-logs-by-verb.json
ADD ./Audit-logs-over-time.json /opt/log-management/kibana/Audit-logs-over-time.json
ADD ./Troubleshoot-logs-severity.json /opt/log-management/kibana/Troubleshoot-logs-severity.json
ADD ./Trouble-shoot-logs-by-type.json /opt/log-management/kibana/Trouble-shoot-logs-by-type.json
ADD ./Audit-logs-by-type.json /opt/log-management/kibana/Audit-logs-by-type.json
ADD ./LOGS-FOR-PLATFORM-NODES-AND-SERVICES.json /opt/log-management/kibana/LOGS-FOR-PLATFORM-NODES-AND-SERVICES.json
ADD ./LOGS-OVER-TIME.json /opt/log-management/kibana/LOGS-OVER-TIME.json
ADD ./platform-logs.json /opt/log-management/kibana/platform-logs.json
ADD ./DEFAULT-DASHBOARD.json /opt/log-management/kibana/DEFAULT-DASHBOARD.json
ADD ./dashboard.sh /opt/log-management/kibana/dashboard.sh
ADD ./manipulate.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/manipulate.js
ADD ./elasticdump.css /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/elasticdump.css
ADD ./jquery.filedownload.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/jquery.filedownload.js
ADD ./run.sh /opt/log-management/kibana/run.sh
ADD ./set_decrypted_pwd.sh /opt/log-management/kibana/set_decrypted_pwd.sh
ADD ./keystone_decrypt.py /opt/log-management/kibana/keystone_decrypt.py
COPY ./index.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/index.js
COPY ./app.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/app.js
ADD ./parser.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/parser.js
COPY ./index-1.js /opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/index.js

# Removing zypper repos
RUN zypper rr caspiancs_common

EXPOSE 5601

RUN chmod +x /opt/log-management/kibana/run.sh
RUN chmod +x /opt/log-management/kibana/dashboard.sh
RUN chmod +x /opt/log-management/kibana/set_decrypted_pwd.sh
RUN chmod +x /opt/log-management/kibana/keystone_decrypt.py

ENTRYPOINT /opt/log-management/kibana/run.sh
