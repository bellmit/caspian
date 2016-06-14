#!/bin/bash

LOGGER_FILE=backgroundHA.log
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
		
    printf "Get the CRS URL $COMPONENT_REGISTRY"
    
	printf "Getting $component_name endpoints from CRS"
	ENDPOINTS=$( curl -k -s $COMPONENT_REGISTRY/v1/services/platform/components/$component_name | python -m json.tool |grep '"url"')
	if [ $? -ne 0 ]; then
		printf "Failed to get $component_name endpoints"
		returnFailure
	fi
	ETCD_SERVICE=`echo $( echo $ENDPOINTS | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',')`
	
	printf "ETCD Endpoint received from CRS is $ETCD_SERVICE"
}


callJobs() {
	#If this key exists then other account instance is executing the background job, so exit logging it
	printf "Checking if other account service is executing the background jobs"
	backgroundJob=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-account-backgroundJobs | python -m json.tool | grep "errorCode")
	errorCode=$( echo $backgroundJob | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	if [ "$errorCode" != "100" ] ; then
		printf "The key backgroundJobs is set with ETCD, So exiting"
		exit 0
	else
		printf "Setting backgroundJob key with ETCD "
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-account-backgroundJobs -X PUT -d value=True -d ttl=82800
		if [ $? -ne 0 ]; then
			printf "Failed to set backgroundJob with ETCD endpoint $ETCD_SERVICE"
			returnFailure
		fi
		printf "backgroundJob set with ETCD"
		printf "Starting Background Jobs "
		#Call the background job scripts here
		#Commenting as not required in 1.0
		#/opt/caspian/scripts/background-sync.py &
		/opt/caspian/scripts/dbcleanup.py &
		printf "Background Jobs initiated"
		
	fi
}		

initLogger
getComponentHostPortDetails "$etcdComponent"
callJobs

		
	