package com.emc.caspian.ccs.account.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.ldap.LdapName;

import org.apache.commons.lang3.StringUtils;

import com.emc.caspian.ccs.account.controller.AccountProtocol.CreateAccountRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.CreateIdpRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.DeleteIdpRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetAccountFromDomainResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetIdpRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetTaskStatusRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.Status;
import com.emc.caspian.ccs.account.controller.AccountProtocol.UpdateAccountRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.UpdateIdpRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.ValidateIdpRequest;
import com.emc.caspian.ccs.account.controller.TaskSubmitter.Tasks;
import com.emc.caspian.ccs.account.datacontract.Account;
import com.emc.caspian.ccs.account.datacontract.AccountDomain;
import com.emc.caspian.ccs.account.datacontract.AccountDomainDetails;
import com.emc.caspian.ccs.account.datacontract.AccountRequest;
import com.emc.caspian.ccs.account.datacontract.DomainDetail;
import com.emc.caspian.ccs.account.datacontract.Idp;
import com.emc.caspian.ccs.account.datacontract.IdpConfig;
import com.emc.caspian.ccs.account.datacontract.IdpRequest;
import com.emc.caspian.ccs.account.datacontract.Link;
import com.emc.caspian.ccs.account.datacontract.PrimaryDomain;
import com.emc.caspian.ccs.account.datacontract.ValidateIdpDetails;
import com.emc.caspian.ccs.account.datacontract.WorkflowTask;
import com.emc.caspian.ccs.account.datacontract.WorkflowTask.Resource;
import com.emc.caspian.ccs.account.model.AccountModel;
import com.emc.caspian.ccs.account.model.IdpPasswordModel;
import com.emc.caspian.ccs.account.model.DbResponse;
import com.emc.caspian.ccs.account.model.ErrorCode;
import com.emc.caspian.ccs.account.model.ErrorMessages;
import com.emc.caspian.ccs.account.model.JobModel;
import com.emc.caspian.ccs.account.model.TableFactory;
import com.emc.caspian.ccs.account.model.mysql.MySQLProperties;
import com.emc.caspian.ccs.account.server.AccountServiceVersions;
import com.emc.caspian.ccs.account.server.KeystoneProperties;
import com.emc.caspian.ccs.account.types.IdpType;
import com.emc.caspian.ccs.account.types.WorkflowTaskStatus;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.emc.caspian.ccs.client.response.ClientResponse;
import com.emc.caspian.ccs.client.response.ClientStatus;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.common.utils.Validator;
import com.emc.caspian.ccs.keystone.model.Domain;
import com.emc.caspian.ccs.keystone.model.DomainConfig;
import com.emc.caspian.ccs.keystone.model.DomainConfigInfo;
import com.emc.caspian.ccs.keystone.model.Domains;
import com.emc.caspian.ccs.keystone.model.Groups;
import com.emc.caspian.ccs.keystone.model.IdentityDriver;
import com.emc.caspian.ccs.keystone.model.LdapConfig;
import com.emc.caspian.ccs.keystone.model.Project;
import com.emc.caspian.ccs.keystone.model.QueryScope;
import com.emc.caspian.ccs.keystone.model.Users;
import com.emc.caspian.encryption.AESUtil;
import com.unboundid.ldap.sdk.Filter;
import com.unboundid.ldap.sdk.LDAPException;

public final class AccountService {

  private static final String TASK_PATH = "/tasks/";
  private static final String LOCATION_HEADER = "Location";
  private static final String KEYSTONE_DOMAIN_URL = "%s/v3/domains/%s";
  private static final String LINK_REL = "self";
  private static final String LDAP_SCHEME = "ldap";
  private static final String LDAPS_SCHEME = "ldaps";
  private static final String IDP_LDAP_DRIVER = "keystone.identity.backends.ldap.Identity";
  private static final String HOST_ENTRY_SEPARATOR = ",";
  private static final String ACCOUNT_ACTIVE_STATUS = "ACTIVE";

  private static final String CHARACTER_ENCODING = "UTF-8";
  private static final int ACCOUNT_NAME_MAX_SIZE_FOR_PROJECT = 56;
  private static final String PROJECT_NAME_PREFIX = "project_";
  private static final String ACCOUNT_V1_PATH = AccountServiceVersions.API_VERSION_V1_PATH + "/accounts/%s";
  private static final String ACCOUNT_V2_PATH = AccountServiceVersions.API_VERSION_V2_PATH + "/accounts/%s";
  private static final String IDENTITY_PROVIDERS_PATH = AccountServiceVersions.API_VERSION_V2_PATH
      + "/accounts/%s/identity-provider";
  private static final String IDENTITY_PROVIDERS_PATH_V1 = AccountServiceVersions.API_VERSION_V1_PATH + "/accounts/domains/%s/identity-provider";
  private static final String ACCOUNT_STATE_ACTIVE = "ACTIVE";
  private static final String ACCOUNT_STATE_DELETING = "DELETING";
  private static final String VALIDATE_BEFORE_SAVE = "before_save";
  private static final String VALIDATE_ONLY = "test";
  private static final String IDP_CONNECTION_ERROR = "100";
  private static final String IDP_AUTHENTICATION_ERROR = "200";
  private static final String IDP_SYNTAX_ERROR = "300";
  private static final String IDP_INTERNAL_ERROR = "500";
  private static final String IDP_SUCCESS = "0";

  /**
   * Method to list accounts
   * 
   * @return AccountProtocol.GetAccountsResponse instance
   */
  public static final AccountProtocol.GetAccountsResponse listAccounts(String version, boolean details, String domainId) {

    AccountProtocol.GetAccountsResponse response = new AccountProtocol.GetAccountsResponse();
    if (domainId != "default" && version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      response.setStatus(AccountProtocol.Status.ERROR_UNAUTHORIZED);
      response.setRespMsg(ResponseErrorMessage.AUTH_INSUFFICIENT_PRIVILEGE);
      return response;
    }

    if (!domainId.equalsIgnoreCase("default")) {
      return listAccountsForAccountAdmin(version, details, domainId);
    }
    DbResponse<List<AccountModel>> resp = null;
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      resp = TableFactory.getAccountTableForV1().getAccounts();
    } else {
      resp = TableFactory.getAccountTable().getAccounts();
    }
    List<AccountModel> accountModels = resp.getResponseObj();
    if (accountModels == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Internal Error while fetching accounts from database. Failed with error " + resp.getErrorCode());
      return response;
    }

    ClientResponse<Domains> domains = null;
    if (details || version.equals(AccountServiceVersions.API_VERSION_V1)) {
      domains = KeystoneHelper.getInstance().getAllDomains();
    }

    List<Account> accounts = null;

