#!/bin/bash

#mysql details
mysql_host=${MYSQL_HOST}
mysql_user=${MYSQL_USER}
mysql_password=${MYSQL_PASSWORD}

#keystone details
#${IDENTITY_API_VERSION}
keystone_identity_url=${KEYSTONE_IDENTITY_URL}
keystone_auth_url=${KEYSTONE_AUTH_URL}
keystone_tenant_name=${KEYSTONE_TENANT_NAME}
keystone_user=${KEYSTONE_USER}
keystone_password=${KEYSTONE_PASSWORD}

#glance details
glance_user=${GLANCE_USER}
glance_password=${GLANCE_PASSWORD}
glance_tenant=${GLANCE_TENANT_NAME}
glance_registry_host=${GLANCE_REGISTRY_HOST}
glance_registry_port=${GLANCE_REGISTRY_PORT}
glance_api_host=${GLANCE_API_HOST}
glance_api_port=${GLANCE_API_PORT}

CONFIG_FILE=/etc/glance/glance-api.conf
GLANCE_SQL_FILE=/tmp/glance.sql

# Setting up environment for glance
CONNECTION_URL="mysql://${glance_user}:${glance_password}@${mysql_host}/glance"
export CONNECTION_URL=$CONNECTION_URL
# default identity api to v3
IDENTITY_API_VERSION=${IDENTITY_API_VERSION:-v3}
export IDENTITY_API_VERSION=$IDENTITY_API_VERSION

echo "Editing $GLANCE_SQL_FILE";
#edit the glance.sql file to include glance user data
sed -i "s#GLANCE_USER#${glance_user}#" $GLANCE_SQL_FILE
sed -i "s#GLANCE_PASSWORD#${glance_password}#" $GLANCE_SQL_FILE

echo "Editing $CONFIG_FILE";
sed -i "s#DB_CONNECTION#${CONNECTION_URL}#" $CONFIG_FILE
sed -i "s#KEYSTONE_IDENTITY_URL#${keystone_identity_url}#" $CONFIG_FILE
sed -i "s#KEYSTONE_AUTH_URL#${keystone_auth_url}#" $CONFIG_FILE
sed -i "s#GLANCE_TENANT_NAME#${glance_tenant}#" $CONFIG_FILE
sed -i "s#GLANCE_USER#${glance_user}#" $CONFIG_FILE
sed -i "s#GLANCE_PASSWORD#${glance_password}#" $CONFIG_FILE
sed -i "s#GLANCE_REGISTRY_HOST#${glance_registry_host}#" $CONFIG_FILE
sed -i "s#GLANCE_REGISTRY_PORT#${glance_registry_port}#" $CONFIG_FILE

#Calling the sql file to setup the MySQL db
echo "Adding SQL DB and user";
mysql -u${mysql_user} -p${mysql_password} -h${mysql_host} < $GLANCE_SQL_FILE
if [ $? -ne 0 ]; then
echo "Failed to execute sql commands"
echo "failure" > /root/install.status
exit 2
fi
#TODO: Verify port option, use mysql_host to supply port

#sync the DB
echo "Calling DB sync";
glance-manage db_sync
if [ $? -ne 0 ]; then
echo "glance-manage --config-file $CONFIG_FILE db_sync: Failed"
echo "failure" > /root/install.status
exit 2
fi

#create shell script with env variables
echo "Creating Keystone variables' file";
AUTHENV=/tmp/admin-openrc.sh
cat >$AUTHENV <<EOF
export OS_TENANT_NAME=${keystone_tenant_name}
export OS_USERNAME=${keystone_user}
export OS_PASSWORD=${keystone_password}
export OS_AUTH_URL=${keystone_auth_url}
EOF

#execute the shell to set environment variables
echo "Executing Keystone variables' file";
chmod 755 /tmp/admin-openrc.sh
source /tmp/admin-openrc.sh

echo "Printing environment variables";
echo "=================================";
env
echo "=================================";
unset OS_SERVICE_TOKEN OS_SERVICE_ENDPOINT
export OS_SERVICE_TOKEN=$(keystone token-get | awk '/ id / {print $4}')
export OS_SERVICE_ENDPOINT=${KEYSTONE_IDENTITY_URL}/v2.0
#create glance user in keystone
echo "Creating Keystone user";
keystone user-create --name ${glance_user} --pass ${glance_password}
if [ $? -ne 0 ]; then
echo "Failed to create a glance user called <${glance_user}>"
echo "failure" > /root/install.status
exit 2
fi

# add the admin role to the glance user
echo "Adding user to admin role";
keystone user-role-add --user ${glance_user} --tenant ${glance_tenant} --role admin
if [ $? -ne 0 ]; then
echo "Failed to add admin role to  ${glance_user}"
echo "failure" > /root/install.status
exit 2
fi

#Create service entity
echo "Creating glance service on Keystone";
keystone service-create --name glance --type image --description "OpenStack Image Service"
if [ $? -ne 0 ]; then
echo "Failed to create image service"
echo "failure" > /root/install.status
exit 2
fi


# Create a service endpoint
echo "Creating endpoint for the service";
keystone endpoint-create \
  --service-id $(keystone service-list | awk '/ image / {print $2}') \
  --publicurl http://${glance_api_host}:${glance_api_port} \
  --internalurl http://${glance_api_host}:${glance_api_port} \
  --adminurl http://${glance_api_host}:${glance_api_port} \
 --region regionOne
if [ $? -ne 0 ]; then
echo "Failed to create image endpoint urls"
echo "failure" > /root/install.status
exit 2
fi

echo "Starting the glance api";
set -m
glance-api --config-file $CONFIG_FILE --debug &

echo "Successfully bootstrapped glance API"
echo "success" > /root/install.status

fg