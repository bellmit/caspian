import requests
import json
import sys
import os
import time

############################# FUNCTION FOR GETTING TOKEN FROM KEYSTONE #############################################

def get_token():
        '''
                Function to get authentication token from Keystone
		Returns the token present in the X-Subject-Token header of response as a string
                SSL Warning has been disabled
        '''

        headers = {
                "Content-Type" : "application/json"
        }
        data = {
                 "auth": {
                        "identity": {
                                "methods": [
                                        "password"
                                        ],
                                "password": {
                                        "user": {
                                                "name": "admin",
                                                "domain": { "id": "default" },
                                                "password": "admin123"
                                                }
                                        }
                                },
                "scope": {
                        "domain": {
                                "id": "default"
                                }
                        }
                }

        }
        requests.packages.urllib3.disable_warnings()  #Remove this line if warnings are wanted
        try:
		req = requests.post('https://keystone:35357/v3/auth/tokens', data = json.dumps(data),
                            headers = headers,verify=False)
        	return req.headers['x-subject-token']
	except KeyError:
		print "\nFailed to get the Authentication Token. Please check that your Account Service is up and Running.\n"
		sys.exit(-1)

############################### IMPORTANT AUTHENTICATION GLOBAL VARIABLES ###########################################

authentication_url = os.environ['ACCOUNT_AUTH_URL']
my_token = get_token()

#############################  ACCOUNT API ACCESS FUNCTIONS #########################################################3
def create_account_basic(name,description):
	'''
        	Function to create new accounts.
        	Parameters required: name of the account, description of the account
        	Parameter Type: String
        	Returns a dictionnary with response, status and reason keys having the response as a dictionnary, 
		the response status code and the status code message respectively.
    	'''

    	headers = {
        	"X-AUTH-TOKEN" : my_token,
                "Content-Type" : "application/json"
    	}
    	data = {
                "name" : name,
                "description" : description
    	}
    	req = requests.post(authentication_url, data = json.dumps(data),
                	headers = headers)
    	response_dict = req.json()
	response_status = req.status_code
	reason = req.reason
	return {'response':response_dict, 'status':response_status, 'reason':reason}


def list_account_basic():
	'''
        	Function to provide list of existing accounts
		Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
        	the response status code and the status code message respectively.
    	'''
    	headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
     	}
    	req = requests.get(authentication_url, headers=headers)
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        return {'response':response_dict, 'status':response_status, 'reason':reason}



def get_account_basic(account_id):
	'''
        	Get information about a particular account.
        	Parameters Required: ID of the account whose info is required
        	Parameter Type: String
        	Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
    	'''

    	headers = {
        	"X-AUTH-TOKEN" : my_token
    	}

    	req = requests.get(authentication_url+account_id,headers=headers)
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        return {'response':response_dict, 'status':response_status, 'reason':reason}

def modify_account_basic(name,description,account_id):
	'''
        	Modify information about a particular account.
        	Parameters required: name after modification, description after modification, id of the account to be modified
        	Parameter Type: String
        	Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
    	'''


    	headers = {
        	"X-AUTH-TOKEN" : my_token,
                "Content-Type" : "application/json"
    	}

    	data = {
                "name" : name,
                "description" : description
    	}
    	req = requests.put(authentication_url+account_id,data = json.dumps(data),
                        headers = headers)

	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        return {'response':response_dict, 'status':response_status, 'reason':reason}


def get_account_task_status(loc_url):
	'''
		Get the status of the workflow task being created in asynchronous API implementations
		Parameters: URL of the task status querying endpoint
		Parameter Type: String
		Returns a dictionnary with response, status and reason keys having the response as a dictionnary
		the response status code and the status code respectively
	'''
	headers = {
                "X-AUTH-TOKEN" : my_token
        }
	req = requests.get(loc_url,headers=headers)
	response_dict = req.json()
	response_status = req.status_code
	reason = req.reason
        if response_status == 200:
                return {'response': response_dict, 'status':response_status, 'reason':reason}
        else:
                return {'response': 'The account does not have any task', 'status':response_status, 'reason':reason}



def delete_account_basic(account_id):
    	'''
        	Delete account based on account ID.
        	Parameters: ID of the account to be deleted
        	Parameter Type : String
        	Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
    	'''

    	headers = {
        	"X-AUTH-TOKEN" : my_token
    	}

    	req = requests.delete(authentication_url+account_id,headers=headers)
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
	if response_status == 202:
		header_dict = req.headers
		loc_url = header_dict['location']
		my_dict = get_account_task_status(loc_url)
		print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
		return {'response':response_dict, 'status':response_status,'reason':reason}
	else:
        	return {'response':response_dict, 'status':response_status, 'reason':reason}


