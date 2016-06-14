/*
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 * 
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.account.controller;

/**
 * Copyright (c) 2014 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

/**
 * Defines the protocol for the REST responses
 * 
 * @author raod4
 *
 */

import java.util.List;

import com.emc.caspian.ccs.account.datacontract.Account;
import com.emc.caspian.ccs.account.datacontract.AccountDomain;
import com.emc.caspian.ccs.account.datacontract.AccountDomainDetails;
import com.emc.caspian.ccs.account.datacontract.AccountRequest;
import com.emc.caspian.ccs.account.datacontract.Domain;
import com.emc.caspian.ccs.account.datacontract.Idp;
import com.emc.caspian.ccs.account.datacontract.IdpRequest;
import com.emc.caspian.ccs.account.datacontract.ValidateIdpDetails;
import com.emc.caspian.ccs.account.datacontract.WorkflowTask;

import java.util.Map;

/**
 * Protocol for request and response for account APIs
 * 
 * @author raod4
 *
 */
public final class AccountProtocol {

  public enum Status {
    SUCCESS_OK,
    SUCCESS_CREATED,
    SUCCESS_NO_CONTENT,
    ERROR_INTERNAL,
    ERROR_BAD_REQUEST,
    ERROR_BAD_REQUEST_WITH_RESPONSE,
    ERROR_UNAUTHORIZED,
    ERROR_CONFLICT,
    ERROR_NOT_FOUND,
    ERROR_DELETE_FAILED,
    NOT_IMPLEMENTED,
    ERROR_UNKNOWN,
    SUCCESS_ACCEPTED,
    PRECONDITION_FAILED;
  }

  public static enum RequestType {
    // all apis will have a type represented here
    GET_ACCOUNTS,
    GET_ACCOUNT,
    UPDATE_ACCOUNT,
    CREATE_ACCOUNT,
    DELETE_ACCOUNT,
    ADD_DOMAIN,
    GET_ACCOUNT_DOMAIN,
    GET_ALL_DOMAINS,
    GET_ACCOUNT_FROM_DOMAIN,
    DELETE_DOMAIN,
    ELECT_PRIMARY_DOMAIN,
    CREATE_DOMAIN,
    UPDATE_DOMAIN,
    SYNCHRONIZE_ROLES,
    GET_IDP_FROM_DOMAIN,
    CREATE_IDP_FOR_DOMAIN,
    LIST_IDP,
    UPDATE_IDP,
    GET_IDP,
    VALIDATE_IDP,
    DELETE_IDP,
    GET_IDP_TASK,
    GET_TASK_STATUS
  }

  public static class Request {
    RequestType requestType;

    protected Request(final RequestType requestType) {
      this.requestType = requestType;
    }

    public RequestType getRequestType() {
      return requestType;
    }
  }

  public static class Response {
    private Status status;
    private String responseMessage;
    private Map<String, String> responseHeaders;

    public Status getStatus() {
      return status;
    }

    public void setStatus(final Status status) {
      this.status = status;
    }

    public String getRespMsg() {
      return responseMessage;
    }

    public Map<String, String> getResponseHeaders() {
      return responseHeaders;
    }

    public void setResponseHeaders(Map<String, String> responseHeaders) {
      this.responseHeaders = responseHeaders;
    }

    public void setRespMsg(String respMsg) {
      this.responseMessage = respMsg;
    }
  }

  /**
   * GetAccounstsRequest represents request for accounts
   */
  public static class GetAccountsRequest extends Request {
    public GetAccountsRequest() {
      super(RequestType.GET_ACCOUNTS);
    }
  }

  public static class GetAccountsResponse extends Response {
    private List<Account> accounts;

    public List<Account> getAccounts() {
      return accounts;
    }

    public void setAccounts(final List<Account> accounts) {
      this.accounts = accounts;
    }
  }

  public static class GetAccountRequest extends Request {
    public GetAccountRequest() {
      super(RequestType.GET_ACCOUNT);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    private String accountId;
  }

  public static class GetAccountResponse extends Response {
    private Account accountDetails;

    public Account getAccount() {
      return accountDetails;
    }

    public void setAccount(final Account account) {
      this.accountDetails = account;
    }
  }

  public static class UpdateAccountRequest extends Request {
    private AccountRequest account;
    private String accountId;

    public UpdateAccountRequest() {
      super(RequestType.UPDATE_ACCOUNT);
    }

    public void setAccount(final AccountRequest account) {
      this.account = account;
    }

    public AccountRequest getAccount() {
      return account;
    }

    public void setId(final String id) {
      this.accountId = id;
    }

