import account
from prettytable import PrettyTable
import collections
import printhelp

def get_response(response_dict):
        '''
                Function to retrieve only the response from the dictionnary returned by the basic functions.
                The response is returned as a dictionnary.
        '''

        my_dict = response_dict['response']
        return my_dict


def delete_multiple_accounts(*args):
    	'''
        	Deleting multiple accounts based on account IDs.
        	Parameters: IDs of the account to be deleted
        	Parameter Type : String
    	'''
    	for acc_id in args:
        	req = account.delete_account_basic(acc_id)
		if req['status'] == 202:
        		print "\nAccount Deletion in progress. Check the task-status using the location URLs provided or use List Accounts to verify.\n"
    		else:
        		print "\nAccount Deletion failed for account-id : %s" %acc_id
        		print "Status code = %s , Error Title = %s" %(req['status'], req['reason'])
        		print "Error message: %s\n" %req['response']['error']['message']


def get_account_id_from_name(name):
        '''
                Function to get account id from name
                Parameters: Name of the account
                Parameter Type: String
                Returns the account-id or 'No such account' error message
        '''

        res_dict = account.list_account_basic()
	my_dict = get_response(res_dict)
	attributes = ["Given Name","Corresponding Account ID"]
	table = PrettyTable(attributes)
	try:
		if len(my_dict['accounts']) == 0:
			return "No such Account"
        	for var in range(len(my_dict['accounts'])):
                	if my_dict['accounts'][var]['name'] == name:
				table.add_row([name,my_dict['accounts'][var]['id']])
                		print table
				return
        	print "\nNo such Account exists\n"
	
	except KeyError:
		if my_dict['error']:
                        print "\nAn error has been encountered. The error message is %s\n" %(my_dict['error']['message'])
                else:
                        print "\nSomething went wrong and the domain_id couldn't be recovered. Please try again\n."

def get_account_info_from_name(name):
	'''
                Function to get account id from name
                Parameters: Name of the account
                Parameter Type: String
                Returns a dictionary with the information about an account with the given name
	        or 'No such account' error message
        '''

        res_dict = account.list_account_basic()
        my_dict = get_response(res_dict)
        try:
                if len(my_dict['accounts']) == 0:
                        return "No such Account"
                for var in range(len(my_dict['accounts'])):
                        if my_dict['accounts'][var]['name'] == name:
                        	printhelp.print_table_for_list_internal_helper(my_dict['accounts'][var]) 
				return       
                print "\nNo such account exists\n"

        except KeyError:
		print "\nSomething went wrong and the domain_id couldn't be recovered. Please try again.\n"



def get_domain_id_from_idp(idp_id):
	'''
		Function which extracts the Domain ID for a corresponding IDP ID
	'''
	res_dict = account.get_one_idp_basic(idp_id)
	my_dict = get_response(res_dict)
	attributes = ["IDP ID", "Domain ID"]
	table = PrettyTable(attributes)
	try:
		res_str = my_dict['domain_id']
		table.add_row([idp_id,res_str])
		print table
		return
	except KeyError:
		res_str = "Wrong IDP ID entered"
		print "\n" + res_str + "\n"
		return

def get_domain_id_from_name(name):
	'''
                Function to get domain id from name
                Parameters: Name of the domain
                Parameter Type: String
                Returns the domain-id or 'No such domain' error message
        '''

        res_dict = account.list_domains_enhanced()
	my_dict = get_response(res_dict)
	attributes = ["Name","Corrsponding Domain ID"]
	table = PrettyTable(attributes)
	try:
		if len(my_dict['domains']) == 0:
			return "No such Domain exists"
        	for var in range(len(my_dict['domains'])):
                	if my_dict['domains'][var]['name'] == name:
                        	table.add_row([name,my_dict['domains'][var]['id']])
				print table
                                return
                print "\nNo such account\n"

        except KeyError:
                if my_dict['error']:
                        print "\nAn error has been encountered. The error message is %s\n" %(my_dict['error']['message'])
                else:
                        print "\nSomething went wrong and the domain_id couldn't be recovered. Please try again\n."


def get_domain_info_from_id(domain_id):
	'''
		Function to get all information about a single domain from domain ID
	'''
	res_dict = account.list_domains_enhanced()
	my_dict = get_response(res_dict)
	try:
		if len(my_dict['domains']) == 0:
			return "No such Domain exists"
		for var in range(len(my_dict['domains'])):
			if my_dict['domains'][var]['id'] == domain_id:
				printhelp.print_table_for_list_domains_internal_helper(my_dict['domains'][var])
                                return
                print "\nNo such Domain exits\n"

        except KeyError:
                if my_dict['error']:
                        print "\nAn error has been encountered. The error message is %s\n" %(my_dict['error']['message'])
                else:
                        print "\nSomething went wrong and the domain_id couldn't be recovered. Please try again.\n"


def get_domain_info_from_id_with_return(domain_id):
        '''
                Function to get all information about a single domain from domain ID
        '''
        res_dict = account.list_domains_enhanced()
        my_dict = get_response(res_dict)
        try:
                if len(my_dict['domains']) == 0:
                        return "No such Domain exists"
                for var in range(len(my_dict['domains'])):
                        if my_dict['domains'][var]['id'] == domain_id:
                                return my_dict['domains'][var]
                return "No such Domain"

        except KeyError:
                if my_dict['error']:
                        return "An error has been encountered. The error message is %s" %(my_dict['error']['message'])
                else:
                        return "Something went wrong and the domain_id couldn't be recovered. Please try again."



def get_domain_info_from_idp(idp_id):
        '''
                Function which extracts the Domain ID for a corresponding IDP ID
        '''
        res_dict = account.get_one_idp_basic(idp_id)
        my_dict = get_response(res_dict)
        try:
                res_str = my_dict['domain_id']
		get_domain_info_from_id(res_str)
                return 
        except KeyError:
                res_str = "Wrong IDP ID entered"
                return 

def get_domain_info_from_idp_with_return(idp_id):
        '''
                Function which extracts the Domain ID for a corresponding IDP ID
        '''
        res_dict = account.get_one_idp_basic(idp_id)
        my_dict = get_response(res_dict)
        try:
                res_str = my_dict['domain_id']
                dom_dict = get_domain_info_from_id_with_return(res_str)
                return dom_dict
        except KeyError:
                res_str = "Wrong IDP ID entered"
                return res_str



def get_domain_info_from_name(name):
	'''
                Function to get domain id from name
                Parameters: Name of the domain
                Parameter Type: String
                Returns the dictionary with all the details of the required domain
                or 'No such domain' error message
        '''

        res_dict = account.list_domains_enhanced()
        my_dict = get_response(res_dict)
        try:
                if len(my_dict['domains']) == 0:
                        return "No such Domain exists"
                for var in range(len(my_dict['domains'])):
                        if my_dict['domains'][var]['name'] == name:
                                printhelp.print_table_for_list_domains_internal_helper(my_dict['domains'][var])
                                return
                print "\nNo such account\n"

        except KeyError:
                if my_dict['error']:
                        print "\nAn error has been encountered. The error message is %s\n" %(my_dict['error']['message'])
                else:
                        print "\nSomething went wrong and the domain_id couldn't be recovered. Please try again\n."

