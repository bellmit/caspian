#!/bin/bash
if [ ! -d /data/db/mysql ] ; then
    if [ ! -d /data/db ]; then
        mkdir -p /data/db
    fi
    set -m
    echo "Fresh install of Maria DB. Recreating the database"
    mysql_install_db --datadir=/data/db
    if [ $? -ne 0 ]; then
        echo "Failed to initialize Maria DB"
        echo "fail" > /data/install.status
        exit 1
    fi
    chown mysql:mysql -R /data/db
    /usr/bin/mysqld_safe &
    sleep 5s
    flag=`mysqladmin -u root status |grep Uptime |wc -l`
    if [ $flag -ne 1 ]; then
        echo "Failed to start the database"
        echo "fail" > /data/install.status
        exit 1
    fi
    echo "Customizing the database"
    cat /etc/bootstrap.sql | mysql
    if [ $? -ne 0 ]; then
        echo "Bootstrapping the database failed"
        echo "fail" > /data/install.status
        exit 1
    fi
    echo "Successfully bootstrapped the database"
    echo "success" > /data/install.status
    fg
else
    echo "Starting MariaDB..."
    exec /usr/bin/mysqld_safe
fi

