#!/bin/bash

#mysql details
mysql_host=${MYSQL_HOST}

#keystone details
#${IDENTITY_API_VERSION}
keystone_identity_url=${KEYSTONE_IDENTITY_URL}
keystone_auth_url=${KEYSTONE_AUTH_URL}

#glance details
glance_user=${GLANCE_USER}
glance_password=${GLANCE_PASSWORD}
glance_tenant=${GLANCE_TENANT_NAME}

CONFIG_FILE=/etc/glance/glance-registry.conf

# Setting up environment for glance
CONNECTION_URL="mysql://${glance_user}:${glance_password}@${mysql_host}/glance"
export CONNECTION_URL=$CONNECTION_URL
# default identity api to v3
IDENTITY_API_VERSION=${IDENTITY_API_VERSION:-v3}
export IDENTITY_API_VERSION=$IDENTITY_API_VERSION

echo "Printing environment variables";
echo "=================================";
env
echo "=================================";

echo "Editing $CONFIG_FILE";
sed -i "s#DB_CONNECTION#${CONNECTION_URL}#" $CONFIG_FILE
sed -i "s#KEYSTONE_IDENTITY_URL#${keystone_identity_url}#" $CONFIG_FILE
sed -i "s#KEYSTONE_AUTH_URL#${keystone_auth_url}#" $CONFIG_FILE
sed -i "s#GLANCE_TENANT_NAME#${glance_tenant}#" $CONFIG_FILE
sed -i "s#GLANCE_USER#${glance_user}#" $CONFIG_FILE
sed -i "s#GLANCE_PASSWORD#${glance_password}#" $CONFIG_FILE


echo "Starting the glance registry";
set -m
glance-registry --config-file $CONFIG_FILE --debug &

echo "Successfully bootstrapped glance Registry"
echo "success" > /root/install.status

fg