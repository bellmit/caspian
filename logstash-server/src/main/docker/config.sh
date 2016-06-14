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

set -e

ES_ENDPOINTS=($( ${CRS_INTEGRATION_SCRIPT} elasticsearch api_endpoint))
CRS_ENDPOINT=$( echo $COMPONENT_REGISTRY | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}:[1-9][0-9]*')
VIP=( $( echo $CRS_ENDPOINT | awk -F":" '{print $1}') )
DEFAULT_ES_PORT=9200

if [ ${#ES_ENDPOINTS[@]} -gt 0 ] && [ ${#VIP[@]} -gt 0 ]
then
  ES_ENDPOINT=${ES_ENDPOINTS[0]}
  ES_URL=${VIP[0]}
  ES_PORT=$( echo $ES_ENDPOINT | awk -F":" '{print $2}')
elif  [ ${#VIP[@]} -gt 0 ]
then
  ES_URL=${VIP[0]}
  ES_PORT=$DEFAULT_ES_PORT
elif [ ${#ES_ENDPOINTS[@]} -gt 0 ]
then
  ES_ENDPOINT=${ES_ENDPOINTS[0]}
  ES_URL=$( echo $ES_ENDPOINT | awk -F":" '{print $1}')
  ES_PORT=$( echo $ES_ENDPOINT | awk -F":" '{print $2}')
fi

sed -i "s/ES_URL/${ES_URL}/" "/opt/log-management/logstash/logstash-${LS_VERSION}/logstash.conf"
sed -i "s/ES_PORT/${ES_PORT}/" "/opt/log-management/logstash/logstash-${LS_VERSION}/logstash.conf"

# Creating directory for logstash log file
mkdir -p /var/log/caspian
# Starting the logstash server
# Using --quiet, only errors will show up.
exec /opt/log-management/logstash/logstash-${LS_VERSION}/bin/logstash -f /opt/log-management/logstash/logstash-${LS_VERSION}/logstash.conf --log /var/log/caspian/logstash-server.log --quiet

