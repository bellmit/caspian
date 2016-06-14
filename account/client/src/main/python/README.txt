CODE AUTHORS: Shrey Shashank, Niharika Verma (In case of any errors please notify)

The files client.py and client_cli.py are implementing the account client. Internally they are calling account.py, printhelp.py and utils.py.
The shell script env.sh contains all the environment variables and the aliasing commands and hence it is critical that before executing any commands the following command is used: 	
'source env.sh'

HOW TO USE THE COMMAND LINE INTERFACE :
1. Go to /etc/hosts file using the 'vi /etc/hosts' command and set the IP of the machine where your account service is running to map to the 'account' keyword.
Eg: Since my account service is running on 10.63.13.145 i will add , '10.63.13.145		account'
2. Traverse to the account_client directory(wherever you have downloaded or unzipped it) and execute the 'source env.sh' command.
3. With the environment variables set you are good to go for the command line usage. Execute 'account -h' or 'account --help' to get the help message. The help message gives the list of accepted commands and gives information on what results they produce.We have added extensive help messages to help if the required arguments are
not passed or wrong arguments are passed.

NOTE: IN THE COMMAND LINE WHENEVER ANY ARGUMENTS ARE BEING PASSED LIKE NAME for eg: account create -n='demo-account' -des='demo-account-des', PLEASE ENSURE 
THAT THERE ARE NO SPACES BETWEEN THE ARGUMENT NAME AND THE EQUAL SIGN AND THE ARGUMENT VALUE.

Currently the following commands are being supported:

	account create -> Will create a new account or give an error message on failure. Takes name and description as command line arguments via -n or --name and -des or --description. 
	Usage: account create -n='test-account-1' -des='test-account-7-des'
	
	account list -> Lists all the existing accounts or gives the error message. 
	Usage: account list  
	An example of the returned value is as follows:
	+--------------+---------------------+--------------------------------------+-----------------------+
