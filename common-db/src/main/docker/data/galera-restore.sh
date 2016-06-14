#/bin/sh
# script to restore mysql (mariadb) from one of the cluster container
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
# Step 2: In the same container, copy the full backup file and set the path to below variable:
#            BACKUP_FILE                    Full path to the backup file, it could be .sql or .bz2
#
# Step 3: Override any of the default values specified here
#            NEUTRINO_VERSION    1.1                    The neutrino version
#            MYSQL_USER              root                   The database root user account
#            MYSQL_PASSWORD     ecipass*             The plaintext root user password for 1.0 or decrypted password for 1.1
#            MYSQL_PORT               3306                 The database service port
#            MYSQL_DATA              /data/                The mysql data location (NOT used in current version)
#
# Step 4: Run the script.
#             Information will be printed on screen and saved to log file.
#             Log file will be created in the current working directory.
#
# Example: # NEUTRINO_VERSION=1.0 BACKUP_FILE=/backup/backup-10.247.47.27-2016-05-30T13-07-55.sql.bz2 mysql-restore.sh
#
##############################################################################
#
# MYSQL_HOST is set during the run by the script. Setting any value will be overwritten.
#            The value could be either localhost IP or any other node IP based on BACKUP_ANY_STABLE_NODE.
# MYSQL_PASSWORD is set to default password for 1.0 and can automatically get the password for 1.1.
#
# Below are required to run the script
# Environment variables:
#            COMPONENT_REGISTRY       Available within mysql-galera container, for accessing CRS service
#            ENCRYPTED_ROOT_PASS     Available within mysql-galera container, for getting root password
#            RESTORE_DEBUG                Enable detailed logging if set to 1
# Utilities and their dependencies:
#            aes_encrypt.py                   Available within mysql-galera container in NEUTRINO_VERSION==1.1
#                                                     NOT required for NEUTRINO_VERSION==1.0
#            bunzip2
#            curl
#            date
#            echo
#            env
#            hostname
#            mysql
#            which
#
##############################################################################
#initialize or override the required values
NEUTRINO_VERSION=${NEUTRINO_VERSION:-"1.1"}
RESTORE_TIME=`date "+%FT%H-%M-%S"`
RESTORE_LOG=./restore-${RESTORE_TIME}.log
LOCALHOST_IP=$( hostname -i )
MYSQL_USER=${MYSQL_USER:-root}
MYSQL_PORT=${MYSQL_PORT:-3306}
MYSQL_DATA=${MYSQL_DATA:-/data/}

# helper functions
log()
{
	# 20160522 16:14:58.170 RESTORE [severity] log message
	local readonly timestamp="$(date +%Y%m%d\ %H:%M:%S.%N | cut -b -21)"
	echo "$timestamp RESTORE: $*"
	echo "$timestamp RESTORE: $*" >> ${RESTORE_LOG}
}

log_error()
{
	log "[ERROR] $*"
}

log_info()
{
	log "[INFO] $*"
}

log_debug()
{
	if [ "$RESTORE_DEBUG" == "1" ]; then {
		log "[DEBUG] $*";
	} fi;
}

