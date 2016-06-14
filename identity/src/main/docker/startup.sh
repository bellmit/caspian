#!/bin/bash

LOGGER_FILE=keystoneinstall.log

KEYSTONE_URL=https://127.0.0.1:6100/v3

ADMIN_TOKEN=$(date | md5sum | awk '{print $1}')

#default users
CLOUD_ADMIN_USER=admin
CPSA_USER=cpsa
CLOUD_SERVICE_USER=csa

PROJECT_ADMIN=admin
PROJECT_SERVICE=service

#supported role
ROLE_ADMIN=admin
ROLE_OPENSTACK_ADMIN=openstack_admin
ROLE_SERVICE=service
ROLE_MONITOR=monitor
ROLE_NEUTRINO_ACCOUNTS_OWNER=neutrino_accounts_owner


AES_ENCRYPT=aes_encrypt.py

DBNAME=keystone
DBUSER=keystoneadmin

DBPWD=`$AES_ENCRYPT --e decrypt --data ${ENCRYPTED_KEYSTONE_DB_PASS}`
    if [ $? -ne 0 ] ; then
     printf "failed to decrypt keystoneadmin pwd"
     exit 1
    fi

MYSQL_GALERA_COMPONENT=mysql-galera
DB_CONTROLLER_COMPONENT=db-controller
PROTOCOL=http
ETCDNAME=etcd-service
LB_PROTOCOL=${LB_PROTOCOL:-https}

cleanup() {

 if [ ! -f /etc/keystone/keystone.conf ]; then
    printf "Can not get keystone.conf"
 else
   sed -i '/admin_token/d' /etc/keystone/keystone.conf
 fi
}

returnFailure() {
    cleanup
    curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgrading -X DELETE
            if [ $? -ne 0 ]; then
                printf "Failed to set dbupgrading with etcd"
            fi
            printf "key keys/keystonedbupgrading deleted from ETCD"   
    exit 1
}

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
	echo $(date -u) INFO $* >> /var/log/caspian/$LOGGER_FILE
}

#using "aes_encrypt" cli provided by python encryption library
decrypt(){

  echo "decrypt variables"
   if [[ ( -z "${KS_ADMIN_PWD}" ) || ( -z "${KS_CPSA_PWD}" ) || ( -z "${KS_CSA_PWD}" ) ]] ; then
    printf "Invaild pwd to decrypt"
    returnFailure
  fi

  if [ -x "$AES_ENCRYPT" ] ; then
     printf "failed to find decryption cli"
     exit $status
  fi

  #only one success case from $AES_ENCRYPT.

  CLOUD_ADMIN_PWD=`$AES_ENCRYPT --e decrypt --data ${KS_ADMIN_PWD}`
   if [ $? -ne 0 ] ; then
   printf "failed to decrypt admin"
   returnFailure
   fi

  CPSA_PWD=`$AES_ENCRYPT --e decrypt --data ${KS_CPSA_PWD}`
  if [ $? -ne 0 ] ; then
   printf "failed to decrypt cpsa"
   returnFailure
   fi

  CLOUD_SERVICE_PWD=`$AES_ENCRYPT --e decrypt --data ${KS_CSA_PWD}`
  if [ $? -ne 0 ] ; then
   printf "failed to decrypt csa"
   returnFailure
   fi

  if [[ ( -z "${CLOUD_ADMIN_PWD}" ) || ( -z "${CPSA_PWD}" ) || ( -z "${CLOUD_SERVICE_PWD}" ) ]] ; then
    printf "Can not decrypt pwd successfully"
    returnFailure
  fi

}


getComponentHostPortDetails(){
    
    component_name=$1
    
    #1. get CRS ip address
    printf "Get the CRS URL $COMPONENT_REGISTRY"
    
	printf "Getting $component_name endpoints from CRS"
	ENDPOINTS="`echo $(curl -k -s -H content-type:application/json $COMPONENT_REGISTRY/v1/services/platform/components/$component_name | python -c "import sys, json; obj=json.load(sys.stdin); endpoints=obj['endpoints']; print [endpoint['url'] for endpoint in endpoints if endpoint['published']]") | sed 's/.*\[//;s/\].*//;' | sed 's|["'u\'']||g'`"
	if [ $? -ne 0 ]; then
		printf "Failed to get $component_name endpoints"
		returnFailure
	fi
	
	printf "$component_name ENDPOINTS List received from the CRS are :$ENDPOINTS"
	ENDPOINT=$( echo $ENDPOINTS | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	printf "Chosen $component_name Endpoint for making requests: $ENDPOINT"

	#get the proto field
	PROTO="`echo $ENDPOINT | grep '://' | sed -e's,^\(.*://\).*,\1,g'`"
	# remove the protocol field
	HOSTPORT=`echo $ENDPOINT | sed -e s,$PROTO,,g`
	printf "$component_name Host:Port is : $HOSTPORT"

	#Get the host ip and port field
	PORT=`echo $HOSTPORT | grep : | cut -d: -f2`
	HOST=`echo $HOSTPORT | grep : | cut -d: -f1`
	
    printf "$component_name PORT to be updated to the configuration files : $PORT"
    printf "$component_name HOST to be updated to the configuration files : $HOST"

	HOST_NAME=$HOST
    PORT_NO=$PORT
}

