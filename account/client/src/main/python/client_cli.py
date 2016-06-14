import argparse
import account
from printhelp import *
from argparse import RawTextHelpFormatter
import utils

############################################### ARGUMENT PARSER RELATED OPERATIONS #################################################

parser = argparse.ArgumentParser(prog='account',usage='%(prog)s [create/list/get/modify/delete/list-domains/list-mapping/task-status/create-domain/associate-domain/delete-domain/lis-all-idps/list-idp-for-domain/create-idp/delete-idp/get-idp/update-idp/get-account-id-from-name/get-account-info-from-name/get-domain-id-from-idp/get-domain-id-from-name/get-domain-info-from-id/get-domain-info-from-idp/get-domain-info-from-name/get-account-id-from-domain/sync-roles/elect-new-primary-domain]',
	description = """
	'account create'                         Creates a new account
	'account list'                           Lists all the accounts that already exist.
	'account get'                            Retrieves information about a particular account.
	'account modify'                         Modifies an existing account. 
	'account delete'                         Deletes an account if all the preconditions for deletion are met.
	'account get-account-id-from-domain'	 Gets the account ID corresponding to the domain ID provided.
	'account sync-roles'			 Synchronizes roles across domains of an account for a given Account ID.
	'account list-domains'                   Lists all the domains that exist including the default domain.
	'account list-mapping'                   Lists the account to domain mapping for a particular account. 
	'account task-status'                    Returns the status of the task represented by the Location URL in asynchronous APIs. 
	'account create-domain'                  Creates a new domain. 
	'account associate-domain'               Associates an existing domain to an account. 
	'account delete-domain'                  Deletes a domain from an account.
	'account elect-new-primary-domain'	 Elects a new primary domain for an account.
	'account list-all-idps'                  Lists all the IDPs present in the form a table.
	'account list-idp-for-domain'            Lists the IDP associated to the given Domain ID. 
	'account create-idp'                     Creates an IDP for a Domain. 
	'account get-idp'                        Gets one IDP configuration.
	'account update-idp'                     Updates an IDP configuration. 
	'account delete-idp'                     Deletes an IDP configuration for the corresponding domain.
	'account get-account-id-from-name'       Given the Account name retrieves the Account ID
	'account get-account-info-from-name'     Given the Account name retrieves all the information about the Account
	'account get-domain-id-from-idp'         Given the IDP ID retreives all the information related to the corresponding Domain
	'account get-domain-id-from-name'        Given the Name of the Domain retrieves the ID of the Domain
	'account get-domain-info-from-id'        Given the ID of the Domain retrieves all the information about the Domain
	'account get-domain-info-from-idp'       Given the IDP ID retrieves all the information about the Domain
	'account get-domain-info-from-name'      Given the Name of the Domain retrieves all the information about the Domain 
	""",formatter_class=RawTextHelpFormatter)

parser.add_argument(
        'func',
        metavar='<Account Action>',
        action = 'store',
        help = "Action to be performed on the Account Service like creating accounts etc",
)
parser.add_argument(
        "-aid","--account-id",
        metavar="<account-id>",
        action="store",
        dest="aid",
        help="ID of the account to be accessed",
)
parser.add_argument(
        "-n","--name",
        metavar="<account-name>",
        action="store",
        dest = 'name',
        help="Name of the new Account to be created",
)
parser.add_argument(
        "-des","--description",
        metavar="<account-description>",
        action="store",
        dest = 'description',
        help="Description for the account",
)
parser.add_argument(
	"-dname","--domain-name",
	metavar="<domain-name>",
	action="store",
	dest="dname",
	help="Name of Domain",
)

