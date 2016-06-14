#!/usr/bin/python
import time
import ConfigParser
import MySQLdb as mdb
import logging
import logging.handlers
import requests
import os
import sys
from platform_encryption.aes_util import AESUtil

# Delay between attempting to connect again to the DB (seconds)
CONNECTION_DELAY= 2
# Separator
ITERATION_SEPARATOR = '------------------------------------'
# Environment variable for CRS
COMPONENT_REGISTRY = "${CRS_IP}"

CONNECTION_TIMEOUT = 2.0
READ_TIMEOUT = 120.0
session = requests.Session()

DEFAULT_DOMAIN_NAME = "default"
X_SUBJECT_TOKEN = "X-Subject-Token"
X_AUTH_TOKEN = "X-Auth-Token"

KEYSTONE_VERSION = "/v3"
KEYSTONE_TOKEN_PATH = "/auth/tokens"
KEYSTONE_DOMAIN_PATH = "/domains"

CONFIG_FILE = r'/opt/caspian/conf/account.conf'
KS_CPSA_PWD = "${CaPassword}"

TOKEN_REQUEST = """\
{
    "auth": {
        "identity": {
            "methods": [
                "password"
            ],
            "password": {
                "user": {
                    "domain": {
                        "name": "%s"
                    },
                    "name": "%s",
                    "password": "%s"
                }
            }
        },
        "scope": {
            "domain": {
                "name": "%s"
            }
        }
    }
}\
"""

CONF_FILE="/opt/caspian/scripts/dbcleanup.conf"
dbs = {}

class DB:
    def __init__(self, dbName):
        self.logger = None
        # DB settings
        self.dbName = dbName
        self.dbHost = None
        self.dbPort = None
        self.dbUser = None
        self.dbPass = None
        # DB modifiers
        self.cursor = None
        self.db = None

    def setHost(self, host):
        self.dbHost = host
    
    def setPort(self, port):
        self.dbPort = port
    