def get_account_id_from_domain_basic(domain_id):
	'''
		Retrieves the account id corresponding to a domain id.
		Parameters: Requires the ID of the Domain whose corresponding Account is required
		Parameter Type: String
		Returns a dictionary containing the request response, the response status code and the status code reason as the keys
	'''
	headers = {
                "X-AUTH-TOKEN" : my_token,
                "Content-Type" : "application/json"
        }
	payload = {'domain_id': domain_id}

        auth_url = authentication_url+'current'
        req = requests.get(auth_url,headers=headers,params=payload)

        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        
	if response_status == 200:
                return {'response': response_dict, 'status':response_status, 'reason':reason}
        elif response_status == 400:
                return {'response': {"error":{"message":"Invalid JSON Request Root (Required Data not provided)"}}, 'status':response_status, 'reason':reason}
	else:
		return {'response': response_dict, 'status':response_status, 'reason':reason}

def sync_roles_basic(account_id):
	'''
		Synchronizes domain scoped role assignments between the primary domain and the other domains of the account.
		Parameters: Requires the ID of the account within which the role syncing has to be done
		Parameter Type: String
		Prints the Current Status of the task being created along with the location of the task
		Returns a dictionary containing the response status and the response reason as well as the response which in this case will be 0

	'''
	headers = {
                "X-AUTH-TOKEN" : my_token
        }
	
	auth_url = authentication_url +	account_id +'/sync-roles'
	req = requests.put(auth_url,headers=headers)
	
        response_status = req.status_code
        reason = req.reason
	header_dict = req.headers
	
	if response_status == 202:
                loc_url = header_dict['location']
                my_dict = get_account_task_status(loc_url)
                print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
                print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':{}, 'status':response_status,'reason':reason}
        else:
		response_dict = req.json()
                return {'response':response_dict, 'status':response_status,'reason':reason}



def create_domain_basic(account_id,domain_name,domain_description):
	'''
		Function to create a domain for an account
		Parameters: Requires of the ID of the account inside which the domain has to be created and the name and description of the 
		domain to be created.
		Returns a dictionary containing the response from the request, the response status code and the status title as values
                for the 'response', 'status' and 'reason' keys respectively
	'''
	headers = {
                "X-AUTH-TOKEN" : my_token,
		"Content-Type" : "application/json"
        }
	
	data = {
                "name" : domain_name,
                "description" : domain_description
        }
        
        auth_url = authentication_url+account_id+'/domains/'
        req = requests.post(auth_url,data = json.dumps(data),headers=headers)
        
	response_dict = req.json()
	response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
        
	if response_status == 202:
		loc_url = header_dict['location']
                my_dict = get_account_task_status(loc_url)
                print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':response_dict, 'status':response_status,'reason':reason}
        else:
                return {'response':response_dict, 'status':response_status,'reason':reason}


def list_domains_enhanced():
        '''
                Function to list domains with enhanced information
                Takes no parameters
		Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
        '''

        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }
        req = requests.get(authentication_url+'domains', headers=headers)
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        return {'response':response_dict, 'status':response_status, 'reason':reason}


def list_account_domain_mapping(account_id):
        '''
                Function to list the domains that are associated with provided account
                Parameters: ID of the account whose domain mapping is wanted
                Parameter Type: String
		Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
        '''
        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }
        req = requests.get(authentication_url+account_id+'/domains', headers=headers)
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        return {'response':response_dict, 'status':response_status, 'reason':reason}

def associate_domain_to_account_basic(domain_id,account_id):
	'''
		Function to Associate an existing domain to an account.
		Parameters: ID of the orphan domain which needs to be associated
			    ID of the account to which the orphan domain has to be associated
		Parameter Type: String
		Prints the existing status of the task flow being created and returns a dictionnary with response, status 
		and reason keys having the response as a dictionnary, the response status code and the status code message respectively.
	'''
	headers = {
                "X-AUTH-TOKEN" : my_token
        }
	
	auth_url = authentication_url + account_id + '/domains/' + domain_id
        req = requests.post(auth_url,headers=headers)
        
	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
        
	if response_status == 204:
		loc_url = header_dict['location']
		my_dict = get_account_task_status(loc_url)
        	print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':response_dict, 'status':response_status,'reason':reason}
        else:
                return {'response':response_dict, 'status':response_status, 'reason':reason}