parser.add_argument(
        "-did","--domain-id",
        metavar="<domain-id>",
        action="store",
        dest="did",
        help="ID of Domain",
)
parser.add_argument(
        "-ddes","--domain-description",
        metavar="<domain-description>",
        action="store",
        dest="ddes",
        help="Description of Domain",
)
parser.add_argument(
        "-idpid",
        metavar="<idp-id>",
        action="store",
        dest="idpid",
        help="ID of IDP",
)
parser.add_argument(
        "-url","--location-url",
        metavar="<location-url>",
        action="store",
        dest="url",
        help="URL of the task status querying endpoint",
)
parser.add_argument(
        "-f","--file",
        metavar="<file path>",
        action="store",
        dest="fpath",
        help="Path to the file to be used as data for requests",
)



parsed_items = parser.parse_args()

################################### FUNCTIONS TO BE CONSUMED BY USERS #########################################################################

def create_account(parsed_results):
	'''
        	Function to create new accounts. Internally calls the create_account_basic() function which makes the 
		HTTP requests on behalf of user. The create_account_basic() function returns a dictionnary of the response,
		status code and the reason. A sample dictionnary is as follows:
	
		{'status': 201, 'reason': 'Created', 'response': {u'active': True, u'description': u'test-acc-4-des', u'id': u'fe1c5357-9c9e-4b57-9ee3-8b9b9f14f2f6', u'		name': u'test-acc-4'}}
	
		Here the value corresponding to the response key is the response that the function call returned. If only response is desired use the get_response()
		function from the utils library.
    	'''
	my_dict = account.create_account_basic(parsed_results.name,parsed_results.description)
	response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
	if my_dict['status']==201:
		print_table_for_others(response_dict)
	else:
		print "\nAccount creation failed"
        	print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
        	print "Error message: %s\n" %response_dict['error']['message']


