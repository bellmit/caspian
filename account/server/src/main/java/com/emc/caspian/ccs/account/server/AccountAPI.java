/**
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.server;

/**
 * This class implements /accounts REST APIs Created by gulavb on 2/26/2015.
 */


import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

import com.emc.caspain.ccs.common.webfilters.KeystonePrincipal;
import com.emc.caspian.ccs.account.api.AccountAPI.Accounts;
import com.emc.caspian.ccs.account.controller.AccountProtocol;
import com.emc.caspian.ccs.account.controller.RequestErrorMessage;
import com.emc.caspian.ccs.account.controller.ResponseErrorMessage;
import com.emc.caspian.ccs.account.controller.AccountProtocol.CreateAccountResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.DeleteAccountResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetAccountFromDomainResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetAccountResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.GetAccountsResponse;
import com.emc.caspian.ccs.account.controller.AccountProtocol.UpdateAccountRequest;
import com.emc.caspian.ccs.account.controller.AccountProtocol.UpdateAccountResponse;
import com.emc.caspian.ccs.account.controller.AccountService;
import com.emc.caspian.ccs.account.datacontract.Account;
import com.emc.caspian.ccs.account.datacontract.AccountDomain;
import com.emc.caspian.ccs.account.datacontract.AccountDomainDetails;
import com.emc.caspian.ccs.account.datacontract.AccountList;
import com.emc.caspian.ccs.account.datacontract.AccountRequest;
import com.emc.caspian.ccs.account.datacontract.IdpRequest;
import com.emc.caspian.ccs.account.datacontract.WorkflowTask;
import com.emc.caspian.ccs.account.util.AppLogger;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.common.utils.Validator;

public class AccountAPI implements Accounts {

  @Context
  public SecurityContext securityContext;
  @Context
  public UriInfo uriInfo;

  /**
   * API implementation to get account Id from domain in token/query parameter
   */
  @Override
  public Response getAccountFromDomain(final String version, final String domainId) {
    AppLogger.debug("Fetching account ID from domain " + domainId);
    if(versionCheck(version, true, false) != null) {
      return Response.serverError().entity(versionCheck(version, true, false)).status(Status.NOT_FOUND).build();
    }
    Response response = null;
    final AccountProtocol.GetAccountFromDomainRequest accountFromDomainRequest =
        new AccountProtocol.GetAccountFromDomainRequest();
    try {

      if (StringUtils.isEmpty(domainId)) {
        KeystonePrincipal ksPrincipal = (KeystonePrincipal) securityContext.getUserPrincipal();
        if (StringUtils.isNotEmpty(ksPrincipal.getDomainId())) {
          accountFromDomainRequest.setDomainId(ksPrincipal.getDomainId());
        }
        // If the given token is project scoped, get the id of the domain in which the project exists
        else if(StringUtils.isNotEmpty(ksPrincipal.getProjectDomainId())){
          accountFromDomainRequest.setDomainId(ksPrincipal.getProjectDomainId());
        } else {
          accountFromDomainRequest.setProjectId(ksPrincipal.getProjectId());
        }
      } else {
        accountFromDomainRequest.setDomainId(domainId);
      }

      response =
          AccountServiceFrontEndHelper.handleRequest(accountFromDomainRequest,
              new HandleRequest<AccountProtocol.GetAccountFromDomainResponse>() {
                @Override
                public AccountProtocol.GetAccountFromDomainResponse processRequest() throws Exception {
                  return AccountService.getAccountFromDomain(version, accountFromDomainRequest);
                }
              }, new TransformResponse<AccountProtocol.GetAccountFromDomainResponse>() {
                @Override
                public Object transform(GetAccountFromDomainResponse response) {
                  AccountDomain accountFromDomain = response.getAccountFromDomain();
                  return JsonHelper.serializeToJson(accountFromDomain);
                }
              });
    } catch (IllegalArgumentException re) {
      return Response.serverError().entity("Invalid JSON request").status(Status.BAD_REQUEST).build();
    }
    return response;
  }

  /**
   * API implementation to create an account
   */

