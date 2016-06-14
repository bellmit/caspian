#! /usr/bin/env bash

function println {
	echo `date` : $1
}

function Check_Mysql {
	mysql_status=`ps -ef | grep mysqld | wc -l`
	if [[ "$mysql_status" -le 1 ]]; then
        println "Mysql is not running, so exiting"
        exit 1
	fi
}

function Restart_Mysql {
	pkill -9 mysqld
	sleep `expr "$sleep_time" + 15`
	println "exiting container"
	exit 1
}

function Connection_Hung {
	loop_counter=0
	exec /opt/mysql/sqlconnection.sh "$MYSQL_ROOT_PASS" &
	sleep 10
	LINES=`ps -eaf | grep 'sqlconnection.sh' | wc -l`
	while [ "$LINES" -gt 2 -a "$loop_counter" -le 3 ]
	do
		println "The node seems to be hung. Attempting again..."
		loop_counter=`expr "$loop_counter" + 1`
		sleep 10
		LINES=`ps -eaf | grep 'sqlconnection.sh' | wc -l`
	done
	if [ "$loop_counter" -gt 3 ]; then
		println "The node is declared to be hung, so restarting"
		Restart_Mysql
	fi
}

function Non_Primary {
	id=$1
	id1=$2
	id2=$3
	println "id=$id, id1=$id1 and id2=$id2"
	state_size=`awk '/member/{count++} END{print count}' /data/gvwstate.dat`
	println "Received primary component size as $state_size"
	timestamp=`date +"%s"`
	state="$state_size#$timestamp"
	#publish the state to etcd
	WRITE_STATUS=`curl -i -s "$ETCD_SERVICE"/v2/keys/galera/STATE_$id -X PUT -d value="$state" | awk '/HTTP/{printf $2}'`
	if [ "$WRITE_STATUS" != "201" -a "$WRITE_STATUS" != "200" ]; then
		println "Couldn't write its state to etcd, received status code $WRITE_STATUS"
		return
	fi
	println "Written value as $state for STATE_$id to etcd"
	#Read the state of other nodes from etcd
	READ_STATUS1=`curl -I -s "$ETCD_SERVICE"/v2/keys/galera/STATE_$id1 | awk '/HTTP/{printf $2}'`
	if [ "$READ_STATUS1" != "200" ]; then
		println "Could not read STATE_$id1 flag from etcd, received status code $READ_STATUS1"
		return
	fi	
	state1=`curl -s "$ETCD_SERVICE"/v2/keys/galera/STATE_$id1 | python -m json.tool | awk '/value/{print $2}' | tr -d '"'`
	println "Received state as $state1 for STATE_$id1 from etcd"
	state1_size=`echo "$state1" | cut -d"#" -f1`
	state1_time=`echo "$state1" | cut -d"#" -f2`
	time_now=`date +"%s"`
	if [ `expr "$time_now" - "$state1_time"` -gt `expr "$sleep_time" + 5` ]; then
		println "Stale entry found for STATE_$id1 in etcd"
		return
	fi
	if [[ "$state_size" -le "$state1_size" ]]; then
		println "Not qualified to restart itself"
		return
	fi
	if [ ! -z "$id2" ]; then
		READ_STATUS2=`curl -I -s "$ETCD_SERVICE"/v2/keys/galera/STATE_$id2 | awk '/HTTP/{printf $2}'`
		if [ "$READ_STATUS2" != "200" ]; then
			println "Could not read STATE_$id2 flag from etcd, received status code $READ_STATUS2"
			return
		fi	
		state2=`curl -s "$ETCD_SERVICE"/v2/keys/galera/STATE_$id2 | python -m json.tool | awk '/value/{print $2}' | tr -d '"'`
		println "Received state as $state2 for STATE_$id2 from etcd"
		state2_size=`echo "$state2" | cut -d"#" -f1`
		state2_time=`echo "$state2" | cut -d"#" -f2`
		time_now=`date +"%s"`
		if [ `expr "$time_now" - "$state2_time"` -gt `expr "$sleep_time" + 5` ]; then
			println "Stale entry found for STATE_$id2 in etcd"
		return
		fi
		if [[ "$state_size" -le "$state2_size" ]]; then
			println "Not qualified to restart itself"
			return
		fi
	fi
	println "Restarting itself"
	Restart_Mysql
}

function Connection_Refused {
	id=$1
	id1=$2
	id2=$3
	println "id=$id, id1=$id1 and id2=$id2"
	timestamp=`date +"%s"`
	#publish the connection status to etcd
	WRITE_STATUS=`curl -i -s "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id -X PUT -d value="$timestamp" | awk '/HTTP/{printf $2}'`
	if [ "$WRITE_STATUS" != "201" -a "$WRITE_STATUS" != "200" ]; then
		println "Couldn't write its connection timestamp to etcd, received status code $WRITE_STATUS"
		return
	fi
	println "Written value as $timestamp for CONNECTION_$id to etcd"
	#Read the connection status of other nodes from etcd
	READ_STATUS1=`curl -I -s "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id1 | awk '/HTTP/{printf $2}'`
	if [ "$READ_STATUS1" != "200" ]; then
		println "Could not read CONNECTION_$id1 flag from etcd, received status code $READ_STATUS1"
		return
	fi
	state1=`curl -s "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id1 | python -m json.tool | awk '/value/{print $2}' | tr -d '"'`
	println "Received $state1 for CONNECTION_$id1 from etcd"
	time_now=`date +"%s"`
	if [ `expr "$time_now" - "$state1"` -gt `expr "$sleep_time" + 15` ]; then
		println "Stale entry found for CONNECTION_$id1 in etcd"
		return
	fi
	if [ ! -z "$id2" ]; then
		READ_STATUS2=`curl -I -s "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id2 | awk '/HTTP/{printf $2}'`
		if [ "$READ_STATUS2" != "200" ]; then
			println "Could not read CONNECTION_$id2 flag from etcd, received status code $READ_STATUS2"
			return
		fi
		state2=`curl -s "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id2 | python -m json.tool | awk '/value/{print $2}' | tr -d '"'`
		println "Received $state2 for CONNECTION_$id2 from etcd"
		time_now=`date +"%s"`
		if [ `expr "$time_now" - "$state2"` -gt `expr "$sleep_time" + 15` ]; then
			println "Stale entry found for CONNECTION_$id2 in etcd"
			return
		fi
	fi
	println "Restarting itself"
	sleep `expr "$sleep_time" + 20`
	DELETE_STATUS=`curl -I -s -X DELETE "$ETCD_SERVICE"/v2/keys/galera/CONNECTION_$id | awk '/HTTP/{printf $2}'`
	if [ "$DELETE_STATUS" == "200" -o "$DELETE_STATUS" == "404" ]; then
		println "mysql connection time stamp deleted successfully from etcd"
	else
		println "Couldn't delete mysql connection time stamp from etcd, received status $DELETE_STATUS"
	fi
	Restart_Mysql
}
