#!/bin/bash

statusfile="ES_DELETE_INDICES_STATUS_FILE"
if [ ! -f "$statusfile" ]
then
	touch $statusfile
fi

nodename=`curl -XGET  http://127.0.0.1:ES_REST_PORT/_cat/nodes?h=name | sed '{:q;N;s/\n/,/g;t q}' | sed 's/ //g'`
timestamp=`date "+%Y-%m-%d %H:%M:%S"`
/usr/bin/curator --host 127.0.0.1 --port ES_REST_PORT delete indices --older-than INDICES_EXIPIRY_IN_DAYS --time-unit days --timestring %y.%m.%d --prefix "logstash-" | awk -F "." '{print "[""'"$timestamp"'""][INFO ][cron-indices-deletion.service ] [""'"$nodename"'""] Deleting ES Index: "$1}' >> $statusfile
