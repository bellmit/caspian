#!/bin/bash

set -e

mkdir -p /var/log/etcd-service
etcd "$@" >>/var/log/etcd-service/etcd.log 2>>/var/log/etcd-service/etcd.log