def delete_domain_basic(account_id, domain_id):
        '''
                Delete domain based on account ID and domain ID
                Parameters: ID of the account to which the domain belongs and ID of the domain to be deleted
                Parameter Type : String
                Returns a dictionnary with response, status and reason keys having the response as a dictionnary,
                the response status code and the status code message respectively.
        '''

        headers = {
                "X-AUTH-TOKEN" : my_token
        }
	auth_url = authentication_url+account_id+'/domains/'+domain_id
        req = requests.delete(auth_url,headers=headers)
        
	response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
	sp_err_dict = {"error":{"message":"Primary Domain cannot be deleted"}}
        
	if response_status == 202:
		loc_url = header_dict['location']
		my_dict = get_account_task_status(loc_url)
		print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':{}, 'status':response_status,'reason':reason}
        elif response_status == 400:
                return {'response':sp_err_dict, 'status':response_status, 'reason':reason}
	else:
		response_dict = req.json()
		return {'response':response_dict, 'status':response_status,'reason':reason}


def elect_new_primary_domain_basic(account_id,domain_id):
	'''
		Sets a new primary domain for the account.
		Parameters: ID of the account whose primary domain has to be changed and ID of the domain which has to be
		elected as the new primary domain.
		Parameter Type: String
		This is also an asynchronous operation so this function prints the URL of the Location returned and the current
		status of the task created. 
		Returns a dictionary having the response returned by the request, the status code of the request and the reason as the 
		values of the dictionary. 
	'''
	headers = {
                "X-AUTH-TOKEN" : my_token
        }
        auth_url = authentication_url+account_id+'/primary-domain/'+domain_id
        req = requests.put(auth_url,headers=headers)

        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers

        if response_status == 202:
                loc_url = header_dict['location']
                my_dict = get_account_task_status(loc_url)
                print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
                print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':{}, 'status':response_status,'reason':reason}
        else:
                response_dict = req.json()
                return {'response':response_dict, 'status':response_status,'reason':reason}

def get_idp_task_status(loc_url):
	'''
                Get the status of the workflow task for IDP handling being created in asynchronous API implementations
                Parameters: URL of the task status querying endpoint
                Parameter Type: String
                Returns a dictionnary with response, status and reason keys having the response as a dictionnary
                the response status code and the status code respectively
        '''
        headers = {
                "X-AUTH-TOKEN" : my_token
        }
        req = requests.get(loc_url,headers=headers)
        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
 
        if response_status == 200:
                return {'response': response_dict, 'status':response_status, 'reason':reason}
        else:
                return {'response': 'The account does not have any task', 'status':response_status, 'reason':reason}


def create_idp_basic(account_id,fpath,kwargs):
	'''
		Function that Creates an IDP for a domain.
		Parameters: Function takes the Account ID followed by an arbitrary number of arguments
		NOTE : The IDP API requires the name, desciption of the IDP to be created and it also requires the ID of the 
		Domain with which it will be associated. All these important variables are mentioned in the 'idp_info.json'
		file which this function will use. So the users can either provide all the details as function arguments or 
		can make the changes in the idp_info file. If function arguments are provided then they should be provided as follows:
			create_idp_basic(name = "demo-idp-1", description = "demo-idp-des-1")
		All the fields in the IDP body can be provided here as keyword arguments. If nothing is provided here then the changes will be
		expected in the file.
		IT IS STRONGLY ADVISED TO EITHER MAKE CHANGES IN THE FILE OR PROVIDE THE KEYWORD ARGUMENTS FOR THE 
		CORRESPONDING FIELDS
		Prints the task flow status of the task being created for creation if the request to create the IDP is accepted
	'''
	try:
		json_file = open(fpath)
		json_str = json_file.read()
		json_data = json.loads(json_str)
	
	except IOError:
		print "\nPlease enter a valid file path\n"
		sys.exit(-1) 
	
	if len(kwargs) > 0:
		for a in kwargs.keys():
			if a in json_data.keys():
				json_data[a] = kwargs[a]
			elif a in json_data['idp_info'].keys():
				json_data['idp_info'][a] = kwargs[a]
			else:
				print "\nThe keyword arguments do not match any of the required keywords\n"
	
	auth_url = authentication_url+account_id+'/identity-providers'
	

	headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }
	
	req = requests.post(auth_url, data = json.dumps(json_data),headers = headers)
        
	response_status = req.status_code
        reason = req.reason
        header_dict = req.headers

        if response_status == 202:
		loc_url = header_dict['location']
               	response_dict = req.json()
		my_dict = get_idp_task_status(loc_url)
		print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':response_dict, 'status':response_status,'reason':reason}
#        elif response_status == 400:
#		sp_error_dict = {"error":{"message":req.text}}
#		return {'response':sp_error_dict,'status':response_status,'reason':reason}
	else:
		response_dict = req.json()
                return {'response':response_dict, 'status':response_status,'reason':reason}		