updateKeystoneConf(){
  
  	MYSQL_DB=$1
  	MYSQL_PORT=$2
  	
    # copy the keystone.conf.template to the keystone.conf
    cp -p /etc/keystone/keystone.conf.template /etc/keystone/keystone.conf
    if [ $? -ne 0 ]; then
        printf "failed to copy keystone.conf.template to keystone.conf file"
        returnFailure
    fi
	
    # update the ip address received to the keystone.conf
	printf "Embedding mysql address and port into /etc/keystone/keystone.conf"
	sed -i "s#\${MYSQL_HOST}#$MYSQL_DB#" /etc/keystone/keystone.conf
	if [ $? -ne 0 ]; then
	    printf "Failed to embed mysql ip into the keystone.conf file"
	    returnFailure
	fi
	
	sed -i "s#\${MYSQL_PORT}#$MYSQL_PORT#" /etc/keystone/keystone.conf
	if [ $? -ne 0 ]; then
	      printf "Failed to embed mysql port into the keystone.conf file"
	      returnFailure
	fi
	printf "Embedded mysql address and port into keystone.conf successfully."
	
	sed -i "s#\${KDB_PASS}#$DBPWD#" /etc/keystone/keystone.conf
	if [ $? -ne 0 ]; then
	      printf "Failed to embed keystoneadmin password into the keystone.conf file"
	      returnFailure
	fi
	printf "Embedded keystoneadmin password into keystone.conf successfully."
	
	printf "Embedding public endpoint in keystone.conf"
    sed -i "s#^\#public_endpoint =.*#public_endpoint = ${LB_PROTOCOL}://${VIP_FQDN}:6100#" /etc/keystone/keystone.conf
    if [ $? -ne 0 ]; then
        printf "Failed to embed public endpoint into the keystone.conf file"
        returnFailure
    fi
    
    printf "Embedding admin endpoint in keystone.conf"
    sed -i "s#^\#admin_endpoint =.*#admin_endpoint = ${LB_PROTOCOL}://${VIP_FQDN}:35357#" /etc/keystone/keystone.conf
    if [ $? -ne 0 ]; then
        printf "Failed to embed admin endpoint into the keystone.conf file"
        returnFailure
    fi
}

updateKeystoneConfOnce (){
 #update admin port in keystone.conf file
    printf "Embedding admintoken to keystone.conf"
    sed -i '/admin_token/s/= .*/= '${ADMIN_TOKEN}'/' /etc/keystone/keystone.conf
    if [ $? -ne 0 ]; then
        printf "Failed to embed admin token into the keystone.conf file"
        returnFailure
    fi
}	

getConfFiles () {
	#Get policy.json file from ETCD
    printf "Skipped Bootstrapping"
	fval=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-policyfile)
	if [ $? -ne 0 ]; then
        printf "Failed to fetch policy.json file from etcd"
        returnFailure
    fi
	temp=$(python -c "import json; js=$fval; print js['node']['value']")
    echo $temp | xxd -r -p > etc/keystone/policy.json
	printf "Embedded policy.json in keystone"
	fval=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-conffile)
	if [ $? -ne 0 ]; then
        printf "Failed to fetch keystone.conf file from etcd"
        returnFailure
    fi
	temp=$(python -c "import json; js=$fval; print js['node']['value']")
	echo $temp | base64 -d  > /etc/keystone/keystone.conf
	
	printf "keystone.conf file retrieved from ETCD"
}	

initializeKeystoneDatabse(){

	printf "Create keystone database"
            
    #Get the controller URI and create DB      
    getComponentHostPortDetails "$DB_CONTROLLER_COMPONENT"
	printf "DB Controller Host IP :$HOST_NAME"
	printf "DB controller PORT :$PORT_NO"
	CNTRL_URI="$PROTOCOL://$HOST_NAME:$PORT_NO"
    
	printf "Chosen DB controller URI create db requests : $CNTRL_URI"
    RESP=$(curl  -sw '%{http_code}' -i -X POST -H "Content-Type: application/json" -d '{"databases": [ {"database_name": "'$DBNAME'" ,"user_name": "'$DBUSER'" ,"password": "'$DBPWD'" } ]}' $CNTRL_URI/v1/databases | head -n 1| cut -d $' ' -f2)
    if [ $? -ne 0 ]; then
        printf "Failed to create the keystone database"
        returnFailure
    fi
	printf "Received :$RESP status code for keystone database creation"
	
	if [ $RESP -eq 409 ]; then
        printf "Database with name keystone already exists please delete the existing database inorder to proceed with installation"
	fi
	
	if [ $RESP -eq 200 ]; then
       	printf "Keystone Database created successfully"
	else
        printf "Failed to create keystone Database"
        returnFailure
	fi

}

