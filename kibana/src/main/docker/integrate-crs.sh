#!/bin/bash

component_name=$1
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

GET_URL()
{
url=$(curl -XGET $platform_component_url | sed -e 's/.*endpoints":\[//' -e 's/].*$//' | grep -o 'http*://[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}:[1-9][0-9]*')
echo $url
}

$(WAIT_FOR_COMPONENT)
echo $(GET_URL)