| Account Name | Account Description |              Account ID              | Account Active Status |
+--------------+---------------------+--------------------------------------+-----------------------+
|  test-acc-5  |    test-acc-5-des   | 1033136d-b46c-4081-a7b4-8d3bcb1f5604 |          True         |
|  test-acc-3  |    test-acc-3-des   | 27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee |          True         |
|  test-acc-2  |    test-acc-2-des   | 4182d1ef-be50-4fa2-8ecd-6b9d3428d990 |          True         |
|  test-acc-1  |    test-acc-1-des   | d2eca704-67ab-4ea5-8a92-32380863e95e |          True         |
|  test-acc-4  |    test-acc-4-des   | fe1c5357-9c9e-4b57-9ee3-8b9b9f14f2f6 |          True         |
+--------------+---------------------+--------------------------------------+-----------------------+

	account get -> Gets information about a single account or returns the error message. Requires the account ID as command line argument 
	via the -aid or --account-id arguments.
	Usage: account get -aid='27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee'

	account modify -> Modifies the information about an existing account. Requires the modified name, modified description and the ID of the account to be modified.
	Usage: account modify -n=<name> -des=<description> -aid=<ID>

	account delete -> Deletes an existing account if all the preconditions are satisfied else returns an error message. 
	Requires the account ID of the acccount to be deleted.
	Usage: account delete -aid='27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee'

	account get-account-id-from-domain -> Gets the corresponding account ID for a given domain. Requires the domain ID whose account ID is to be found.
	Usage: account get-account-id-from-domain -did=<domain-ID>

	account sync-roles -> SYNCHRONIZES THE ROLES CREATED IN KEYSTONE TO ALL DOMAINS INSIDE AN ACCOUNT. Requires the account ID of the account 
	whose domains have to be synchronized,
	Usage: account sync-roles -aid=<ID>

	account create-domain -> Creates a domain for the account whose ID is given. Requires ID of the account to which the domain is to be mapped, 
	name of the domain to be created and its description.
	Usage: account create-domain -aid=<account-id> -dname=<domain-name> -ddes=<domain-description> 

	account associate-domain -> ASSOCIATES A DOMAIN CREATED IN KEYSTONE TO AN ACCOUNT WHOSE ID IS TO BE PROVIDED.
	Requires ID of the domain to be associated and the ID of the account to which the domain has to be associated to.
	Usage: account associate-domain -aid=<account-id> -did=<domain-id>

	account delete-domain -> Deletes a domain for a account whose ID is given. Requires the ID of the account to which the domain to be deleted 
	is associated with and the ID of the domain to be deleted.
	Usage: account delete-domain -aid=<account-id> -did=<domain-id>
	
	account list-domains -> Lists all the existing domains including the default one.
	Usage: account list-domains

	account list-mapping -> Lists all the domains mapped to the account whose ID is given. Requires ID of the account whose domain list is required.
	Usage: account list-mapping -aid='27b8d2a7-b1df-4eb1-aaa9-e8ff903cdeee'

	account elect-new-primary-domain -> ELECTS A NEW PRIMARY DOMAIN INCASE THE ORIGINAL PRIMARY DOMAIN IS DELETED FROM WITHIN KEYSTONE.
	Requires the ID of the account whose primary domain has to be selected and the ID of the Domain which has to be elected as the new primary domain.
	Usage: account elect-new-primary-domain -aid=<account-id> -did=<domain-id> 
	
	account all-idps -> Lists all existing IDPs.
	Usage: account all-idps
	
	account idp-for-domain -> Lists all the IDPs associated to a domain whose ID is given. Requires the ID of the domain for which the IDPs are to be listed.
	Usage: account idp-for-domain -did=<domain-id>

	account create-idp -> Creates an IDP for an account whose ID is given. Requires the ID of the account for which the IDP is to be created. 
	IMPORTANT: Other parameters (such as name, description etc.) for the IDP have to be modified in a json file provided with the scripts as per 
	the user's requirements. The path to the json file is required to get the command to work.
	Usage: account create-idp -aid=<account-id> -f=<file-path>

	account get-idp -> Gets all information for a particular IDP whose ID is given. Requires the ID of the IDP for which the information is required.
	Usage: account get-idp -idpid=<idp-id>

	account update-idp -> Update the details for a particular IDP whose ID is given. 
	IMPORTANT: Other parameters (such as name, description etc.) for the IDP have to be provided in a json file provided with the scripts as per 
	the user's requirements. The path to the json file is also required for the command to work.
	Usage: account update-idp -idpid=<idp-id> -f=<file-path>

	account delete-idp -> Deletes a particular IDP whose ID is given. Requires the ID of the IDP for which the information is required. 
	Usage: account delete-idp -idpid=<idp-id>

	account task-status -> Gets the status of the workflow task for IDP handling being created in asynchronous API implementations. 
	Requires URL of the task status querying endpoint.
	Usage: account task-status -url=<loc-url>

	account get-account-id-from-name -> Returns the account ID if the account name is given. Takes account name as parameter.
        Usage: account get-account-id-from-name -n=<Account-Name>

        account get-account-info-from-name -> Returns all the information related to an Account if the account name is given. Requires the Account Name.
        Usage: account get-account-info-from-name -n=<Account-Name>

        account get-domain-id-from-idp -> Returns the Domain ID if the IDP ID is provided.
        Usage: account get-domain-id-from-idp -idpid=<IDP ID>

        account get-domain-id-from-name -> Returns the Domain ID if the Name of the Domain is provided.
        Usage: account get-domain-id-from-name -dname=<Domain-Name>

        account get-domain-info-from-id -> Returns all the Domain Information if the Domain ID is provided.
        Usage: account get-domain-info-from-id -did=<Domain-ID>

        account get-domain-info-from-idp -> Returns all the Domain Information if the IDP ID is provided.
        Usage: account get-domain-info-from-idp -idpid=<IDP ID>

        account get-domain-info-from-name -> Returns all the Domain information if the Domain Name is provided.
        Usage: account get-domain-info-from-name -dname=<Domain-Name>



HOW TO USE FROM INSIDE YOUR OWN SCRIPT :
1. Go to /etc/hosts file using the 'vi /etc/hosts' command and set the IP of the machine where your account service is running to map to the 'account' keyword.
Eg: Since my account service is running on 10.63.13.145 i will add , '10.63.13.145		account'
2. Traverse to the account_client directory(wherever you have downloaded or unzipped it) and execute the 'source env.sh' command.
3. In the account_client directory there is the client.py file which is the library you would want to use in your scripts.
4. In your python scripts do 'import client' to import the client library and in turn all the functionality for the account client.
5. Once you have imported the client library, you can use its functions via the 'client.function_name' command. If using client.function_name seems tough you can do 'from client import *' to directly get all the functions from the client library and then the functions can be directly called.

IMPORTANT: The client.py file internally calls the account.py script which in turn makes the request calls. If in your scripts you want to use the exact request 
response then you will need to find the function matching your requirements in the account.py file and from there you cann access the 'req' variable which is the
Request response. Some of the useful functions regarding the request response are as follows(Assumption that 'req' is the request response):
	req.status_code -> Returns the status code returned by the request
	req.headers -> Returns the headers of the response as a Python dictionary
	req.encoding -> Returns the encoding of the content in the Response
	req.text -> Returns the response content as text in the UTF format
	req.json() -> Returns the response content as a Python Dictionary
