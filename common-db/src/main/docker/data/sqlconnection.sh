#! /usr/bin/env bash

mysql -p"$1" -e 'show databases;' 2> /opt/mysql/output.txt > /dev/null
