#!/usr/bin/python

import ConfigParser
import requests
import time
import random
from time import gmtime, strftime
import logging
import logging.handlers
from platform_encryption.aes_util import AESUtil
import os

# Log file path
LOG_FILE_PATH = '/var/log/caspian/background-sync.log'
# Maximum size for log files
LOG_MAX_SIZE = 20*1024*1024
# Log back up files
LOG_BACKUP_COUNT = 5

CONFIGURATION_FILE = r'/opt/caspian/conf/account.conf'

DEFAULT_DOMAIN_NAME = "default"
X_SUBJECT_TOKEN = "X-Subject-Token"
X_AUTH_TOKEN = "X-Auth-Token"
LOCATION_HEADER = "Location"

STATUS = "status"
STATUS_READY = "Ready"
STATUS_RUNNING = "Running"
STATUS_SUCCESSFUL = "Successful"
STATUS_FLEETING_ERROR = "FleetingError"
STATUS_FATAL_ERROR = "FatalError"

NO_OF_RETRIES = 5
CONNECTION_TIMEOUT = 2.0
READ_TIMEOUT = 120.0
POLL_INTERVAL = 5
MAX_POLL_INTERVAL = 5 * 60

ACCOUNT_SERVICE_URI = "http://127.0.0.1:35359"
KEYSTONE_VERSION = "/v3"
KEYSTONE_TOKEN_PATH = "/auth/tokens"
ACCOUNT_SERVICE_PATH = "/v1/accounts"
RESOURCE_SEPERATOR = "/"
SYNC_ROLES = "/sync-roles"
#The environment variable shared because cron job cannot access environment variables
KS_CPSA_PWD = "${CaPassword}"

session = requests.Session()

session.mount("http://", requests.adapters.HTTPAdapter(max_retries=NO_OF_RETRIES))
session.mount("https://", requests.adapters.HTTPAdapter(max_retries=NO_OF_RETRIES))

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

class BackgroundSync:
    def __init__(self):
        self.accountServiceUrI = None
        self.keystoneUrI = None
        self.adminUser = None
        self.adminPassword = None

    def get_task_status(self, headers, taskLocation):

        response = self.api_request(taskLocation, headers)

        if response == None:
            logger.error('Unknown error while getting Task Status, received null response')
            return None

        if response.status_code == 200:
            return response.json()[STATUS]
        else:
            logger.error('Unknown error while getting Task Status, failed with status ' + str(response.status_code))
            return None

    def sync_roles(self, headers, accountId):

        sync_roles_path = self.accountServiceUrI + ACCOUNT_SERVICE_PATH + RESOURCE_SEPERATOR + accountId +  SYNC_ROLES
        response = self.api_request(sync_roles_path, headers, request_type = 'PUT')

        if response == None:
            logger.error('Unknown error while syncing roles for account ' + accountId + ', received null response')
            return False, None

        if response.status_code == 202:
            return True, response.headers[LOCATION_HEADER]
        else:
            logger.error('Error while syncing roles for account ' + accountId)
            logger.error('Request failed with status ' + str(response.status_code))
            return False, None

    def get_accounts(self , headers):

        response = self.api_request(self.accountServiceUrI + ACCOUNT_SERVICE_PATH, headers)

        if response == None:
            logger.error('Unknown error while getting list of accounts, received null response')
            return None

        if response.status_code == 200:
            return response.json()
        else:
            logger.error('Get accounts request failed with status ' + str(response.status_code))
            return None

    def generate_token(self, headers):
        values = TOKEN_REQUEST % (DEFAULT_DOMAIN_NAME, self.adminUser, self.adminPassword, DEFAULT_DOMAIN_NAME)

        response = self.api_request(self.keystoneUrI + KEYSTONE_VERSION + KEYSTONE_TOKEN_PATH, headers, data = values, request_type = 'POST', no_of_retries = 0)

        if response == None:
            logger.error('Unknown error while getting token, received null response')
            return None

        if response.status_code == 201:
            logger.debug('Successfully created a new token')
            return response.headers[X_SUBJECT_TOKEN]
        else:
            logger.error('Get token request failed with status ' + str(response.status_code));
            return None

    def api_request(self, url, headers, data = None, verify = False, timeout = (CONNECTION_TIMEOUT, READ_TIMEOUT), request_type = 'GET', no_of_retries = 1):
        while True:
            try:
                if(request_type == 'GET'):
                    response = session.get(url, headers = headers, verify = verify, timeout = timeout)
                elif(request_type == 'POST'):
                    response = session.post(url, data = data, headers = headers, verify = verify, timeout = timeout)
                elif(request_type == 'PUT'):
                    response = session.put(url, data = data, headers = headers, verify = verify, timeout = timeout)
                elif(request_type == 'DELETE'):
                    response = session.delete(url, data = data, headers = headers,verify = verify, timeout = timeout)
            except requests.exceptions.ConnectTimeout as e:
                logger.error("The request timed out while trying to connect to the server, failed with error " + str(e))
            except requests.exceptions.ReadTimeout as e:
                logger.error("The server did not send any data in the allotted amount of time, failed with error " + str(e))
            else:
                if response.status_code != 401 or no_of_retries == 0:
                    return response
                else:
                    no_of_retries = no_of_retries - 1
                    headers[X_AUTH_TOKEN] = self.generate_token(headers)

    def initialize_logger(self):
        global logger
        logger = logging.getLogger(__name__)
        logger.setLevel(logging.INFO)
        logging.Formatter.converter = time.gmtime
        formatter = logging.Formatter('%(asctime)s.%(msecs)03dZ %(levelname)s %(name)s %(message)s', '%Y-%m-%dT%H:%M:%S')
        handler = logging.handlers.RotatingFileHandler(LOG_FILE_PATH, maxBytes=LOG_MAX_SIZE, backupCount=LOG_BACKUP_COUNT)
        handler.setFormatter(formatter)
        logger.addHandler(handler)
    
    def configure(self):
        self.initialize_logger()

        configParser = ConfigParser.RawConfigParser()
        configParser.read(CONFIGURATION_FILE)
        logger.debug('Successfully read the configuration file')

        self.accountServiceUrI = ACCOUNT_SERVICE_URI
        logger.debug('Setting Account Service URI to ' + self.accountServiceUrI)

        self.keystoneUrI = configParser.get('keystone', 'auth_uri')
        logger.debug('Setting Keystone URI to ' + self.keystoneUrI)

        self.adminUser = configParser.get('keystone', 'admin_user')
        if self.adminUser == None:
            logger.error('Admin username not set')
        with AESUtil() as au:
            self.adminPassword = au.decrypt(KS_CPSA_PWD)
        if self.adminPassword == None or self.adminPassword == '':
            logger.error('Admin password not set')

