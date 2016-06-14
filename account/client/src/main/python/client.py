import account
from printhelp import *
from utils import *

def create_account(name,description):
        '''
                Function to create new accounts. Internally calls the create_account_basic() function which makes the
                HTTP requests on behalf of user. The create_account_basic() function returns a dictionnary of the response,
                status code and the reason. A sample dictionnary is as follows:

                {'status': 201, 'reason': 'Created', 'response': {u'active': True, u'description': u'test-acc-4-des', u'id': u'fe1c5357-9c9e-4b57-9ee3-8b9b9f14f2f6', u'                name': u'test-acc-4'}}

                Here the value corresponding to the response key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
        my_dict = account.create_account_basic(name,description)
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
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

                {'status': 200, 'reason': 'OK', 'response': {u'accounts': [{u'active': True, u'description': u'test-acc-3-des', u'id': u'27b8d2a7-b1df-4eb1-aaa9-e8ff903                cdeee', u'name': u'test-acc-3'}, {u'active': True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', u'name': u'test-ac                c-2'}, {u'active': True, u'description': u'test-acc-1-des', u'id': u'd2eca704-67ab-4ea5-8a92-32380863e95e', u'name': u'test-acc-1'}]}}

                Here the value corresponding to the 'response' key is the response that the function call returned. The response key is itself a dictionnary and can be
                accessed by the utils library get_response() function or by accessing the 'response' key. Taking out only the response we will get something as follows:

                {u'accounts': [{u'active': True, u'description': u'test-acc-3-des', u'id': u'27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee', u'name': u'test-acc-3'}, {u'active':                True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990', u'name': u'test-acc-2'}, {u'active': True, u'description': u'te                st-acc-1-des',  u'id': u'd2eca704-67ab-4ea5-8a92-32380863e95e', u'name': u'test-acc-1'}]}

                We can see that the response is a dictionnary with one key 'accounts' which has a value as a list of dictionnaries.

        '''
        my_dict = account.list_account_basic()
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_list(response_dict)
        else:
                print "\nListing of accounts failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_account_id_from_domain_id(domain_id):
	'''
		Function that Retrieves the account id corresponding to a domain id. Internally calls the get_account_id_from_domain_basic()
		The get_account_id_from_domain_basic() function returns a dictionary of the response, status code and the reason.
		A sample dictionary of the correct response case is as follows:

		{'status':200, 'reason':'OK', 'response':{"account_id":"0a5acaec-4928-4790-b8fe-53186f8e49f4","is_primary":false}}

		Here the value corresponding to the 'response' key is the response that the function call returned. If the actions are to be 
		performed on the response dictionary the function can be modified to use the my_dict dictionary as used below.
	'''
	my_dict = account.get_account_id_from_domain_basic(domain_id)
	print my_dict
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_get_account_from_domain(domain_id,response_dict)
        else:
                print "\nFailed to retrieve Account ID from the given Domain ID"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def sync_roles(account_id):
	'''
		Function which Synchronizes domain scoped role assignments between the primary domain and the other domains of the account.
		Internally it calls the sync_roles_basic() function which returns a dictionary with the response, status code and the reason as the keys.
		This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked 
		via the location which will be printed by the function.		
	'''
	my_dict = account.sync_roles_basic(account_id)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nSyncing of roles for account {aid} failed".format(aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_one_account(account_id):
        '''
                Function to get information about a single account. Internally calls the get_account_basic() function which makes the
                HTTP requests on behalf of user. The get_account_basic() function returns a dictionary of the response,status code and the reason.
                A sample dictionnary is as follows:

                {'status': 200, 'reason': 'OK', 'response': {u'active': True, u'description': u'test-acc-2-des', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990',
                u'name': u'test-acc-2'}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
        my_dict = account.get_account_basic(account_id)
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_others(response_dict)
        else:
                print "\nFailed to retrieve information"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s" %response_dict['error']['message']


def modify_account(name,description,account_id):
        '''
                Function to modify the information about a single account. Internally calls the modify_account_basic() function which makes the
                HTTP requests on behalf of user. The modify_account_basic() function returns a dictionnary of the response,status code and the reason.
                A sample dictionnary is as follows(Here the test-acc-2 has been modified and named as test-acc-2-mod):

                {'status': 200, 'reason': 'OK', 'response': {u'active': True, u'description': u'test-acc-2-des-mod', u'id': u'4182d1ef-be50-4fa2-8ecd-6b9d3428d990',
                u'name': u'test-acc-2-mod'}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
        my_dict = account.modify_account_basic(name,description,account_id)
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200:
                print_table_for_others(response_dict)
        else:
                print "\nAccount modification failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']



def delete_account(account_id):
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
        my_dict = account.delete_account_basic(account_id)
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status'] != 202:
                print "\nAccount deletion failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']




def get_task_status(loc_url):
        '''
                Get the status of the workflow task for IDP handling being created in asynchronous API implementations
                Parameters: URL of the task status querying endpoint
                Parameter Type: String
                Returns a dictionnary with response, status and reason keys having the response as a dictionnary
                the response status code and the status code respectively
        '''
        my_dict = account.get_account_task_status(loc_url)

        if my_dict['status'] == 200:
                print "\nThe status of the task is " + my_dict['response']['status'] + "\n"
        else:
                print "\nThe mentioned task does not exist.\n"


def elect_new_primary_domain(account_id,domain_id):
	'''
		Function to elect a new primary domain for an account
		Internally it calls the elect_new_primary_domain_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
	'''
	my_dict = account.elect_new_primary_domain_basic(account_id,domain_id)
	response_dict = get_response(my_dict) 
	if my_dict['status'] != 200:
		print "\nElecting of new primary domain for account {aid} failed".format(aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def create_domain(account_id,domain_name,domain_description):
	'''
		Function to create a domain for an account
                Internally it calls the create_domain_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
	'''
	my_dict = account.create_domain_basic(account_id,domain_name,domain_description)
	response_dict = get_response(my_dict)
	if my_dict['status'] != 202:
                print "\nDomain creation for account {aid} failed".format(aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def associate_domain(account_id,domain_id):
	'''
		Function to associate a domain to an account
                Internally it calls the associate_domain_to_account() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
	'''
	my_dict = account.associate_domain_to_account_basic(account_id,domain_id)
        response_dict = get_response(my_dict)
	if my_dict['status'] != 202:
                print "\nFailed to associate domain {domain} to account {aid}".format(domain = domain_id,aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


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
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
	print_response_as_json(response_dict)
        if my_dict['status']==200:
                print_table_for_list_domains(response_dict)
        else:
                print "\nListing of domains failed"
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']



def account_domain_mapping(account_id):
        '''
                Function to get account to domain mapping for an account. Internally calls the list_account_domain_mapping() function which makes the
                HTTP requests on behalf of user. The list_account_domain_mapping() function returns a dictionnary of the response,status code and the reason.
                A sample dictionnary is as follows:

                {'status': 200, 'reason': 'OK', 'response': {u'domains': [{u'is_primary': True, u'description': u'Primary domain for account test-acc-2',
                u'enabled': True, u'id': u'b174fa2e4fec48a8b2f4f0be78fe99b8', u'name': u'test-acc-2'}]}}

                Here the value corresponding to the 'response' key is the response that the function call returned. If only response is desired use the get_response()
                function from the utils library.
        '''
        my_dict = account.list_account_domain_mapping(account_id)
        response_dict = get_response(my_dict) #This is the response depicted as a dictionnary
        if my_dict['status']==200 and len(response_dict['domains'])>0:
                print_table_for_mapping(response_dict)
        else:
                print "\nListing of domains for the given account failed."
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def delete_domain(account_id,domain_id):
	'''
		Function to delete a domain for a given account. Gives error if primary domain is tried to be deleted.
                Internally it calls the delete_domain_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.

	'''
	my_dict = account.delete_domain_basic(account_id,domain_id)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to delete domain {domain} for account {aid}".format(domain = domain_id,aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_all_idps():
	'''
		Function which Lists all IDPs in the system.
		Internally calls the list_idp_basic() function which returns a dictionary with the response, status code and the reason as the keys.
		The function then checks the status code and on the basis of it, either prints a table containing the details of the IDP or 
		prints an error message.
	'''
	my_dict = account.list_idp_basic()
	response_dict = get_response(my_dict)
	if my_dict['status'] == 200 and len(response_dict['identity_providers'])>0:
                print_table_for_idp(response_dict)
        else:
                print "\nListing of IDPs failed."
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']


def get_idp_for_domain(domain_id):
	'''
		Function to get all IDPs associated to a domain
		Internally calls the list_idp_domain_filter() function which returns a dictionary with the response, status code and the reason as the keys.
                The function then checks the status code and on the basis of it, either prints a table containing the details of the IDP or
                prints an error message.
	'''
	my_dict = account.list_idp_domain_filter(domain_id)
        response_dict = get_response(my_dict)
        if my_dict['status'] == 200 and len(response_dict['identity_providers'])>0:
                print_table_for_idp(response_dict)
        else:
                print "\nListing of IDPs for the domain {did} failed.".format(did = domain_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def create_idp(account_id, fpath, **kwargs):
	'''
		Function to create an IDP for a given account
		Internally it calls the create_idp_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
	'''
	my_dict = account.create_idp_basic(account_id,fpath,kwargs)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to create IDP for account {aid}".format(aid = account_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']



def get_one_idp(idp_id):
	'''
		Function to get information about a single IDP
		Internally calls the get_one_idp_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                The function then checks the status code and on the basis of it, either prints a table containing the details of the IDP or
                prints an error message.
	'''
	my_dict = account.get_one_idp_basic(idp_id)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 200:
                print "\nFailed to get information about IDP {idpid}".format(idpid = idp_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']
	else:
		print_table_for_one_idp(response_dict)



def update_idp(idp_id, fpath,**kwargs):
	'''
		Function to update the details for a particular IDP
		Internally it calls the update_idp_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
		
	'''
	my_dict = account.update_idp_basic(idp_id,fpath,kwargs)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to update IDP for IDP {iid}".format(iid = idp_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

def delete_idp(idp_id,**kwargs):
	'''
		Function to delete a particular IDP
		Internally it calls the delete_idp_basic() function which returns a dictionary with the response, status code and the reason as the keys.
                This is an asynchronous API call meaning that the function call will only start the task and the result will have to checked
                via the location which will be printed by the function.
	'''
	my_dict = account.delete_idp_basic(idp_id)
        response_dict = get_response(my_dict)
        if my_dict['status'] != 202:
                print "\nFailed to delete IDP for IDP {iid}".format(iid = idp_id)
                print "Status code = %s , Error Title = %s" %(my_dict['status'], my_dict['reason'])
                print "Error message: %s\n" %response_dict['error']['message']

