# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#
# Caspian Common Service : ECI DB controller Image 
#

FROM asdrepo.isus.emc.com:8091/neutrino-base/java8:1.0.0.102.6fb02c3
MAINTAINER poovanna

RUN mkdir -p /opt/caspian/db_controller

COPY build/install/db_controller /opt/caspian/db_controller/

EXPOSE 6666

ENTRYPOINT ["/opt/caspian/db_controller/bin/db_controller"]
