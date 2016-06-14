#! /usr/bin/env bash

source /opt/mysql/monitor_functions.sh
set -e

#Ansible mounts logs to container /var/log, but mysql is currently configured to require the directory /var/log/mysql
mkdir -p /var/log/mysql
touch /var/log/mysql/mysql-service.log
exec &>> /var/log/mysql/mysql-service.log

AES_ENCRYPT=aes_encrypt.py

echo "decrypt variables"
if [[ ( -z "${ENCRYPTED_ROOT_PASS}" ) ]] ; then
	printf "Invaild pwd to decrypt"
	exit 1
fi

if [ -x "$AES_ENCRYPT" ] ; then
	printf "failed to find decryption cli"
	exit 1
fi


MYSQL_ROOT_PASS=`$AES_ENCRYPT --e decrypt --data ${ENCRYPTED_ROOT_PASS}`
if [ $? -ne 0 ] ; then
	printf "failed to decrypt root password"
	exit 1
fi


sed -i "s#^bind-address=.*#bind-address=$CONTAINER_HOST_ADDRESS#" /etc/my.cnf

#If DB installation has already succeeded then don't redo on container restart
if [ ! -d "/data/mysql" ] ; then
  println "Installing mysql db"

  mysql_install_db --datadir=/data

  # Do some SQL configuration, secure server, remove anon users, open root to remotes, create xtrabackup user for sst
      mysqld_safe & \
      mysqladmin --wait=10 -u root password "$MYSQL_ROOT_PASS"; \
      echo -e "$MYSQL_ROOT_PASS\nn\ny\nn\ny\ny" | mysql_secure_installation; \
      { echo "GRANT ALL ON *.* TO root@'%' IDENTIFIED BY \"$MYSQL_ROOT_PASS\" WITH GRANT OPTION;"; \
      echo "GRANT RELOAD, LOCK TABLES, REPLICATION CLIENT, SUPER ON *.* TO xtrabackup@'%' IDENTIFIED BY \"$XTRABACKUP_PASS\";"; \
      echo "DELETE FROM mysql.user WHERE user='' OR (user='root' AND host!='%');"; \
      } | mysql --password=$MYSQL_ROOT_PASS

  #Bring the mysql service down after installation and hardening so that it
  #Can be brought back online with the proper clustering options
  mysqladmin shutdown -p$MYSQL_ROOT_PASS
fi

