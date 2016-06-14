package com.emc.caspian.ccs.account.datacontract;


import com.emc.caspian.ccs.common.utils.JsonHelper;
import com.emc.caspian.ccs.common.utils.Validator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonRootName("domain")
public class Domain {

  @JsonInclude(value = Include.NON_EMPTY)
  private String id;

  @JsonInclude(value = Include.NON_EMPTY)
  private Link link;

  @JsonInclude(value = Include.NON_EMPTY)
  private String name;

  @JsonInclude(value = Include.NON_NULL)
  private String description;

  @JsonInclude(value = Include.NON_EMPTY)
  private Boolean enabled;
  
  private static final String CHARACTER_ENCODING = "UTF-8";

 
  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("link")
  public Link getLink() {
    return link;
  }

  @JsonProperty("link")
  public void setLink(Link link) {
    this.link = link;
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

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }
  
  // Any JSON processing exception thrown by de-serialization will be caught by the upper layers
  public static Domain createAndValidate(String json) {
    Domain domainRequest = null;
    domainRequest = JsonHelper.deserializeFromJson(json, Domain.class);
    // after the JSON request is validated, we normalize and validate the JSON properties
    normalize(domainRequest);
    validate(domainRequest);
    return domainRequest;
  }

  // Any JSON processing exception thrown by de-serialization will be caught by the upper layers
  public static Domain updateAndValidate(String json) {
    Domain domainRequest = null;
    domainRequest = JsonHelper.deserializeFromJson(json, Domain.class);
    // after the JSON request is validated, we normalize and validate the JSON properties
    normalize(domainRequest);
    validateUpdate(domainRequest);
    return domainRequest;
  }

  private static void normalize(Domain domainRequest) {
    if (domainRequest.getName() != null) {
      domainRequest.name = domainRequest.getName().trim();
    }
    if (domainRequest.getDescription() != null) {
      domainRequest.description = domainRequest.getDescription().trim();
    }
  }

  public static void validate(Domain domainRequest) {
    Validator.validateLength(CHARACTER_ENCODING, domainRequest.name, JsonRequestErrorMessages.REQUEST_NAME_EMPTY,
        JsonRequestErrorMessages.REQUEST_NAME_TOO_LONG, Constants.NAME_FIELD_MIN_SIZE, Constants.NAME_FIELD_MAX_SIZE);
    Validator.validateLength(CHARACTER_ENCODING, domainRequest.description,
        JsonRequestErrorMessages.REQUEST_DESCRIPTION_TOO_LONG, JsonRequestErrorMessages.REQUEST_MIN_LENGTH_VALIDATION,
        Constants.DESCRIPTION_FIELD_MIN_SIZE, Constants.DESCRIPTION_FIELD_MAX_SIZE);
  }

  public static void validateUpdate(Domain domainRequest) {
    Validator.validateLength(CHARACTER_ENCODING, domainRequest.name,
        JsonRequestErrorMessages.REQUEST_NAME_TOO_LONG, JsonRequestErrorMessages.REQUEST_MIN_LENGTH_VALIDATION,
        Constants.DOMAIN_UPDATE_NAME_FIELD_MIN_SIZE, Constants.NAME_FIELD_MAX_SIZE);
    Validator.validateLength(CHARACTER_ENCODING, domainRequest.description,
        JsonRequestErrorMessages.REQUEST_DESCRIPTION_TOO_LONG, JsonRequestErrorMessages.REQUEST_MIN_LENGTH_VALIDATION,
        Constants.DESCRIPTION_FIELD_MIN_SIZE, Constants.DESCRIPTION_FIELD_MAX_SIZE);
  }
}