Our code returns a dictionary from each function in the account.py script which have the request response as dictionary, the response status code and the 
response status title as the values to the 'response','status' and 'reason' keys. 
If you look at the code for the functions in the client.py file it can be seen that the first step generally is the account.py internal function being called
and the response being scored in the 'my_dict' variable. The next step sees us extracting only the request response from the my_dict dictionary and 
storing it in the 'response_dict
Our code returns a dictionary from each function in the account.py script which have the request response as dictionary, the response status code and the 
response status title as the values to the 'response','status' and 'reason' keys. 
If you look at the code for the functions in the client.py file it can be seen that the first step generally is the account.py internal function being called
and the response being scored in the 'my_dict' variable. The next step sees us extracting only the request response from the my_dict dictionary and 
storing it in the 'response_dict'. Thus if any operations are to be done on the request response we request you to use the 'my_dict' and 'response_dict' dictionaaries.



The supported functions are(Names should be self sufficient):
	
	create_account -> Takes name and description as parameters. 
	Usage: client.create_account('name','description')

	list_account -> Takes no parameters.
	Usage: client.list_account()

	get_one_account -> Takes account ID as parameter.
	Usage: client.get_one_account('AccountID')

	modify_account -> Takes name, description and account ID as parameters.
	Usage: client.modify_account('name','description','Account ID')

	delete_account -> Takes account ID as parameter.
	Usage: client.delete_account('AccountID')

	get_account_id_from_domain_id -> Takes domain ID as parameter.
	Usage: client.get_account_id_from_domain_id('DomainID')

	sync_roles -> Takes account ID as parameter.
	Usage: client.sync_roles('AccountID')

	elect_new_primary_domain -> Takes Account ID and Domain ID as parameter.
	Usage: client.elect_new_primary_domain('Account ID', 'Domain ID')

	list_all_domains -> Takes no parameters.
	Usage: client.list_all_domains()

	account_domain_mapping -> Takes the account ID as parameter.
	Usage: client.account_domain_mapping('Account ID')

	get_task_status -> Takes URL of the task status as parameter.
	Usage: client.get_task_status('URL')

	create_domain -> Takes account ID, domain name and domain description as parameters
	Usage: client.create_domain('AccountID','Domain name','Domain description')

	associate_domain -> Takes account ID and domain ID as parameters.
	Usage: client.associate_domain('AccountID','DomainID')

	delete_domain -> Takes account ID and domain ID as parameters.
	Usage: client.delete_domain('AccountID','DomainID')

	get_all_idps -> Takes no parameters.
	Usage: client.get_all_idps()

	get_idp_for_domain -> Takes domain ID as parameter.
	Usage: client.get_idp_for_domain('DomainID')
	
	create_idp -> Takes account ID and key-word arguments as parameters.
	Usage: client.create_idp('AccountID','FILE_PATH',**kwargs)

	get_one_idp -> Takes IDP ID as parameter.
	Usage: client.get_one_idp('IDP ID')

	update_idp -> Takes IDP ID and key-word arguments as parameters.
	Usage: client.update_idp('IDP ID','FILE-PATH', **kwargs)

	delete_idp -> Takes IDP ID and key-word arguments as parameters.
	Usage: client.delete_idp('IDP ID')

	SOME EXTRA HELPER FUNCTIONS INCLUDE:
	get_account_id_from_name -> Returns the account ID if the account name is given. Takes account name as parameter.
	Usage: client.get_account_id_from_name('Account-Name')
	
	get_account_info_from_name -> Returns all the information related to an Account if the account name is given. Requires the Account Name.
	Usage: client.get_account_info_from_name('Account-Name')

	get_domain_id_from_idp -> Returns the Domain ID if the IDP ID is provided. 
	Usage: client.get_domain_id_from_idp('IDP ID')

	get_domain_id_from_name -> Returns the Domain ID if the Name of the Domain is provided.
	Usage: client.get_domain_id_from_name('Domain-Name')

	get_domain_info_from_id -> Returns all the Domain Information if the Domain ID is provided.
	Usage: client.get_domain_info_from_id('Domain ID')
	
	get_domain_info_from_idp -> Returns all the Domain Information if the IDP ID is provided.
	Usage: client.get_domain_info_from_idp('IDP ID')

	get_domain_info_from_name -> Returns all the Domain information if the Domain Name is provided.
	Usage: client.get_domain_info_from_name('Domain-Name')

		