if [ ! -f "/etc/xinetd.d/mysqlchk" ] ; then
   # copy mysql_chek_cluster.sh to /usr/bin
   cp /opt/mysql/mysql_chek_cluster.sh /usr/bin
   chmod a+x /usr/bin/mysql_chek_cluster.sh

   # copy xinetd conf file to  /etc/xinetd.d/mysqlchk
   # start xinetd
   rm /etc/xinetd.d/*
   cp /opt/mysql/mysqlchk /etc/xinetd.d/mysqlchk
   echo "mysqlchk        9500/tcp                        # mysqlchk">>/etc/services
fi

set +e

# Add clustered node IPs and bind ip to my.cnf
println "Updating my.cnf"
CRS_STATUS=`curl -I -s $COMPONENT_REGISTRY/v1/services/platform/components/mysql-galera | awk '/HTTP/{printf $2}'`
if [ "$CRS_STATUS" == "200" ]; then
   CLUSTER_IPS=`curl -s -H"Accept: text/plain" -XGET "$COMPONENT_REGISTRY/v1/services/platform/components/mysql-galera?name=mysql-galera&type=private_backend&published=all" | cut -d : -f2 | tr -d '/' | tr '\n' ','`
   if [ ! -z "$CLUSTER_IPS" ]; then
      sed -i "s#^wsrep_cluster_address=gcomm://.*#wsrep_cluster_address=gcomm://$CLUSTER_IPS#" /etc/my.cnf
   fi
elif [ "$CRS_STATUS" == "404" ]; then
   println "mysql-galera component not found in CRS, proceeding"
else
   println "Couldn't access CRS, received status code $CRS_STATUS so exiting"
   exit 1
fi

sed -i "s#^wsrep_node_address=.*#wsrep_node_address=$CONTAINER_HOST_ADDRESS#" /etc/my.cnf

/usr/sbin/xinetd -stayalive -filelog /var/log/xinetd.log &

echo "---- Using my.cnf: ----"
cat /etc/my.cnf
echo "-----------------------"
set -m
# Check if DB bootstapped or not in ETCD
BOOTSTRAP_STATUS=`curl -I -s $ETCD_SERVICE/v2/keys/galera/COMMONDB_BOOTSTRAPPED | awk '/HTTP/{printf $2}'`
if [ "$BOOTSTRAP_STATUS" == "200" -o "$BOOTSTRAP_STATUS" == "404" ]; then 
   if [ "$BOOTSTRAP_STATUS" == "200" ]; then
      exec mysqld_safe &

   else
      #set the DB bootstrap flag in ETCD
      WRITE_STATUS=`curl -i -s $ETCD_SERVICE/v2/keys/galera/COMMONDB_BOOTSTRAPPED -X PUT -d value="yes" | awk '/HTTP/{printf $2}'`
      if [ "$WRITE_STATUS" == "201" ]; then
         exec mysqld_safe --wsrep-new-cluster &
      else
         println "Couldn't write COMMONDB_BOOTSTRAPPED flag to etcd, received status code $WRITE_STATUS so exiting"
         exit 1
      fi
   fi
else
   println "Could not read COMMONDB_BOOTSTRAPPED flag from etcd, received status code $BOOTSTRAP_STATUS so exiting"
   exit 1
fi

sleep 20
export sleep_time=5

while true
do
	sleep "$sleep_time"
	Check_Mysql
	CRS_STATUS=`curl -I -s "$COMPONENT_REGISTRY"/v1/services/platform/components/mysql-galera | awk '/HTTP/{printf $2}'`
	if [ "$CRS_STATUS" != "200" ]; then
		println "Couldn't access CRS, received status code $CRS_STATUS"
		continue
	fi
	CLUSTER_IPS=`curl -s -H"Accept: text/plain" -XGET "$COMPONENT_REGISTRY/v1/services/platform/components/mysql-galera?name=mysql-galera&type=private_backend&published=all" | cut -d : -f2 | tr -d '/' | tr '\n' ','`
	id=`echo "$CONTAINER_HOST_ADDRESS" | tr -d '.'`
	cluster_ids=`echo "$CLUSTER_IPS" | tr ',' ' ' | tr -d '.'`
	other_node_ids="${cluster_ids/$id/}"
	id1=`echo "$other_node_ids" | awk '{printf $1}'`
	id2=`echo "$other_node_ids" | awk '{printf $2}'`
	
	CLUSTER_STATUS=`awk '/view\(view_id\(/ {a=$0} END{print a}' /var/log/mysql/mysqld.error.log | cut -d"," -f 1 | cut -d"(" -f 3`
	if [ "$CLUSTER_STATUS" == "NON_PRIM" ]; then
		println "Received $CLUSTER_STATUS status"
		Non_Primary "$id" "$id1" "$id2"		
		continue
	fi
	DELETE_STATUS=`curl -I -s -X DELETE "$ETCD_SERVICE"/v2/keys/galera/STATE_$id | awk '/HTTP/{printf $2}'`
	if [ "$DELETE_STATUS" != "200" -a "$DELETE_STATUS" != "404" ]; then
		println "Couldn't delete node state from etcd, received status $DELETE_STATUS"
	fi
	
	Connection_Hung
	
	MYSQL_CONNECTION_STATUS=`grep "Connection refused" /opt/mysql/output.txt`
	rm /opt/mysql/output.txt
	if [ ! -z "$MYSQL_CONNECTION_STATUS" ]; then
		println "Received $MYSQL_CONNECTION_STATUS for mysql connection."
		Connection_Refused "$id" "$id1" "$id2"
	fi
done


		




