#!/bin/bash

MYSQLHOST=$CONTAINER_HOST_ADDRESS
MYSQLPORT=$MYSQL_PORT
MYSQLUSERNAME="root"

AES_ENCRYPT=aes_encrypt.py

if [[ ( -z "${ENCRYPTED_ROOT_PASS}" ) ]] ; then
        echo "Invaild pwd to decrypt"
        exit 1
fi

if [ -x "$AES_ENCRYPT" ] ; then
        echo "failed to find decryption cli"
        exit 1
fi


MYSQLPASSWORD=`$AES_ENCRYPT --e decrypt --data ${ENCRYPTED_ROOT_PASS}`
if [ $? -ne 0 ] ; then
        echo "failed to decrypt root password"
        exit 1
fi

READY=`mysql --force --host=$MYSQLHOST --port=$MYSQLPORT --user=$MYSQLUSERNAME --password=$MYSQLPASSWORD -B -N -e "SHOW STATUS WHERE Variable_name = 'wsrep_ready';" | awk '{print $2}'`

if [ $READY = "ON" ]; then
  IN_SYNC=`mysql --force --host=$MYSQLHOST --port=$MYSQLPORT --user=$MYSQLUSERNAME --password=$MYSQLPASSWORD -B -N -e "SHOW STATUS WHERE Variable_name = 'wsrep_local_state_comment';" | awk '{print $2}'`
  if [ $IN_SYNC = "Synced" ]; then
    echo -en "HTTP/1.1 200 OK\r\n"
    echo -en "Content-length: 25\r\n"
    echo -en "Content-Type: text/plain\r\n"
    echo -en "\r\n"
    echo -en "MySQL Galera is Working\r\n"
  else
    echo -en "HTTP/1.1 503 Service Unavailable\r\n"
    echo -en "Content-length: 25\r\n"
    echo -en "Content-Type: text/plain\r\n"
    echo -en "\r\n"
    echo -en "MySQL Galera has Failed\r\n"
  fi
else
  echo -en "HTTP/1.1 503 Service Unavailable\r\n"
  echo -en "Content-length: 25\r\n"
  echo -en "Content-Type: text/plain\r\n"
  echo -en "\r\n"
  echo -en "MySQL Galera has Failed\r\n"
fi
