#!/bin/bash

LOGGER_FILE=backgroundTokenFlush.log
etcdComponent=etcd-service
COMPONENT_REGISTRY=${CRS_IP}

initLogger() {

	if [ ! -d /var/log/caspian ];
	then 
   		mkdir -p /var/log/caspian
	fi
   
	if [ ! -f /var/log/caspian/$LOGGER_FILE ];
	then 
  		touch /var/log/caspian/$LOGGER_FILE
	fi

}

printf(){ 
    echo $*
	echo $(date -u) $* >> /var/log/caspian/$LOGGER_FILE
}

returnFailure() {
    exit 1
}

getComponentHostPortDetails(){
    
    component_name=$1
    
    #1. get CRS ip address
	if [ ! "$COMPONENT_REGISTRY" ] ; then
		printf "CRS endpoint is not set, exit"
		returnFailure
	fi	
		
	# Getting $component_name endpoints from CRS
	ENDPOINTS=$( curl -k -s $COMPONENT_REGISTRY/v1/services/platform/components/$component_name | python -m json.tool |grep '"url"')
	if [ $? -ne 0 ]; then
		printf "Failed to get $component_name endpoints"
		returnFailure
	fi
	ETCD_SERVICE=`echo $( echo $ENDPOINTS | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',')`
	# printf "ETCD Endpoint received from CRS is $ETCD_SERVICE"
}

callJobs() {
	#If this key exists then other keystone instance is executing the background job, so exit logging it
	backgroundJob=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-backgroundJobs | python -m json.tool | grep "errorCode")
	errorCode=$( echo $backgroundJob | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	if [ "$errorCode" != "100" ] ; then
		printf "The key backgroundJobs is set with ETCD, So exiting"
		exit 0
	else
		# Setting backgroundJob key with ETCD
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-account-backgroundJobs -X PUT -d value=True -d ttl=3300
		if [ $? -ne 0 ]; then
			printf "Failed to set backgroundJob with ETCD endpoint $ETCD_SERVICE"
			returnFailure
		fi
		#Call the background job scripts here
		/etc/keystone/scripts/TokenFlush.py &
	fi
}		

initLogger
getComponentHostPortDetails "$etcdComponent"
callJobs