class CleanupManager:
    def __init__(self, dbProperties):
        self.logger = None
        # DB modifiers
        self.cursor = None
        self.db = None
        self.dbProperties = dbProperties
        # Target tables
        self.targetTables = None
        self.keystoneUrI = None
        self.adminUser = None
        self.adminPassword = None
    
    def bootstrap(self):
        try:
            # Initialize logging
            self.initializeLogger()
            # Fetch DB details
            dbEndpoints = self.getDBEndpoints()    
            # If no endpoint was found, exit with error
            if dbEndpoints == None or 'endpoints' not in dbEndpoints or len(dbEndpoints['endpoints'])==0:
                self.logger.warn("Fetching DB details failed")
                sys.exit()
            endpoints = dbEndpoints['endpoints']
            
            # Pick the first db endpoint
            endpoint = endpoints[0]['url']
            
            # Get DB port and host
            dbPort = int(endpoint.split(":")[2])
            dbHost = endpoint.split(":")[1][2:]
            # Set db properties
            self.dbProperties.setHost(dbHost)
            self.dbProperties.setPort(dbPort)
            self.logger.debug("Bootstrap stage completed for db: "+ self.dbProperties.dbName)
            # Need to set the DB details for all databases except Keystone
            if not self.dbProperties.dbName=="keystone":
                self.setDBAccessDetails()
            else:
                self.dbProperties.dbUser = dbs[self.dbProperties.dbName]['user']
                self.dbProperties.dbPass = dbs[self.dbProperties.dbName]['pass']
            self.setupDBConnection()
        except Exception as e:
            self.logger.exception("Error in bootstrap stage")
            sys.exit()

    def generate_token(self, headers):
        values = TOKEN_REQUEST % (DEFAULT_DOMAIN_NAME, self.adminUser, self.adminPassword, DEFAULT_DOMAIN_NAME)

        response = session.post(self.keystoneUrI + KEYSTONE_VERSION + KEYSTONE_TOKEN_PATH, headers=headers, data = values, verify=False, timeout = (CONNECTION_TIMEOUT, READ_TIMEOUT))

        if response == None:
            self.logger.error('Unknown error while getting token, received null response')
            return None

        if response.status_code == 201:
            self.logger.debug('Successfully created a new token')
            return response.headers[X_SUBJECT_TOKEN]
        else:
            self.logger.error('Get token request failed with status ' + str(response.status_code));
            return None

    def performIdpCleanup(self, tableName):
        self.configure()
        headers = {"Content-Type" : "application/json"}
        token = self.generate_token(headers)
        headers[X_AUTH_TOKEN] = token
        try:
            self.cursor.execute("SELECT {0} from {1};".format(dbs[self.dbProperties.dbName]['tables'][tableName]['attr'], \
                                                                        tableName))
            for idpid in self.cursor:
                domainid = ", ".join( s.join(['"','"']) for s in idpid)
                response = session.get(self.keystoneUrI + KEYSTONE_VERSION + KEYSTONE_DOMAIN_PATH + domainid, headers=headers,verify = False, \
                                       timeout = (CONNECTION_TIMEOUT, READ_TIMEOUT))
                if response.status_code == 404:
                    query = ("DELETE FROM IdpPassword where idp_id = %s")
                    self.cursor.execute(query,idpid)
        except Exception as e:
            self.logger.exception("Cleanup failed for table: "+tableName)
    
    def performCleanup(self):   
        try:
            for tableName in dbs[self.dbProperties.dbName]['tables'].keys():
                if tableName == 'IdpPassword':
                    self.performIdpCleanup(tableName)
                else:
                    self.performTableCleanup(tableName)
            self.logger.debug("Cleanup completed for db: "+ self.dbProperties.dbName)
        except Exception as e:      
            self.logger.info("Cleanup failed for table: "+tableName)
        finally:
            self.logger.info(ITERATION_SEPARATOR)
    
    def performTableCleanup(self, tableName):
        try:
            # Setup the queries based on the table attributes and their time formats
            if dbs[self.dbProperties.dbName]['tables'][tableName]['format'] == "datetime":
                queryGetTableSize = "SELECT count(*) from {0} where DATEDIFF(NOW(), {1})>={2};".format(tableName, \
                dbs[self.dbProperties.dbName]['tables'][tableName]['attr'], dbs[self.dbProperties.dbName]['tables'][tableName]['age'])
                queryClearTable = "DELETE FROM {0} where DATEDIFF(NOW(), {1})>={2};".format(tableName, \
                dbs[self.dbProperties.dbName]['tables'][tableName]['attr'], \
                dbs[self.dbProperties.dbName]['tables'][tableName]['age'])

            elif dbs[self.dbProperties.dbName]['tables'][tableName]['format'] == "bigint":
                queryGetTableSize = "SELECT count(*) from {0} where DATEDIFF(NOW(),  FROM_UNIXTIME({1} / 1000))>={2};".format(tableName, \
                dbs[self.dbProperties.dbName]['tables'][tableName]['attr'], dbs[self.dbProperties.dbName]['tables'][tableName]['age'])
                queryClearTable = "DELETE FROM {0} where DATEDIFF(NOW(), FROM_UNIXTIME({1} / 1000))>={2};".format(tableName, \
                dbs[self.dbProperties.dbName]['tables'][tableName]['attr'], \
                dbs[self.dbProperties.dbName]['tables'][tableName]['age'])       
            # Get table size
            if self.retriableSQLTask(queryGetTableSize):
                result = self.cursor.fetchone()
                self.logger.debug('Fetched table details')
                tableSize = result[0]
                self.logger.debug('Table size in rows: '+str(tableSize))
                # If it exceeds the threshold size of the table
                if tableSize > dbs[self.dbProperties.dbName]['tables'][tableName]['size']:
                    # Clear tokens older than the minimum age
                    if self.retriableSQLTask(queryClearTable):
                        self.logger.debug('Number of deleted rows: '+str(self.cursor.rowcount))
                        self.logger.info('Cleanup successful for table: '+tableName)
                    else:
                        self.logger.warn('Cleanup failed for table: '+tableName)
                else:
                    self.logger.debug('No need for cleanup for table: '+tableName)
            else:
                self.logger.warn('Failed to fetch details for table '+tableName)
        except Exception as e:
            self.logger.exception("Cleanup failed for table: "+tableName)            
    
    def initializeLogger(self):
        self.logger = logging.getLogger(self.dbProperties.dbName)
        self.logger.setLevel(logging.DEBUG)
        logging.Formatter.converter = time.gmtime
        formatter = logging.Formatter('%(asctime)s.%(msecs)03dZ - %(name)s - %(levelname)s - %(message)s', '%Y-%m-%dT%H:%M:%S')
        handler = logging.handlers.RotatingFileHandler(dbs[self.dbProperties.dbName]['log'], maxBytes=20*1024*1024, backupCount=5)
        handler.setFormatter(formatter)
        self.logger.addHandler(handler)
        
    def retriableSQLTask(self, query):
        # Retries a SQL query 3 times just in case a connection issue is causing it to fail, returns True if query execution succeeds
        attempts = 0
        while attempts<3:
            try:
                attempts = attempts+1
                self.cursor.execute(query)
                return True
            except mdb.Error, e:
                self.logger.warn("Attempt %s: Error during query execution: %s %d: %s" % (query, attempts, e.args[0], e.args[1]))
                time.sleep(CONNECTION_DELAY)
                self.setupDBConnection()
                continue
            except Exception as e:
                self.logger.exception("Exception during query execution: %s" %(str(e)))
                time.sleep(CONNECTION_DELAY)
                self.setupDBConnection() 
        return False

    def setupDBConnection(self):
        attempts = 0
        if self.cursor != None:
            self.cursor.close
        if self.db !=None:
            self.db.close
        self.cursor = None
        self.db = None
        while attempts<10:
            try:
                attempts = attempts+1
                self.db = mdb.connect(host = self.dbProperties.dbHost, user =self.dbProperties.dbUser, passwd =self.dbProperties.dbPass, db=self.dbProperties.dbName, port = self.dbProperties.dbPort)
                self.cursor = self.db.cursor()
                self.db.autocommit(True)
                self.logger.debug("DB connection setup successful")
                break
            except mdb.Error, e:
                self.logger.warn("Attempt %s: Error during creating connection: %d: %s" % (attempts, e.args[0], e.args[1]))
                time.sleep(CONNECTION_DELAY)
                continue
            except Exception as e:
                self.logger.exception("Exception during connection creation: %s" %(str(e)))
                time.sleep(CONNECTION_DELAY)
                continue

                
    def setDBAccessDetails(self):
        try:
            configParser = ConfigParser.RawConfigParser()
            configParser.read(dbs[self.dbProperties.dbName]['conf'])
            self.dbProperties.dbUser = configParser.get('database', dbs[self.dbProperties.dbName]['userAttr'])
            self.dbProperties.dbPass = configParser.get('database', dbs[self.dbProperties.dbName]['passAttr'])
            self.logger.debug("Successfully read the conf file for db: "+ self.dbProperties.dbName)
        except:
            self.logger.exception("Failed to read from the conf file for db: "+self.dbProperties.dbName)
                
    def getDBEndpoints(self):
        try:
            path = "/v1/services/platform/components/mysql-galera"
            r = requests.get(COMPONENT_REGISTRY+path)
            return r.json()
        except Exception as e:
            self.logger.exception("Exception while fetching DB endpoints")
            return None

    def configure(self):

        configParser = ConfigParser.RawConfigParser()
        configParser.read(CONFIG_FILE)

        self.keystoneUrI = configParser.get('keystone', 'auth_uri')
        self.adminUser = configParser.get('keystone', 'admin_user')
        with AESUtil() as au:
            self.adminPassword = au.decrypt(KS_CPSA_PWD)
        if self.adminPassword == None or self.adminPassword == '':
            self.logger.error('Admin password not set')