    public String getId() {
      return accountId;
    }
  }

  public static class UpdateAccountResponse extends Response {
    public Account getAccount() {
      return account;
    }

    public void setAccount(final Account account) {
      this.account = account;
    }

    private Account account;
  }

  public static class CreateAccountRequest extends Request {
    private AccountRequest account;

    public CreateAccountRequest() {
      super(RequestType.CREATE_ACCOUNT);
    }

    public void setAccount(final AccountRequest account) {
      this.account = account;
    }

    public AccountRequest getAccount() {
      return account;
    }
  }

  public static class CreateAccountResponse extends Response {
    public Account getAccount() {
      return account;
    }

    public void setAccount(final Account account) {
      this.account = account;
    }

    private Account account;
  }

  public static class DeleteAccountRequest extends Request {
    public DeleteAccountRequest() {
      super(RequestType.DELETE_ACCOUNT);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    private String accountId;
  }

  public static class DeleteAccountResponse extends Response {
    public Account getAccount() {
      return account;
    }

    public void setAccount(final Account account) {
      this.account = account;
    }

    private Account account;
  }

  public static class AddDomainRequest extends Request {
    public AddDomainRequest() {
      super(RequestType.ADD_DOMAIN);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(final String domainId) {
      this.domainId = domainId;
    }

    private String accountId;
    private String domainId;
  }

  public static class UpdateDomainRequest extends Request {
    public UpdateDomainRequest() {
      super(RequestType.UPDATE_DOMAIN);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    public String getDomainName() {
      return domainName;
    }

    public void setDomainName(final String domainName) {
      this.domainName = domainName;
    }

    public String getDomainDescription() {
      return domainDescription;
    }

    public void setDomainDescription(final String domainDescription) {
      this.domainDescription = domainDescription;
    }

    public Boolean getEnabled() {
      return enabled;
    }

    public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
    }

    private String accountId;
    private String domainId;
    private String domainName;
    private String domainDescription;
    private Boolean enabled;
  }

  public static class UpdateDomainResponse extends Response {

    private Domain domain;

    public Domain getDomain() {
      return domain;
    }

    public void setDomain(Domain domain) {
      this.domain = domain;
    }
  }

  public static class GetAccountDomainsRequest extends Request {
    public GetAccountDomainsRequest() {
      super(RequestType.GET_ACCOUNT_DOMAIN);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    public Boolean getPrimary() {
      return primary;
    }

    public void setPrimary(final Boolean primary) {
      this.primary = primary;
    }

    private String accountId;
    private Boolean primary;
  }

  public static class GetAccountDomainsResponse extends Response {
    private AccountDomainDetails domainDetails;

    public AccountDomainDetails getDomainDetails() {
      return domainDetails;
    }

    public void setDomains(final AccountDomainDetails domainDetails) {
      this.domainDetails = domainDetails;
    }
  }

  public static class GetAllDomainsRequest extends Request {
    public GetAllDomainsRequest() {
      super(RequestType.GET_ALL_DOMAINS);
    }
  }

  public static class GetAllDomainsResponse extends Response {
    private AccountDomainDetails domainDetails;

    public AccountDomainDetails getDomainDetails() {
      return domainDetails;
    }

    public void setDomains(final AccountDomainDetails domainDetails) {
      this.domainDetails = domainDetails;
    }
  }

  public static class GetAccountFromDomainRequest extends Request {

    public GetAccountFromDomainRequest() {
      super(RequestType.GET_ACCOUNT_FROM_DOMAIN);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    public String getProjectId() {
      return projectId;
    }

    public void setProjectId(String projectId) {
      this.projectId = projectId;
    }

    private String domainId;
    private String projectId;
  }

  public static class GetAccountFromDomainResponse extends Response {
    private AccountDomain accountFromDomain;

    public AccountDomain getAccountFromDomain() {
      return accountFromDomain;
    }

    public void setAccountFromDomain(AccountDomain accountFromDomain) {
      this.accountFromDomain = accountFromDomain;
    }
  }

  public static class DeleteDomainRequest extends Request {
    private String domainId;
    private String accountId;
    private boolean disable;

