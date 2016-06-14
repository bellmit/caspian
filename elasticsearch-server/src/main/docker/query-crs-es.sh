#!/bin/bash

component_registry_url=$COMPONENT_REGISTRY
query_es_url=$component_registry_url/v1/services/platform/components/elasticsearch
num_es_nodes_check=3
num_of_attempts_crs_check=10

##Checks whether Elasticsearch is registered into CRS
CHECK_ES_REGISTERED()
{
  http_status_code=`curl --write-out %{http_code} --silent --output /dev/null $query_es_url`
  if [ $http_status_code -eq 200 ]
  then
    echo "true"
  else
    echo "false"
  fi
}

##Gets Elasticsearch Ips from CRS.Waits for other ES nodes to register in CRS,if not goes ahead with available IPs
GET_CRS_ES_INFO()
{
 json_entry=$( curl -XGET $query_es_url )
 nodes_up=$(python $CRS_PARSER_PYTHON_SCRIPT $json_entry | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}'  | wc -l)
 #nodes_up=$(curl -XGET  $query_es_url | sed -e 's/.*endpoints":\[//' -e 's/].*$//' | grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}'  | wc -l)
 if [ $nodes_up -ge $num_es_nodes_check ]
 then
   #ELASTICSEARCH_IPS=$(curl -XGET  $query_es_url |sed -e 's/.*endpoints":\[//' -e 's/].*$//' |sed -e 's/CONTAINER_HOST_ADDRESS//'| grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}')
   ELASTICSEARCH_IPS=$(python $CRS_PARSER_PYTHON_SCRIPT $json_entry |sed -e 's/CONTAINER_HOST_ADDRESS//'| grep -o '[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}\.[0-9]\{1,3\}')
   ELASTICSEARCH_IPS=$(echo $ELASTICSEARCH_IPS|sed -e 's/ /\,/g')
   sed -i "s#ELASTICSEARCH_IPS#${ELASTICSEARCH_IPS}#" "${ES_DIR}/bin/config-es-cluster"
   ${ES_DIR}/bin/config-es-cluster
   break
 elif [ $num_es_nodes_check -gt 1 ]
 then
   sleep 2
   let num_es_nodes_check-=1
   `GET_CRS_ES_INFO`
 else
   break
 fi
}



##Waits for two seconds if Elasticsearch is not registered in CRS, if not found goes ahead for starting elasticsearch
while [ $num_of_attempts_crs_check -ge 1 ]
do
  is_es_registered=`CHECK_ES_REGISTERED`
  if [ "$is_es_registered" = "true" ]
  then
    `GET_CRS_ES_INFO`
     break
  else
    let num_of_attempts_crs_check-=1
    echo "Unable to get Elasticsearch node IPs from CRS ,trying again...."
    sleep 2
  fi
done