def readFromConf():
    try:
        global dbs
        global CRS_ENVIRON
        configParser = ConfigParser.RawConfigParser()
        configParser.read(CONF_FILE)
        databases = configParser.get('databases', 'dbs').split(",")
        for database in databases:
            dbs[database] = {}
            if database =='accounts':
                dbs[database]['userAttr'] = configParser.get(database, 'userAttr')
                dbs[database]['passAttr'] = configParser.get(database, 'passAttr')
                dbs[database]['conf'] = configParser.get(database, 'confFile')
                dbs[database]['log'] = configParser.get(database, 'logPath')
                tables = configParser.get(database, 'tables').split(",")
                dbs[database]['tables'] = {}
                for table in tables:
                    dbs[database]['tables'][table] = {}
                    dbs[database]['tables'][table]['attr'] = configParser.get(table, 'attr')
                    dbs[database]['tables'][table]['format'] = configParser.get(table, 'format')
            else:
                if database =='keystone':
                    dbs[database]['user'] = configParser.get(database, 'user')
                    dbs[database]['pass'] = configParser.get(database, 'pass')
                else:
                    dbs[database]['userAttr'] = configParser.get(database, 'userAttr')
                    dbs[database]['passAttr'] = configParser.get(database, 'passAttr')
                    dbs[database]['conf'] = configParser.get(database, 'confFile')
                dbs[database]['log'] = configParser.get(database, 'logPath')
                tables = configParser.get(database, 'tables').split(",")
                dbs[database]['tables'] = {}
                for table in tables:
                    dbs[database]['tables'][table] = {}
                    dbs[database]['tables'][table]['attr'] = configParser.get(table, 'attr')
                    dbs[database]['tables'][table]['format'] = configParser.get(table, 'format')
                    dbs[database]['tables'][table]['age'] = int(configParser.get(table, 'age'))
                    dbs[database]['tables'][table]['size'] = int(configParser.get(table, 'size'))
    except Exception as e:
        print e
            
def main():
    readFromConf()
    keystone = DB("keystone")
    workflow = DB("workflow")
    accounts = DB("accounts")
    accountsCleanupManager = CleanupManager(accounts)
    keystoneCleanupManager = CleanupManager(keystone)
    workflowCleanupManager = CleanupManager(workflow)
    accountsCleanupManager.bootstrap()
    keystoneCleanupManager.bootstrap()
    workflowCleanupManager.bootstrap()
    keystoneCleanupManager.performCleanup()
    workflowCleanupManager.performCleanup()
    accountsCleanupManager.performCleanup()

if __name__ == "__main__":
    main()
