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

set -e 

${KIBANA_INFO_REPLACER}

/opt/log-management/kibana/dashboard.sh
/opt/log-management/kibana/set_decrypted_pwd.sh
