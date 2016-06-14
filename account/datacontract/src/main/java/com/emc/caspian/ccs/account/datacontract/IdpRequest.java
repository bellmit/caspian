package com.emc.caspian.ccs.account.datacontract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.emc.caspian.ccs.account.types.IdpType;
import com.emc.caspian.ccs.account.types.QueryScope;
import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@JsonInclude(Include.NON_NULL)
public class IdpRequest {

  public IdpRequest() {
  }

  public IdpRequest(String id, IdpType type, IdpConfig idpConfig,
      String domainName) {
    this.id = id;
    this.type = type;
    this.idpConfig = idpConfig;
  }

  /**
   * Validates an IDPRequest object received from either a createIdp or an updateIdp call
   * 
   * @param createRequest Set true in case of a createIdp call, false in case of an updateIdp call
   */
  public static IdpRequest createAndValidate(String json, boolean createRequest) {

    boolean invalid = false;
    IdpRequest idp;
    // Create the IdpRequest object
    try {
      idp = JsonHelper.deserializeFromJson(json, IdpRequest.class);
    } catch (IllegalArgumentException iae) {
      // Add appropriate error message
      if (iae.getCause() instanceof InvalidFormatException) {
        InvalidFormatException ife = (InvalidFormatException) iae.getCause();
        // If enum mapping error occurs, check the class causing the
        // error
        if (ife.getTargetType().equals(QueryScope.class)) {
          throw new IllegalArgumentException(String.format(INVALID_QUERY_SCOPE_INPUT,
              StringUtils.join(QueryScope.values(), PARAMS_SEPARATOR)));
        } else if (ife.getTargetType().equals(IdpType.class)) {
          throw new IllegalArgumentException(String.format(INVALID_IDP_TYPE_INPUT,
              StringUtils.join(IdpType.values(), PARAMS_SEPARATOR)));
        }
      }
      throw new IllegalArgumentException(INVALID_JSON);
    }

    List<String> missingParams = new ArrayList<String>();
    if (idp.type == null) {
      missingParams.add(PARAMS_IDP_TYPE);
      invalid = true;
    }
    if (invalid) {
      throw new IllegalArgumentException(String.format(INVALID_PARAMS, StringUtils.join(missingParams, ",")));
    }
    // Validate IDP configuration parameters
    idp.idpConfig.validate();
    return idp;
  }
  
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("type")
  public IdpType getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(IdpType type) {
    this.type = type;
  }

  @JsonProperty("idp_info")
  public IdpConfig getIdpConfig() {
    return idpConfig;
  }

  @JsonProperty("idp_info")
  public void setIdpConfig(IdpConfig idpConfig) {
   this.idpConfig = idpConfig;
  }

  private String id;
  private IdpType type;
  private IdpConfig idpConfig;
  private static final String PARAMS_IDP_TYPE = "type";
  private static final String INVALID_PARAMS = "Insufficient parameters. Mandatory attribute(s) [%s] are missing";
  private static final String INVALID_QUERY_SCOPE_INPUT =
      "Invalid query_scope value. Permitted values are [%s]. (Case sensitive)";
  private static final String INVALID_IDP_TYPE_INPUT = "Invalid IDP type. Permitted types are [%s]. (Case sensitive)";
  private static final String INVALID_JSON = "Invalid JSON request";
  private final static String PARAMS_SEPARATOR = ",";
}
