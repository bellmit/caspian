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

#Author dyamam

#!/bin/bash


CHECK_ES_AVAILABILITY()
{
  is_elasticsearch_available=`curl --write-out %{http_code} --silent --output /dev/null http://ES_URL:ES_PORT`
  if [ $is_elasticsearch_available -eq 200 ]
  then
    echo "true"
  else
    echo "false"
  fi
}


UPLOAD_CERTIFICATE_IF_MISSING()
{
  #check whether certificate already exists in ES
  curl -XGET http://ES_URL:ES_PORT/.ssl/certificate/CONTAINER_HOST_IP >  /opt/log-management/logstash/SslWrapper/cert_from_ES
  sed -e 's/.*"found"://' -e 's/,.*$//'  /opt/log-management/logstash/SslWrapper/cert_from_ES >  /opt/log-management/logstash/SslWrapper/result
  status_cert_in_ES=$(</opt/log-management/logstash/SslWrapper/result)

  if [ "$status_cert_in_ES" = "false}" ]
  then
        echo "uploading cert\n"
        CERTIFICATE=`tr '\n' "_" < /opt/log-management/logstash/SslWrapper/logstash.crt`
        LOGSTASH_CERTIFICATE="$CERTIFICATE"
        sed -i "s#LOGSTASH_CERTIFICATE#${LOGSTASH_CERTIFICATE}#" /opt/log-management/logstash/SslWrapper/cert.json
        curl -XPUT http://ES_URL:ES_PORT/.ssl/certificate/CONTAINER_HOST_IP -d @/opt/log-management/logstash/SslWrapper/cert.json
        rm -rf /opt/log-management/logstash/SslWrapper/cert.json

        LKEY=`tr '\n' "_" < /opt/log-management/logstash/SslWrapper/logstash.key`
        LOGSTASH_KEY="$LKEY"
        sed -i "s#LOGSTASH_KEY#${LOGSTASH_KEY}#" /opt/log-management/logstash/SslWrapper/key.json
        curl -XPUT http://ES_URL:ES_PORT/.ssl/key/CONTAINER_HOST_IP -d @/opt/log-management/logstash/SslWrapper/key.json
        rm -rf /opt/log-management/logstash/SslWrapper/key.json

   fi
}


while :
do
  is_es_avail=`CHECK_ES_AVAILABILITY`
  if [ "$is_es_avail" = "true" ]
  then
	`UPLOAD_CERTIFICATE_IF_MISSING`
	break
  fi
  sleep 4  
done