def main():
    start_time = time.time()
    backgroundSync = BackgroundSync()
    backgroundSync.configure()
    logger.info('Starting sync roles cron job at ' + strftime("%a, %d %b %Y %X", gmtime()))

    headers = {"Content-Type" : "application/json"}
    token = backgroundSync.generate_token(headers)

    if token == None:
        logger.error('Could not generate token')
        logger.info('Ending sync roles cron job at ' + strftime("%a, %d %b %Y %X", gmtime()))
        logger.info('Failure while performing sync roles cron job for current period. Time taken: ' + (time.time() - startTime) + ' seconds')
        return

    headers[X_AUTH_TOKEN] = token

    accounts = backgroundSync.get_accounts(headers)

    if accounts == None:
        logger.error('Error while receiving list of accounts')
        logger.info('Ending sync roles cron job at ' + strftime("%a, %d %b %Y %X", gmtime()))
        logger.info('Failure while performing sync roles cron job for current period. Time taken: ' + (time.time() - startTime) + ' seconds')
        return
    else:
        logger.info('Successfully received list of accounts')

    accountList = []
    for accountNumber in range(0, len(accounts['accounts'])):
        accountId = accounts['accounts'][accountNumber]['id']
        accountList.append(accountId)
    random.shuffle(accountList)

    noOfFleetingTasks = (0,[])
    noOfFatalTasks = (0,[])
    noOfSuccessfulTasks = (0,[])
    noOfUnderterminedTasks = (0,[])

    for accountId in accountList:
        isSynced, taskLocation = backgroundSync.sync_roles(headers, accountId)
        # Wait for maximum(task_completion,MAX_POLL_INTERVAL seconds)
        wait_interval = MAX_POLL_INTERVAL
        if isSynced:
            logger.debug('Successfully requested for sync roles for account ' + accountId)
            taskStatus = STATUS_READY
            while taskStatus == STATUS_READY or taskStatus == STATUS_RUNNING:
                 taskStatus = backgroundSync.get_task_status(headers, taskLocation)
                 if wait_interval <= 0:
                     noOfUnderterminedTasks = (noOfUnderterminedTasks[0] + 1, noOfUnderterminedTasks[1] + [accountId])
                     break
                 wait_interval = wait_interval - POLL_INTERVAL;
                 time.sleep(POLL_INTERVAL)

            if taskStatus == STATUS_SUCCESSFUL:
                noOfSuccessfulTasks = (noOfSuccessfulTasks[0] + 1, noOfSuccessfulTasks[1] + [accountId] )
            if taskStatus == STATUS_FLEETING_ERROR:
                noOfFleetingTasks = (noOfFleetingTasks[0] + 1, noOfFleetingTasks[1] + [accountId])
            if taskStatus == STATUS_FATAL_ERROR:
                noOfFatalTasks = (noOfFatalTasks[0] + 1, noOfFatalTasks[1] + [accountId])
        else:
            noOfUnderterminedTasks = (noOfUnderterminedTasks[0] + 1, noOfUnderterminedTasks[1] + [accountId])
            logger.error('Unsuccessful request for sync roles for account ' + accountId)
        time.sleep(POLL_INTERVAL)

    logger.info('----------------STATISTICS----------------')
    logger.info('List of Accounts Processed : ' + str(accountList))
    logger.info("List of Successful Tasks : " + str(noOfSuccessfulTasks))
    logger.info("List of Tasks with Fleeting Error : " + str(noOfFleetingTasks))
    logger.info("List of Tasks with Fatal Error : " + str(noOfFatalTasks))
    logger.info("List of Undetermined Tasks : " + str(noOfUnderterminedTasks))

    logger.info('Ending sync roles cron job at ' + strftime("%a, %d %b %Y %X", gmtime()))
    logger.info('Successfully finished sync roles cron job for current period. Time taken: ' + str(time.time() - start_time) + ' seconds')
    logger.info('------------------------------------------------------------------------')

if __name__ == "__main__":
    main()