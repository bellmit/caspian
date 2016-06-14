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
#!/bin/sh

# export the decrypted password so that it can be used for kibana

decrypted=$(python keystone_decrypt.py 2>&1)
export CSA_PWD=$decrypted
if [ -z "${CSA_PWD}" ] ; then
    echo "Can not decrypt pwd successfully"
    exit 1
fi

/opt/log-management/kibana/kibana-${KIBANA_VERSION}/bin/kibana





