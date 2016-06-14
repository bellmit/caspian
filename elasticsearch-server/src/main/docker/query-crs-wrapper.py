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

import json
import sys

json_data = json.loads(sys.argv[1])


endpoints_str = ""
for endpoint in json_data['endpoints']:
  if endpoint['name']=='API':
    endpoints_str = endpoint['url'] + " " + endpoints_str


print endpoints_str