    if (details || version.equals(AccountServiceVersions.API_VERSION_V1)) {
      if (domains.getStatus() == ClientStatus.SUCCESS) {
        accounts = new ArrayList<Account>();
        
        Map<String, Domain> domainDetails = new HashMap<String, Domain>();
        for (Domain domain : domains.getHttpResponse().getResponseBody()) {
          domainDetails.put(domain.getId(), domain);
        }

        for (AccountModel accountModel : accountModels) {
          Account account = null;
          Domain domain = domainDetails.get(accountModel.getId());

          Link link = new Link();
          link.setRel(LINK_REL);

          // If domain is deleted from keystone
          if (domain != null) {
            if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
              link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
              if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
                account = new Account(accountModel.getId(), domain.getName(), domain.getDescription(), true);
              } else {
                account = new Account(accountModel.getId(), domain.getName(), domain.getDescription(), false);
              }
            } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
              account =
                  new Account(accountModel.getId(), domain.getName(), domain.getDescription(), accountModel.getState(),
                      domain.getEnabled());
              link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
            }
          } else {
            if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
              link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
              if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
                account = new Account(accountModel.getId(), "", "", true);
              } else {
                account = new Account(accountModel.getId(), "", "", false);
              }
            } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
              account = new Account(accountModel.getId(), "", "", accountModel.getState(), null);
              link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
            }
          }

          account.setLink(link);

          accounts.add(account);
        }
        response.setAccounts(accounts);
        response.setStatus(AccountProtocol.Status.SUCCESS_OK);
        AppLogger.info("Successfully fetched list of Accounts");
      } else {
        AppLogger.info("Internal error while fetching list of domains from keystone. Status: " + domains.getStatus());
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      }
    } else {
      accounts = new ArrayList<Account>();
      for (AccountModel accountModel : accountModels) {
        Account account = new Account(accountModel.getId(), "", "", accountModel.getState(), null);
        Link link = new Link();
        link.setRel(LINK_REL);
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
        account.setLink(link);

        accounts.add(account);
      }
      response.setAccounts(accounts);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.info("Successfully fetched list of Accounts");
    }
    return response;
  }
  

  /**
   * Method to list accounts
   * 
   * @return AccountProtocol.GetAccountsResponse instance
   */
  public static final AccountProtocol.GetAccountsResponse listAccountsForAccountAdmin(String version, boolean details, String domainId) {

    AccountProtocol.GetAccountsResponse response = new AccountProtocol.GetAccountsResponse();
    ClientResponse<Domain> domain = null;
    if (details || version.equals(AccountServiceVersions.API_VERSION_V1)) {
      domain = KeystoneHelper.getInstance().getDomain(domainId);
    }
    DbResponse<AccountModel> resp = TableFactory.getAccountTable().getAccount(domainId);
    if (resp.getResponseObj() == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Internal Error while fetching account "+ domainId +" from database. Failed with error " + resp.getErrorCode());
      return response;
    }
    List<AccountModel> accountModels = new ArrayList<AccountModel>();
    accountModels.add(resp.getResponseObj());

    List<Account> accounts = null;

    if (details || version.equals(AccountServiceVersions.API_VERSION_V1)) {
      if (domain.getStatus() == ClientStatus.SUCCESS) {
        accounts = new ArrayList<Account>();

        Map<String, Domain> domainDetails = new HashMap<String, Domain>();
          domainDetails.put(domain.getHttpResponse().getResponseBody().getId(), domain.getHttpResponse().getResponseBody());
        

        for (AccountModel accountModel : accountModels) {
          Account account = null;
          Domain eachDomain = domainDetails.get(accountModel.getId());

          Link link = new Link();
          link.setRel(LINK_REL);

          // If domain is deleted from keystone
          if (domain != null) {
            if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
              link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
              if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
                account = new Account(accountModel.getId(), eachDomain.getName(), eachDomain.getDescription(), true);
              } else {
                account = new Account(accountModel.getId(), eachDomain.getName(), eachDomain.getDescription(), false);
              }
            } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
              account =
                  new Account(accountModel.getId(), eachDomain.getName(), eachDomain.getDescription(), accountModel.getState(),
                      eachDomain.getEnabled());
              link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
            }
          } else {
            if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
              link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
              if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
                account = new Account(accountModel.getId(), "", "", true);
              } else {
                account = new Account(accountModel.getId(), "", "", false);
              }
            } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
              account = new Account(accountModel.getId(), "", "", accountModel.getState(), null);
              link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
            }
          }

          account.setLink(link);

          accounts.add(account);
        }
        response.setAccounts(accounts);
        response.setStatus(AccountProtocol.Status.SUCCESS_OK);
        AppLogger.info("Successfully fetched list of Accounts for account admin");
      } else {
        AppLogger.info("Internal error while fetching list of domains from keystone. Status: " + domain.getStatus());
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      }
    } else {
      accounts = new ArrayList<Account>();
      for (AccountModel accountModel : accountModels) {
        Account account = new Account(accountModel.getId(), "", "", accountModel.getState(), null);
        Link link = new Link();
        link.setRel(LINK_REL);
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
        account.setLink(link);

        accounts.add(account);
      }
      response.setAccounts(accounts);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.info("Successfully fetched list of Accounts for account admin");
    }
    return response;
  }
  
  

  /**
   * Method to get a particular account
   * 
   * @param accountRequest
   * @return AccountProtocol.GetAccountResponse instance
   */
  public static final AccountProtocol.GetAccountResponse getAccount(String version,
      final AccountProtocol.GetAccountRequest accountRequest) {
    AccountProtocol.GetAccountResponse response = new AccountProtocol.GetAccountResponse();

    Validator.validateNotNull(accountRequest);
    Validator.validateNotEmpty(accountRequest.getAccountId(), RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
    DbResponse<AccountModel> resp = null;
    
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      resp = TableFactory.getAccountTableForV1().getAccount(accountRequest.getAccountId());
    } else {
      resp = TableFactory.getAccountTable().getAccount(accountRequest.getAccountId());
    }
      
    AccountModel accountModel = resp.getResponseObj();

    if (accountModel == null) {
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      AppLogger.warn("Internal Error while fetching account" + accountRequest.getAccountId()
          + " from database. Failed with error " + resp.getErrorCode());
      response.setRespMsg(resp.getErrorMessage());

      return response;
    }

    Account account = null;

    ClientResponse<Domain> domainResponse = KeystoneHelper.getInstance().getDomain(accountRequest.getAccountId());

    if (domainResponse.getStatus() == ClientStatus.SUCCESS) {

      Domain domain = domainResponse.getHttpResponse().getResponseBody();
      Link link = new Link();
      link.setRel(LINK_REL);

      if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
        link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
        if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
          account = new Account(accountModel.getId(), domain.getName(), domain.getDescription(), true);
        } else {
          account = new Account(accountModel.getId(), domain.getName(), domain.getDescription(), false);
        }
      } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
        account =
            new Account(accountRequest.getAccountId(), domain.getName(), domain.getDescription(),
                accountModel.getState(), domain.getEnabled());
      }
      account.setLink(link);
      response.setAccount(account);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.info("Successfully fetched account " + accountRequest.getAccountId());

    }
    // If domain is deleted in keystone
    else if (domainResponse.getStatus() == ClientStatus.ERROR_HTTP
        && domainResponse.getHttpResponse().getStatusCode() == 404) {
      Link link = new Link();
      link.setRel(LINK_REL);

      if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
        link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
        if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
          account = new Account(accountRequest.getAccountId(), "", "", true);
        } else {
          account = new Account(accountRequest.getAccountId(), "", "", false);
        }
      } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
        account = new Account(accountRequest.getAccountId(), "", "", accountModel.getState(), null);
      }
      account.setLink(link);
      response.setAccount(account);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.warn("Domain %s not found in keystone", accountRequest.getAccountId());
    } else {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn(String.format("Error while fetching domain %s from keystone", accountRequest.getAccountId()));
    }

    return response;

  }

  /**
   * Method to get account information from the domain in the token
   *
   * @param domainRequest
   * @return GetAccountFromDomainResponse instance
   */
  public static final GetAccountFromDomainResponse getAccountFromDomain(final String version,
      final AccountProtocol.GetAccountFromDomainRequest domainRequest) {
    AccountProtocol.GetAccountFromDomainResponse response = new AccountProtocol.GetAccountFromDomainResponse();
    DbResponse<AccountModel> resp = null;
    Validator.validateNotNull(domainRequest);

    String requestDomainId = domainRequest.getDomainId();

    // requestDomainId will be empty if both domainId and projectDomainId is not set, make a rest call to get the
    // domainId
    if (StringUtils.isEmpty(requestDomainId) && StringUtils.isNotEmpty(domainRequest.getProjectId())) {
      Project project = KeystoneHelper.getInstance().getProjectDetails(domainRequest.getProjectId());
      if (project != null) {
        requestDomainId = project.getDomainId();
      } else {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn(String.format("Failure while getting project details for project %s from keystone",
            domainRequest.getProjectId()));
      }
    }

    Validator.validateNotEmpty(requestDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
      resp = TableFactory.getAccountTableForV1().getAccount(requestDomainId);
    if (resp != null) {
      AccountModel accountModel = resp.getResponseObj();
      if (accountModel != null) {
        AccountDomain accountDomain = new AccountDomain(accountModel.getId(), true);
        response.setAccountFromDomain(accountDomain);
        response.setStatus(AccountProtocol.Status.SUCCESS_OK);
        AppLogger.info("Successfully fetched account " + accountDomain.getAccountId() + " for domain "
            + requestDomainId);
      } else {
        response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
        AppLogger
            .warn("Failure in fetching account for domain " + requestDomainId + ". Status " + response.getStatus());
        response.setRespMsg(resp.getErrorMessage());
      }
    } else {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Failure while fetching account from database for domain " + requestDomainId);
    }
    return response;
  }

  /**
   * Method to create an account
   * 
   * 
   * @param createAccountRequest
   * @return AccountProtocol.CreateAccountResponse instance
   */
  public static final AccountProtocol.CreateAccountResponse createAccount(final String version,
      final CreateAccountRequest createAccountRequest) {
    AccountProtocol.CreateAccountResponse response = new AccountProtocol.CreateAccountResponse();
    Validator.validateNotNull(createAccountRequest);

    AccountRequest accountRequest = createAccountRequest.getAccount();

    Validator.validateNotNull(accountRequest);
    Validator.validateNotEmpty(accountRequest.getName(), RequestErrorMessage.REQUEST_ACCOUNT_NAME_EMPTY);
    String accountName = accountRequest.getName();

    ClientResponse<Domain> domainInsertedResponse = createPrimaryDomain(createAccountRequest);

    if (domainInsertedResponse == null) {
      AppLogger.warn("Received null response while inserting primary domain for account " + accountRequest.getName());
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      return response;
    }

    if (domainInsertedResponse.getStatus() != ClientStatus.SUCCESS
        && domainInsertedResponse.getHttpResponse().getStatusCode() == 409) {
      AppLogger.warn("Primary domain creation failed for this account. Received response "
          + domainInsertedResponse.getHttpResponse().getStatusCode());
      response.setRespMsg(ResponseErrorMessage.ACCOUNT_DOMAIN_CREATION_FAILED);
      // we are specifically checking for http status 409 so we are sure that the error has occurred because of a
      // conflict
      response.setStatus(AccountProtocol.Status.ERROR_CONFLICT);
      return response;
    }

    Domain domainInserted = domainInsertedResponse.getHttpResponse().getResponseBody();
    String domainInsertedId = domainInserted.getId();

    AppLogger.info("Successfully created primary domain " + domainInsertedId);

    // account_name + project prefix results in project name being more than 64 bytes and this causes problem while
    // persisting project in keystone db
    // as the maximum size of the project name permitted in the db is 64 bytes. The below logic takes care of this
    // scenario
    try {
      if (accountName.getBytes(CHARACTER_ENCODING).length > ACCOUNT_NAME_MAX_SIZE_FOR_PROJECT) {
        AppLogger.debug("Received account name with more that 56 characters, hence truncating it for the project name");
        accountName = accountName.substring(0, 31);
      }
    } catch (UnsupportedEncodingException e) {
      AppLogger.warn("Unsupported Character coding exception occurred");
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
    }
    ClientResponse<Project> projectInsertedResponse = null;
    
    // If an account with enabled as false is created then, domain is created with enabled as false, so in that case
    // Project need not be created with domain
    if (domainInserted.getEnabled().booleanValue()) {
      projectInsertedResponse = createPrimaryProject(domainInsertedId, PROJECT_NAME_PREFIX + accountName);

      if (projectInsertedResponse == null || projectInsertedResponse.getStatus() != ClientStatus.SUCCESS) {
        ClientStatus resp = (projectInsertedResponse == null) ? null : projectInsertedResponse.getStatus();
        // There shouldn't be any 409 errors here because this is the first project to be created in primary domain
        AppLogger.warn("Project insertion failed for domain " + domainInsertedId + ". Status : " + resp);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        return response;
      }
      AppLogger.info("Successfully created project "
          + projectInsertedResponse.getHttpResponse().getResponseBody().getId() + " for domain " + domainInsertedId);
    }
    
    AccountModel accountModel = new AccountModel(domainInsertedId, ACCOUNT_ACTIVE_STATUS);

    DbResponse<Boolean> resp = TableFactory.getAccountTable().addAccount(accountModel);
    boolean result = resp.getResponseObj();
    if (result) {
      Account account = null;
      Link link = new Link();
      link.setRel(LINK_REL);

      // If the request comes from V1
      if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
        link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
        if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
          account = new Account(accountModel.getId(), domainInserted.getName(), domainInserted.getDescription(), true);
        } else {
          account = new Account(accountModel.getId(), domainInserted.getName(), domainInserted.getDescription(), false);
        }
        // If the request comes from V2
      } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
        account =
            new Account(accountModel.getId(), domainInserted.getName(), domainInserted.getDescription(),
                accountModel.getState(), domainInserted.getEnabled());
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
      }

      account.setLink(link);

      response.setAccount(account);
      response.setStatus(AccountProtocol.Status.SUCCESS_CREATED);
      AppLogger.info("Successfully created account " + accountModel.getId());

      // After account creation is successfully done, we will notify controller services of this
      notifyControllersOfAccountCreation(domainInserted, accountModel.getId());

    } else {
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      AppLogger.info("Reverting insertion of primary domain in keystone as insertion failed for account "
          + accountRequest.getName());
      removePrimaryDomainOnFailure(domainInserted);
      AppLogger.warn("Failure in inserting account " + accountRequest.getName() + ". Status : " + response.getStatus());
      response.setRespMsg(resp.getErrorMessage());
    }

    return response;
  }

  /**
   * Method to create default primary domain for each account
   * 
   * @param createAccountRequest
   */
  private static ClientResponse<Domain> createPrimaryDomain(CreateAccountRequest createAccountRequest) {
    Domain domainToCreate = new Domain();
    domainToCreate.setName(createAccountRequest.getAccount().getName());
    domainToCreate.setDescription(createAccountRequest.getAccount().getDescription());
    domainToCreate.setNeutrinoReserved(true);
    if (createAccountRequest.getAccount().getEnabled() != null) {
      domainToCreate.setEnabled(createAccountRequest.getAccount().getEnabled());
    }
    ClientResponse<Domain> domainInserted = KeystoneHelper.getInstance().createDomain(domainToCreate);
    return domainInserted;
  }

  /**
   * Method to remove the default primary domain for each account when the account creation fails
   * 
   * @param primaryDomainInserted
   */
  private static void removePrimaryDomainOnFailure(Domain primaryDomainInserted) {
    KeystoneHelper.getInstance().deleteDomain(primaryDomainInserted);
  }

  /**
   * Method to create a default project in primary domain
   *
   * @param domainId
   * @return
   */
  private static ClientResponse<Project> createPrimaryProject(String domainId, String projectName) {
    Project project = new Project();
    project.setDomainId(domainId);
    project.setName(projectName);
    ClientResponse<Project> projectInserted = KeystoneHelper.getInstance().createProject(project);

    return projectInserted;
  }

  /**
   * Method to delete an account
   *
   * @param deleteRequest
   * @return
   */
  public static final AccountProtocol.DeleteAccountResponse deleteAccount(final String version,
      final AccountProtocol.DeleteAccountRequest deleteRequest) {
    AccountProtocol.DeleteAccountResponse response = new AccountProtocol.DeleteAccountResponse();

    Validator.validateNotNull(deleteRequest);
    Validator.validateNotEmpty(deleteRequest.getAccountId(), RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
    Account account = null;

    // Here we will allow de-activation of the account even if it is already de-activated, to handle the scenario where
    // the account clean up by workflow fails
    // and the account still exists in accounts DB in inactive state and we can proceed again to create the workflow
    // task to delete the account.
    DbResponse<AccountModel> resp =
        TableFactory.getAccountTable().changeAccountState(deleteRequest.getAccountId(), ACCOUNT_STATE_DELETING);
    if (resp.getResponseObj() != null) {
      AppLogger.info("Successfully changed the state of account " + deleteRequest.getAccountId());
      AccountModel result = resp.getResponseObj();

      com.emc.caspian.ccs.workflow.model.DbResponse<com.emc.caspian.ccs.workflow.model.JobModel> jobResponse =
          TaskSubmitter.submitTaskForWorkflow(Tasks.DELETE_ACCOUNT, deleteRequest.getAccountId(),
              KeystoneProperties.getkeystoneUri(), MySQLProperties.getAccountsUser(),
              MySQLProperties.getAccountsPassword(), MySQLProperties.getHostname(), MySQLProperties.getPort(),
              MySQLProperties.getAccountsDatabase(),
              String.join(HOST_ENTRY_SEPARATOR, ControllerClientHelper.getInstance().getListOfControllerHosts()));

      if (jobResponse != null) {
        if (jobResponse.getErrorCode() == null) {
          response.setStatus(AccountProtocol.Status.SUCCESS_ACCEPTED);

          Map<String, String> locationPathHeader = new HashMap<String, String>();
          // null check for AS host returned by the CaspianControllerClient
          String locationPath =
              (ControllerClientHelper.getInstance().getASHostFromCRS() != null) ? ControllerClientHelper.getInstance()
                  .getASHostFromCRS() : "";

          if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
            ClientResponse<Domain> domainResponse =
                KeystoneHelper.getInstance().getDomain(deleteRequest.getAccountId());

            Link link = new Link();
            link.setRel(LINK_REL);
            link.setHref(String.format(ACCOUNT_V1_PATH, result.getId()));
            Domain domain = null;
            if (domainResponse.getStatus() == ClientStatus.SUCCESS) {
              domain = domainResponse.getHttpResponse().getResponseBody();
              account = new Account(result.getId(), domain.getName(), domain.getDescription(), false);
              account.setLink(link);
            }
            // Corner case : An account is created from neutrino and the corresponding domain is deleted in keystone
            else if (domainResponse.getStatus() == ClientStatus.ERROR_HTTP
                && domainResponse.getHttpResponse().getStatusCode() == 404) {
              account = new Account(result.getId(), "", "", null);
              account.setLink(link);
            } else {
              response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
              response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
              AppLogger
                  .warn(String.format("Error while fetching domain %s from keystone", deleteRequest.getAccountId()));
              return response;
            }

            locationPath =
                locationPath + String.format(ACCOUNT_V1_PATH, deleteRequest.getAccountId()) + TASK_PATH
                    + jobResponse.getResponseObj().getId();
            response.setAccount(account);
          } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
            locationPath =
                locationPath + String.format(ACCOUNT_V2_PATH, deleteRequest.getAccountId()) + TASK_PATH
                    + jobResponse.getResponseObj().getId();
          }

          locationPathHeader.put(LOCATION_HEADER, locationPath);

          response.setResponseHeaders(locationPathHeader);
          AppLogger.info("Successfully submitted delete request for account " + deleteRequest.getAccountId()
              + " with taskId " + jobResponse.getResponseObj().getId());
        } else {
          response.setStatus(ErrorCodeToProtocolStatusMapper.convertJobError(jobResponse.getErrorCode()));
          AppLogger.warn("Failure in marking account as deleted for account " + deleteRequest.getAccountId() + " "
              + response.getStatus());
          response.setRespMsg(jobResponse.getErrorMessage());
        }
      } else {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn("Failure in submitting delete account task to workflow for account "
            + deleteRequest.getAccountId());
      }
    } else {
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      response.setRespMsg(resp.getErrorMessage());
    }
    return response;
  }

  /**
   * Method to update an account
   *
   * @param updateRequest
   * @return AccountProtocol.UpdateAccountResponse instance
   */
  public static final AccountProtocol.UpdateAccountResponse updateAccount(String version,
      final UpdateAccountRequest updateRequest) {
    AccountProtocol.UpdateAccountResponse response = new AccountProtocol.UpdateAccountResponse();

    Validator.validateNotNull(updateRequest);
    Validator.validateNotEmpty(updateRequest.getAccount().getName(), RequestErrorMessage.REQUEST_ACCOUNT_NAME_EMPTY);

    // Precondition check : If account does not exist or if it is not active
    ConditionResponse conditionResponse = accountExistsAndActive(updateRequest.getId());
    // If account does not exist or is not active
    if (!conditionResponse.isSuccess()) {
      response.setStatus(conditionResponse.getStatus());
      response.setRespMsg(conditionResponse.getResponseMessage());
      return response;
    }

    // Update primary domain in keystone
    Domain domainToUpdate = new Domain();
    domainToUpdate.setName(updateRequest.getAccount().getName());
    domainToUpdate.setDescription(updateRequest.getAccount().getDescription());
    if (updateRequest.getAccount().getEnabled() != null) {
      domainToUpdate.setEnabled(updateRequest.getAccount().getEnabled());
    }
    ClientResponse<Domain> domainUpdated =
        KeystoneHelper.getInstance().updateDomain(domainToUpdate, updateRequest.getId());

    if (domainUpdated.getStatus() == ClientStatus.SUCCESS) {
      AppLogger.info("Successfully updated primary domain " + updateRequest.getId() + " in keystone");
    } else {
      if (domainUpdated.getStatus() == ClientStatus.ERROR_HTTP
          && domainUpdated.getHttpResponse().getStatusCode() == 404) {
        response.setRespMsg(ResponseErrorMessage.ACCOUNT_NOT_FOUND);
        response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        AppLogger.warn("Primary domain " + updateRequest.getId() + " does not exist in Keystone");
      } else if (domainUpdated.getStatus() == ClientStatus.ERROR_HTTP
          && domainUpdated.getHttpResponse().getStatusCode() == 409) {
        response.setRespMsg(ResponseErrorMessage.ACCOUNT_ALREADY_EXISTS);
        response.setStatus(AccountProtocol.Status.ERROR_CONFLICT);
        AppLogger.warn("Domain with name " + updateRequest.getAccount().getName() + " already exists in Keystone");
      }
      // If the domain is not updated, return an error
      else {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn("Internal Error while updating domain " + updateRequest.getId() + " in keystone");
      }
      return response;
    }

    Domain updatedDomain = domainUpdated.getHttpResponse().getResponseBody();

    DbResponse<AccountModel> resp = TableFactory.getAccountTable().getAccount(updateRequest.getId());
    AccountModel accountModel = resp.getResponseObj();
    if (accountModel != null) {
      Account account = null;
      Link link = new Link();
      link.setRel(LINK_REL);

      if (version.equals(AccountServiceVersions.API_VERSION_V1)) {
        link.setHref(String.format(ACCOUNT_V1_PATH, accountModel.getId()));
        if (accountModel.getState().equals(ACCOUNT_STATE_ACTIVE)) {
          account = new Account(updateRequest.getId(), updatedDomain.getName(), updatedDomain.getDescription(), true);
        } else {
          account = new Account(updateRequest.getId(), updatedDomain.getName(), updatedDomain.getDescription(), false);
        }
      } else if (version.equals(AccountServiceVersions.API_VERSION_V2)) {
        link.setHref(String.format(ACCOUNT_V2_PATH, accountModel.getId()));
        account =
            new Account(updateRequest.getId(), updatedDomain.getName(), updatedDomain.getDescription(),
                accountModel.getState(), updatedDomain.getEnabled());
      }
      account.setLink(link);
      response.setAccount(account);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.info("Successfully updated the account " + updateRequest.getId());
    } else {
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      AppLogger
          .warn("Internal Error while getting account details from database, for account " + updateRequest.getId());
      response.setRespMsg(resp.getErrorMessage());
    }
    return response;
  }

  /**
   * Method to list domains of an account
   *
   * @param request
   * @return AccountProtocol.GetAccountDomainsResponse instance
   */

  public static final AccountProtocol.GetAccountDomainsResponse listAccountDomains(
      AccountProtocol.GetAccountDomainsRequest request) {
    AccountProtocol.GetAccountDomainsResponse response = new AccountProtocol.GetAccountDomainsResponse();

    Validator.validateNotNull(request);
    Validator.validateNotEmpty(request.getAccountId(), RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);

    // Precondition check 1: If account does not exist
    DbResponse<AccountModel> accountDetails = TableFactory.getAccountTable().getAccount(request.getAccountId());

    if (accountDetails.getErrorCode() == ErrorCode.DB_RECORD_NOT_FOUND) {
      AppLogger.warn("Account " + request.getAccountId() + " does not exist");
      response.setRespMsg(ResponseErrorMessage.ACCOUNT_NOT_FOUND);
      response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);

      return response;
    } else if (accountDetails.getResponseObj() == null) {
      AppLogger.warn("An error encountered while fetching the account " + request.getAccountId());
      response.setRespMsg(accountDetails.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(accountDetails.getErrorCode()));

      return response;
    }

    ClientResponse<Domain> domainResponse = KeystoneHelper.getInstance().getDomain(request.getAccountId());

    AccountDomainDetails domainDetails = new AccountDomainDetails();
    List<DomainDetail> list = new ArrayList<DomainDetail>();
    DomainDetail domainDetail = new DomainDetail();
    // set link information
    Link link = new Link();
    link.setRel(LINK_REL);
    link.setHref(String.format(KEYSTONE_DOMAIN_URL, KeystoneProperties.getkeystoneUri(), request.getAccountId()));
    domainDetail.setLink(link);
    domainDetail.setId(request.getAccountId());

    if (domainResponse.getStatus() == ClientStatus.SUCCESS) {
      Domain domain = domainResponse.getHttpResponse().getResponseBody();
      domainDetail.setPrimary(true);
      domainDetail.setDescription(domain.getDescription());
      domainDetail.setEnabled(domain.getEnabled());
      domainDetail.setName(domain.getName());
      domainDetail.setIsPresent(true);
    }
    // If domain is not found in keystone
    else if (domainResponse.getStatus() == ClientStatus.ERROR_HTTP
        && domainResponse.getHttpResponse().getStatusCode() == 404) {
      domainDetail.setPrimary(true);
      domainDetail.setDescription("");
      domainDetail.setName("");
      domainDetail.setIsPresent(false);
    } else {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn(String.format("Error while fetching domain %s from keystone", request.getAccountId()));
      return response;
    }
    list.add(domainDetail);

    domainDetails.setDomains(list);
    response.setDomains(domainDetails);
    response.setStatus(AccountProtocol.Status.SUCCESS_OK);
    AppLogger.info("Successfully fetched domains mapped to account " + request.getAccountId());

    return response;
  }
  
  /**
   * Method to list all domains of an account
   * 
   * @return AccountProtocol.GetAllDomainsResponse instance
   */
  public static final AccountProtocol.GetAllDomainsResponse listAllDomains(final String version) {
    AccountProtocol.GetAllDomainsResponse response = new AccountProtocol.GetAllDomainsResponse();

    // TODO Get the list of domains between a particular range
    ClientResponse<Domains> domains = KeystoneHelper.getInstance().getAllDomains();
    DbResponse<List<AccountModel>> respAccountDomainModel=null;
    if (domains.getStatus() == ClientStatus.SUCCESS) {

      AccountDomainDetails domainDetails = new AccountDomainDetails();
      List<DomainDetail> list = new ArrayList<DomainDetail>();
      
        respAccountDomainModel = TableFactory.getAccountTableForV1().getAccountDomainsWithEnhancedInfo();

      if (respAccountDomainModel == null || respAccountDomainModel.getResponseObj() == null) {
        AppLogger.warn("Internal Error while fetching all domains from database. Failed with error "
            + respAccountDomainModel.getErrorCode());
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);

        return response;
      }

      List<AccountModel> accountModels = respAccountDomainModel.getResponseObj();
      HashMap<String, String> accounts = new HashMap<String,String>();
      for (AccountModel accountModel : accountModels) {
        accounts.put(accountModel.getAccount_id(), accountModel.getId());
      }
      
      for (Domain domain : domains.getHttpResponse().getResponseBody()) {

        DomainDetail domainDetail = new DomainDetail();
        domainDetail.setDescription(domain.getDescription());
        domainDetail.setEnabled(domain.getEnabled());
        domainDetail.setId(domain.getId());
        domainDetail.setName(domain.getName());
        domainDetail.setIsPresent(true);
        // set link information
        Link link = new Link();
        link.setRel(LINK_REL);
        link.setHref(String.format(KEYSTONE_DOMAIN_URL, KeystoneProperties.getkeystoneUri(), domain.getId()));
        domainDetail.setLink(link);

        if (accounts.containsValue(domain.getId())) {
          domainDetail.setPrimary(true);
          for (Entry<String, String> account : accounts.entrySet()) {
            if (domain.getId().equalsIgnoreCase(account.getValue()))
              domainDetail.setAccountId(account.getKey());
            if (account.getKey() != null) {
              domainDetail.setAccountName(domain.getName());
            } else {
              domainDetail.setAccountName(null);
            }
          }
        } else {
          AppLogger.warn("Domain " + domain.getId() + " is not associated with any account");

        }
        AppLogger.debug("Adding domain " + domain.getId() + " to the list of all domains");
        list.add(domainDetail);
      }

      for (Entry<String, String> accountDomain : accounts.entrySet()) {
        ClientResponse<Domain> domain = KeystoneHelper.getInstance().getDomain(accountDomain.getValue());
        if(domain.getHttpResponse().getStatusCode() == 404 ){
        DomainDetail domainDetail = new DomainDetail();
        domainDetail.setDescription("");
        domainDetail.setId(accountDomain.getValue());
        domainDetail.setName("");
        domainDetail.setPrimary(true);
        domainDetail.setAccountId(accountDomain.getKey());
        domainDetail.setAccountName("");
        domainDetail.setIsPresent(false);
        list.add(domainDetail);
        }
      }
      domainDetails.setDomains(list);
      response.setDomains(domainDetails);
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      AppLogger.info("Successfully fetched list of all domains with enhanced information");
    } else {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Error while fetching list of all domains from keystone");
    }
    return response;
  }

  /**
   * Method to create IDP from a domain
   * 
   * 
   * @param createIdpFromDomainRequest
   * @return AccountProtocol.CreateIdpResponse instance
   */
  public static AccountProtocol.CreateIdpResponse createIdpFromDomain(String version,
      CreateIdpRequest createIdpFromDomainRequest, String validate) {
    AccountProtocol.CreateIdpResponse response = new AccountProtocol.CreateIdpResponse();

    // Perform validation for necessary fields
    Validator.validateNotNull(createIdpFromDomainRequest);
    // Domain ID must not be empty
    Validator.validateNotEmpty(createIdpFromDomainRequest.getDomainId(), RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);

    IdpRequest idpRequest = createIdpFromDomainRequest.getIdp();
    // Idp Type must not be empty
    Validator.validateNotEmpty(idpRequest.getType().name(), RequestErrorMessage.REQUEST_IDP_TYPE_EMPTY);

    IdpConfig idpConfig = idpRequest.getIdpConfig();

    String domainId = createIdpFromDomainRequest.getDomainId();
    IdpType idpType = idpRequest.getType();
    DomainConfig domainConfig = new DomainConfig();
    DomainConfigInfo domainConfigInfo = null;

    // Check 1: Return an error if account is inactive
    ConditionResponse isAccountActivePrecondition = isAccountActivePreconditionPassed(domainId);
    if (!isAccountActivePrecondition.isSuccess()) {
      response.setStatus(isAccountActivePrecondition.getStatus());
      response.setRespMsg(isAccountActivePrecondition.getResponseMessage());
      return response;
    }

    // Check 2: Return an error if domain does not exist
    // Can happen if the domain was deleted via Keystone but is still associated to the account
    ConditionResponse domainExistsCondition = domainExistsPreconditionPassed(domainId);
    if (!domainExistsCondition.isSuccess()) {
      response.setRespMsg(domainExistsCondition.getResponseMessage());
      response.setStatus(domainExistsCondition.getStatus());
      return response;
    }

    // Check 3: Return an error if IDP exists already
    ConditionResponse idpDoesNotExistPrecondition = idpDoesNotExistPreconditionPassed(domainId);
    if (!idpDoesNotExistPrecondition.isSuccess()) {
      response.setStatus(idpDoesNotExistPrecondition.getStatus());
      response.setRespMsg(idpDoesNotExistPrecondition.getResponseMessage());
      return response;
    }

    // Check 4: If any users/groups are associated with this domain already
    // OR an error occurs in retrieving them return an error
    ConditionResponse usersGroupsPrecondition = usersGroupsPreconditionPassed(domainId);
    if (!usersGroupsPrecondition.isSuccess()) {
      response.setStatus(usersGroupsPrecondition.getStatus());
      response.setRespMsg(usersGroupsPrecondition.getResponseMessage());
      return response;
    }
    if(validate.equalsIgnoreCase(VALIDATE_ONLY) || validate.equalsIgnoreCase(VALIDATE_BEFORE_SAVE)){
      //validate ldap connection
      ValidateIdpDetails validateIdp = validateIdpConfig(idpConfig,true).getValidateIdpDetails();
      response.setValidateIdpDetails(validateIdp);
      if(validate.equalsIgnoreCase(VALIDATE_ONLY)){
        response.setStatus(AccountProtocol.Status.SUCCESS_OK);
        return response;
      }
      else if(!response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SUCCESS)){
        response.setStatus(AccountProtocol.Status.ERROR_BAD_REQUEST_WITH_RESPONSE);
        return response;
      }
    }

    if (idpType.equals(IdpType.LDAP)) {
      AppLogger.debug(String.format("Request to create LDAP Idp for domain %s", domainId));
      domainConfigInfo = getDomainConfFromIdpConf(idpConfig, idpType, validate);

    } else if (idpType.equals(IdpType.KEYSTONE2KEYSTONE)) {
      AppLogger.debug(String.format("Request to create Keystone to Keystone Idp for domain %s ", domainId));
      response.setStatus(AccountProtocol.Status.NOT_IMPLEMENTED);
      response.setRespMsg(ResponseErrorMessage.METHOD_NOT_IMPLEMENTED);
      return response;
    }

    domainConfig.setDomainConfigInfo(domainConfigInfo);

    ClientResponse<DomainConfig> createDomainIdp =
        KeystoneHelper.getInstance().createDomainIdp(JsonHelper.serializeToJson(domainConfig), domainId);

    if (createDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If the domain's config is not added, return an error
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn(String.format("Error while creating domain config for domain %s in keystone", domainId));
      return response;
    }

    String idpPwd = null;
    try (AESUtil au = AESUtil.getInstance()) {
      // AESUtil is used here to encrypt idp password obtained from idpConfig
      idpPwd = au.encrypt(idpConfig.getUserBindPwd());
    }
    IdpPasswordModel idpPasswordModel = new IdpPasswordModel(domainId, idpConfig.getUserBindDn(), idpPwd);
    DbResponse<Boolean> resp = TableFactory.getIdpPasswordTable().addIdpPassword(idpPasswordModel);
    if (resp == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn(String.format("Received null response while adding idp %s in database" + domainId));
      return response;
    }
    boolean result = resp.getResponseObj();
    if (result != true) {
      AppLogger.warn("Failed to add idp %s in database", idpPasswordModel.getIdpId());
      response.setRespMsg(resp.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      return response;
    }

    Idp responseIdp = new Idp();
    responseIdp.setId(domainId);
    Link link = new Link();
    link.setRel(LINK_REL);
    link.setHref(String.format(IDENTITY_PROVIDERS_PATH, domainId));
    responseIdp.setLink(link);
    response.setIdp(responseIdp);
    response.setStatus(AccountProtocol.Status.SUCCESS_CREATED);
    return response;
  }

  /**
   * Method to get a particular IDP
   * 
   * 
   * @param getIdpRequest
   * @return AccountProtocol.GetIdpResponse instance
   */
  public static AccountProtocol.GetIdpResponse getIdp(String version, GetIdpRequest getIdpRequest) {
    String domainId = getIdpRequest.getDomainId();
    Validator.validateNotEmpty(domainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
    AccountProtocol.GetIdpResponse response = new AccountProtocol.GetIdpResponse();

    // Check 1: Return an error if domain does not exist
    ConditionResponse domainExistsCondition = domainExistsPreconditionPassed(domainId);
    if (!domainExistsCondition.isSuccess()) {
      response.setRespMsg(domainExistsCondition.getResponseMessage());
      response.setStatus(domainExistsCondition.getStatus());
      return response;
    }

    ClientResponse<DomainConfig> getDomainIdp = KeystoneHelper.getInstance().getDomainIdp(domainId);
    if (getDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If an IDP was not found for this domain
      if (getDomainIdp.getHttpResponse().getStatusCode() == 404) {
        response.setRespMsg(String.format(ResponseErrorMessage.DOMAIN_IDP_NOT_FOUND, domainId));
        response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        AppLogger.warn(String.format("IDP not found for domain %s", domainId));

        return response;
      }
      // If the domain's config could not be retrieved, return an error
      else {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn(String.format("Error while fetching domain configuration for domain %s in keystone", domainId));
        return response;
      }
    }

    // get password from database
    DbResponse<IdpPasswordModel> resp = TableFactory.getIdpPasswordTable().getPassword(domainId);
    if (resp == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn(String.format("Received null response while getting idp %s from database " + domainId));
      return response;
    }

    IdpPasswordModel idpPasswordModel = resp.getResponseObj();
    if (idpPasswordModel == null) {
      response.setRespMsg(resp.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      AppLogger.warn("Failed to get idp %s from database", domainId);
      return response;
    }
    DomainConfig domainConfig = getDomainIdp.getHttpResponse().getResponseBody();
    DomainConfigInfo domainConfigInfo = domainConfig.getDomainConfigInfo();
    String driver = domainConfigInfo.getIdentityDriver().getDriver();
    IdpConfig idpConfig = new IdpConfig();

    Idp idp = new Idp();
    idp.setId(domainId);

    // Based on driver set LDAP or Keystone to Keystone config
    // remove hardcoding
    if (driver.equals(IDP_LDAP_DRIVER)) {
      idpConfig = getIdpConfFromDomainConf(domainConfigInfo, IdpType.LDAP, idpPasswordModel.getIdpPwd());
      idp.setType(IdpType.LDAP);
    } else {
      AppLogger.debug("Not implemented");
      response.setStatus(AccountProtocol.Status.NOT_IMPLEMENTED);
      response.setRespMsg(ResponseErrorMessage.METHOD_NOT_IMPLEMENTED);
      return response;
    }
    idp.setIdpConfig(idpConfig);
    Link link = new Link();
    link.setRel(LINK_REL);
    if(version.equalsIgnoreCase(ACCOUNT_V1_PATH))
      link.setHref(String.format(IDENTITY_PROVIDERS_PATH_V1, idp.getId()));
    else
      link.setHref(String.format(IDENTITY_PROVIDERS_PATH, idp.getId()));
    idp.setLink(link);
    response.setIdp(idp);
    response.setStatus(AccountProtocol.Status.SUCCESS_OK);
    AppLogger.info("Successfully fetched IDP details for domain %s", domainId);
    return response;
  }

  /**
   * 
   * Method to update an IDP
   * 
   * @param updateIdpRequest
   * @return AccountProtocol.UpdateIdpResponse instance
   */

  public static AccountProtocol.UpdateIdpResponse updateIdp(String version, UpdateIdpRequest updateIdpRequest, String validate) {
    AccountProtocol.UpdateIdpResponse response = new AccountProtocol.UpdateIdpResponse();

    String domainId = updateIdpRequest.getDomainId();
    IdpRequest idpRequest = updateIdpRequest.getIdp();
    IdpConfig idpConfig = idpRequest.getIdpConfig();
    IdpType idpType = idpRequest.getType();
    DomainConfig domainConfig = new DomainConfig();
    DomainConfigInfo domainConfigInfo = null;

    // Check 1: Return an error if account is inactive
    ConditionResponse isAccountActivePrecondition = isAccountActivePreconditionPassed(domainId);
    if (!isAccountActivePrecondition.isSuccess()) {
      response.setStatus(isAccountActivePrecondition.getStatus());
      response.setRespMsg(isAccountActivePrecondition.getResponseMessage());
      return response;
    }

    // Check 2: Return an error if domain does not exist
    // Can happen if the domain was deleted via Keystone but is still associated to the account
    ConditionResponse domainExistsCondition = domainExistsPreconditionPassed(domainId);
    if (!domainExistsCondition.isSuccess()) {
      response.setRespMsg(domainExistsCondition.getResponseMessage());
      response.setStatus(domainExistsCondition.getStatus());
      return response;
    }

    // Check 3: Return an error if IDP does not exist
    ConditionResponse idpExistsCondition = idpExistsPreconditionPassed(domainId);
    if (!idpExistsCondition.isSuccess()) {
      response.setRespMsg(idpExistsCondition.getResponseMessage());
      response.setStatus(idpExistsCondition.getStatus());
      return response;
    }

    if(validate.equalsIgnoreCase(VALIDATE_ONLY) || validate.equalsIgnoreCase(VALIDATE_BEFORE_SAVE)){
        //validate ldap connection
        ValidateIdpDetails validateIdp = validateIdpConfig(idpConfig,true).getValidateIdpDetails();
        response.setValidateIdpDetails(validateIdp);
        if(validate.equalsIgnoreCase(VALIDATE_ONLY)){
          response.setStatus(AccountProtocol.Status.SUCCESS_OK);
          return response;
        }
        else if(!response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SUCCESS)){
          response.setStatus(AccountProtocol.Status.ERROR_BAD_REQUEST_WITH_RESPONSE);
          return response;
        }
      }

    if (idpType.equals(IdpType.LDAP)) {

      AppLogger.debug("Request to update LDAP Idp for domain %s", domainId);
      domainConfigInfo = getDomainConfFromIdpConf(idpConfig, idpType, validate);


    } else if (idpType.equals(IdpType.KEYSTONE2KEYSTONE)) {
      AppLogger.debug("K2K not implemented");
      response.setStatus(AccountProtocol.Status.NOT_IMPLEMENTED);
      response.setRespMsg(ResponseErrorMessage.METHOD_NOT_IMPLEMENTED);
      return response;
    }

    domainConfig.setDomainConfigInfo(domainConfigInfo);

    // Update IDP now for this domain

    ClientResponse<DomainConfig> updateDomainIdp =
        KeystoneHelper.getInstance().updateDomainIdp(JsonHelper.serializeToJson(domainConfig), domainId);

    if (updateDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If an IDP was not found for this domain
      if (updateDomainIdp.getHttpResponse().getStatusCode() == 404) {
        response.setRespMsg(String.format(ResponseErrorMessage.DOMAIN_IDP_NOT_FOUND, domainId));
        response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        AppLogger.warn("IDP not found for domain %s", domainId);

        return response;
      }
      // If the domain's config is not added, return an error
      else if (updateDomainIdp.getHttpResponse().getStatusCode() != 200) {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn("Error while updating domain configuration for domain %s in keystone", domainId);
        return response;
      }
    }

    DbResponse<IdpPasswordModel> passwordDetails = TableFactory.getIdpPasswordTable().getPassword(domainId);
    if (passwordDetails == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Received null response while getting idp %s from database " + domainId);
      return response;
    }

    IdpPasswordModel idpPasswordModel = passwordDetails.getResponseObj();
    if (idpPasswordModel == null) {
      response.setRespMsg(passwordDetails.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(passwordDetails.getErrorCode()));
      AppLogger.warn("Failed to get idp %s from database", domainId);
      return response;
    }
    String idpPwd = null;

    try (AESUtil au = AESUtil.getInstance()) {
      idpPwd = au.encrypt(idpConfig.getUserBindPwd());
    }

    idpPasswordModel.setIdpId(domainId);
    idpPasswordModel.setIdpPwd(idpPwd);
    idpPasswordModel.setIdpUser(idpConfig.getUserBindDn());
    DbResponse<Boolean> resp = TableFactory.getIdpPasswordTable().updateIdpPassword(idpPasswordModel);
    if (resp == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Received null response while updating idp %s in database " + domainId);
      return response;
    }
    boolean result = resp.getResponseObj();
    if (result != true) {
      AppLogger.warn("Failed to update idp %s in database", idpPasswordModel.getIdpId());
      response.setRespMsg(resp.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      return response;
    }
    AppLogger.info("Successfully updated IDP %s in database", domainId);
    // For a 200 response on updateIDP return response

    Idp responseIdp = new Idp();
    responseIdp.setId(domainId);

    Link link = new Link();
    link.setRel(LINK_REL);
    link.setHref(String.format(IDENTITY_PROVIDERS_PATH, responseIdp.getId()));

    responseIdp.setIdpConfig(getIdpConfFromDomainConf(updateDomainIdp.getHttpResponse().getResponseBody()
        .getDomainConfigInfo(), idpType, idpPasswordModel.getIdpPwd()));
    responseIdp.setType(idpType);
    responseIdp.setLink(link);
    response.setIdp(responseIdp);
    response.setStatus(AccountProtocol.Status.SUCCESS_OK);
    AppLogger.info("Successfully updated the IDP for domain %s", domainId);
    return response;
  }
  /**
   * Method to validate an IDP
   * 
   * @param validateIdpRequest
   * @return 
   */

  public static AccountProtocol.ValidateIdpResponse validateIdp(String version, ValidateIdpRequest validateIdpRequest) {
    AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
    String domainId = validateIdpRequest.getDomainId();
    AccountProtocol.GetIdpRequest getIdpRequest = new AccountProtocol.GetIdpRequest();
    getIdpRequest.setDomainId(domainId);
    AccountProtocol.GetIdpResponse getIdpResponse = getIdp(version,getIdpRequest);
    Idp idp = getIdpResponse.getIdp();
    IdpConfig idpConfig = idp.getIdpConfig();
    String idpPwd = null;
    try (AESUtil au = AESUtil.getInstance()) {
      // AESUtil is used here to decrypt idp password obtained from idpConfig
      idpPwd = au.decrypt(idpConfig.getUserBindPwd());
    }
    idpConfig.setUserBindPwd(idpPwd);
    response = validateIdpConfig(idpConfig,false);
    return response;
  }
        

  /**
   * Method to remove an IDP
   * 
   * @param deleteIdpRequest
   * @return AccountProtocol.DeleteIdpResponse instance
   */
  public static AccountProtocol.DeleteIdpResponse removeIdp(DeleteIdpRequest deleteIdpRequest) {
    AccountProtocol.DeleteIdpResponse response = new AccountProtocol.DeleteIdpResponse();

    String domainId = deleteIdpRequest.getDomainId();

    // Check 1: Return an error if domain does not exist
    ConditionResponse domainExistsCondition = domainExistsPreconditionPassed(domainId);
    if (!domainExistsCondition.isSuccess()) {
      response.setRespMsg(domainExistsCondition.getResponseMessage());
      response.setStatus(domainExistsCondition.getStatus());
      return response;
    }


    // Check 2: Return an error if IDP does not exist
    ConditionResponse idpExistsCondition = idpExistsPreconditionPassed(domainId);
    if (!idpExistsCondition.isSuccess()) {
      response.setRespMsg(idpExistsCondition.getResponseMessage());
      response.setStatus(idpExistsCondition.getStatus());
      return response;
    }

    // If the domain and the IDP exists, we delete the IDP now

    ClientResponse<DomainConfig> deleteDomainIdp = KeystoneHelper.getInstance().deleteDomainIdp(domainId);

    if (deleteDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If an IDP was not found for this domain
      if (deleteDomainIdp.getHttpResponse().getStatusCode() == 404) {
        response.setRespMsg(String.format(ResponseErrorMessage.DOMAIN_IDP_NOT_FOUND, domainId));
        response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        AppLogger.warn("Domain does not exist or no configuration found for domain %s", domainId);
        return response;
      }
      // If the domain's config is not added, return an error
      else {
        response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
        AppLogger.warn("Error while deleting domain configuration for domain %s from keystone", domainId);

        return response;
      }
    }
    DbResponse<Boolean> resp = TableFactory.getIdpPasswordTable().removeIdpPassword(domainId);
    if (resp == null) {
      response.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      response.setRespMsg(ResponseErrorMessage.INTERNAL_ERROR);
      AppLogger.warn("Received null response while deleting idp %s in database " + domainId);
      return response;
    }
    boolean result = resp.getResponseObj();
    if (result != true) {
      AppLogger.warn("Failed to delete idp %s in database", domainId);
      response.setRespMsg(resp.getErrorMessage());
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      return response;
    }
    AppLogger.info("Successfully deleted IDP for domain %s", domainId);
    response.setStatus(AccountProtocol.Status.SUCCESS_NO_CONTENT);
    return response;
  }

  /**
   * Method to get the status of a workflow task
   * 
   * @param getTaskStatusRequest
   * @return AccountProtocol.GetTaskStatusResponse instance
   */
  public static AccountProtocol.GetTaskStatusResponse getTaskStatus(final String version, GetTaskStatusRequest getTaskStatusRequest) {
    AccountProtocol.GetTaskStatusResponse response = new AccountProtocol.GetTaskStatusResponse();

    Validator.validateNotNull(getTaskStatusRequest);
    Validator.validateNotEmpty(getTaskStatusRequest.getAccountId(), RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);

    DbResponse<JobModel> resp = TableFactory.getAccountTable().getTaskStatusForAccount(getTaskStatusRequest.getId());
    JobModel jobTaskModel = resp.getResponseObj();
    if (jobTaskModel != null) {
      if (jobTaskModel.getParametersList().get(0).equals(getTaskStatusRequest.getAccountId())) {
        Resource resource = new WorkflowTask().new Resource();

        // set link information in resource
        Link resourceLink = new Link();
        resourceLink.setRel(LINK_REL);
        if(version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)){
        resourceLink.setHref(String.format(ACCOUNT_V1_PATH, getTaskStatusRequest.getAccountId()));
        } else {
          resourceLink.setHref(String.format(ACCOUNT_V2_PATH, getTaskStatusRequest.getAccountId()));
        }
        resource.setLink(resourceLink);
        resource.setId(getTaskStatusRequest.getAccountId());

        WorkflowTask task = new WorkflowTask(jobTaskModel.getId(), jobTaskModel.getStatus(), resource);
        // set link information for workflow
        Link link = new Link();
        link.setRel(LINK_REL);
        if(version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)){
        link.setHref(String.format(ACCOUNT_V1_PATH, getTaskStatusRequest.getAccountId()) + TASK_PATH
            + jobTaskModel.getId());
        } else {
          link.setHref(String.format(ACCOUNT_V2_PATH, getTaskStatusRequest.getAccountId()) + TASK_PATH
              + jobTaskModel.getId());
        }
        task.setResource(resource);
        task.setLink(link);

        if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V2)) {
          if (task.getStatus().equals(WorkflowTaskStatus.Successful))
            task.setStatus(WorkflowTaskStatus.Complete);
          if (task.getStatus().equals(WorkflowTaskStatus.Running))
            task.setStatus(WorkflowTaskStatus.Executing);
          if (task.getStatus().equals(WorkflowTaskStatus.FatalError)
              || task.getStatus().equals(WorkflowTaskStatus.FleetingError))
            task.setStatus(WorkflowTaskStatus.Error);
        }

        response.setWorkflowTask(task);
        response.setStatus(AccountProtocol.Status.SUCCESS_OK);
        AppLogger.info("Successfully fetched task status for task " + getTaskStatusRequest.getId() + " in account "
            + getTaskStatusRequest.getAccountId());
      } else {
        AppLogger.warn("Task %s not mapped with account %s", getTaskStatusRequest.getId(),
            getTaskStatusRequest.getAccountId());
        response.setRespMsg("Task " + getTaskStatusRequest.getId() + " is not mapped to account "
            + getTaskStatusRequest.getAccountId()
            + ". Please check if the account exists and is correctly mapped to the task requested");
        response.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
      }
    } else {
      response.setStatus(ErrorCodeToProtocolStatusMapper.convert(resp.getErrorCode()));
      AppLogger.warn("Failure in fetching task %s for account %s", getTaskStatusRequest.getId(),
          getTaskStatusRequest.getAccountId());
      response.setRespMsg(resp.getErrorMessage());
    }
    return response;
  }

  /**
   * This method notifies the controllers about account creation
   * 
   * @param primaryDomainInserted
   * @param accountId
   */
  public static void notifyControllersOfAccountCreation(Domain primaryDomainInserted, String accountId) {

    Validator.validateNotNull(accountId, "Account ID cannot be empty for notification");
    Validator.validateNotNull(primaryDomainInserted,
        "Primary domain for the account cannot be empty for account creation notification");

    // This list can be used to get bdata controller endpoint in future
    List<String> listOfControllerHosts = new ArrayList<String>();
    try {
      listOfControllerHosts = ControllerClientHelper.getInstance().getListOfControllerHosts();
      // According to spec account creation will not fail irrespective of CRS / C3 not installed
    } catch (IllegalStateException e) {
      AppLogger.warn(e.getMessage());
      return;
    }

    if (listOfControllerHosts.isEmpty()) {
      AppLogger.warn("No controller hosts found to notify creation of account " + accountId);
      return;
    }

    String authToken = KeystoneHelper.getInstance().getCSAToken();
    PrimaryDomain primaryDomain = new PrimaryDomain();
    primaryDomain.setPrimaryDomainId(primaryDomainInserted.getId());

    // Convert response to JSON
    String respInJson = JsonHelper.serializeToJson(primaryDomain);
    AppLogger
        .info("Sending create account notification to C3 for account " + accountId + " JSON request " + respInJson);
    Map<String, ClientResponse<String>> clientResponses =
        ControllerClientHelper.getInstance().notifyCreateAccount(authToken, respInJson, accountId,
            listOfControllerHosts);

    for (Entry<String, ClientResponse<String>> entry : clientResponses.entrySet()) {

      if (entry.getValue().getStatus() == ClientStatus.SUCCESS) {
        AppLogger.info("Create account notification sent successfully to c3 " + entry.getKey() + " for account "
            + accountId);
      } else {
        AppLogger.warn("An error occurred while sending create account notification to " + entry.getKey()
            + " response status " + entry.getValue().getStatus() + " status code "
            + entry.getValue().getHttpResponse().getStatusCode() + " for resource " + accountId);
      }
    }
  }

  private static AccountProtocol.ValidateIdpResponse validateIdpConfig(IdpConfig idpConfig, boolean syntaxValidate) {
    AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
    Hashtable<String, String> env = new Hashtable<String, String>();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put(Context.PROVIDER_URL, idpConfig.getUrl());
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.SECURITY_PRINCIPAL, idpConfig.getUserBindDn());
    env.put(Context.SECURITY_CREDENTIALS, idpConfig.getUserBindPwd());
    ValidateIdpDetails validateIdp = new ValidateIdpDetails();

    //validate ldap parameters
    if(syntaxValidate == true){
      //validate ldap user_bind_dn
      response = validateLdapDn(idpConfig.getUserBindDn());
      if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
    	response.getValidateIdpDetails().setField_name("user_bind_dn");
    	return response;  
      }
      // validate ldap user_tree_dn
      response = validateLdapDn(idpConfig.getUserTreeDn());
      if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
    	response.getValidateIdpDetails().setField_name("user_tree_dn");
    	return response;
      }
      //validate ldap group_tree_dn
      response = validateLdapDn(idpConfig.getGroupTreeDn());
      if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
    	response.getValidateIdpDetails().setField_name("group_tree_dn");
    	return response;
      }
      //validate ldap user_filter
      response = validateLdapFilter(idpConfig.getUserFilter());
      if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
    	response.getValidateIdpDetails().setField_name("user_filter");
    	return response;  
      }
      //validate ldap group_filter
    response = validateLdapFilter(idpConfig.getGroupFilter());
    if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
  	response.getValidateIdpDetails().setField_name("group_filter");
  	return response;  
    }
    //validate ldap user_class_name
    response = validateLdapObject(idpConfig.getUserClassName());
    if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
  	response.getValidateIdpDetails().setField_name("user_class_name");
  	return response;  
    }
    //validate ldap group_class_name
    response = validateLdapObject(idpConfig.getGroupClassName());
    if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
  	response.getValidateIdpDetails().setField_name("group_class_name");
  	return response;  
    }
    //validate ldap user_name_attribute
    response = validateLdapAttribute(idpConfig.getUserNameAttribute());
    if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
  	response.getValidateIdpDetails().setField_name("user_name_attribute");
  	return response;  
    }
    //validate ldap group_name_attribute
    response = validateLdapAttribute(idpConfig.getGroupNameAttribute());
    if(response.getValidateIdpDetails().getCode().equalsIgnoreCase(IDP_SYNTAX_ERROR)){
  	response.getValidateIdpDetails().setField_name("group_name_attribute");
  	return response;  
    }
    
  }  
    try {
      DirContext ctx = new InitialDirContext(env);
      AppLogger.info("Connected to ldap Server");
      ctx.close();
    } catch (AuthenticationException ex) {
      validateIdp.setCode(IDP_AUTHENTICATION_ERROR);
      validateIdp.setMessage("IDP authentication error");
      validateIdp.setDescription("Failed to authenticate the bind user: " + idpConfig.getUserBindDn() + " to LDAP server at: "  + idpConfig.getUrl());
      validateIdp.setField_name("user_bind_dn, user_bind_password");
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      response.setValidateIdpDetails(validateIdp);
      return response;
    } catch (CommunicationException ex) {
      validateIdp.setCode(IDP_CONNECTION_ERROR);
      validateIdp.setMessage("IDP connection error");
      validateIdp.setDescription("Failed to connect with the LDAP server at: " + idpConfig.getUrl());
      validateIdp.setField_name("url");
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      response.setValidateIdpDetails(validateIdp);
      return response;
    } catch (NamingException ex) {
      validateIdp.setCode(IDP_INTERNAL_ERROR);
      validateIdp.setMessage("IDP internal error");
      validateIdp.setDescription("An internel error occured while connecting to LDAP server at: " + idpConfig.getUrl());
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      response.setValidateIdpDetails(validateIdp);
      return response;
    }
    response.setStatus(AccountProtocol.Status.SUCCESS_OK);
    validateIdp.setCode(IDP_SUCCESS);
    response.setValidateIdpDetails(validateIdp);
    return response;
  }

  private static IdpConfig getIdpConfFromDomainConf(DomainConfigInfo domainConfigInfo, IdpType idpType, String idpPwd) {
    IdpConfig config = new IdpConfig();
    LdapConfig ldapConfig = domainConfigInfo.getLdapConfig();
    if (idpType.equals(IdpType.LDAP)) {
      config.setUrl(ldapConfig.getUrl());
      config.setUserBindDn(ldapConfig.getUser());
      config.setUserBindPwd(idpPwd);
      config.setUserClassName(ldapConfig.getUserObjectClass());
      config.setGroupClassName(ldapConfig.getGroupObjectClass());
      config.setUserTreeDn(ldapConfig.getUserTreeDn());
      config.setGroupTreeDn(ldapConfig.getGroupTreeDn());
      config.setGroupFilter(ldapConfig.getGroupFilter());
      config.setGroupNameAttribute(ldapConfig.getGroupNameAttribute());
      config.setUserFilter(ldapConfig.getUserFilter());
      config.setUserNameAttribute(ldapConfig.getUserNameAttribute());
      config.setQueryScope(com.emc.caspian.ccs.account.types.QueryScope.valueOf(ldapConfig.getQueryScope().name()));
    }
    return config;
  }


  private static DomainConfigInfo getDomainConfFromIdpConf(IdpConfig idpConfig, IdpType idpType, String validate) {
    DomainConfigInfo domainConfigInfo = new DomainConfigInfo();
    if (idpType.equals(IdpType.LDAP)) {
      // Validate LDAP URL
      Validator.validateUrl(idpConfig.getUrl(), new String[] {LDAP_SCHEME, LDAPS_SCHEME});
      
      String idpUserFilter;
      if (idpConfig.getUserFilter() != null && idpConfig.getUserFilter().length() > 0) 
        if (idpConfig.getUserFilter().charAt(0) != '('
            && idpConfig.getUserFilter().charAt(idpConfig.getUserFilter().length() - 1) != ')')
          idpUserFilter = "(" + idpConfig.getUserFilter() + ")";
        else if (idpConfig.getUserFilter().charAt(0) != '(')
          idpUserFilter = "(" + idpConfig.getUserFilter();
        else if (idpConfig.getUserFilter().charAt(idpConfig.getUserFilter().length() - 1) != ')')
          idpUserFilter = idpConfig.getUserFilter() + ")";
        else
          idpUserFilter = idpConfig.getUserFilter();
      else
        idpUserFilter = idpConfig.getUserFilter();

      String idpGroupFilter;
      if (idpConfig.getGroupFilter() != null && idpConfig.getGroupFilter().length() > 0) 
        if (idpConfig.getGroupFilter().charAt(0) != '('
            && idpConfig.getGroupFilter().charAt(idpConfig.getGroupFilter().length() - 1) != ')')
          idpGroupFilter = "(" + idpConfig.getGroupFilter() + ")";
        else if (idpConfig.getGroupFilter().charAt(0) != '(')
          idpGroupFilter = "(" + idpConfig.getGroupFilter();
        else if (idpConfig.getGroupFilter().charAt(idpConfig.getGroupFilter().length() - 1) != ')')
          idpGroupFilter = idpConfig.getGroupFilter() + ")";
        else
          idpGroupFilter = idpConfig.getGroupFilter();
      else
        idpGroupFilter = idpConfig.getGroupFilter();

      IdentityDriver identityDriver = new IdentityDriver();
      identityDriver.setDriver(IDP_LDAP_DRIVER);
      LdapConfig ldapConfig = new LdapConfig();
      ldapConfig.setUrl(idpConfig.getUrl());
      ldapConfig.setUser(idpConfig.getUserBindDn());
      ldapConfig.setPassword(idpConfig.getUserBindPwd());
      ldapConfig.setUserTreeDn(idpConfig.getUserTreeDn());
      ldapConfig.setGroupTreeDn(idpConfig.getGroupTreeDn());
      ldapConfig.setUserFilter(idpUserFilter);
      ldapConfig.setGroupFilter(idpGroupFilter);
      ldapConfig.setUserObjectClass(idpConfig.getUserClassName());
      ldapConfig.setGroupObjectClass(idpConfig.getGroupClassName());
      ldapConfig.setUserNameAttribute(idpConfig.getUserNameAttribute());
      ldapConfig.setGroupNameAttribute(idpConfig.getGroupNameAttribute());
      ldapConfig.setQueryScope(QueryScope.valueOf(idpConfig.getQueryScope().name()));
      domainConfigInfo.setIdentityDriver(identityDriver);
      domainConfigInfo.setLdapConfig(ldapConfig);
    }
    return domainConfigInfo;
  }
  private static AccountProtocol.ValidateIdpResponse validateLdapDn(String ldapDn)
  {
    AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
    ValidateIdpDetails validateIdp = new ValidateIdpDetails();
    try{
      new LdapName(ldapDn);
    }
    catch(InvalidNameException e){
      response.setStatus(AccountProtocol.Status.SUCCESS_OK);
      validateIdp.setCode(IDP_SYNTAX_ERROR);
      validateIdp.setMessage("Syntax error");
      validateIdp.setDescription("This parameter doesn't meet the syntax requirements: " + ldapDn );
      response.setValidateIdpDetails(validateIdp);
      return response;
    }
    response.setStatus(AccountProtocol.Status.SUCCESS_OK);
    validateIdp.setCode(IDP_SUCCESS);
    response.setValidateIdpDetails(validateIdp);
    return response;
  }

	private static AccountProtocol.ValidateIdpResponse validateLdapFilter(String ldapFilter) {
		AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
		ValidateIdpDetails validateIdp = new ValidateIdpDetails();
		if ((ldapFilter == null) || (ldapFilter.length() == 0)) {
			response.setStatus(AccountProtocol.Status.SUCCESS_OK);
			validateIdp.setCode(IDP_SUCCESS);
			response.setValidateIdpDetails(validateIdp);
			return response;
		}

		try {
			Filter.create(ldapFilter);
		} catch (LDAPException e) {
			response.setStatus(AccountProtocol.Status.SUCCESS_OK);
			validateIdp.setCode(IDP_SYNTAX_ERROR);
			validateIdp.setMessage("Syntax error");
			validateIdp.setDescription("This parameter doesn't meet the syntax requirements: " + ldapFilter);
			response.setValidateIdpDetails(validateIdp);
			return response;
		}
		response.setStatus(AccountProtocol.Status.SUCCESS_OK);
		validateIdp.setCode(IDP_SUCCESS);
		response.setValidateIdpDetails(validateIdp);
		return response;
	}

	private static AccountProtocol.ValidateIdpResponse validateLdapObject(String ldapObject) {
		AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
		ValidateIdpDetails validateIdp = new ValidateIdpDetails();
		if (ldapObject == null || (ldapObject.length() == 0)) {
			response.setStatus(AccountProtocol.Status.SUCCESS_OK);
			validateIdp.setCode(IDP_SUCCESS);
			response.setValidateIdpDetails(validateIdp);
			return response;
		}
		int i = 0, length;
		length = ldapObject.length();
		// oid:=descr
		if ((ldapObject.charAt(0) >= 65 && ldapObject.charAt(0) <= 90)
				|| (ldapObject.charAt(0) >= 97 && ldapObject.charAt(0) <= 122)) {

			while (i < length) {
				if (!(((ldapObject.charAt(i) >= 65 && ldapObject.charAt(i) <= 90)
						|| (ldapObject.charAt(i) >= 97 && ldapObject.charAt(i) <= 122)
						|| (ldapObject.charAt(i) >= 48 && ldapObject.charAt(i) <= 57) || ldapObject.charAt(i) == 45)))

				{
					response.setStatus(AccountProtocol.Status.SUCCESS_OK);
					validateIdp.setCode(IDP_SYNTAX_ERROR);
					validateIdp.setMessage("Syntax error");
					validateIdp.setDescription(
							"This LDAP Object parameter doesn't meet the syntax requirements: " + ldapObject);
					response.setValidateIdpDetails(validateIdp);
					return response;
				}
				i++;
			}
		}
		// oid:=numericoid
		else if (!(ldapObject.matches("\\d+(\\.\\d+)*"))) {
			response.setStatus(AccountProtocol.Status.SUCCESS_OK);
			validateIdp.setCode(IDP_SYNTAX_ERROR);
			validateIdp.setMessage("Syntax error");
			validateIdp.setDescription("This parameter doesn't meet the syntax requirements: " + ldapObject);
			response.setValidateIdpDetails(validateIdp);
			return response;
		}
		response.setStatus(AccountProtocol.Status.SUCCESS_OK);
		validateIdp.setCode(IDP_SUCCESS);
		response.setValidateIdpDetails(validateIdp);
		return response;
	}

	private static AccountProtocol.ValidateIdpResponse validateLdapAttribute(String distName) {
		AccountProtocol.ValidateIdpResponse response = new AccountProtocol.ValidateIdpResponse();
		ValidateIdpDetails validateIdp = new ValidateIdpDetails();
		if (distName == null || (distName.length() == 0)) {
			response.setStatus(AccountProtocol.Status.SUCCESS_OK);
			validateIdp.setCode(IDP_SYNTAX_ERROR);
			validateIdp.setMessage(distName + "is NULL");
			validateIdp.setDescription("This parameter doesn't meet the syntax requirements: " + distName);
			response.setValidateIdpDetails(validateIdp);
			return response;
		} else {
			int i = 0, count = 0, distLength = distName.length();
			while (i < distLength) {
				if (distName.charAt(i) == ',') {
					count++;
				}
				i++;
			}
			if (count == 0) {
				i = 0;
				if ((distName.charAt(i) >= 65 && distName.charAt(i) <= 90)
						|| (distName.charAt(i) >= 97 && distName.charAt(i) <= 122)) {
					i += 1;
					while (i < distLength) {
						if (!((distName.charAt(i) >= 65 && distName.charAt(i) <= 90)
								|| (distName.charAt(i) >= 97 && distName.charAt(i) <= 122)
								|| (distName.charAt(i) >= 48 && distName.charAt(i) <= 57)
								|| (distName.charAt(i) == 45))) {
							response.setStatus(AccountProtocol.Status.SUCCESS_OK);
							validateIdp.setCode(IDP_SYNTAX_ERROR);
							validateIdp.setMessage("Syntax error");
							validateIdp
									.setDescription("This parameter doesn't meet the syntax requirements: " + distName);
							response.setValidateIdpDetails(validateIdp);
							return response;
						}
						i++;
					}
				} else if (!(distName.matches("\\d+(\\.\\d+)*"))) {
					response.setStatus(AccountProtocol.Status.SUCCESS_OK);
					validateIdp.setCode(IDP_SYNTAX_ERROR);
					validateIdp.setMessage("Syntax error");
					validateIdp.setDescription("This parameter doesn't meet the syntax requirements: " + distName);
					response.setValidateIdpDetails(validateIdp);
					return response;
				}
				response.setStatus(AccountProtocol.Status.SUCCESS_OK);
				validateIdp.setCode(IDP_SUCCESS);
				response.setValidateIdpDetails(validateIdp);
				return response;
			}
			String[] rdList = distName.trim().split("\\s*,\\s*");
			int j;
			i = 0;
			for (i = 0; i < (rdList.length); i++) {
				String[] attList = rdList[i].trim().split("\\s*+\\s*");
				for (j = 0; j < (attList.length); j++) {
					String[] att = attList[j].trim().split("\\s*=\\s*");
					String attType = att[0];
					String attVal = att[1];
					if (!(checkForAttributeType(attType, attVal))) {
						response.setStatus(AccountProtocol.Status.SUCCESS_OK);
						validateIdp.setCode(IDP_SYNTAX_ERROR);
						validateIdp.setMessage("Syntax error");
						validateIdp.setDescription(
								"This LDAP attribute parameter doesn't meet the syntax requirements: " + distName);
						response.setValidateIdpDetails(validateIdp);
						return response;
					}
				}
			}
		}
		response.setStatus(AccountProtocol.Status.SUCCESS_OK);
		validateIdp.setCode(IDP_SUCCESS);
		response.setValidateIdpDetails(validateIdp);
		return response;
	}

	private static boolean checkForAttributeType(String attType, String attVal) {
		if (attType.matches("\\d+(\\.\\d+)*")) // attType:=numericoid;
												// numericoid:=dotted decimal
												// form
		{
			if (!(attVal.charAt(0) == '#')) // attVal=hexString;
											// hexString=SHARP(1*hexPair);
											// SHARP=#; hexpair=HEXHEX ;
											// HEX="0"-"9"/"A"-"F"/"a"-"f";
			{
				return false;
			}
			String val = attVal.substring(1);
			int i = 0, k;
			int[] arrayForHexPair = new int[16];
			for (k = 0; k < 16; k++) {

				arrayForHexPair[k] = 0;
			}
			while ((val.charAt(i) >= 48 && val.charAt(i) <= 57) || (attType.charAt(i) >= 65 && attType.charAt(i) <= 70)
					|| (attType.charAt(i) >= 97 && attType.charAt(i) <= 102)) {
				if (val.charAt(i) >= 48 && val.charAt(i) <= 57) // for
																// [0-9]
				{
					if (arrayForHexPair[val.charAt(i) - 48] == 0) {
						arrayForHexPair[val.charAt(i) - 48]++;
					} else {
						arrayForHexPair[val.charAt(i) - 48]--;
					}
				} else if (attType.charAt(i) >= 65 && attType.charAt(i) <= 70) // for
																				// [A-F]
				{
					if (arrayForHexPair[val.charAt(i) - 65 + 10] == 0) {
						arrayForHexPair[val.charAt(i) - 65 + 10]++;
					} else {
						arrayForHexPair[val.charAt(i) - 65 + 10]--;
					}
				} else // for a-f
				{
					if (arrayForHexPair[val.charAt(i) - 97 + 10] == 0) {
						arrayForHexPair[val.charAt(i) - 97 + 10]++;
					} else {
						arrayForHexPair[val.charAt(i) - 97 + 10]--;
					}
				}
				i++;
			}
			for (k = 0; k < 16; k++) {
				if (arrayForHexPair[k] != 0) {
					return false;
				}
			}
		} else if ((attType.charAt(0) >= 65 && attType.charAt(0) <= 90)
				|| (attType.charAt(0) >= 97 && attType.charAt(0) <= 122))
		// attType:=descr; descr:=keystring; keystring:=leadkeychar*keychar;
		// leadkeychar=ALPHA; keychar=ALPHA/DIGIT/HYPHEN
		{
			int i = 0;

			while (attType.charAt(i) != '\0') // checking if attributeType is
												// valid(a-z or A-Z or 0-9 or
												// hyphen)
			{
				if (!((attType.charAt(i) >= 65 && attType.charAt(i) <= 90)
						|| (attType.charAt(i) >= 97 && attType.charAt(i) <= 122)
						|| (attType.charAt(i) >= 48 && attType.charAt(i) <= 57) || attType.charAt(i) == 45)) {
					return false;
				}
				i++;
			}

			// now checking begins for attributevalue if attributeType syntax is
			// correct
			// attributeValue=string; string=[( leadchar/ pair ) [ *(
			// stringchar / pair )( trailchar / pair ) ] ];
			// checking for
			// string:=pair;
			// pair = ESC (
			// ESC / special
			// /
			// hexpair
			// );special =
			// escaped /
			// SPACE / SHARP
			// / EQUALS;
			// escaped
			// = DQUOTE /
			// PLUS / COMMA
			// / SEMI /
			// LANGLE /
			// RANGLE

			// checking for attribute type
			// checking for string=[( leadchar/ pair ); leadchar = LUTF1 /
			// UTFMB;
			i = 0;
			int hex_flag = 0; // checking for hex pair is done when hex_flag is
								// set
			while (attVal.charAt(i) != '\0') {
				if ((i == 0) && !((attVal.charAt(i) >= 1 && attVal.charAt(i) <= 31) || (attVal.charAt(i) == 33)
						|| (attVal.charAt(i) >= 36 && attVal.charAt(i) <= 42)
						|| (attVal.charAt(i) >= 45 && attVal.charAt(i) <= 58) || (attVal.charAt(i) == 61)
						|| (attVal.charAt(i) >= 63 && attVal.charAt(i) <= 91)
						|| (attVal.charAt(i) >= 93 && attVal.charAt(i) <= 127))) {

					if (attVal.charAt(i) == 92 && !(attVal.charAt(i + 1) == 34 || attVal.charAt(i + 1) == 43
							|| attVal.charAt(i + 1) == 44 || attVal.charAt(i + 1) == 60 || attVal.charAt(i + 1) == 62
							|| attVal.charAt(i + 1) == 59 || attVal.charAt(i + 1) == 32 || attVal.charAt(i + 1) == 35
							|| attVal.charAt(i + 1) == 61))
						hex_flag = 1;
				}
				// checking for stringchar
				else if ((i != 0) && (!((attVal.charAt(i) >= 1 && attVal.charAt(i) <= 33)
						|| (attVal.charAt(i) >= 35 && attVal.charAt(i) <= 42)
						|| (attVal.charAt(i) >= 45 && attVal.charAt(i) <= 58) || (attVal.charAt(i) == 61)
						|| (attVal.charAt(i) >= 63 && attVal.charAt(i) <= 91)
						|| (attVal.charAt(i) >= 93 && attVal.charAt(i) <= 127)))) {
					if (attVal.charAt(i) == 92 && !(attVal.charAt(i + 1) == 34 || attVal.charAt(i + 1) == 43
							|| attVal.charAt(i + 1) == 44 || attVal.charAt(i + 1) == 60 || attVal.charAt(i + 1) == 62
							|| attVal.charAt(i + 1) == 59 || attVal.charAt(i + 1) == 32 || attVal.charAt(i + 1) == 35
							|| attVal.charAt(i + 1) == 61))
						hex_flag = 1;
				}
				// checking for for trailchar
				else if ((i != 0) && (!((attVal.charAt(i) >= 1 && attVal.charAt(i) <= 31) || (attVal.charAt(i) == 33)
						|| (attVal.charAt(i) >= 35 && attVal.charAt(i) <= 42)
						|| (attVal.charAt(i) >= 45 && attVal.charAt(i) <= 58) || (attVal.charAt(i) == 61)
						|| (attVal.charAt(i) >= 63 && attVal.charAt(i) <= 91)
						|| (attVal.charAt(i) >= 93 && attVal.charAt(i) <= 127)))) {
					if (attVal.charAt(i) == 92 && !(attVal.charAt(i + 1) == 34 || attVal.charAt(i + 1) == 43
							|| attVal.charAt(i + 1) == 44 || attVal.charAt(i + 1) == 60 || attVal.charAt(i + 1) == 62
							|| attVal.charAt(i + 1) == 59 || attVal.charAt(i + 1) == 32 || attVal.charAt(i + 1) == 35
							|| attVal.charAt(i + 1) == 61))
						hex_flag = 1;
				} else if ((hex_flag == 1) && ((attVal.charAt(i + 1) >= 48 && attVal.charAt(i + 1) <= 57)
						|| (attType.charAt(i + 1) >= 65 && attType.charAt(i + 1) <= 70)
						|| (attType.charAt(i + 1) >= 97 && attType.charAt(i + 1) <= 102))) {

					// checking for pair=hexpair
					int[] arrayForHexPair = new int[16];
					int k;
					for (k = 0; k < 16; k++) {
						arrayForHexPair[k] = 0;
					}

					while ((attVal.charAt(i + 1) >= 48 && attVal.charAt(i + 1) <= 57)
							|| (attType.charAt(i + 1) >= 65 && attType.charAt(i + 1) <= 70)
							|| (attType.charAt(i + 1) >= 97 && attType.charAt(i + 1) <= 102)) {
						i += 1;
						if (attVal.charAt(i) >= 48 && attVal.charAt(i) <= 57) // for
																				// [0-9]
						{
							if (arrayForHexPair[attVal.charAt(i) - 48] == 0) {
								arrayForHexPair[attVal.charAt(i) - 48]++;
							} else {
								arrayForHexPair[attVal.charAt(i) - 48]--;
							}
						} else if (attType.charAt(i) >= 65 && attType.charAt(i) <= 70) // for
																						// [A-F]
						{
							if (arrayForHexPair[attVal.charAt(i) - 65 + 10] == 0) {
								arrayForHexPair[attVal.charAt(i) - 65 + 10]++;
							} else {
								arrayForHexPair[attVal.charAt(i) - 65 + 10]--;
							}
						} else // for a-f
						{
							if (arrayForHexPair[attVal.charAt(i) - 97 + 10] == 0) {
								arrayForHexPair[attVal.charAt(i) - 97 + 10]++;
							} else {
								arrayForHexPair[attVal.charAt(i) - 97 + 10]--;
							}
						}

					}
					for (k = 0; k < 16; k++) {
						if (arrayForHexPair[k] != 0) {
							return false;
						}
					}
					hex_flag = 0;
				} else {
					return false;
				}
				i++;
			}

		} else {
			return false;
		}

		return true;
	}
  /**
   * Check if the given account exists and is active
   *
   * @param accountId
   * @return true if account exists and is active, false if account exists and is not active, null if account does not
   *         exist
   */
  private static ConditionResponse accountExistsAndActive(String accountId) {
    ConditionResponse conditionResponse = new ConditionResponse();
    DbResponse<AccountModel> accountDetails = TableFactory.getAccountTable().getAccount(accountId);

    // If account exists and is active
    if (accountDetails.getResponseObj() != null
        && accountDetails.getResponseObj().getState().equals(ACCOUNT_ACTIVE_STATUS)) {
      AppLogger.debug("Account " + accountId + " exists and is active");
      conditionResponse.setStatus(AccountProtocol.Status.SUCCESS_OK);
      conditionResponse.setSuccess(true);

      return conditionResponse;
    }
    // Account exists and is not active
    else if (accountDetails.getResponseObj() != null
        && !accountDetails.getResponseObj().getState().equals(ACCOUNT_ACTIVE_STATUS)) {
      AppLogger.warn("Account " + accountId + " is not active");
      conditionResponse.setResponseMessage(ResponseErrorMessage.ACCOUNT_NOT_ACTIVE);
      conditionResponse.setStatus(AccountProtocol.Status.PRECONDITION_FAILED);
      conditionResponse.setSuccess(false);

      return conditionResponse;
    }
    // Account does not exist
    else if (accountDetails.getErrorCode() == ErrorCode.DB_RECORD_NOT_FOUND) {
      AppLogger.warn("Account " + accountId + " does not exist");
      conditionResponse.setResponseMessage(ResponseErrorMessage.ACCOUNT_NOT_FOUND);
      conditionResponse.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
      conditionResponse.setSuccess(false);

      return conditionResponse;
    }
    // Any other error
    else {
      AppLogger.warn("An error encountered while fetching account " + accountId + " from database");
      conditionResponse.setResponseMessage(accountDetails.getErrorMessage());
      conditionResponse.setStatus(ErrorCodeToProtocolStatusMapper.convert(accountDetails.getErrorCode()));
      conditionResponse.setSuccess(false);

      return conditionResponse;
    }
  }

  private static ConditionResponse domainExistsPreconditionPassed(String domainId) {
    ConditionResponse conditionResponse = new ConditionResponse();
    ClientResponse<Domain> domain = KeystoneHelper.getInstance().getDomain(domainId);
    if (domain == null) {
      AppLogger.warn("Received null response from getDomain for domain %s", domainId);
      conditionResponse.setSuccess(false);
      conditionResponse.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
      conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
      return conditionResponse;
    }
    if (domain.getStatus() != ClientStatus.SUCCESS) {
      if (domain.getStatus() == ClientStatus.ERROR_HTTP && domain.getHttpResponse().getStatusCode() == 404) {
        AppLogger.warn("Domain %s does not exist in Keystone", domainId);
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        conditionResponse.setResponseMessage(String.format(ResponseErrorMessage.DOMAIN_NOT_FOUND, domainId));
        return conditionResponse;
      } else {
        AppLogger.warn("Error while fetching domain %s from keystone", domainId);
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
        return conditionResponse;
      }
    } else {
      AppLogger.debug("Domain %s retrieved successfully", domainId);
      conditionResponse.setSuccess(true);
      return conditionResponse;
    }
  }



  private static ConditionResponse isAccountActivePreconditionPassed(String domainId) {
    ConditionResponse conditionResponse = new ConditionResponse();

    DbResponse<AccountModel> accountDetails = TableFactory.getAccountTable().getAccount(domainId);
    if (accountDetails == null || (accountDetails.getResponseObj() == null)){
      if(accountDetails.getErrorCode().equals(ErrorCode.DB_RECORD_NOT_FOUND)) {
        AppLogger.warn("An error encountered while fetching the account %s: account not found", domainId);
        conditionResponse.setSuccess(false);
        conditionResponse.setResponseMessage(accountDetails.getErrorMessage());
        conditionResponse.setStatus(ErrorCodeToProtocolStatusMapper.convert(accountDetails.getErrorCode()));
        } 
      else {
        AppLogger.warn("An error encountered while fetching the account %s: %s", domainId, accountDetails.getErrorMessage());
        conditionResponse.setSuccess(false);
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
        }
      return conditionResponse;
    }
    else if (!accountDetails.getResponseObj().getState().equals(ACCOUNT_ACTIVE_STATUS) ) {
      AppLogger.warn("Account %s is not active", domainId);
      conditionResponse.setSuccess(false);
      conditionResponse.setStatus(AccountProtocol.Status.ERROR_BAD_REQUEST);
      conditionResponse.setResponseMessage(ResponseErrorMessage.ACCOUNT_NOT_ACTIVE);
      return conditionResponse;
    } else {
      AppLogger.debug("Account %s is active", domainId);
      conditionResponse.setSuccess(true);
      return conditionResponse;
    }

  }



  private static ConditionResponse idpExistsPreconditionPassed(String domainId) {
    ConditionResponse conditionResponse = new ConditionResponse();
    ClientResponse<DomainConfig> getDomainIdp = KeystoneHelper.getInstance().getDomainIdp(domainId);
    if (getDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If an IDP was not found for this domain
      if (getDomainIdp.getHttpResponse().getStatusCode() == 404) {
        AppLogger.warn("IDP not found for domain %s", domainId);
        conditionResponse.setSuccess(false);
        conditionResponse.setResponseMessage(String.format(ResponseErrorMessage.DOMAIN_IDP_NOT_FOUND, domainId));
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_NOT_FOUND);
        return conditionResponse;
      }
      // If the domain's config could not be fetched
      else {
        AppLogger.warn("Error while fetching domain config for domain %s from keystone", domainId);
        conditionResponse.setSuccess(false);
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
        return conditionResponse;
      }
    } else {
      AppLogger.debug("IDP exists for domain %s", domainId);
      conditionResponse.setSuccess(true);
      return conditionResponse;
    }
  }


  private static ConditionResponse idpDoesNotExistPreconditionPassed(String domainId) {
    ConditionResponse conditionResponse = new ConditionResponse();
    ClientResponse<DomainConfig> getDomainIdp = KeystoneHelper.getInstance().getDomainIdp(domainId);

    if (getDomainIdp.getStatus() != ClientStatus.SUCCESS) {
      // If the domain config does not exist
      if (getDomainIdp.getHttpResponse().getStatusCode() == 404) {
        AppLogger.debug("IDP does not exist for domain %s", domainId);
        conditionResponse.setSuccess(true);
        return conditionResponse;
      } else {
        AppLogger.warn("Error while fetching domain config for domain %s from keystone", domainId);
        conditionResponse.setSuccess(false);
        conditionResponse.setStatus(AccountProtocol.Status.ERROR_INTERNAL);
        conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
        return conditionResponse;
      }
    } else {
      // Something went wrong while fetching domain config
      AppLogger.warn("IDP already exists for the domain %s", domainId);
      conditionResponse.setSuccess(false);
      conditionResponse.setStatus(Status.ERROR_BAD_REQUEST);
      conditionResponse.setResponseMessage(String.format(ResponseErrorMessage.DOMAIN_IDP_EXISTS, domainId));
      return conditionResponse;
    }
  }


  private static ConditionResponse usersGroupsPreconditionPassed(String domainId) {
    ConditionResponse conditionResponse = new ConditionResponse();
    Users users = KeystoneHelper.getInstance().getUsers(domainId);
    // Get all groups for this domain
    Groups groups = KeystoneHelper.getInstance().getGroups(domainId);
    if (users == null || groups == null) {
      AppLogger.warn("Failed to retrieve users/groups for domain %s", domainId);
      conditionResponse.setSuccess(false);
      conditionResponse.setStatus(Status.ERROR_INTERNAL);
      conditionResponse.setResponseMessage(ResponseErrorMessage.INTERNAL_ERROR);
      return conditionResponse;
    }

    // If any users or groups exist for the domain, give a
    // precondition failed error
    if (!users.getList().isEmpty() || !groups.getList().isEmpty()) {
      AppLogger.warn("Failed to create IDP, users/groups already exist for domain %s", domainId);
      conditionResponse.setSuccess(false);
      conditionResponse.setStatus(Status.ERROR_BAD_REQUEST);
      conditionResponse.setResponseMessage(ResponseErrorMessage.USERS_GROUPS_ALREADY_EXIST);
      return conditionResponse;
    }
    // No users/groups exist for this domain
    conditionResponse.setSuccess(true);
    AppLogger.debug("No groups/users exist in domain " + domainId);
    return conditionResponse;
  }
}


class ConditionResponse {
  private Status status;
  private String responseMessage;
  private boolean success;

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getResponseMessage() {
    return responseMessage;
  }

  public void setResponseMessage(String responseMessage) {
    this.responseMessage = responseMessage;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean response) {
    this.success = response;
  }
}
