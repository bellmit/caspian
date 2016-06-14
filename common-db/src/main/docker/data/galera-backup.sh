#!/bin/sh
#
# script to backup mysql (mariadb) from one of the cluster container
# author: thomas3@emc.com
#
# TODO: command line argument support
# TODO: validations of many variables
#
##############################################################################
#
# README 1ST
#
##############################################################################
#
# Step 1: Copy this script file to a mysql-galera container.
#
# Step 2: In the container, locate a folder to store the backup file. Default /data/backup/.
#            But the folder has to be present in the container as it will NOT be created by the script.
#
# Step 3: Override any of the default values specified here
#            NEUTRINO_VERSION    1.1                    The neutrino version
#            MYSQL_USER              root                   The database root user account
#            MYSQL_PASSWORD     ecipass*             The plaintext root user password for 1.0 or decrypted password for 1.1
#            MYSQL_PORT               3306                 The database service port
#            BACKUP_FOLDER          /data/backup/   The backup file location
#            BACKUP_ANY_STABLE_NODE (not set)   Set to 1 to enable remote backup if local node is note synced
#
# Step 4: Run the script.
#             Information will be printed on screen and saved to log file.
#             Log file will be created the current working directory.
#
##############################################################################
#
# MYSQL_HOST is set during the run by the script. Setting any value will be overwritten.
#            The value could be either localhost IP or any other node IP based on BACKUP_ANY_STABLE_NODE.
# MYSQL_PASSWORD is set to default password for 1.0 and can automatically get the password for 1.1.
# BACKUP_ANY_STABLE_NODE can be used to enable remote backup if local node is not synced.
#            By default this is not set and hence if the local node is not synced within the cluster, the backup fails.
#            When set to 1, the script try doing backup using other node registered in the cluster (through CRS).
#
# Below are required to run the script
# Environment variables:
#            COMPONENT_REGISTRY       Available within mysql-galera container, for accessing CRS service
#            ENCRYPTED_ROOT_PASS     Available within mysql-galera container, for getting root password
# Utilities and their dependencies:
#            aes_encrypt.py                   Available within mysql-galera container in NEUTRINO_VERSION==1.1
#                                                     NOT required for NEUTRINO_VERSION==1.0
#            bzip2
#            curl
#            date
#            echo
#            env
#            hostname
#            mysql
#            mysqldump
#            pgrep
#            which
#
##############################################################################
#initialize or override the required values
NEUTRINO_VERSION=${NEUTRINO_VERSION:-"1.1"}
BACKUP_TIME=$( date "+%FT%H-%M-%S" )
BACKUP_FOLDER=${BACKUP_FOLDER:-/data/backup/}
BACKUP_LOG=./backup-${BACKUP_TIME}.log
LOCALHOST_IP=$( hostname -i )
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PORT=${MYSQL_PORT:-3306}

# helper functions
log()
{
	# 20160522 16:14:58.170 BACKUP [severity] log message
	local readonly timestamp="$(date +%Y%m%d\ %H:%M:%S.%N | cut -b -21)"
	echo "$timestamp BACKUP: $*"
	echo "$timestamp BACKUP: $*" >> ${BACKUP_LOG}
}

log_error()
{
	log "[ERROR] $*"
}

log_info()
{
	log "[INFO] $*"
}