getEtcdEndpoint(){
	component_name=$1

	printf "The CRS url is $COMPONENT_REGISTRY"

	printf "getting $component_name endpoints from CRS"
	ENDPOINTS=$( curl -k -s $COMPONENT_REGISTRY/v1/services/platform/components/$component_name | python -m json.tool |grep '"url"')
	if [ $? -ne 0 ]; then
		printf "Failed to get $component_name endpoints from CRS"
		returnFailure
	fi
	printf "$component_name ENDPOINTS List received from the CRS are :$ENDPOINTS"
	ENDPOINT=$( echo $ENDPOINTS | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	printf "Chosen $component_name Endpoint for making requests: $ENDPOINT"

	ETCD_SERVICE=$ENDPOINT
}

# Token clean up cron job needs CRS IP for getting etcd endpoints
setEnvForCronJob(){
	sed -i "s#\${CRS_IP}#$COMPONENT_REGISTRY#" /etc/keystone/scripts/CronJobs.sh
        if [ $? -ne 0 ]; then
            printf "Failed to pass CRS IP to Cron Job"
            returnFailure
        fi
        printf "Successfully passed CRS IP to Cron Job"
}

initLogger
printf "Get ETCD endpoint from CRS"
getEtcdEndpoint "$ETCDNAME"
setEnvForCronJob

if [ ! -f /root/install.status ] ; then

	#check to see if ETCD is present
	if [ ! "$ETCD_SERVICE" ] ; then
		printf "ETCD not available, exit"
		returnFailure
	fi
	printf "ETCD endpoint is $ETCD_SERVICE"	

	#If this key exists then other keystone instance is initializing db, so wait
	initializing=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitializing | python -m json.tool | grep "errorCode")
	errorCode=$( echo $initializing | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	while [ "$errorCode" != "100" ]
	do
		printf "waiting for keystone bootstrapping to finish"
		sleep 10s
		initializing=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitializing | python -m json.tool | grep "errorCode")
		errorCode=$( echo $initializing | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	done

	#checking whether keystone db is initialized
	printf "Checking if key ccs-keystone-keystonedbInitialized is set with ETCD"
	initialized=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitialized | python -m json.tool | grep "value")
	initValue=$( echo $initialized | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
		
	printf "The initValue from ETCD is $initValue ."
	if [ ! "$initValue" ] ; then
		#Setting value with ETCD for no initialization collisions
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitializing -X PUT -d value="initializing db"
		if [ $? -ne 0 ]; then
			printf "Failed to set dbInitializing with etcd"
			returnFailure
		fi
		printf "keystonedbInitializing set with ETCD"
		
		#check to see if LB_IP is set
		if [ ! "$LB_IP" ] ; then
				printf "LB_IP is not present"
				returnFailure
		fi
		printf "LB_IP address is $LB_IP"

		getComponentHostPortDetails "$MYSQL_GALERA_COMPONENT"
		printf "DB NAME :$HOST_NAME"
		printf "DB PORT :$PORT_NO"

		printf "Updating keystone.conf"
		updateKeystoneConf "$HOST_NAME" "$PORT_NO"	
		
		printf "Initializing keystone"        
		initializeKeystoneDatabse
		
		printf "Bootstrapping keystone database"
		keystone-manage db_sync
		keystone-manage bootstrap --bootstrap-password admin123
		if [ $? -ne 0 ]; then
			printf "keystone-manage db_sync: Failed"
			returnFailure
		fi
		printf "Successfully bootstrapped keystone database"

		printf "updating keystone conf one more time"
		updateKeystoneConfOnce

		#decrypt admin, cpsa, csa password
		decrypt

		echo "Starting httpd process"
		set -m
		/usr/sbin/apache2ctl -DFOREGROUND &
		sleep 5
		
		#generate metadata of identity provider keystone 
		keystone-manage saml_idp_metadata > /etc/keystone/saml2_idp_metadata.xml
		
		if [ $? -ne 0 ]; then
                        printf "keystone-manage saml_idp_metadata: Failed"
                        returnFailure
		fi
		printf "Successfully generated keystone metadata"


		# create all the roles    
		echo "Getting admin role ID"
				fval=$(curl -s -k -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/roles?name=admin | python -m json.tool | grep '"id"' )
				ADMIN_ROLE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	    echo "Got admin role ID $ADMIN_ROLE_ID"

		echo "Creating openstack admin role"
		ROLE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"role":{"name":"'$ROLE_OPENSTACK_ADMIN'"}}' $KEYSTONE_URL/roles | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
				echo "Failed to create openstack admin role"
				returnFailure
		fi
		OPENSTACK_ADMIN_ROLE_ID=$( echo $ROLE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Creating service role"
		ROLE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"role":{"name":"'$ROLE_SERVICE'"}}' $KEYSTONE_URL/roles | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create service role"
			returnFailure
		fi
		SERVICE_ROLE_ID=$( echo $ROLE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Creating monitor role"
		ROLE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"role":{"name":"'$ROLE_MONITOR'"}}' $KEYSTONE_URL/roles | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create monitor role"
			returnFailure
		fi
		MONITOR_ROLE_ID=$( echo $ROLE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Creating neutrino_accounts_owner role"
		ROLE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"role":{"name":"'$ROLE_NEUTRINO_ACCOUNTS_OWNER'"}}' $KEYSTONE_URL/roles | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create neutrino accounts owner role"
			returnFailure
		fi
		NEUTRINO_ACCOUNTS_OWNER_ROLE_ID=$( echo $ROLE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		# create projects
		echo "Getting admin project ID"
			   fval1=$(curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/projects?name=admin | python -m json.tool | grep '"id"' )
        PROJECT_ADMIN_ID=$( echo $fval1 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	    echo "Got Project $PROJECT_ADMIN_ID"

		echo "Creating project service"
		PROJECT_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"project": {"enabled": true,"domain_id": "default","name": "'$PROJECT_SERVICE'"}}' $KEYSTONE_URL/projects | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create service project in default domain"
			returnFailure
		fi
		PROJECT_SERVICE_ID=$( echo $PROJECT_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )


		# create users
		echo "Getting cloud admin ID"
				fval2=$(curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/users?domain_id=default | python -m json.tool | grep '"id"' )
        CLOUD_ADMIN_ID=$( echo $fval2 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
		echo "Got $CLOUD_ADMIN_ID"

		echo "Associating admin role to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_ADMIN_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role to cloud admin user"
			returnFailure
		fi
		
		echo "Associating openstack_admin role to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_ADMIN_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role to cloud admin user"
			returnFailure
		fi

		unset CLOUD_ADMIN_PWD

		echo "Associating admin role on admin project to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CLOUD_ADMIN_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role on admin project to cloud admin user"
			returnFailure
		fi

		echo "Associating openstack_admin role on admin project to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CLOUD_ADMIN_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role on admin project to cloud admin user"
			returnFailure
		fi

		echo "Creating cpsa user"
		USER_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"user":{"description":"Privileged Service User", "domain_id":"default","enabled":true,"name":"'$CPSA_USER'","password":"'$CPSA_PWD'"}}' $KEYSTONE_URL/users | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create cpsa user"
			returnFailure
		fi
		CPSA_USER_ID=$( echo $USER_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Associating admin role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role to cpsa user"
			returnFailure
		fi

		echo "Associating neutrino_accounts_owner role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${NEUTRINO_ACCOUNTS_OWNER_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate neutrino_accounts_owner role to cpsa user"
			returnFailure
		fi

		echo "Associating openstack_admin role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role to account admin user"
			returnFailure
		fi

		echo "Associating admin role on admin project to cpsa user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CPSA_USER_ID}/roles/${ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate admin role on admin project to cpsa user"
				returnFailure
			fi

		echo "Associating openstack_admin role on admin project to cpsa user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CPSA_USER_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate openstack_admin role on admin project to cpsa user"
				returnFailure
			fi

		unset CPSA_PWD

		# creating csa user; it will be used by other components (read only to KS)
		echo "Creating cloud service user"
		SERVICE_USER_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"user":{"description":"Service user", "domain_id":"default",
	"enabled":true,"name":"'$CLOUD_SERVICE_USER'","password":"'$CLOUD_SERVICE_PWD'"}}' $KEYSTONE_URL/users | python -m json.tool | grep '"id"' )
			if [ $? -ne 0 ]; then
				echo "Failed to create cloud service user"
				returnFailure
			fi
		CLOUD_SERVICE_ID=$( echo $SERVICE_USER_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Associating service role to cloud service user"
		curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_SERVICE_ID}/roles/${SERVICE_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate service role to cloud sevice user"
				returnFailure
			fi

                echo "Associating monitor role to cloud service user in default domain"
                curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_SERVICE_ID}/roles/${MONITOR_ROLE_ID}
                        if [ $? -ne 0 ]; then
                                echo "Failed to associate monitor role to cloud sevice user"
                                returnFailure
                        fi

		 echo "Associating admin role on service project to cloud service user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_SERVICE_ID}/users/${CLOUD_SERVICE_ID}/roles/${ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate admin role on service project to cloud service user"
				returnFailure
			fi

                 echo "Associating monitor role to cloud service user on service project of default domain"
                        curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_SERVICE_ID}/users/${CLOUD_SERVICE_ID}/roles/${MONITOR_ROLE_ID}
                        if [ $? -ne 0 ]; then
                                echo "Failed to associate monitor role on service project to cloud service user"
                                returnFailure
                        fi

		unset CLOUD_SERVICE_PWD

		# create keystone service and endpoint urls
		echo "Creating service entry for identity"
		SERVICE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"service": {"type": "identity", "name": "keystone"}}' $KEYSTONE_URL/services | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create service entry for identity"
			returnFailure
		fi
		IDENTITY_SERVICE_ID=$( echo $SERVICE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

                echo "Creating regionV2 for v2.0 endpoints"
                curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"region": {"description": "Region to which keystone v2.0 endpoints are associated", "id": "regionV2"}' $KEYSTONE_URL/regions
                if [ $? -ne 0 ]; then
                        echo "Failed to regionV2"
                        returnFailure
                fi

		echo "Creating v3 identity endpoints"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "public", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create public v3 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "internal", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create internal v3 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "admin", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create admin v3 endpoint for identity"
			returnFailure
		fi

		echo "Creating v2.0 identity endpoints"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "public", "name": "keystone", "region": "regionV2", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create public v2.0 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "internal", "name": "keystone", "region": "regionV2","url":"'$LB_PROTOCOL'://'$VIP_FQDN':35357/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create internal v2.0 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "admin", "name": "keystone", "region": "regionV2", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':35357/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create admin v2.0 endpoint for identity"
			returnFailure
		fi

		# Embed project IDs to allow identity v2 clients to act as cloud admin, monitor and service
		echo "Embedding service project id into policy.json"
		sed -i "s#\${PROJECT_SERVICE_ID}#$PROJECT_SERVICE_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed service project's id into the policy.json file"
			returnFailure
		fi

		echo "Embedding admin project id into policy.json"
		sed -i "s#\${PROJECT_ADMIN_ID}#$PROJECT_ADMIN_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed admin project's id into the policy.json file"
			returnFailure
		fi

		# Embed reserved user ids into policy.json to prevent accidental deletes
		echo "Embedding cloud admin id into policy.json"
		sed -i "s#\${CLOUD_ADMIN_ID}#$CLOUD_ADMIN_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed cloud admin id into the policy.json file"
			returnFailure
		fi

		echo "Embedding account admin id into policy.json"
		sed -i "s#\${CPSA_USER_ID}#$CPSA_USER_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed account admin id into the policy.json file"
			returnFailure
		fi
		
		echo "Embedding cloud service id into policy.json"
		sed -i "s#\${CLOUD_SERVICE_ID}#$CLOUD_SERVICE_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed cloud service id into the policy.json file"
			returnFailure
		fi
		
		cleanup
		unset ADMIN_TOKEN
		
		#sharing policy.json file with all other keystone nodes
		printf "Sharing policy.json file across all nodes"
		encodedValue=$(xxd -p /etc/keystone/policy.json)
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-policyfile -X PUT --data-urlencode value="$encodedValue"
		if [ $? -ne 0 ]; then
			printf "Failed to share policy.json across all nodes"
			returnFailure
		fi
		printf "Policy.json shared"
		
		#need to share keystone.conf through ETCD
		printf "Sharing keystone.conf file across all nodes"
		encodedValue=$(base64 -w0 /etc/keystone/keystone.conf)
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-conffile -X PUT --data-urlencode value="$encodedValue"
		if [ $? -ne 0 ]; then
			printf "Failed to share keystone.conf file across all nodes"
			returnFailure
		fi
		printf "keystone.conf shared"

		#setting dbInitialized with etcd for single time bootstrapping.
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitialized -X PUT -d value="initialized"
		if [ $? -ne 0 ]; then
			printf "Failed to set dbInitialized with etcd"
			returnFailure
		fi
		printf "Flag set with ETCD for notifying that bootstrapping is done"
		
		
		sleep 5 
		
		curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbinitializing -X DELETE
		if [ $? -ne 0 ]; then
			printf "Failed to set dbInitializing with etcd"
			returnFailure
		fi
		printf "key keys/keystonedbInitializing deleted from ETCD"
		
		
		#to handle container restart scenario
		echo "success" > /root/install.status
		echo "Keystone bootstrapped successfully"

		# bring httpd process back to foreground to make it the main docker process
		fg
    
	else
	    
	    echo "Checking if db is latest one"
	    command=`keystone-manage db_version` 
	    echo "got db version as $command"
	    
	    if [ $command -ne 96 ] ; then
	    
	    echo "DB version is not latest hence proceeding with upgrade"
	    
		#If this key exists then other keystone instance is upgrading db, so wait
		echo "Keystone db is initialized , hence proceeding with upgrade"
        upgrading=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgrading | python -m json.tool | grep "errorCode")
        errorCode=$( echo $upgrading | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
        while [ "$errorCode" != "100" ]
        do
            printf "waiting for keystone upgrade to finish"
            sleep 10s
            upgrading=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgrading | python -m json.tool | grep "errorCode")
            errorCode=$( echo $upgrading | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
        done
		#checking whether keystone db is upgraded
		printf "Checking if key ccs-keystone-keystonedbupgraded is set with ETCD"
		upgraded=$(curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgraded | python -m json.tool | grep "value")
        upgradeValue=$( echo $upgraded | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
		
		printf "The upgradeValue from ETCD is $upgradeValue ."
        if [ ! "$upgradeValue" ] ; then
            #Setting value with ETCD for no upgrading collisions
			curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgrading -X PUT -d value="upgrading db"
	        if [ $? -ne 0 ]; then
                printf "Failed to set dbUpgrading with etcd"
                returnFailure
            fi
            printf "keystonedbupgrading set with ETCD"

            #check to see if LB_IP is set
            if [ ! "$LB_IP" ] ; then
                printf "LB_IP is not present"
                returnFailure
            fi
            printf "LB_IP address is $LB_IP"
	
        	getComponentHostPortDetails "$MYSQL_GALERA_COMPONENT"
            printf "DB NAME :$HOST_NAME"
            printf "DB PORT :$PORT_NO"
	
        	printf "Updating keystone.conf"
            updateKeystoneConf "$HOST_NAME" "$PORT_NO"

            printf "Upgrading and Bootstrapping keystone database"
            keystone-manage db_sync
            keystone-manage bootstrap --bootstrap-password admin123
            if [ $? -ne 0 ]; then
                printf "keystone-manage db_sync: Failed"
                returnFailure
            fi
            printf "Successfully updated keystone database"
			
			printf "updating keystone conf one more time"
            updateKeystoneConfOnce
			
			echo "Starting httpd process"
		set -m
		/usr/sbin/apache2ctl -DFOREGROUND &
		sleep 5
			
############## update the policy.json with values from keystone db ##############
			# get and create  the roles not present in older version   
		echo "Getting admin role ID"
		fval=$(curl -s -k -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/roles?name=admin | python -m json.tool | grep '"id"' )
		ADMIN_ROLE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	    echo "Got admin role ID $ADMIN_ROLE_ID"

		echo "Getting openstack admin role"
		fval=$(curl -s -k -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/roles?name=openstack_admin | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
				echo "Failed to get openstack admin role"
				returnFailure
		fi
		OPENSTACK_ADMIN_ROLE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Getting service role"
		fval=$(curl -s -k -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/roles?name=service | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to get service role"
			returnFailure
		fi
		SERVICE_ROLE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Getting monitor role"
		fval=$(curl -s -k -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/roles?name=monitor | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to get monitor role"
			returnFailure
		fi
		MONITOR_ROLE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Creating neutrino_accounts_owner role"
		ROLE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"role":{"name":"'$ROLE_NEUTRINO_ACCOUNTS_OWNER'"}}' $KEYSTONE_URL/roles | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create neutrino accounts owner role"
			returnFailure
		fi
		NEUTRINO_ACCOUNTS_OWNER_ROLE_ID=$( echo $ROLE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		# create projects
		echo "Getting admin project ID"
		fval1=$(curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/projects?name=admin | python -m json.tool | grep '"id"' )
        PROJECT_ADMIN_ID=$( echo $fval1 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
	    echo "Got admin Project $PROJECT_ADMIN_ID"

		echo "Getting project service"
		fval1=$(curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X GET $KEYSTONE_URL/projects?name=service | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to get service project in default domain"
			returnFailure
		fi
		PROJECT_SERVICE_ID=$( echo $fval1 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )


		# Get users
		echo "Getting cloud admin ID"
		fval2=$(curl -k -s "$KEYSTONE_URL/users?domain_id=default&name=admin" -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" | python -m json.tool | grep '"id"')
        CLOUD_ADMIN_ID=$( echo $fval2 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
		echo "Got $CLOUD_ADMIN_ID"

		echo "Associating admin role to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_ADMIN_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role to cloud admin user"
			returnFailure
		fi
		
		echo "Associating openstack_admin role to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_ADMIN_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role to cloud admin user"
			returnFailure
		fi

		unset CLOUD_ADMIN_PWD

		echo "Associating admin role on admin project to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CLOUD_ADMIN_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role on admin project to cloud admin user"
			returnFailure
		fi

		echo "Associating openstack_admin role on admin project to cloud admin user"
		curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CLOUD_ADMIN_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role on admin project to cloud admin user"
			returnFailure
		fi

		echo "Getting cpsa user"
		fval2=$(curl -k -s "$KEYSTONE_URL/users?domain_id=default&name=cpsa" -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" | python -m json.tool | grep '"id"')
		if [ $? -ne 0 ]; then
			echo "Failed to create cpsa user"
			returnFailure
		fi
		CPSA_USER_ID=$( echo $fval2 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Associating admin role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate admin role to cpsa user"
			returnFailure
		fi

		echo "Associating neutrino_accounts_owner role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${NEUTRINO_ACCOUNTS_OWNER_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate neutrino_accounts_owner role to cpsa user"
			returnFailure
		fi

		echo "Associating openstack_admin role to cpsa user"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CPSA_USER_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
		if [ $? -ne 0 ]; then
			echo "Failed to associate openstack_admin role to account admin user"
			returnFailure
		fi

		echo "Associating admin role on admin project to cpsa user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CPSA_USER_ID}/roles/${ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate admin role on admin project to cpsa user"
				returnFailure
			fi

		echo "Associating openstack_admin role on admin project to cpsa user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_ADMIN_ID}/users/${CPSA_USER_ID}/roles/${OPENSTACK_ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate openstack_admin role on admin project to cpsa user"
				returnFailure
			fi

		unset CPSA_PWD

		# Getting csa user; it will be used by other components (read only to KS)
		echo "Getting cloud service user"
		fval2=$(curl -k -s "$KEYSTONE_URL/users?domain_id=default&name=csa" -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" | python -m json.tool | grep '"id"')
			if [ $? -ne 0 ]; then
				echo "Failed to create cloud service user"
				returnFailure
			fi
		CLOUD_SERVICE_ID=$( echo $fval2 | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )

		echo "Associating service role to cloud service user"
		curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_SERVICE_ID}/roles/${SERVICE_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate service role to cloud sevice user"
				returnFailure
			fi

         echo "Associating monitor role to cloud service user in default domain"
                curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/domains/default/users/${CLOUD_SERVICE_ID}/roles/${MONITOR_ROLE_ID}
                        if [ $? -ne 0 ]; then
                                echo "Failed to associate monitor role to cloud sevice user"
                                returnFailure
                        fi

		 echo "Associating admin role on service project to cloud service user"
			curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_SERVICE_ID}/users/${CLOUD_SERVICE_ID}/roles/${ADMIN_ROLE_ID}
			if [ $? -ne 0 ]; then
				echo "Failed to associate admin role on service project to cloud service user"
				returnFailure
			fi

         echo "Associating monitor role to cloud service user on service project of default domain"
                        curl -k -s -H "X-AUTH-TOKEN: $ADMIN_TOKEN" -H "Content-Type: application/json" -X PUT $KEYSTONE_URL/projects/${PROJECT_SERVICE_ID}/users/${CLOUD_SERVICE_ID}/roles/${MONITOR_ROLE_ID}
                        if [ $? -ne 0 ]; then
                                echo "Failed to associate monitor role on service project to cloud service user"
                                returnFailure
                        fi

		unset CLOUD_SERVICE_PWD
    ########## set endpoints #######################
    
    # create keystone service and endpoint urls
		echo "get service entry for identity"
		fval=$(curl -k -s "$KEYSTONE_URL/services?type=identity" -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to get service entry for identity"
			returnFailure
		fi
		IDENTITY_SERVICE_ID=$( echo $fval | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
        
		echo "Deleting the existing identity service endpoints"
		curl -k -s "$KEYSTONE_URL/services/$IDENTITY_SERVICE_ID" -X DELETE -H "X-Auth-Token: ${ADMIN_TOKEN}" -H 'Content-Type: application/json'
        if [ $? -ne 0 ]; then
			echo "Failed to delete service entry for identity"
			returnFailure
		fi
         
	    echo "Creating a new service entry for identity for upgrade"
		SERVICE_CREATE=$( curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"service": {"type": "identity", "name": "keystone"}}' $KEYSTONE_URL/services | python -m json.tool | grep '"id"' )
		if [ $? -ne 0 ]; then
			echo "Failed to create service entry for identity"
			returnFailure
		fi
		IDENTITY_SERVICE_ID=$( echo $SERVICE_CREATE | awk '{printf "%s",$NF}' | tr -d '"' | tr -d ',' )
		
		echo "Creating v3 identity endpoints"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "public", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create public v3 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "internal", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create internal v3 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "admin", "name": "keystone", "region": "regionOne", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v3","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create admin v3 endpoint for identity"
			returnFailure
		fi

		echo "Creating v2.0 identity endpoints"
		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "public", "name": "keystone", "region": "regionV2", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':6100/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create public v2.0 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "internal", "name": "keystone", "region": "regionV2","url":"'$LB_PROTOCOL'://'$VIP_FQDN':35357/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create internal v2.0 endpoint for identity"
			returnFailure
		fi

		curl -k -s -H "X-AUTH-TOKEN: ${ADMIN_TOKEN}" -H "Content-Type: application/json" -d '{"endpoint": {"interface": "admin", "name": "keystone", "region": "regionV2", "url":"'$LB_PROTOCOL'://'$VIP_FQDN':35357/v2.0","service_id":"'$IDENTITY_SERVICE_ID'"}}' $KEYSTONE_URL/endpoints
		if [ $? -ne 0 ]; then
			echo "Failed to create admin v2.0 endpoint for identity"
			returnFailure
		fi


		# Embed project IDs to allow identity v2 clients to act as cloud admin, monitor and service
		echo "Embedding service project id into policy.json"
		sed -i "s#\${PROJECT_SERVICE_ID}#$PROJECT_SERVICE_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed service project's id into the policy.json file"
			returnFailure
		fi

		echo "Embedding admin project id into policy.json"
		sed -i "s#\${PROJECT_ADMIN_ID}#$PROJECT_ADMIN_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed admin project's id into the policy.json file"
			returnFailure
		fi

		# Embed reserved user ids into policy.json to prevent accidental deletes
		echo "Embedding cloud admin id into policy.json"
		sed -i "s#\${CLOUD_ADMIN_ID}#$CLOUD_ADMIN_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed cloud admin id into the policy.json file"
			returnFailure
		fi

		echo "Embedding account admin id into policy.json"
		sed -i "s#\${CPSA_USER_ID}#$CPSA_USER_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed account admin id into the policy.json file"
			returnFailure
		fi
		
		echo "Embedding cloud service id into policy.json"
		sed -i "s#\${CLOUD_SERVICE_ID}#$CLOUD_SERVICE_ID#" /etc/keystone/policy.json
		if [ $? -ne 0 ]; then
			echo "Failed to embed cloud service id into the policy.json file"
			returnFailure
		fi
				
################################################################################

             cleanup
		     unset ADMIN_TOKEN
		     
			#sharing policy.json file with all other keystone nodes
		    printf "Sharing policy.json file across all nodes"
		    encodedValue=$(xxd -p /etc/keystone/policy.json)
		    curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-policyfile -X PUT --data-urlencode value="$encodedValue"
		    if [ $? -ne 0 ]; then
			  printf "Failed to share policy.json across all nodes"
			  returnFailure
		   fi
		   printf "Policy.json shared"
		
		  #need to share keystone.conf through ETCD
		  printf "Sharing keystone.conf file across all nodes"
		  encodedValue=$(base64 -w0 /etc/keystone/keystone.conf)
		  curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-conffile -X PUT --data-urlencode value="$encodedValue"
		  if [ $? -ne 0 ]; then
			printf "Failed to share keystone.conf file across all nodes"
			returnFailure
		  fi
		  printf "keystone.conf shared"
			
		#setting dbupgraded with etcd for single time upgrade.
         curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgraded -X PUT -d value="upgraded"
            if [ $? -ne 0 ]; then
                printf "Failed to set dbupgraded with etcd"
                returnFailure
            fi
            printf "Flag set with ETCD for notifying that upgrading is done"

            curl -k -s $ETCD_SERVICE/v2/keys/ccs-keystone-keystonedbupgrading -X DELETE
            if [ $? -ne 0 ]; then
                printf "Failed to set dbupgrading with etcd"
                returnFailure
            fi
            printf "key keys/keystonedbupgrading deleted from ETCD"
            
            echo "Stopping httpd service"
           `killall5 -9 httpd2`
           echo "Done"
            
		fi		
	  fi
		printf "Fetching keystone.conf and policy.json files from ETCD"
		sleep 10
		getConfFiles
		echo "success" > /root/install.status
		printf "Starting cron"
		/usr/sbin/cron start &
		echo "Starting the apacheservice in foreground"
		exec /usr/sbin/apache2ctl -DFOREGROUND
	fi
else
	printf "Docker restart scenario"
	printf "Starting cron"
	/usr/sbin/cron start &
	printf "Launching keystone"
	exec /usr/sbin/apache2ctl -DFOREGROUND
	
fi