def list_account():
        '''
                Function to list existing accounts. Internally calls the core list_account_basic() function which makes the
                HTTP requests on behalf of user. The create_account_basic() function returns a dictionnary of the response,
                status code and the reason. A sample dictionnary for list call is as follows:

                {'status': 200, 'reason': 'OK', 'response': {u'accounts': [{u'active': True, u'description': u'test-acc-3-des', u'id': u'27b8d2a7-b1df-4eb1-aaa9-e8ff903		cdeee', u'name': u'test-acc-3'}, {u'active': True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', u'name': u'test-ac		c-2'}, {u'active': True, u'description': u'test-acc-1-des', u'id': u'd2eca704-67ab-4ea5-8a92-32380863e95e', u'name': u'test-acc-1'}]}}

                Here the value corresponding to the 'response' key is the response that the function call returned. The response key is itself a dictionnary and can be 
		accessed by the utils library get_response() function or by accessing the 'response' key. Taking out only the response we will get something as follows:

		{u'accounts': [{u'active': True, u'description': u'test-acc-3-des', u'id': u'27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee', u'name': u'test-acc-3'}, {u'active': 		True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', u'name': u'test-acc-2'}, {u'active': True, u'description': u'te		st-acc-1-des', 	u'id': u'd2eca704-67ab-4ea5-8a92-32380863e95e', u'name': u'test-acc-1'}]}
		
		We can see that the response is a dictionnary with one key 'accounts' which has a value as a list of dictionnaries.
		
        '''
	my_dict = account.list_account_basic()
	response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
        	print_table_for_list(response_dict)
        else:
        	print "\nListing of accounts failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_one_account(parsed_results):
	'''
                Function to get information about a single account. Internally calls the get_account_basic() function which makes the
                HTTP requests on behalf of user. The get_account_basic() function returns a dictionnary of the response,status code and the reason. 
		A sample dictionnary is as follows:
                
		{'status': 200, 'reason': 'OK', 'response': {u'active': True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', 
		u'name': u'test-acc-2'}}

		Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
	my_dict = account.get_account_basic(parsed_results.aid)
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_others(response_dict)
        else:
                print "\nFailed to retrieve information"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s" %response_dict['error']['message']


def modify_account(parsed_results):		
	'''
                Function to modify the information about a single account. Internally calls the modify_account_basic() function which makes the
                HTTP requests on behalf of user. The modify_account_basic() function returns a dictionnary of the response,status code and the reason.
                A sample dictionnary is as follows(Here the test-acc-2 has been modified and named as test-acc-2-mod):

                {'status': 200, 'reason': 'OK', 'response': {u'active': True, u'description': u'test-acc-2-des-mod', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', 
		u'name': u'test-acc-2-mod'}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
	my_dict = account.modify_account_basic(parsed_results.name,parsed_results.description,parsed_results.aid)
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_others(response_dict)
        else:
                print "\nAccount modification failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def delete_account(parsed_results):
	'''
                Function to delete an account if the precondition is satisfied. Internally calls the delete_account_basic() function which makes the
                HTTP requests on behalf of user. The delete_account_basic() function returns a dictionnary of the response,status code and the reason if the 
		account deletion fails due to preconditions not being satisfied. If the account is successfully deleted the 'response' key in the returned 
		dictionnary will have value as 'Deletion successful'.
                A sample dictionnary is as follows:
		
		{'status': 204, 'reason': 'No Content', 'response': 'Deletion successful'}
                
                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
	my_dict = account.delete_account_basic(parsed_results.aid)
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']!=202:
                print "\nAccount deletion failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']




def get_account_id_from_domain_id(parsed_results):
        '''
                Function that Retrieves the account id corresponding to a domain id. Internally calls the get_account_id_from_domain_basic()
                The get_account_id_from_domain_basic() function returns a dictionary of the response, status code and the reason.
                A sample dictionary of the correct response case is as follows:

                {'status':200, 'reason':'OK', 'response':{"account_id":"0a5acaec-4928-4790-b8fe-53186f8e49f4","is_primary":false}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If the actions are to be
                performed on the response dictionary the function can be modified to use the my_dict dictionary as used below.
        '''
        my_dict = account.get_account_id_from_domain_basic(parsed_results.did)
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_get_account_from_domain(parsed_results.did,response_dict)
        else:
                print "\nFailed to retrieve Account ID from the given Domain ID"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def sync_roles(parsed_results):
        '''
                Function which Synchronizes domain scoped role assignments between the primary domain and the other domains of the account.
                Internally it calls the sync_roles_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
        '''
        my_dict = account.sync_roles_basic(parsed_results.aid)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nSyncing of roles for account {aid} failed".format(aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_task_status(parsed_results):
        '''
                Get the status of the workflow task for IDP handling being created in asynchronous API implementations
                Parameters: URL of the task status querying endpoint
                Parameter Type: String
                Returns a dictionnary with response, status and reason keys having the response as a dictionnary
                the response status code and the status code respectively
        '''
        my_dict = account.get_account_task_status(parsed_results.url)
	if my_dict['status'] == 200:
		print "\nThe task status is " + my_dict['response']['status'] + "\n"
	else:
		print "\nThe mentioned task does not exist\n" 


def list_all_domains():
	'''
                Function to list all existing domains including the default domain. Internally calls the core list_domains_enhanced() function which makes the
                HTTP requests on behalf of user. The list_domains_enhanced() function returns a dictionnary of the response,
                status code and the reason. A sample dictionnary for list call is as follows:

                {'status': 200, 'reason': 'OK', 'response': {u'domains': [{u'description': u'Primary domain for account test-acc-4', u'name': u'test-acc-4', 
		u'is_primary': True, u'enabled': True, u'id': u'0ba1424ca23747de8e737b6a185db9d4', u'account_name': u'test-acc-4', 
		u'account_id': u'fe1c5357-9c9e-4b57-9ee3-8b9b9f14f2f6'}, {u'description': u'Primary domain for account test-acc-1', u'name': u'test-acc-1', 
		u'is_primary': True, u'enabled': True, u'id': u'40f5c2555fb74c69bc8d57bcfe0bb81b', u'account_name': u'test-acc-1', 
		u'account_id': u'd2eca704-67ab-4ea5-8a92-32380863e95e'}, {u'description': u'Primary domain for account test-acc-2', u'name': u'test-acc-2', 
		u'is_primary': True, u'enabled': True, u'id': u'b174fa2e4fec48a8b2f4f0be78fe99b8', u'account_name': u'test-acc-2-mod', 
		u'account_id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990'}, {u'description': u'Primary domain for account test-acc-3', u'name': u'test-acc-3', 
		u'is_primary': True, u'enabled': True, u'id': u'bc60682a5eda4c97a75898c0f8dccbbe', u'account_name': u'test-acc-3', 
		u'account_id': u'27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee'}, {u'description': u'Owns users and tenants (i.e. projects) available on Identity API v2.', 
		u'enabled': True, u'id': u'default', u'name': u'Default'}]}}


                Here the value corresponding to the 'response' key is the response that the function call returned. 
		The response key is itself a dictionnary and can be accessed by the utils library get_response() function or by accessing the 'response' key. 
                We can see that the response is a dictionnary with one key 'domains' which has a value as a list of dictionnaries.

        '''
	my_dict = account.list_domains_enhanced()
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_list_domains(response_dict)
        else:
                print "\nListing of domains failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def account_domain_mapping(parsed_results):
	'''
                Function to get account to domain mapping for an account. Internally calls the list_account_domain_mapping() function which makes the
                HTTP requests on behalf of user. The list_account_domain_mapping() function returns a dictionnary of the response,status code and the reason.
                A sample dictionnary is as follows:

                {'status': 200, 'reason': 'OK', 'response': {u'domains': [{u'is_primary': True, u'description': u'Primary domain for account test-acc-2', 
		u'enabled': True, u'id': u'b174fa2e4fec48a8b2f4f0be78fe99b8', u'name': u'test-acc-2'}]}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
	my_dict = account.list_account_domain_mapping(parsed_results.aid)
        response_dict = utils.get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_mapping(response_dict)
        else:
                print "\nListing of domains for the given account failed."
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def elect_new_primary_domain(parsed_results):
        '''
                Function to elect a new primary domain for an account
                Internally it calls the elect_new_primary_domain_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
        '''
        my_dict = account.elect_new_primary_domain_basic(parsed_results.aid,parsed_results.did)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 200:
                print "\nElecting of new primary domain for account {aid} failed".format(aid = parsed_results.aid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']



def create_domain(parsed_results):
        '''
                Function which Creates and associates a domain to an account.
		Requires the Account ID to which the Domain has to be associated and the name and description of the
		new domain to be created. Prints the current status of the task created for domain creation and 
		also prints the Location Header i.e. the URL which can be used to check the final status of the task.
		Typically takes a few seconds for the domain to be created and it is advised to use 'account list-domains' to check
		if the domain creation was successful.
		
        '''
        my_dict = account.create_domain_basic(parsed_results.aid,parsed_results.dname,parsed_results.ddes)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nDomain creation for account {aid} failed".format(aid = parsed_results.aid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def associate_domain(parsed_results):
        '''
                Function to associate a domain to an account
		Requires the Account ID to which the Domain has to be associated and the name and the ID of the Domain to be associated.
	        This is an Asynchronous API call meaning that it only starts the task and the task continues to execute in the background.
		Prints the current status of the task created for domain association and
                also prints the Location Header i.e. the URL which can be used to check the final status of the task.
                Typically takes a few seconds for the domain to be associated and it is advised to use the location URL
		with the 'account task-status' to check if the task was succcessfuly completed. 
		
        '''
        my_dict = account.associate_domain_to_account_basic(parsed_results.aid,parsed_results.did)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to associate domain {domain} to account {aid}".format(domain = parsed_results.did,aid = parsed_results.aid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def delete_domain(parsed_results):
        '''
                Function to delete a domain for a given account. Gives error if primary domain is tried to be deleted.
		Requires the Account ID to which the Domain is associated and the name and the ID of the Domain to be deleted.
                This is an Asynchronous API call meaning that it only starts the task and the task continues to execute in the background.
                Prints the current status of the task created for domain deletion and
                also prints the Location Header i.e. the URL which can be used to check the final status of the task.
                Typically takes a few seconds for the domain to be deleted and it is advised to use the location URL
                with the 'account task-status' to check if the task was succcessfuly completed.
        '''
        my_dict = account.delete_domain_basic(parsed_results.aid,parsed_results.did)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to delete domain {domain} for account {aid}".format(domain = parsed_results.did,aid = parsed_results.aid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_all_idps():
        '''
		Function to get all IDPs present in the account service
		Prints the result retrieved from the API call as a table.
        '''
        my_dict = account.list_idp_basic()
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] == 200 and len(response_dict['identity_providers'])>=0:
                print_table_for_idp(response_dict)
        else:
                print "\nListing of IDPs failed."
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" % (response_dict['error']['message'])


def get_idp_for_domain(parsed_results):
        '''
                Function to get all IDPs associated to a domain
		Prints the result retrieved from the API call as a table
        '''
        my_dict = account.list_idp_domain_filter(parsed_results.did)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] == 200 and len(response_dict['identity_providers'])>0:
                print_table_for_idp(response_dict)
	elif my_dict['status'] == 200 and len(response_dict['identity_providers']) == 0:
		print "\nThis domain has no IDPs associated with it\n"
        else:
                print "\nListing of IDPs for the domain {did} failed.".format(did = parsed_results.did)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_one_idp(parsed_results):
        '''
                Function to get information about a single IDP
		Prints the information retrieved from the API call in the form of a table
        '''
        my_dict = account.get_one_idp_basic(parsed_results.idpid)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 200:
                print "\nFailed to get information about IDP {idpid}".format(idpid = parsed_results.idpid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']
        else:
                print_table_for_one_idp(response_dict)


def create_idp(parsed_results):
        '''
                Function to create an IDP for a given account
		Requires the Account ID to which the Domain (to which the IDP has to be associated)belongs
		and the path of the file which contains the other details for making the API call.
                This is an Asynchronous API call meaning that it only starts the task and the task continues to execute in the background.
                Prints the current status of the task created for IDP creation and
                also prints the Location Header i.e. the URL which can be used to check the final status of the task.
                Typically takes a few seconds for the IDP to be created and it is advised to use the location URL
                with the 'account task-status' to check if the task was succcessfuly completed.
        '''
	kwargs={}
        my_dict = account.create_idp_basic(parsed_results.aid,parsed_results.fpath,kwargs)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to create IDP for account {aid}".format(aid = parsed_results.aid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" % (response_dict["error"]["message"])


def update_idp(parsed_results):
        '''
                Function to update the details for a particular IDP
		Requires the ID of the IDP to be modified and the path of the file which contains the other details for making the API call.
                This is an Asynchronous API call meaning that it only starts the task and the task continues to execute in the background.
                Prints the current status of the task created for IDP modification and
                also prints the Location Header i.e. the URL which can be used to check the final status of the task.
                Typically takes a few seconds for the IDP to be modified and it is advised to use the location URL
                with the 'account task-status' to check if the task was succcessfuly completed.
        '''
	kwargs={}
        my_dict = account.update_idp_basic(parsed_results.idpid,parsed_results.fpath,kwargs)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to update IDP for IDP {iid}".format(parsed_results.idpid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def delete_idp(parsed_results):
        '''
                Function to delete a particular IDP
		Requires the ID of the IDP to be deleted.
                This is an Asynchronous API call meaning that it only starts the task and the task continues to execute in the background.
                Prints the current status of the task created for IDP deletion and
                also prints the Location Header i.e. the URL which can be used to check the final status of the task.
                Typically takes a few seconds for the IDP to be deleted and it is advised to use the location URL
                with the 'account task-status' to check if the task was succcessfuly completed.
        '''
	kwargs={}
        my_dict = account.delete_idp_basic(parsed_results.idpid)
        response_dict = utils.get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to delete IDP for IDP {iid}".format(iid = parsed_results.idpid)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def account_id_from_name(parsed_results):
	'''
		Function to get Account ID from Account Name
		Prints a table with the Given Name and the Corresponding Account ID as the columns
		If the given account name is incorrect prints "No such Account exists"
	'''
	utils.get_account_id_from_name(parsed_results.name)

def account_info_from_name(parsed_results):
	'''
		Function which gives all the information about an Account based on its Name
		Very similar to Get Account from Account ID function
	'''
	utils.get_account_info_from_name(parsed_results.name)

def domain_id_from_idp(parsed_results):
	'''
		Function to get Domain ID from IDP ID
		Prints a table with IDP ID and corresponding Domain ID as the columns
	'''
	utils.get_domain_id_from_idp(parsed_results.idpid)


def domain_id_from_name(parsed_results):
	'''
		Function to get Domain ID from Domain Name
		Prints a table with the Given Domain Name and the corresponding Domain ID as the columns
	'''
	utils.get_domain_id_from_name(parsed_results.dname)

def domain_info_from_id(parsed_results):
	'''
		Function to get all information about a Domain when we have been provided with the Domain ID
		Prints the Information about the Domain in the form of a table similar to one printed in list_all_domains() function
	'''
	utils.get_domain_info_from_id(parsed_results.did)

def domain_info_from_idp(parsed_results):
        '''
                Function to get all information about a Domain when we have been provided with the IDP ID
                Prints the Information about the Domain in the form of a table similar to one printed in list_all_domains() function
        '''
        utils.get_domain_info_from_idp(parsed_results.idpid)

def domain_info_from_name(parsed_results):
        '''
                Function to get all information about a Domain when we have been provided with the Domain ID
                Prints the Information about the Domain in the form of a table similar to one printed in list_all_domains() function
        '''
        utils.get_domain_info_from_name(parsed_results.dname)



#################################### COMMAND LINE- CHECKING ACTION AND ASSIGNING FUNCTION ###############################################3#

def function_decider(parsed):
        '''
                Kind of like the main function which will call the other functions based on the parsed arguments
                Takes a populated namespace as input i.e. the object returned via parse_args() option
        '''
        if parsed.func == 'create':
                
		if parsed.name is not None:
                        if parsed.description is not None:
                                create_account(parsed)
                        else:

                                print "\nDescription of the account is required for account creation. Provide using -des or --description argument."
                                print "Usage: account create -n=<account-name> -des=<account-description\n>"
                else:
                        print "\nName of the account is required for account creation. Provide using -n or --name argument"
                        print "Usage: account create -n=<account-name> -des=<account-description>\n"
        
	elif parsed.func == 'list':
                list_account()
        
	elif parsed.func == 'get':
                if parsed.aid is not None:
                        get_one_account(parsed)
                else:
                        print "\nID of the account is required to get account information. Provide using -aid or --account-id argument."
                        print "Usage: account get -aid<account-id>\n"
        
	elif parsed.func == 'modify':
                if parsed.name is not None:
                        if parsed.description is not None:
                                if parsed.aid is not None:
                                        modify_account(parsed)
                                else:
                                        print "\nID of the account is required for account modification. Provide using -aid or --account-id argument."
                                        print "Usage: account modify -n=<account-name> -des=<account-description> -aid=<account-id>\n"

                        else:

                                print "\nDescription of the account is required for account modification. Provide using -des or --description argument."
                                print "Usage: account modify -n=<account-name> -des=<account-description> -aid=<account-id>\n"
                else:
                        print "\nName of the account is required for account modification. Provide using -n or --name argument"
                        print "Usage: account modify -n=<account-name> -des=<account-description> -aid=<account-id>\n"
	
	elif parsed.func == 'delete':
                if parsed.aid is not None:
                        delete_account(parsed)
                else:
                        print "\nID of the account is required to get account deletion. Provide using -aid or --account-id argument."
                        print "Usage: account delete -aid=<account-id>\n"
       
	elif parsed.func == 'get-account-id-from-domain':
		if parsed.did is not None:
                        get_account_id_from_domain_id(parsed);
                else:
                        print "\nID of the domain is required to get account id from domain id. Provide using -did or --domain-id argument."
                        print "Usage: account get-account-id-from-domain -did=<domain-id>\n"

	elif parsed.func == 'sync-roles':
		 if parsed.aid is not None:
                        sync_roles(parsed);
                 else:
                        print "\nID of the account is required to synchronize roles across domains of an account. Provide using -aid or --account-id argument."
                        print "Usage: account sync-roles -aid=<account-id>\n"

	elif parsed.func == 'list-domains':
                list_all_domains()
        
	elif parsed.func == 'list-mapping':
                if parsed.aid is not None:
                        account_domain_mapping(parsed)
                else:
                        print "\nID of the account is required to get account domain mapping. Provide using -aid or --account-id argument."
                        print "Usage: account list-mapping -aid=<account-id>\n"
	
	elif parsed.func == 'task-status':
		if parsed.url is not None:
			get_task_status(parsed)
		else:
			print "\nURL of the task status querying endpoint is required to get IDP task status. Provide using -url or --location-url argument."
                        print "Usage: account task-status -url=<loc-url>\n"
	
	elif parsed.func == 'create-domain':
		if parsed.aid is not None:
                        if parsed.dname is not None:
				if parsed.ddes is not None:
                                	create_domain(parsed)
                        	else:
                                	print "\nDescription of the domain is required for domain creation. Provide using -ddes or --domain-description argument."
                                	print "Usage: account create-domain -dname=<domain-name> -ddes=<domain-description> -aid=<account-id>\n"
                	else:
                        	print "\nName of the domain is required for domain creation. Provide using -dname or --domain-name argument"
                        	print "Usage: account create-domain -dname=<domain-name> -ddes=<domain-description> -aid=<account-id>\n"
        
		else:
                        print "\nID of the account is required for domain creation. Provide using -aid or --account-id argument"
                	print "Usage: account create-domain -dname=<domain-name> -ddes=<domain-description> -aid=<account-id>\n"
	
	elif parsed.func == 'associate-domain':
		if parsed.aid is not None:
			if parsed.did is not None:
                        	associate_domain(parsed)
                	else:
                        	print "\nID of the domain is required to get associate domain. Provide using -did or --domain-id argument."
                        	print "Usage: account associate-domain -aid=<account-id> -did=<domain-id>\n"
		else:
			print "\nID of the account is required to associate domain. Provide using -aid or --account-id argument."
			print "Usage: account associate-domain -aid=<account-id> -did=<domain-id>\n"
	
	elif parsed.func == 'delete-domain':
		if parsed.aid is not None:
                        if parsed.did is not None:
                                delete_domain(parsed)
                        else:
                                print "\nID of the domain is required to delete domain. Provide using -did or --domain-id argument."
                                print "Usage: account delete-domain -aid=<account-id> -did=<domain-id>\n"
                else:
                        print "\nID of the account is required to delete domain. Provide using -aid or --account-id argument."
                        print "Usage: account delete-domain -aid=<account-id> -did=<domain-id>\n"
	
	elif parsed.func == 'elect-new-primary-domain':
                if parsed.aid is not None:
                        if parsed.did is not None:
                                elect_new_primary_domain(parsed)
                        else:
                                print "\nID of the domain is required to elect new primary domain. Provide using -did or --domain-id argument."
                                print "Usage: account elect-new-primary-domain -aid=<account-id> -did=<domain-id>\n"
                else:
                        print "\nID of the account is required to elect new primary domain. Provide using -aid or --account-id argument."
                        print "Usage: account elect-new-primary-domain -aid=<account-id> -did=<domain-id>\n"

	elif parsed.func == 'list-all-idps':
		get_all_idps()
	
	elif parsed.func == 'list-idp-for-domain':
		if parsed.did is not None:
                	get_idp_for_domain(parsed)
                else:
                        print "\nID of the domain is required to get IDPs associated to a single domain. Provide using -did or --domain-id argument."
                        print "Usage: account idp-for-domain -did=<domain-id>\n"
	
	elif parsed.func == 'create-idp':
		if parsed.aid is not None:
			if parsed.fpath is not None:
                        	create_idp(parsed)
			else:
				print "\nPath to the file containing the request body is required. Provide using -f or --file argument."
                        	print "Usage: account create-idp -aid=<account-id> -f=<path-to-file>\n"
                else:
                        print "\nID of the account is required to create idp. Provide using -aid or --account-id argument."
                        print "Usage: account create-idp -aid=<account-id> -f=<path-to-file>\n"
	
	elif parsed.func == 'get-idp':
		if parsed.idpid is not None:
			get_one_idp(parsed)
		else:
			print "\nID of the IDP is required to fetch information about the single idp.Provide using -idpid argument.\n"
			print "Usage: account get-idp -idpid=<idp-id>\n"
	
	elif parsed.func == 'update-idp':
		if parsed.idpid is not None:
			if parsed.fpath is not None:
                        	update_idp(parsed)
			else:
				print "\nPath to the file containing the request body is required. Provide using -f or --file argument."
                                print "Usage: account update-idp -idpid=<IDP-ID> -f=<path-to-file>\n"
                else:
                        print "\nID of the IDP is required to update the particular idp. Provide using -idpid argument.\n"
			print "Usage: account update-idp -idpid=<idp-id>\n"
	
	elif  parsed.func == 'delete-idp':
		if parsed.idpid is not None:
                        delete_idp(parsed)
                else:
                        print "\nID of the IDP is required to delete the particular idp.Provide using -idpid argument.\n"
                        print "Usage: account delete-idp -idpid=<idp-id>\n"
	
	

	elif parsed.func == 'get-account-id-from-name':
		if parsed.name is not None:
			account_id_from_name(parsed)
		else:
			print "\nName of the Account is required to get the Account ID in this case.Provide using -n or --name argument.\n"
			print "Usage: account get-account-id-from-name -n=<Account Name>\n"

	elif parsed.func == 'get-account-info-from-name':
                if parsed.name is not None:
                        account_info_from_name(parsed)
                else:
                        print "\nName of the Account is required to get all Account related information in this case. Provide using -n or --name argument.\n"
                        print "Usage: account get-account-info-from-name -n=<Account Name>\n"

	elif parsed.func == 'get-domain-id-from-idp':
                if parsed.idpid is not None:
                        domain_id_from_idp(parsed)
                else:
                        print "\n ID of the IDP is required to get the Domain ID in this case. Provide using -idpid argument.\n"
                        print "Usage: account get-domain-id-from-idp -idpid=<ID of the IDP>\n"

	elif parsed.func == 'get-domain-info-from-idp':
                if parsed.idpid is not None:
                        domain_info_from_idp(parsed)
                else:
                        print "\n ID of the IDP is required to get all the Domain Informtaion in this case. Provide using -idpid argument.\n"
                        print "Usage: account get-domain-info-from-idp -idpid=<ID of the IDP>\n"
	
	elif parsed.func == 'get-domain-info-from-id':
                if parsed.did is not None:
                        domain_info_from_id(parsed)
                else:
                        print "\n ID of the Domain is required to get all the Domain Information in this case. Provide using -did or --domain-id argument.\n"
                        print "Usage: account get-domain-info-from-id -did=<ID of the Domain>\n"

	elif parsed.func == 'get-domain-id-from-name':
                if parsed.dname is not None:
                        domain_id_from_name(parsed)
                else:
                        print "\nName of the Domain is required to get the Domain ID in this case. Provide using -dname argument. \n"
                        print "Usage: account get-domain-id-from-name -dname=<Domain Name>\n"

	elif parsed.func == 'get-domain-info-from-name':
                if parsed.dname is not None:
                        domain_info_from_name(parsed)
                else:
                        print "\nName of the Domain is required to get all the Domain related information in this case. Provide using -dname argument.\n"
			print "Usage: account get-domain-info-from-name -dname=<Domain Name>\n"
	else:
                print "\nERROR: Action for account service not recognised"
                print "Please provide valid action for account service."
                print "To get a list of available actions please use 'account -h' or 'account --help' \n"

function_decider(parsed_items) #Calling the main function for the program to execute

