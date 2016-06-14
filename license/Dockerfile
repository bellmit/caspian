# Copyright (c) 2015 EMC Corporation
# All Rights Reserved
# This software contains the intellectual property of EMC Corporation
# or is licensed to EMC Corporation from third parties.  Use of this
# software and the intellectual property contained therein is expressly
# limited to the terms and conditions of the License Agreement under which
# it is provided by or on behalf of EMC.
#
# Caspian Common Service : ECI Licensing Service Image 
#
FROM asdrepo.isus.emc.com:8091/neutrino-base/java8:1.0.0.102.6fb02c3

MAINTAINER mehroa3

RUN mkdir -p /opt/caspian/license

COPY build/install/license /opt/caspian/license/

RUN mkdir -p /license/data/raw
 
EXPOSE 8765

WORKDIR /opt/caspian/license

ENTRYPOINT ["bin/license"]
