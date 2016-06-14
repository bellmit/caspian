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

#/bin/bash

component_name=$1
request_param=$2
component_registry_url=$COMPONENT_REGISTRY
platform_component_url=$component_registry_url/v1/services/platform/components/$component_name


WAIT_FOR_COMPONENT()
{

while :
do
   http_status=$(curl -I $platform_component_url |  grep 'HTTP' | awk '{print $2}')
   if [ "$http_status" != "" ];
   then
     if [ $http_status == "200" ] ;
     then
        endpoints=($(curl -XGET $platform_component_url | sed -e 's/.*endpoints":\[//' -e 's/].*$//' | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}:[1-9][0-9]*'))
        if [ ${#endpoints[@]} -gt 0 ];
        then
          break;
        fi
     fi
   fi
   sleep 5
done

}

GET_API_ENDPOINT()
{
   json_entry=$( curl -XGET $platform_component_url )
   endpoint=$( python json_parser.py $json_entry | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}:[1-9][0-9]*' )
   echo $endpoint
}

GET_ENDPOINTS()
{
   endpoints=$(curl -XGET $platform_component_url | sed -e 's/.*endpoints":\[//' -e 's/].*$//' | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}:[1-9][0-9]*')
   echo $endpoints
}


GET_NODES()
{
   nodes=$(curl -XGET $platform_component_url | sed -e 's/.*endpoints":\[//' -e 's/].*$//' | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}')
   echo $nodes
}


GET_VIP()
{
   vip=$(curl -XGET $platform_component_url | sed -e 's/.*balance":\[//' -e 's/].*$//' | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}')
   echo $vip
}

$(WAIT_FOR_COMPONENT)

if [ "$request_param" == "endpoints" -o "$request_param" == "" ];
then
 echo $(GET_ENDPOINTS)
fi

if [ "$request_param" == "vip" -o "$request_param" == "" ];
then
 echo $(GET_VIP)
fi

if [ "$request_param" == "api_endpoint" -o "$request_param" == "" ];
then
 echo $(GET_API_ENDPOINT)
fi

if [ "$request_param" == "nodes" ];
then
 echo $(GET_NODES)
fi