def list_idp_domain_filter(domain_id):
	'''
		Function to List all IDPs in a domain
		Parameters: Requires the ID of the Domain whose IDP listing is wished to be viewed
		Parameter Type : String
		Returns a dictionary containing the response from the request, the response status code and the status title as values
                for the 'response', 'status' and 'reason' keys respectively
	'''
	payload = {'domain_id': domain_id}
	auth_url = authentication_url+'identity-providers'
        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }

        req = requests.get(auth_url,headers = headers,params = payload)

        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
	return {'response':response_dict, 'status':response_status, 'reason':reason}

def list_idp_basic():
	'''
		Function to list all IDPs regardless of their associated domains
		Returns a dictionary containing the response from the request, the response status code and the status title as values
                for the 'response', 'status' and 'reason' keys respectively
	'''
	auth_url = authentication_url+'identity-providers'
        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }

        req = requests.get(auth_url,headers = headers)

        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
        return {'response':response_dict, 'status':response_status, 'reason':reason}

def get_one_idp_basic(idp_id):
	'''
		Function to get information about a particular IDP
		Parameters: Requires the ID of the IDP whose information is required
		Parameter Type: String
		Returns a dictionary containing the response from the request, the response status code and the status title as values
		for the 'response', 'status' and 'reason' keys respectively
	'''
	auth_url = authentication_url+'identity-providers/'+idp_id
        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }

        req = requests.get(auth_url,headers = headers)

        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
	if response_status == 200:
        	return {'response':response_dict, 'status':response_status, 'reason':reason}
        else:
		return {'response':response_dict, 'status':response_status, 'reason':reason}


def update_idp_basic(idp_id,fpath,kwargs):
	'''
		Function that Updates an existing IDP.
                Parameters: Function takes the IDP ID and the filepath, followed by an arbitrary number of arguments
                NOTE : The IDP API requires the name, desciption of the IDP to be created and it also requires the ID of the
                Domain with which it will be associated. All these important variables are mentioned in the 'idp_info.json'
                file which this function will use. So the users can either provide all the details as function arguments or
                can make the changes in the idp_info file. If function arguments are provided then they should be provided as follows:
                       update_idp_basic(idpid,fpath,name = "demo-idp-1", description = "demo-idp-des-1")
                All the fields in the IDP body can be provided here as keyword arguments. If nothing is provided here then the changes will be
                expected in the file.
                IT IS STRONGLY ADVISED TO EITHER MAKE CHANGES IN THE FILE OR PROVIDE THE KEYWORD ARGUMENTS FOR THE
                CORRESPONDING FIELDS
                Prints the task flow status of the task being created for creation if the request to create the IDP is accepted
		Returns a dictionary containing the response from the request, the response status code and the status title as values
                for the 'response', 'status' and 'reason' keys respectively
	'''
	json_file = open(fpath)
        json_str = json_file.read()
        json_data = json.loads(json_str)

        if len(kwargs) > 0:
                for a in kwargs.keys():
                        if a in json_data.keys():
                                json_data[a] = kwargs[a]
                        elif a in json_data['idp_info'].keys():
                                json_data['idp_info'][a] = kwargs[a]
                        else:
                                print "\nThe keyword arguments do not match any of the required keywords\n"

        auth_url = authentication_url+'identity-providers/'+idp_id


        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }

        req = requests.put(auth_url, data = json.dumps(json_data),
                        headers = headers)

	response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers
	
	if response_status == 202:
		loc_url = header_dict['location']
                my_dict = get_idp_task_status(loc_url)
                print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':response_dict, 'status':response_status,'reason':reason}
        else:
                return {'response':response_dict, 'status':response_status,'reason':reason}


def delete_idp_basic(idp_id):
	'''
		Function to Delete an IDP
		Parameters: IDP of the ID which has to be deleted
		Parameter Type: String
		Returns a dictionary containing the response from the request, the response status code and the status title as values
                for the 'response', 'status' and 'reason' keys respectively
	'''
        auth_url = authentication_url+'identity-providers/'+idp_id


        headers = {
                    "X-AUTH-TOKEN" : my_token,
                    "Content-Type" : "application/json"
        }

        req = requests.delete(auth_url,headers = headers)

        response_dict = req.json()
        response_status = req.status_code
        reason = req.reason
        header_dict = req.headers

        if response_status == 202:
		loc_url = header_dict['location']
                my_dict = get_idp_task_status(loc_url)
                print "\nThe current task flow status is " + my_dict['response']['status'] + "\n"
		print "\nTo check the task flow status use the following url: " + loc_url + "\n"
                return {'response':response_dict, 'status':response_status,'reason':reason}
        else:
                return {'response':response_dict, 'status':response_status,'reason':reason}


