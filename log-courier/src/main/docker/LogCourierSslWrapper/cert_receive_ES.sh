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


CERTIFICATE_FILENAME=/opt/log-management/log-courier/LogCourierSslWrapper/logstash.crt

CHECK_CERTIFICATE()
{

curl -XGET http://ES_URL:ES_PORT/.ssl/certificate/ELK_SERVER_IP >  /opt/log-management/log-courier/LogCourierSslWrapper/cert_check
sed -e 's/.*"found"://' -e 's/,.*$//' /opt/log-management/log-courier/LogCourierSslWrapper/cert_check > /opt/log-management/log-courier/LogCourierSslWrapper/result_GET
status_cert_in_ES=$(</opt/log-management/log-courier/LogCourierSslWrapper/result_GET)
echo $status_cert_in_ES

}

DOWNLOAD_CERTIFICATE()
{
 
 outputfilename=$1

 curl -XGET http://ES_URL:ES_PORT/.ssl/certificate/ELK_SERVER_IP > /opt/log-management/log-courier/LogCourierSslWrapper/cert_from_ES
 sed -e 's/.*certificate":"//' -e 's/".*$//' /opt/log-management/log-courier/LogCourierSslWrapper/cert_from_ES > /opt/log-management/log-courier/LogCourierSslWrapper/output_cert
 sed 's/_/\n/g;s/"//g' /opt/log-management/log-courier/LogCourierSslWrapper/output_cert > $outputfilename
 rm -rf /opt/log-management/log-courier/LogCourierSslWrapper/cert_from_ES /opt/log-management/log-courier/LogCourierSslWrapper/output_cert   

}



while true
do
     is_certificate_available=`CHECK_CERTIFICATE`
     if [ "$is_certificate_available" = "true" ]
     then
       `DOWNLOAD_CERTIFICATE $CERTIFICATE_FILENAME`
       break
     fi
     sleep 4
done