get_local_node()
{
	local cluster_nodes=( $( echo $wsrep_incoming_addresses | tr "," "\n" ) )
	 for node in ${cluster_nodes[@]} ; do {
		if [ -z ${node#${LOCALHOST_IP}:${MYSQL_PORT}} ] ; then {
			echo ${node%:${MYSQL_PORT}}; 
			return 0;
		} fi;
	} done;
	return 1;
}

get_synced_node()
{
	local cluster_nodes=( $(  curl -s -H"Accept: text/plain" -XGET "$COMPONENT_REGISTRY/v1/services/platform/components/mysql-galera?name=mysql-galera&type=private_backend&published=all" | tr ":\/" "\n" | awk '/[0-9]+\./ {print }' ) )
	
	for node in ${cluster_nodes[@]} ; do {
		if [ -n ${node} ] ; then {
#			if [ ! -z ${LOCALHOST_IP#$node} ]; then	# allow a case that if this local node is synced when executing this command, but was not earlier {
				other_node_state=$( mysql --force --host=${node} --password=${MYSQL_PASSWORD} -B -N -e "SHOW STATUS WHERE Variable_name = 'wsrep_local_state_comment';" | awk '{print $2}' )
				if [ "Synced" == $other_node_state ]; then {
					echo $node;
					return 0;
				} fi;
#			} fi;
		} fi;
	} done;
	return 1;
}

dump_variables()
{
	local env_vars=( $( env ) )
	for e in ${env_vars[@]}; do { log_info "$e"; } done
	echo "-----------------------------------------------------"
	log_info "NEUTRINO_VERSION=${NEUTRINO_VERSION}"
	log_info "MYSQL_USER=${MYSQL_USER}"
	log_info "MYSQL_PORT=${MYSQL_PORT}"
	log_info "MYSQL_HOST=${MYSQL_HOST}"
	log_info "BACKUP_FOLDER=${BACKUP_FOLDER}"
	log_info "BACKUP_TIME=${BACKUP_TIME}"
	log_info "BACKUP_LOG=${BACKUP_LOG}"
	log_info "BACKUP_FILE=${BACKUP_FILE}"
	log_info "LOCALHOST_IP=${LOCALHOST_IP}"
	log_info "BACKUP_ANY_STABLE_NODE=${BACKUP_ANY_STABLE_NODE}"
	echo "-----------------------------------------------------"
}
dump_variables_and_exit()
{
	dump_variables
	exit $1
}

# initialize some more variables
MYSQL_HOST=""
if [ ${NEUTRINO_VERSION} == "1.0" ]; then {
	DECRYPTED_PASSWORD=ecipass;
} elif [ ${NEUTRINO_VERSION} == "1.1" ]; then {
	if [ ! -x `which aes_encrypt.py` ]; then {
		log_error "Required dependent utility is not available for NEUTRINO_VERSION=${NEUTRINO_VERSION}, exiting.";
		exit 2;
	} fi;
	DECRYPTED_PASSWORD=$( aes_encrypt.py --e decrypt --data ${ENCRYPTED_ROOT_PASS} );
} else {
	log_error "Unknown Neutrino version, exiting.";
	exit 2;
} fi;
MYSQL_PASSWORD=${MYSQL_PASSWORD:-${DECRYPTED_PASSWORD}}


# find if galera nodes are in sync
# if nodes are in sync, select one node
# if not in sync it is an error condition
# but if it has to preoceed with backup, find and select node with highest commit number
WSREP_VARS=(`mysql --force --password=${MYSQL_PASSWORD} -B -N -e "SHOW STATUS WHERE Variable_name like 'wsrep_%';" | awk '{print $1"="$2}'`)
for var in "${WSREP_VARS[@]}"; do { export "$var" ;} done;
if [ "ON" == ${wsrep_ready} ]; then {
	if [ ${wsrep_cluster_size} < 3 ]; then {
		# cluster is running with lesser number of nodes
		log_info "Cluster ${wsrep_cluster_state_uuid} has ${wsrep_cluster_size} nodes"
	} fi;
	if [ "Synced" == ${wsrep_local_state_comment} ]; then {
		# found that all are in sync, so, backup can be done on any node.
		# continuing backup operation on this node
		MYSQL_HOST=$( get_local_node );
		if [ -z $MYSQL_HOST ]; then {
			log_error "${LOCALHOST_IP} is not part of the cluster! Something wrong! This should never happen.";
			dump_variables_and_exit 2;
		} fi;
	#elif [ "Initialized" == ${wsrep_local_state_comment} ]; then	# this state can be clubbed with other states too
	#	# cluster is not in a good shape, please try backup on a different node
	} else {
		# found problem with nodes not being in sync with each other in the cluster
		# so, need to find which one is synced
		if [ ${BACKUP_ANY_STABLE_NODE} == 1 ]; then {
			log_info "${LOCALHOST_IP} has not joined the cluster. Trying to find a node that is synced (BACKUP_ANY_STABLE_NODE=${BACKUP_ANY_STABLE_NODE}).";
			MYSQL_HOST=$( get_synced_node );
		} else {
			log_error "${LOCALHOST_IP} has not joined the cluster. Please run backup on a different node or rerun by setting BACKUP_ANY_STABLE_NODE=1.";
			dump_variables_and_exit 2;
		} fi;
	} fi;
} else {
	# this node does not seem to be a mysql-galera node or the db service is down.
	if [ -x /usr/bin/mysql ]; then {
		if [ `pgrep -cx mysqld` == 1 ]; then {
			# connection issue. resolve the issue by looking at the log and try again.
			log_error "Not able to connect to ${LOCALHOST_IP} database service. Please check the log file and try again."
		} else {
			# service is not running. run backup after starting the db service.
			log_error "Database service is not running on ${LOCALHOST_IP}. Please check the service and try again."
		} fi;
	} else {
		# nope! mysql is not present, run backup inside mysql-galera container
		log_error "Database service is not detected on ${LOCALHOST_IP}. Please try on a correct node/container."
	} fi;
	dump_variables_and_exit 2;
} fi;

BACKUP_FILE=${BACKUP_FOLDER}/backup-${MYSQL_HOST}-${BACKUP_TIME}.sql
# TODO: ensure the backup folder is available
# TODO: ensure the backup folder has sufficient free disk space
# TODO: ensure the backup file is not present already
# TODO: ensure write permission is there to create backup sql file
# TODO: ensure write permission is there to create backup log file

# display all information (along with writing them into log file ${BACKUP_LOG}) and wait for user conformation to proceed
dump_variables

log_info "Ready to start backup."
read -p "Continue? [y| n] " -n 1 -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then {
	log_info "Backup aborted by user.";
	exit 1;
} fi;

# backup from the selected node
log_info "Starting backup..."
mysqldump --host=${MYSQL_HOST} --port=${MYSQL_PORT} --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} -x --all-databases --skip-comments --compress > ${BACKUP_FILE} 2>> ${BACKUP_LOG}
if [ $? != 0 ] ; then {
	log_error "Backup failed. Please check the log file (${BACKUP_LOG}) and try again.";
	# errors will be printed in the log file directly
	# mysqldump: Got error: 1045: "Access denied for user 'root'@'localhost' (using password: YES)" when trying to connect
	# mysqldump: Got error: 2003: "Can't connect to MySQL server on 'lgloi041' (111 "Connection refused")" when trying to connect
	# mysqldump: Got error: 2005: "Unknown MySQL server host 'lgloi0411' (-2)" when trying to connect
	# more errors at https://mariadb.com/kb/en/mariadb/mariadb-error-codes/
	exit 2;
} fi;

# backup completed successfully
# compress the SQL file to save space
log_info "Compressing backup file...";
bzip2 ${BACKUP_FILE} >> ${BACKUP_LOG} 2>&1
if [ $? == 0 ]; then
	log_info "Compressed backup file is available as ${BACKUP_FILE}.bz2.";
else
	if [ -e ${BACKUP_FILE} ]; then {
		log_info "Could not compress the file. Please check the log file (${BACKUP_LOG}). However, ${BACKUP_FILE} is available.";
	} else {
		log_error "Could not compress the file. Please check the log file (${BACKUP_LOG}) and try again.";
		exit 2;
	} fi;
fi;

log_info "Backup completed successfully.";
exit 0;

