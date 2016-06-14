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
#!/bin/bash

set -e 

elasticsearch_url=($(python ${CRS_INTEGRATION_SCRIPT} elasticsearch))
keystone_url=($(python ${CRS_INTEGRATION_SCRIPT} keystone))
export_url=($(python ${CRS_INTEGRATION_SCRIPT} export))
export_download_url=($(python ${CRS_INTEGRATION_SCRIPT} export_download))

SUBSTITUTE_ENV_ELASTICSEARCH()
{
  url=$1;
  sed -i "s#elasticsearch_url#${url}#g#" "/opt/log-management/kibana/dashboard.sh"
  sed -i "s;^elasticsearch_url:.*;elasticsearch_url: \"${url}\";" "/opt/log-management/kibana/kibana-${KIBANA_VERSION}/config/kibana.yml"
}

SUBSTITUTE_ENV_EXPORT()
{
  url=$1
  sed -i "s#export_url#${url}#g#" "/opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/index.js"
}
SUBSTITUTE_ENV_EXPORT_DOWNLOAD()
{
  url=$1
  sed -i "s#export_download_url#${url}#g#" "/opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/public/index.js"
}

if [ ${#export_download_url[@]} -gt 0 ];
then
  url=${export_download_url[0]}
  $( SUBSTITUTE_ENV_EXPORT_DOWNLOAD $url)
fi


if [ ${#export_url[@]} -gt 0 ];
then
  url=${export_url[0]}
  $( SUBSTITUTE_ENV_EXPORT $url)
fi

SUBSTITUTE_ENV_KEYSTONE() 
{
  url=$1
  sed -i "s#keystone_url#${url}#g#" "/opt/log-management/kibana/kibana-${KIBANA_VERSION}/src/app.js"
}

if [ ${#elasticsearch_url[@]} -gt 0 ]; 
then
  url=${elasticsearch_url[0]}
  $( SUBSTITUTE_ENV_ELASTICSEARCH $url) 
fi

if [ ${#keystone_url[@]} -gt 0 ];
then
  url=${keystone_url[0]}
  $( SUBSTITUTE_ENV_KEYSTONE $url)
fi