  @Override
  public Response createAccount(final String version, final String request) {
    AppLogger.debug("Create account request received");
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final AccountProtocol.CreateAccountRequest createRequest = new AccountProtocol.CreateAccountRequest();
    Response response = null;
    response =
        AccountServiceFrontEndHelper.handleRequest(createRequest,
            new HandleRequest<AccountProtocol.CreateAccountResponse>() {
              @Override
              public CreateAccountResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                final AccountRequest accountRequest = AccountRequest.createAndValidate(request);
                createRequest.setAccount(accountRequest);
                return AccountService.createAccount(version, createRequest);
              }
            }, new TransformResponse<AccountProtocol.CreateAccountResponse>() {
              @Override
              public Object transform(CreateAccountResponse response) throws Exception {
                Account account = response.getAccount();
                return JsonHelper.serializeToJson(account);
              }
            });
    return response;
  }

  /**
   * 
   * API implementation to list accounts
   */

  @Override
  public Response listAccounts(final String version, final boolean details) {
    AppLogger.debug("Fetching list of accounts");
    if(versionCheck(version, true, true) != null) {
      return Response.serverError().entity(versionCheck(version, true, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetAccountsRequest request = new AccountProtocol.GetAccountsRequest();
    Response response =
        AccountServiceFrontEndHelper.handleRequest(request, new HandleRequest<AccountProtocol.GetAccountsResponse>() {
          @Override
          public AccountProtocol.GetAccountsResponse processRequest() {
            KeystonePrincipal ksPrincipal = (KeystonePrincipal) securityContext.getUserPrincipal();
            String domainId = (ksPrincipal.getDomainId().equalsIgnoreCase("default"))? "default":ksPrincipal.getDomainId();
            return AccountService.listAccounts(version, details, domainId);
          }
        }, new TransformResponse<AccountProtocol.GetAccountsResponse>() {
          @Override
          public Object transform(GetAccountsResponse response) {
            AccountList accounts = new AccountList(response.getAccounts());
            return JsonHelper.serializeToJson(accounts);
          }
        });
    return response;
  }

  /**
   * API implementation to get a particular account
   * 
   */
  @Override
  public Response getAccount(final String version, final String accountId) {
    AppLogger.debug("Fetching account " + accountId);
    if(versionCheck(version, true, true) != null) {
      return Response.serverError().entity(versionCheck(version, true, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetAccountRequest request = new AccountProtocol.GetAccountRequest();
    final String normalizedAccountId = normalize(accountId);
    Response response =
        AccountServiceFrontEndHelper.handleRequest(request, new HandleRequest<AccountProtocol.GetAccountResponse>() {
          @Override
          public AccountProtocol.GetAccountResponse processRequest() throws Exception {
            Validator.validateNotEmpty(normalizedAccountId, RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
            request.setAccountId(normalizedAccountId);
            return AccountService.getAccount(version, request);
          }
        }, new TransformResponse<AccountProtocol.GetAccountResponse>() {
          @Override
          public Object transform(GetAccountResponse response) {
            Account account = response.getAccount();
            return JsonHelper.serializeToJson(account);
          }
        });
    return response;
  }

  /**
   * API implementation to update an account
   * 
   */

  @Override
  public Response updateAccount(final String version, final String accountId, final String request) {
    AppLogger.debug("Received update account request for " + accountId);
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final UpdateAccountRequest updateRequest = new AccountProtocol.UpdateAccountRequest();
    final String normalizedAccountId = normalize(accountId);
    Response response = null;
    response =
        AccountServiceFrontEndHelper.handleRequest(updateRequest,
            new HandleRequest<AccountProtocol.UpdateAccountResponse>() {
              @Override
              public UpdateAccountResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                Validator.validateNotEmpty(normalizedAccountId, RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
                final AccountRequest accountRequest = AccountRequest.createAndValidate(request);
                updateRequest.setId(normalizedAccountId);
                updateRequest.setAccount(accountRequest);
                return AccountService.updateAccount(version, updateRequest);
              }
            }, new TransformResponse<AccountProtocol.UpdateAccountResponse>() {
              @Override
              public Object transform(UpdateAccountResponse response) throws Exception {
                Account account = response.getAccount();
                return JsonHelper.serializeToJson(account);
              }
            });

    return response;
  }

  /**
   * API implementation to delete an account
   * 
   */
  @Override
  public Response deleteAccount(final String version, final String accountId) {
    AppLogger.debug("Received delete account request for " + accountId);
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final AccountProtocol.DeleteAccountRequest request = new AccountProtocol.DeleteAccountRequest();
    final String normalizedAccountId = normalize(accountId);
    Response response =
        AccountServiceFrontEndHelper.handleRequest(request, new HandleRequest<AccountProtocol.DeleteAccountResponse>() {
          @Override
          public DeleteAccountResponse processRequest() {
            Validator.validateNotEmpty(normalizedAccountId, RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
            request.setAccountId(normalizedAccountId);
            return AccountService.deleteAccount(version, request);
          }
        }, new TransformResponse<AccountProtocol.DeleteAccountResponse>() {
          @Override
          public Object transform(DeleteAccountResponse response) {
            Account account = response.getAccount();
            return JsonHelper.serializeToJson(account);
          }
        });
    return response;
  }

  /**
   * API implementation to list domains mapped with an account
   * 
   */
  @Override
  public Response listAccountDomains(final String version, final String accountId) {
    AppLogger.debug("Received list account domain request for account " + accountId);
    if(versionCheck(version, true, false) != null) {
      return Response.serverError().entity(versionCheck(version, true, false)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetAccountDomainsRequest request = new AccountProtocol.GetAccountDomainsRequest();
    final String normalizedAccountId = normalize(accountId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(request,
            new HandleRequest<AccountProtocol.GetAccountDomainsResponse>() {
              @Override
              public AccountProtocol.GetAccountDomainsResponse processRequest() {
                Validator.validateNotEmpty(normalizedAccountId, RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
                request.setAccountId(normalizedAccountId);
                return AccountService.listAccountDomains(request);
              }
            }, new TransformResponse<AccountProtocol.GetAccountDomainsResponse>() {
              @Override
              public Object transform(AccountProtocol.GetAccountDomainsResponse response) {
                AccountDomainDetails accounts = new AccountDomainDetails();
                accounts = response.getDomainDetails();
                return JsonHelper.serializeToJson(accounts);
              }
            });

    return response;
  }

  /**
   * API implementation to create IDP for a domain
   * 
   */
 
  public Response createIdpForDomain(final String version, final String domainId, final String request) {
    AppLogger.debug("Received create IDP request for domain " + domainId);
    if(versionCheck(version, true, false) != null) {
        return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
      }
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final AccountProtocol.CreateIdpRequest createIdpFromDomainRequest = new AccountProtocol.CreateIdpRequest();
    Response response = null;
    final String normalizedDomainId = normalize(domainId);

    response =
        AccountServiceFrontEndHelper.handleRequest(createIdpFromDomainRequest,
            new HandleRequest<AccountProtocol.CreateIdpResponse>() {
              @Override
              public AccountProtocol.CreateIdpResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                IdpRequest idp = IdpRequest.createAndValidate(request, true);
                createIdpFromDomainRequest.setIdp(idp);
                createIdpFromDomainRequest.setDomainId(normalizedDomainId);
                return AccountService.createIdpFromDomain(version, createIdpFromDomainRequest, "no");
              }
            }, new TransformResponse<AccountProtocol.CreateIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.CreateIdpResponse response) throws Exception {
                return response.getIdp();
              }
            });
    return response;
  }

  /**
   * v2 API implementation to create IDP for a domain
   * 
   */
  public Response createIdpForDomainv2(final String version, final String domainId, final String request, final String validate) {
    AppLogger.debug("Received create IDP request for domain " + domainId);
    if(versionCheck(version, false, true) != null) {
      return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.CreateIdpRequest createIdpFromDomainRequest = new AccountProtocol.CreateIdpRequest();
    Response response = null;
    final String normalizedDomainId = normalize(domainId);

    response =
        AccountServiceFrontEndHelper.handleRequest(createIdpFromDomainRequest,
            new HandleRequest<AccountProtocol.CreateIdpResponse>() {
              @Override
              public AccountProtocol.CreateIdpResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                IdpRequest idp = IdpRequest.createAndValidate(request, true);
                createIdpFromDomainRequest.setIdp(idp);
                createIdpFromDomainRequest.setDomainId(normalizedDomainId);
                return AccountService.createIdpFromDomain(version, createIdpFromDomainRequest, validate);
              }
            }, new TransformResponse<AccountProtocol.CreateIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.CreateIdpResponse response) throws Exception {
            	if(response.getStatus() == AccountProtocol.Status.SUCCESS_CREATED)
            	  return response.getIdp();
            	else
            	  return response.getValidateIdpDetails();
              }
            });
    return response;
  }

 
  /**
   * API implementation to get IDP
   * 
   */
  @Override
  public Response getIdp(final String version, final String domainId) {
    AppLogger.debug("Received get IDP request for domain " + domainId);
    if(versionCheck(version, true, false) != null) {
      return Response.serverError().entity(versionCheck(version, true, false)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetIdpRequest getIdpRequest = new AccountProtocol.GetIdpRequest();
    final String normalizedDomainId = normalize(domainId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(getIdpRequest, new HandleRequest<AccountProtocol.GetIdpResponse>() {
          @Override
          public AccountProtocol.GetIdpResponse processRequest() {
            Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
            getIdpRequest.setDomainId(normalizedDomainId);
            return AccountService.getIdp(version, getIdpRequest);
          }
        }, new TransformResponse<AccountProtocol.GetIdpResponse>() {
          @Override
          public Object transform(AccountProtocol.GetIdpResponse response) throws Exception {
            return response.getIdp();
          }
        });
    return response;
  }
  
  /**
   * v2 API implementation to get IDP
   * 
   */
  @Override
  public Response getIdpv2(final String version, final String domainId) {
    AppLogger.debug("Received get IDP request for domain " + domainId);
    if(versionCheck(version, false, true) != null) {
      return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetIdpRequest getIdpRequest = new AccountProtocol.GetIdpRequest();
    final String normalizedDomainId = normalize(domainId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(getIdpRequest, new HandleRequest<AccountProtocol.GetIdpResponse>() {
          @Override
          public AccountProtocol.GetIdpResponse processRequest() {
            Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
            getIdpRequest.setDomainId(normalizedDomainId);
            return AccountService.getIdp(version, getIdpRequest);
          }
        }, new TransformResponse<AccountProtocol.GetIdpResponse>() {
          @Override
          public Object transform(AccountProtocol.GetIdpResponse response) throws Exception {
            return response.getIdp();
          }
        });
    return response;
  }
  
  

  /**
   * API implementation to update an IDP
   * 
   */
  @Override
  public Response updateIdp(final String version, final String domainId, final String request) {
    AppLogger.debug("Received update IDP request for domain " + domainId);
    if(versionCheck(version, true, false) != null) {
        return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
      }
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final AccountProtocol.UpdateIdpRequest updateIdpRequest = new AccountProtocol.UpdateIdpRequest();
    final String normalizedDomainId = normalize(domainId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(updateIdpRequest,
            new HandleRequest<AccountProtocol.UpdateIdpResponse>() {
              @Override
              public AccountProtocol.UpdateIdpResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                IdpRequest idp = IdpRequest.createAndValidate(request, false);
                updateIdpRequest.setDomainId(domainId);
                updateIdpRequest.setIdp(idp);
                return AccountService.updateIdp(version, updateIdpRequest, "no");
              }
            }, new TransformResponse<AccountProtocol.UpdateIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.UpdateIdpResponse response) throws Exception {
                return response.getIdp();
              }
            });
    return response;
  }

  /**
   * v2 API implementation to update an IDP
   * 
   */
  @Override
  public Response updateIdpv2(final String version, final String domainId, final String request, final String validate) {
    AppLogger.debug("Received update IDP request for domain " + domainId);
    if(versionCheck(version, false, true) != null) {
      return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.UpdateIdpRequest updateIdpRequest = new AccountProtocol.UpdateIdpRequest();
    final String normalizedDomainId = normalize(domainId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(updateIdpRequest,
            new HandleRequest<AccountProtocol.UpdateIdpResponse>() {
              @Override
              public AccountProtocol.UpdateIdpResponse processRequest() {
                Validator.validateNotEmpty(request, RequestErrorMessage.REQUEST_JSON_EMPTY);
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                IdpRequest idp = IdpRequest.createAndValidate(request, false);
                updateIdpRequest.setDomainId(domainId);
                updateIdpRequest.setIdp(idp);
                return AccountService.updateIdp(version, updateIdpRequest, validate);
              }
            }, new TransformResponse<AccountProtocol.UpdateIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.UpdateIdpResponse response) throws Exception {
            	if(response.getStatus() == AccountProtocol.Status.SUCCESS_OK && response.getIdp() != null)
                  return response.getIdp();
                else
                  return response.getValidateIdpDetails();
              }
            });
    return response;
  }
  
  /**
   * API implementation to validate an IDP
   * 
   */
  @Override
  public Response validateIdp(final String version, final String domainId) {
    AppLogger.debug("Received validate IDP request for domain " + domainId);
    if(versionCheck(version, false, true) != null) {
        return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
      }
    final AccountProtocol.ValidateIdpRequest validateIdpRequest = new AccountProtocol.ValidateIdpRequest();
    final String normalizedDomainId = normalize(domainId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(validateIdpRequest,
            new HandleRequest<AccountProtocol.ValidateIdpResponse>() {
              @Override
              public AccountProtocol.ValidateIdpResponse processRequest() {
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                validateIdpRequest.setDomainId(domainId);
                return AccountService.validateIdp(version, validateIdpRequest);
              }
            }, new TransformResponse<AccountProtocol.ValidateIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.ValidateIdpResponse response) throws Exception {
                return response.getValidateIdpDetails();
              }
            });
    return response;
  }

  /**
   * APi implementation to delete an IDP
   * 
   */
  @Override
  public Response removeIdp(final String version, final String domainId) {
    AppLogger.debug("Received delete idp request for domain %s" , domainId);
    if(versionCheck(version, true, false) != null) {
        return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
      }
    if (version.equalsIgnoreCase(AccountServiceVersions.API_VERSION_V1)) {
      return Response.serverError().entity(ResponseErrorMessage.METHOD_NOT_SUPPORTED_FOR_V1)
          .status(Status.METHOD_NOT_ALLOWED).build();
    }
    final AccountProtocol.DeleteIdpRequest deleteIdpRequest = new AccountProtocol.DeleteIdpRequest();
    final String normalizedDomainId = normalize(domainId);
    Response response =
        AccountServiceFrontEndHelper.handleRequest(deleteIdpRequest,
            new HandleRequest<AccountProtocol.DeleteIdpResponse>() {
              @Override
              public AccountProtocol.DeleteIdpResponse processRequest() {
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                deleteIdpRequest.setDomainId(normalizedDomainId);
                return AccountService.removeIdp(deleteIdpRequest);
              }
            }, new TransformResponse<AccountProtocol.DeleteIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.DeleteIdpResponse response) throws Exception {
                return response.getDomainId();
              }
            });
    return response;
  }
  
  /**
   * v2 APi implementation to delete an IDP
   * 
   */
  @Override
  public Response removeIdpv2(final String version, final String domainId) {
    AppLogger.debug("Received delete idp request for domain %s" , domainId);
    if(versionCheck(version, false, true) != null) {
      return Response.serverError().entity(versionCheck(version, false, true)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.DeleteIdpRequest deleteIdpRequest = new AccountProtocol.DeleteIdpRequest();
    final String normalizedDomainId = normalize(domainId);
    Response response =
        AccountServiceFrontEndHelper.handleRequest(deleteIdpRequest,
            new HandleRequest<AccountProtocol.DeleteIdpResponse>() {
              @Override
              public AccountProtocol.DeleteIdpResponse processRequest() {
                Validator.validateNotEmpty(normalizedDomainId, RequestErrorMessage.REQUEST_DOMAIN_ID_EMPTY);
                deleteIdpRequest.setDomainId(normalizedDomainId);
                return AccountService.removeIdp(deleteIdpRequest);
              }
            }, new TransformResponse<AccountProtocol.DeleteIdpResponse>() {
              @Override
              public Object transform(AccountProtocol.DeleteIdpResponse response) throws Exception {
                return response.getDomainId();
              }
            });
    return response;
  }

  /**
   * API implementation to list all domains
   * 
   */
  @Override
  public Response listAllDomains(final String version) {
    AppLogger.debug("Received request for listing all domains");
    if(versionCheck(version, true, false) != null) {
      return Response.serverError().entity(versionCheck(version, true, false)).status(Status.NOT_FOUND).build();
    }
    final AccountProtocol.GetAllDomainsRequest request = new AccountProtocol.GetAllDomainsRequest();

    Response response =
        AccountServiceFrontEndHelper.handleRequest(request, new HandleRequest<AccountProtocol.GetAllDomainsResponse>() {
          @Override
          public AccountProtocol.GetAllDomainsResponse processRequest() {
            return AccountService.listAllDomains(version);
          }
        }, new TransformResponse<AccountProtocol.GetAllDomainsResponse>() {
          @Override
          public Object transform(AccountProtocol.GetAllDomainsResponse response) {
            AccountDomainDetails accounts = new AccountDomainDetails();
            accounts = response.getDomainDetails();
            return JsonHelper.serializeToJson(accounts);
          }
        });

    return response;
  }

  @Override
  public Response getTaskStatus(final String version, final String accountId, final String taskId) {
    AppLogger.debug("Received get status request of task " + taskId + " for account " + accountId);
    final AccountProtocol.GetTaskStatusRequest request = new AccountProtocol.GetTaskStatusRequest();
    final String normalizedAccountId = normalize(accountId);
    final String normalizedTaskId = normalize(taskId);

    Response response =
        AccountServiceFrontEndHelper.handleRequest(request, new HandleRequest<AccountProtocol.GetTaskStatusResponse>() {
          @Override
          public AccountProtocol.GetTaskStatusResponse processRequest() {
            Validator.validateNotEmpty(normalizedAccountId, RequestErrorMessage.REQUEST_ACCOUNT_ID_EMPTY);
              Validator.validateNotEmpty(normalizedTaskId, RequestErrorMessage.REQUEST_TASK_ID_EMPTY);
            request.setId(normalizedTaskId);
            request.setAccountId(normalizedAccountId);
            return AccountService.getTaskStatus(version, request);
          }
        }, new TransformResponse<AccountProtocol.GetTaskStatusResponse>() {
          @Override
          public Object transform(AccountProtocol.GetTaskStatusResponse response) {
            WorkflowTask workflowTask = new WorkflowTask();
            workflowTask = response.getWorkflowTask();
            return JsonHelper.serializeToJson(workflowTask);
          }
        });
    return response;
  }
  
  /**
   * this method will check for null and return the trim of the request string
   * 
   * @param parameter
   * @return
   */
  private static String normalize(String parameter) {
    if (parameter != null) {
      parameter = parameter.trim();
    }
    return parameter;
  }

  /**
   * this method checks the version of account service request
   *
   * @param version       : version the API was called on
   * @param isV1Supported : true if the API is suuported in V1
   * @param isV2Supported : true if the API is suuported in V2
   *
   * @return Error Message if version is not supported else null
   */
  private static String versionCheck(String version, boolean isV1Supported, boolean isV2Supported) {
    if(!version.equals(AccountServiceVersions.API_VERSION_V1) && !version.equals(AccountServiceVersions.API_VERSION_V2)) {
      return String.format(ResponseErrorMessage.VERSION_NOT_SUPPORTED, version);
    } else if ((version.equals(AccountServiceVersions.API_VERSION_V1) && !isV1Supported)
        || (version.equals(AccountServiceVersions.API_VERSION_V2) && !isV2Supported)) {
      return ResponseErrorMessage.RESOURCE_NOT_FOUND;
    }
    return null;
  }

}