set_sql_filename()
{
	local filename=$1;
	if [ ${filename:${#filename},-4} == ".bz2" ]; then
	{
		log_info "BZ2 file found. Decompressing the file.";
		# it is a bz2 file, so extract
		bunzip2 $1 >> ${RESTORE_LOG} 2>&1;
		if [ $? -ne 0 ]; then
		{
			log_error "Error while decompressing $filename, please check log file (${RESTORE_LOG}) for information.";
		} fi;
		filename=${filename%.bz2};
	} fi;
	if [ ${filename:${#filename},-4} == ".sql" ]; then
	{
		BACKUP_SQL_FILE=${filename};
		return 0;
	} fi;
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
	log_info "MYSQL_DATA=${MYSQL_DATA}"
	log_info "BACKUP_FILE=${BACKUP_FILE}"
	log_info "BACKUP_SQL_FILE=${BACKUP_SQL_FILE}"
	log_info "RESTORE_TIME=${RESTORE_TIME}"
	log_info "RESTORE_LOG=${RESTORE_LOG}"
	log_info "LOCALHOST_IP=${LOCALHOST_IP}"
	echo "-----------------------------------------------------"
}
dump_variables_and_exit()
{
	dump_variables
	exit $1
}

# initialize some more variables
MYSQL_HOST=${LOCALHOST_IP}
if [ ${NEUTRINO_VERSION} == "1.0" ]; then {
	DECRYPTED_PASSWORD=ecipass;
} elif [ ${NEUTRINO_VERSION} == "1.1" ]; then {
	if [ ! -x `which aes_encrypt.py` ]; then {
		log_error "Required dependent utility is not available for NEUTRINO_VERSION=${NEUTRINO_VERSION}, exiting.";
		dump_variables_and_exit 2;
	} fi;
	DECRYPTED_PASSWORD=$( aes_encrypt.py --e decrypt --data ${ENCRYPTED_ROOT_PASS} );
} else {
	log_error "Unknown Neutrino version, exiting.";
	dump_variables_and_exit 2;
} fi;
MYSQL_PASSWORD=${MYSQL_PASSWORD:-${DECRYPTED_PASSWORD}}

# ensure the backup file is available
# ensure the data folder has sufficient free disk space
# ensure write permission is there to create restore log file

# if backup file is a compressed file, deflat it and update ${BACKUP_FILE} env variable
# ensure file to restore is having .sql file extension
set_sql_filename ${BACKUP_FILE}
if [ ! -r ${BACKUP_SQL_FILE} ]; then {
	log_error "Invalid backup filename. Set correct value for BACKUP_FILE variable and try again.";
	dump_variables_and_exit 2;
} fi;

# display all information (along with writing them into log file ${RESTORE_LOG}) and wait for user conformation to proceed
dump_variables

# Optionally clear all files under ${MYSQL_DATA} in THIS node
#   Stop mysqld in THIS node
# 	 rm -rf ${MYSQL_DATA}/*
#   Start fresh mysqld in THIS node

log_info "Ready to restore."
read -p "Continue? [y| n] " -n 1 -r
if [[ ! $REPLY =~ ^[Yy]$ ]]; then {
	log_info "Restore aborted by user.";
	exit 1;
} fi;

log_info "Starting restore..."
# Start the restoration in THIS node
# WARNING: DATA LOSS! Matching tables in the current database will be removed and old tables will be added.
# NOTE: Any additional databases or tables in the current setup will be left as it is.
mysql --host=${MYSQL_HOST} --port=${MYSQL_PORT} --user=${MYSQL_USER} --password=${MYSQL_PASSWORD} < ${BACKUP_SQL_FILE} > ${RESTORE_LOG} 2>&1
if [ $? != 0 ] ; then {
	log_error "Restore failed. Please check the log file (${RESTORE_LOG}) and try again.";
	# ERROR 1045 (28000): Access denied for user 'rwoot'@'localhost' (using password: YES)
	# ERROR 2003 (HY000): Can't connect to MySQL server on 'lgloi041' (111 "Connection refused")
	# ERROR 2005 (HY000): Unknown MySQL server host 'lgloi0411' (-2)
	# more errors at https://mariadb.com/kb/en/mariadb/mariadb-error-codes/
	exit 2;
} fi;
# Apply correct ownership to ${MYSQL_DATA} in THIS node
#chown -Rf mysql ${MYSQL_DATA}

# If required, reassign required privileges (GRANT) on the database nodes

# Wait till all nodes status turn 'Synced'
CHECK_NODES=( )
CLUSTER_NODES=( $(  curl -s -H"Accept: text/plain" -XGET "$COMPONENT_REGISTRY/v1/services/platform/components/mysql-galera?name=mysql-galera&type=private_backend&published=all" | tr ":\/" "\n" | awk '/[0-9]+\./ {print }' ) )
for node in ${CLUSTER_NODES[@]}; do {
	CHECK_NODES[${#CHECK_NODES[@]}]=$node;
} done;
# TODO: depending on the size of BACKUP_FILE, derive appropriate value for retry below
for (( retry = 30; retry > 0; --retry  )); do {
	echo -n " ";
	cluster_size=${#CHECK_NODES[@]};
	log_info "Waiting for ${cluster_size} nodes to sync, $retry attempts remaining.";
	for (( node=0; node < $cluster_size; ++node )); do {
		echo -n ".";
		log_debug "Checking node ${CHECK_NODES[$node]}..."
		node_state=$( mysql --force --host=${CHECK_NODES[$node]} --password=${MYSQL_PASSWORD} -B -N -e "SHOW STATUS WHERE Variable_name = 'wsrep_local_state_comment';" | awk '{print $2}' );
		if [  $node_state == "Synced" ]; then {
			# node has become Synced
			log_info "${CHECK_NODES[$node]} is now synced.";
			CHECK_NODES[$node]=${CHECK_NODES[$(( ${#CHECK_NODES[@]} - 1 ))]};
			unset CHECK_NODES[$(( ${#CHECK_NODES[@]} - 1 ))];
			node=$(( $node - 1 ));
			cluster_size=$(( $cluster_size - 1 ));
		} fi;
	} done;
	if [ ${#CHECK_NODES[@]} != 0 ]; then {
		# NOT ALL nodes are in sync state, so check them again after sometime
		if (( $retry % 2 )); then {
			sleep 20;
		} else {
			sleep 40;
		} fi;
	} else {
		# all nodes are in sync state
		break;
	} fi;
} done;

cluster_size=${#CHECK_NODES[@]};
for (( node=0; node < $cluster_size; ++node )); do {
	# nodes have NOT synced within the timelimit
	node_state=$( mysql --force --host=${CHECK_NODES[$node]} --password=${MYSQL_PASSWORD} -B -N -e "SHOW STATUS WHERE Variable_name = 'wsrep_local_state_comment';" | awk '{print $2}' );
	if [  $node_state == "Synced" ]; then {
		# node has become Synced
		log_info "${CHECK_NODES[$node]} is in $node_state state.";
		# remove the good node from the list
		CHECK_NODES[$node]=${CHECK_NODES[$(( ${#CHECK_NODES[@]} - 1 ))]};
		unset CHECK_NODES[$(( ${#CHECK_NODES[@]} - 1 ))];
		node=$(( $node - 1 ));
		cluster_size=$(( $cluster_size - 1 ));
	} else {
		log_error "${CHECK_NODES[$node]} is in $node_state state. Please take corrective action.";
		# exit will happen in the following if check outside this loop, allowing to print multiple node failures one after other
	} fi;
} done;

if [ ${#CHECK_NODES[@]} != 0 ]; then {
	log_error "Restore operation failed in ${#CHECK_NODES[@]} nodes in a timely manner.";
	# TODO: based on the state, provide info if the node will be good in sometime later.
	exit 2;
} fi;

log_info "Restore operation completed successfully in all ${#CLUSTER_NODES[@]} nodes.";
# restore completed successfully
# try to compress the deflated file again to save space
exit 0;

