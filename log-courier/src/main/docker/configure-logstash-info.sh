#/bin/bash

set -e 

sleep 20

vip=( $( ${CRS_INTEGRATION_SCRIPT} logstash vip ) )
logstash_endpoints=( $( ${CRS_INTEGRATION_SCRIPT} logstash endpoints ) )

SUBSTITUTE_ENV_RESTART_LC()
{
  ip=$1
  port=$2
  sed -i "s/ELK_SERVER_IP/${ip}/" "${LOG_COURIER_CONF_FILE}"
  sed -i "s/ELK_SERVER_PORT/${port}/" "${LOG_COURIER_CONF_FILE}"
}

WAIT_FOR_LCREST()
{

  while :
  do
     http_status=$(curl -XGET -I http://0.0.0.0:${LC_REST_PORT}/api/log-paths |  grep 'HTTP' | awk '{print $2}')
     if [ "$http_status" != "" ];
     then 
        if [ $http_status == "200" ] ;
        then
           break;
        fi
     fi
     sleep 5
  done

}

SYNCH_CACHE()
{

  $( WAIT_FOR_LCREST )
  echo $(curl -XPUT -H "Content-Type: application/json" http://0.0.0.0:${LC_REST_PORT}/api/synch-cache)

}



if [ ${#logstash_endpoints[@]} -gt 0 ]; 
then
  endpoint=${logstash_endpoints[0]}
  logstash_ipaddress=$( echo $endpoint | awk -F":" '{print $1}')
  logstash_port=$( echo $endpoint | awk -F":" '{print $2}')
  if [ ${#vip[@]} -gt 0 ];
  then
    logstash_ipaddress=${vip[0]}
  fi
  echo $( SUBSTITUTE_ENV_RESTART_LC $logstash_ipaddress $logstash_port ) 
  echo $( SYNCH_CACHE )

fi

