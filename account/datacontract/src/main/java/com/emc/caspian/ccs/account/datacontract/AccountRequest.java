/**
 * Copyright (c) 2015 EMC Corporation All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation or is licensed to EMC Corporation from third
 * parties. Use of this software and the intellectual property contained therein is expressly limited to the terms and
 * conditions of the License Agreement under which it is provided by or on behalf of EMC.
 */

package com.emc.caspian.ccs.account.datacontract;

import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.common.utils.Validator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by gulavb on 2/26/2015.
 */
public class AccountRequest {

  public AccountRequest() {
    this.name = this.description = null;
  }

  public AccountRequest(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  @JsonProperty
  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty
  public String getDescription() {
    return description;
  }

  @JsonProperty
  public void setDescription(String description) {
    this.description = description;
  }

  @JsonProperty
  public Boolean getEnabled() {
    return enabled;
  }

  @JsonProperty
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Any JSON processing exception thrown by de-serialization will be caught by the methods calling this.
   * @param json
   * @return
   */
  public static AccountRequest createAndValidate(String json) {
    AccountRequest accountRequest = null;
    accountRequest = JsonHelper.deserializeFromJson(json, AccountRequest.class);
    // after the JSON request is validated, we normalize and validate the JSON properties
    normalize(accountRequest);
    validate(accountRequest);
    return accountRequest;
  }

  private static void normalize(AccountRequest accountRequest) {
    if (accountRequest.getName() != null) {
      accountRequest.name = accountRequest.getName().trim();
    }
    if (accountRequest.getDescription() != null) {
      accountRequest.description = accountRequest.getDescription().trim();
    }
  }

  public static void validate(AccountRequest accountRequest) {
    Validator.validateLength(CHARACTER_ENCODING, accountRequest.name, JsonRequestErrorMessages.REQUEST_NAME_EMPTY,
        JsonRequestErrorMessages.REQUEST_NAME_TOO_LONG, Constants.NAME_FIELD_MIN_SIZE, Constants.NAME_FIELD_MAX_SIZE);
    Validator.validateLength(CHARACTER_ENCODING, accountRequest.description,
        JsonRequestErrorMessages.REQUEST_MIN_LENGTH_VALIDATION, JsonRequestErrorMessages.REQUEST_DESCRIPTION_TOO_LONG,
        Constants.DESCRIPTION_FIELD_MIN_SIZE, Constants.DESCRIPTION_FIELD_MAX_SIZE);
  }

  private static final String CHARACTER_ENCODING = "UTF-8";
  private String name;
  private String description;
  private Boolean enabled;
}
