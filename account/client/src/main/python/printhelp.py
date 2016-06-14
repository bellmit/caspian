from prettytable import PrettyTable
import collections
import json
import utils

def print_table_for_list(res_lst):
        '''
                Function which prints the response from a list function call in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Account Name","Account Description","Account ID","Account Active Status"])
        for val in range(len(res_lst['accounts'])):
                od = collections.OrderedDict(sorted(res_lst['accounts'][val].items()))
#		if od['active'] == True:
                table.add_row([od['name'],od['description'],od['id'],od['active']])
        print table

def print_table_for_list_internal_helper(res_lst):
	'''
                Function which prints the response from a list function call in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Account Name","Account Description","Account ID","Account Active Status"])
        table.add_row([res_lst['name'],res_lst['description'],res_lst['id'],res_lst['active']])
        print table



def print_table_for_list_domains(res_lst):
        '''
                Function which prints the response from a list domain function call in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Domain Name","Domain ID","Account Name","Account ID","Is Primary","Enabled"])
        for val in range(len(res_lst['domains'])):
                od = collections.OrderedDict(sorted(res_lst['domains'][val].items()))
                if od['id']=='default':
                        pass
                else:
                        table.add_row([od["name"],od['id'],od['account_name'],od['account_id'],od['is_primary'],od['enabled']])
        print table

def print_table_for_list_domains_internal_helper(res_lst):
	'''
                Function which prints the response from a list domain function call in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Account Name","Account ID","Domain Name","Domain ID","Is Primary","Enabled"])
        if res_lst['id']=='default':
        	table.add_row(['   ','   ',res_lst['name'],res_lst['id'],'   ',res_lst['enabled']])
        else:
        	table.add_row([res_lst["account_name"],res_lst['account_id'],res_lst['name'],res_lst['id'],res_lst['is_primary'],res_lst['enabled']])
        print table



def print_table_for_mapping(res_lst):
        '''
                Function which prints the response in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Domain Name","Description","Domain ID","Is Primary","Is Enabled"])
        for val in range(len(res_lst['domains'])):
                od = collections.OrderedDict(sorted(res_lst['domains'][val].items()))
		if 'name' in od and 'description' in od:
                	table.add_row([od['name'],od['description'],od['id'],od['is_primary'],od['enabled']])
        	else:
			table.add_row(['  ','   ',od['id'],od['is_primary'],'   '])
	print table

def print_table_for_idp(res_lst):
	'''
		Function which prints the response for List all IDPs in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
	'''
	attributes = ["IDP Name","Description","IDP ID","Domain Name","Domain ID"]
	table = PrettyTable(attributes)
	for val in range(len(res_lst['identity_providers'])):
		od = collections.OrderedDict(sorted(res_lst['identity_providers'][val].items()))
		my_dict = utils.get_domain_info_from_idp_with_return(od['id'])	
		table.add_row([od['name'],od['description'],od['id'],my_dict['name'],my_dict['id']])
	print table

def print_table_for_one_idp(res_lst):
	'''
		Function which prints the response for API call for getting the information about a single IDP
		Parameters: A dictionary conatining the response from the API call
		Parameter Type: Python Dictionary
	'''
	attributes = ["IDP ID", "IDP Name","IDP Description","Domain ID","Type"]
	table = PrettyTable(attributes)
	table.add_row([res_lst['id'],res_lst['name'],res_lst['description'],res_lst['domain_id'],res_lst['type']])
	print table 
	print "\n"
	print json.dumps(res_lst['idp_info'], indent=4, sort_keys=True)

def print_table_for_others(res_lst):
        '''
                Function which prints the response from function calls other than the ones which have their own functions
		 in the form of table
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
        table = PrettyTable(["Account Name","Account Description","Account ID","Account Active Status"])
        od = collections.OrderedDict(sorted(res_lst.items()))
        table.add_row([od['name'],od['description'],od['id'],od['active']])
        print table

def print_table_for_get_account_from_domain(domain_id,res_lst):
	'''
		Function which prints the response from function calls for getting account ID from domain ID in the form of a table
                Parameters: A dictionnary conatining the response from the API call and the ID of the domain which was entered
                Parameter Type: Python Dictionnary
	'''
	attributes = ["Domain ID", "Account ID","Is Primary"]
	table = PrettyTable(attributes)
        od = collections.OrderedDict(sorted(res_lst.items()))
        table.add_row([domain_id,od['account_id'],od['is_primary']])
        print table

def print_response_as_json(res_lst):
	'''
                Function which prints the response from function calls other
                Parameters: A dictionnary conatining the response from the API call
                Parameter Type: Python Dictionnary
        '''
	print json.dumps(res_lst, indent = 4)
