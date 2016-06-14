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




GENERATE_CERTIFICATE()
{
curl -XGET http://ES_URL:ES_PORT/.ssl/certificate/CONTAINER_HOST_IP >  /opt/log-management/logstash/SslWrapper/cert_check
sed -e 's/.*"found"://' -e 's/,.*$//' /opt/log-management/logstash/SslWrapper/cert_check > /opt/log-management/logstash/SslWrapper/result_GET
status_cert_in_ES=$(</opt/log-management/logstash/SslWrapper/result_GET)
if [ ! "$status_cert_in_ES" = "true" ]
then
        openssl req -config /opt/log-management/logstash/SslWrapper/ssl.cnf -x509 -days 3650 -batch -nodes -newkey rsa:2048 -keyout /opt/log-management/logstash/SslWrapper/logstash.key -out /opt/log-management/logstash/SslWrapper/logstash.crt
fi

if [  "$status_cert_in_ES" = "true" ]
then
        curl -XGET http://ES_URL:ES_PORT/.ssl/certificate/CONTAINER_HOST_IP > /opt/log-management/logstash/SslWrapper/cert_from_ES
        sed -e 's/.*certificate":"//' -e 's/".*$//' /opt/log-management/logstash/SslWrapper/cert_from_ES > /opt/log-management/logstash/SslWrapper/output_cert
        sed 's/_/\n/g;s/"//g' /opt/log-management/logstash/SslWrapper/output_cert > /opt/log-management/logstash/SslWrapper/logstash.crt
        rm -rf /opt/log-management/logstash/SslWrapper/cert_from_ES /opt/log-management/logstash/SslWrapper/output_cert

        curl -XGET http://ES_URL:ES_PORT/.ssl/key/CONTAINER_HOST_IP > /opt/log-management/logstash/SslWrapper/key_from_ES
        sed -e 's/.*key":"//' -e 's/".*$//' /opt/log-management/logstash/SslWrapper/key_from_ES > /opt/log-management/logstash/SslWrapper/output_key
        sed 's/_/\n/g;s/"//g' /opt/log-management/logstash/SslWrapper/output_key > /opt/log-management/logstash/SslWrapper/logstash.key
        rm -rf /opt/log-management/logstash/SslWrapper/key_from_ES /opt/log-management/logstash/SslWrapper/output_key
fi
}

while :
do
  is_es_avail=`CHECK_ES_AVAILABILITY`
  if [ $is_es_avail = "true" ]
  then
        `GENERATE_CERTIFICATE`
        break
  fi
  sleep 4
done


