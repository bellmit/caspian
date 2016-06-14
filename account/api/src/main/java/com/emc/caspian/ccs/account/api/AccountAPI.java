/**
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.api;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.emc.caspian.ccs.account.authorization.AuthorizationPolicy;
import com.emc.caspian.ccs.account.authorization.AuthorizationPolicy.Rule;

/**
 * Created by gulavb on 2/26/2015.
 */
public final class AccountAPI {

  @Path("/{version}/accounts")
  public static interface Accounts {

    /**
     * API to get account ID from domain
     * 
     * @param request
     * @return
     */
    @GET
    @Path("/current")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_SERVICE, Rule.ALLOW_ACCOUNT_ADMIN,
        Rule.ALLOW_SCOPED_USER})
    public Response getAccountFromDomain(@PathParam("version") final String version,
        @QueryParam("domain_id") final String domainId);

    /**
     * Create an account
     * 
     * @param request AccountRequest json
     * @return account json
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response createAccount(@PathParam("version") final String version, final String request);

    /**
     * Lists all accounts
     * 
     * @return Account list json
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR, Rule.ALLOW_CLOUD_SERVICE,
        Rule.ALLOW_ACCOUNT_ADMIN, Rule.ALLOW_ACCOUNT_MONITOR})
    public Response listAccounts(@PathParam("version") final String version,
    	@DefaultValue("true") @QueryParam("details") final boolean details);

    /**
     * Fetch a specific account
     * 
     * @param accountId Identifier for the account to be fetched
     * @return account json
     */
    @GET
    @Path("/{accountId}")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_ACCOUNT_ADMIN, Rule.ALLOW_CLOUD_MONITOR,
        Rule.ALLOW_ACCOUNT_MONITOR, Rule.ALLOW_CLOUD_SERVICE})
    public Response getAccount(@PathParam("version") final String version,
        @PathParam("accountId") final String accountId);

    /**
     * Update properties of an account
     * 
     * @param accountId Identifier of an account to be updated
     * @param request AccountRequest json
     * @return updated account json
     */
    @PUT
    @Path("/{accountId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.DISALLOW_EDIT_DEFAULT_ACCOUNT, Rule.ALLOW_CLOUD_ADMIN})
    public Response updateAccount(@PathParam("version") final String version,
        @PathParam("accountId") final String accountId, final String request);


    /**
     * Delete an account
     * 
     * @param accountId Identifier of the account to be deleted
     * @return empty
     */
    @DELETE
    @Path("/{accountId}")
    @AuthorizationPolicy({Rule.DISALLOW_EDIT_DEFAULT_ACCOUNT, Rule.ALLOW_CLOUD_ADMIN})
    public Response deleteAccount(@PathParam("version") final String version,
        @PathParam("accountId") final String accountId);


    /**
     * List domains within an account
     * 
     * @return AccountDomain list json
     */
    @GET
    @Path("/{accountId}/domains")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_ACCOUNT_ADMIN, Rule.ALLOW_CLOUD_MONITOR,
        Rule.ALLOW_ACCOUNT_MONITOR, Rule.ALLOW_CLOUD_SERVICE})
    public Response listAccountDomains(@PathParam("version") final String version,
        @PathParam("accountId") final String accountId);

    /**
     * List all domains
     * 
     * @return Domain list json
     */
    @GET
    @Path("/domains")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_CLOUD_MONITOR, Rule.ALLOW_CLOUD_SERVICE})
    public Response listAllDomains(@PathParam("version") final String version);

    /**
     * API to create an IDP
     * 
     * @param domainId
     * @param request
     * @return
     */
    @POST
    @Path("domains/{domain-id}/identity-provider")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response createIdpForDomain(@PathParam("version") final String version,
        @PathParam("domain-id") final String domainId, final String request);
    /**
     * v2 API to create an IDP
     * 
     * @param domainId
     * @param request
     * @return
     */
    @POST
    @Path("{domain-id}/identity-provider")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response createIdpForDomainv2(@PathParam("version") final String version,
        @PathParam("domain-id") final String domainId, final String request, @DefaultValue("before_save") @QueryParam("validate") final String validate);

    /**
     * API to get a particular IDP
     * 
     * @param idpId
     * @return
     */
    @GET
    @Path("domains/{domain-id}/identity-provider")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_ACCOUNT_ADMIN_IDP, Rule.ALLOW_CLOUD_MONITOR,
        Rule.ALLOW_ACCOUNT_MONITOR_IDP, Rule.ALLOW_CLOUD_SERVICE, Rule.ALLOW_DOMAIN_ADMIN, Rule.ALLOW_DOMAIN_MONITOR})
    public Response getIdp(@PathParam("version") final String version, @PathParam("domain-id") final String domainId);

    /**
     * v2 API to get a particular IDP
     * 
     * @param idpId
     * @return
     */
    @GET
    @Path("{domain-id}/identity-provider")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_ACCOUNT_ADMIN_IDP, Rule.ALLOW_CLOUD_MONITOR,
        Rule.ALLOW_ACCOUNT_MONITOR_IDP, Rule.ALLOW_CLOUD_SERVICE, Rule.ALLOW_DOMAIN_ADMIN, Rule.ALLOW_DOMAIN_MONITOR})
    public Response getIdpv2(@PathParam("version") final String version, @PathParam("domain-id") final String domainId);

    /**
     * API to update a particular IDP
     * 
     * @param idpId
     * @param request JSON
     * @return
     */
    @PUT
    @Path("domains/{domain-id}/identity-provider")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response updateIdp(@PathParam("version") final String version, @PathParam("domain-id") final String domainId,
        final String request);
    /**
     * v2 API to update an IDP
     * 
     * @param idpId
     * @param request JSON
     * @return
     */
    @PUT
    @Path("{domain-id}/identity-provider")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response updateIdpv2(@PathParam("version") final String version, @PathParam("domain-id") final String domainId,
        final String request, @DefaultValue("before_save") @QueryParam("validate") final String validate);

    /**
     * API to validate an existing IDP
     * 
     * @param idpId
     * @return
     */
    @PUT
    @Path("{domain-id}/identity-provider/validate")
    @Produces({MediaType.APPLICATION_JSON})
    public Response validateIdp(@PathParam("version") final String version, @PathParam("domain-id") final String domainId);

    /**
     * API to delete a particular IDP
     * 
     * @param idpId
     * @return
     */
    @DELETE
    @Path("domains/{domain-id}/identity-provider")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response removeIdp(@PathParam("version") final String version,
        @PathParam("domain-id") final String domainId);
    
    /**
     * v2 API to delete a particular IDP
     * 
     * @param idpId
     * @return
     */
    @DELETE
    @Path("{domain-id}/identity-provider")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy(Rule.ALLOW_CLOUD_ADMIN)
    public Response removeIdpv2(@PathParam("version") final String version,
        @PathParam("domain-id") final String domainId);


    /**
     * API to get a task status from workflow
     * 
     * @param idpId
     * @return
     */
    @GET
    @Path("{accountId}/tasks/{taskId}")
    @Produces({MediaType.APPLICATION_JSON})
    @AuthorizationPolicy({Rule.ALLOW_CLOUD_ADMIN, Rule.ALLOW_ACCOUNT_ADMIN})
    public Response getTaskStatus(@PathParam("version") final String version, @PathParam("accountId") final String accountId,
        @PathParam("taskId") final String taskId);
  }
}