    public DeleteDomainRequest() {
      super(RequestType.DELETE_DOMAIN);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(final String domainId) {
      this.domainId = domainId;
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    public boolean getDisable() {
      return disable;
    }

    public void setDisable(boolean disable) {
      this.disable = disable;
    }

  }

  public static class DeleteDomainResponse extends Response {
    // TODO: implementation
  }

  public static class ElectPrimaryDomainRequest extends Request {
    private String domainId;
    private String accountId;

    public ElectPrimaryDomainRequest() {
      super(RequestType.ELECT_PRIMARY_DOMAIN);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(final String domainId) {
      this.domainId = domainId;
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }
  }

  public static class ElectPrimaryDomainResponse extends Response {
    // TODO: implementation
  }

  public static class CreateDomainRequest extends Request {
    public CreateDomainRequest() {
      super(RequestType.CREATE_DOMAIN);
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(final String accountId) {
      this.accountId = accountId;
    }

    public String getDomainName() {
      return domainName;
    }

    public void setDomainName(final String domainName) {
      this.domainName = domainName;
    }

    public String getDomainDescription() {
      return domainDescription;
    }

    public void setDomainDescription(final String domainDescription) {
      this.domainDescription = domainDescription;
    }

    public Boolean getEnabled() {
      return enabled;
    }

    public void setEnabled(Boolean enabled) {
      this.enabled = enabled;
    }

    private String accountId;
    private String domainName;
    private String domainDescription;
    private Boolean enabled;
  }

  public static class CreateDomainResponse extends Response {
    
    private Domain domain;

    public Domain getDomain() {
      return domain;
    }

    public void setDomain(Domain domain) {
      this.domain = domain;
    }
  }

  public static class SynchronizeRolesRequest extends Request {

    private String userId;
    private String groupId;
    private String domainId;
    private String accountId;

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }

    public SynchronizeRolesRequest() {
      super(RequestType.SYNCHRONIZE_ROLES);
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(final String userId) {
      this.userId = userId;
    }

    public String getGroupId() {
      return groupId;
    }

    public void setGroupId(final String groupId) {
      this.groupId = groupId;
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(final String domainId) {
      this.domainId = domainId;
    }
  }

  public static class SynchronizeRolesResponse extends Response {
    // TODO: need to be implemented
  }

  public static class GetIdpFromDomain extends Request {
    public IdpInfo getIdp_info() {
      return idp_info;
    }

    public void setIdp_info(IdpInfo idp_info) {
      this.idp_info = idp_info;
    }

    public GetIdpFromDomain() {
      super(RequestType.GET_IDP_FROM_DOMAIN);
    }

    public String getDomain_id() {
      return domain_id;
    }

    public void setDomain_id(String domain_id) {
      this.domain_id = domain_id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDescription() {
      return description;
    }

    public void setDescription(String description) {
      this.description = description;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }

    public String getDomain_name() {
      return domain_name;
    }

    public void setDomain_name(String domain_name) {
      this.domain_name = domain_name;
    }

    public class IdpInfo {
      public String getUrl() {
        return url;
      }

      public void setUrl(String url) {
        this.url = url;
      }

      public String getUser_bind_dn() {
        return user_bind_dn;
      }

      public void setUser_bind_dn(String user_bind_dn) {
        this.user_bind_dn = user_bind_dn;
      }

      public String getUser_bind_pwd() {
        return user_bind_pwd;
      }

      public void setUser_bind_pwd(String user_bind_pwd) {
        this.user_bind_pwd = user_bind_pwd;
      }

      public String getUser_tree_dn() {
        return user_tree_dn;
      }

      public void setUser_tree_dn(String user_tree_dn) {
        this.user_tree_dn = user_tree_dn;
      }

      public String getGroup_tree_dn() {
        return group_tree_dn;
      }

      public void setGroup_tree_dn(String group_tree_dn) {
        this.group_tree_dn = group_tree_dn;
      }

      public String getUser_filter() {
        return user_filter;
      }

      public void setUser_filter(String user_filter) {
        this.user_filter = user_filter;
      }

      public String getGroup_filter() {
        return group_filter;
      }

      public void setGroup_filter(String group_filter) {
        this.group_filter = group_filter;
      }

      public String getUser_class_name() {
        return user_class_name;
      }

      public void setUser_class_name(String user_class_name) {
        this.user_class_name = user_class_name;
      }

      public String getGroup_class_name() {
        return group_class_name;
      }

      public void setGroup_class_name(String group_class_name) {
        this.group_class_name = group_class_name;
      }

      public String getUser_name_attribute() {
        return user_name_attribute;
      }

      public void setUser_name_attribute(String user_name_attribute) {
        this.user_name_attribute = user_name_attribute;
      }

      public String getGroup_name_attribute() {
        return group_name_attribute;
      }

      public void setGroup_name_attribute(String group_name_attribute) {
        this.group_name_attribute = group_name_attribute;
      }

      private String url;
      private String user_bind_dn;
      private String user_bind_pwd;
      private String user_tree_dn;
      private String group_tree_dn;
      private String user_filter;
      private String group_filter;
      private String user_class_name;
      private String group_class_name;
      private String user_name_attribute;
      private String group_name_attribute;
    }

    private String domain_id;
    private String name;
    private String description;
    private String type;
    private IdpInfo idp_info;
    private String domain_name;
  }

  public static class CreateIdpRequest extends Request {
    public CreateIdpRequest() {
      super(RequestType.CREATE_IDP_FOR_DOMAIN);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }
    
    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }

    public IdpRequest getIdp() {
      return idp;
    }

    public void setIdp(IdpRequest idp) {
      this.idp = idp;
    }

    private String domainId;
    private String accountId;
    private IdpRequest idp;
  }

  public static class CreateIdpResponse extends Response {

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }

    public Idp getIdp() {
      return idp;
    }

    public void setIdp(Idp idp) {
      this.idp = idp;
    }

    public ValidateIdpDetails getValidateIdpDetails() {
      return validateIdp;
    }

    public void setValidateIdpDetails(ValidateIdpDetails validateIdp) {
      this.validateIdp = validateIdp;
    }

    private ValidateIdpDetails validateIdp;
    private String accountId;
    private Idp idp;
  }

  public static class ListIdpRequest extends Request {
    private String domainId;
    private String accountId;

    public ListIdpRequest() {
      super(RequestType.LIST_IDP);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }
  }

  public static class ListIdpResponse extends Response {

    public List<Idp> getIdps() {
      return identity_providers;
    }

    public void setIdps(List<Idp> idps) {
      this.identity_providers = idps;
    }

    private List<Idp> identity_providers;
  }

  public static class GetIdpRequest extends Request {
    private String domainId;

    public GetIdpRequest() {
      super(RequestType.GET_IDP);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

  }

  public static class GetIdpResponse extends Response {

    public Idp getIdp() {
      return idp;
    }

    public void setIdp(Idp idp) {
      this.idp = idp;
    }

    private Idp idp;
  }

  public static class UpdateIdpRequest extends Request {
    public UpdateIdpRequest() {
      super(RequestType.UPDATE_IDP);
    }

    public IdpRequest getIdp() {
      return idp;
    }

    public void setIdp(IdpRequest idp) {
      this.idp = idp;
    }
    
    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }
    
    public String getDomainId() {
      return domainId;
    }

    private IdpRequest idp;
    private String domainId;
    
  }

  public static class UpdateIdpResponse extends Response {
    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }

    public Idp getIdp() {
      return idp;
    }

    public void setIdp(Idp idp) {
      this.idp = idp;
    }

    public ValidateIdpDetails getValidateIdpDetails() {
      return validateIdp;
    }

    public void setValidateIdpDetails(ValidateIdpDetails validateIdp) {
      this.validateIdp = validateIdp;
    }

    private ValidateIdpDetails validateIdp;
    private String accountId;
    private Idp idp;
  }
  public static class ValidateIdpRequest extends Request {
    public ValidateIdpRequest() {
      super(RequestType.VALIDATE_IDP);
    }
    
    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }
    
    public String getDomainId() {
      return domainId;
    }
   
    private String domainId;
    
  }

  public static class ValidateIdpResponse extends Response {

    public ValidateIdpDetails getValidateIdpDetails() {
      return validateIdp;
    }

    public void setValidateIdpDetails(ValidateIdpDetails validateIdp) {
      this.validateIdp = validateIdp;
    }

    private ValidateIdpDetails validateIdp;
  }

  public static class DeleteIdpRequest extends Request {

    public DeleteIdpRequest() {
      super(RequestType.DELETE_IDP);
    }

    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    private String domainId;

  }

  public static class DeleteIdpResponse extends Response {
    public String getDomainId() {
      return domainId;
    }

    public void setDomainId(String domainId) {
      this.domainId = domainId;
    }

    private String domainId;
  }

  public static class GetTaskStatusRequest extends Request {
    private String id;
    private String accountId;

    public GetTaskStatusRequest() {
      super(RequestType.GET_TASK_STATUS);
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getAccountId() {
      return accountId;
    }

    public void setAccountId(String accountId) {
      this.accountId = accountId;
    }
  }

  public static class GetTaskStatusResponse extends Response {
    private WorkflowTask workflowTask;

    public WorkflowTask getWorkflowTask() {
      return workflowTask;
    }

    public void setWorkflowTask(WorkflowTask workflowTask) {
      this.workflowTask = workflowTask;
    }
  }
}
