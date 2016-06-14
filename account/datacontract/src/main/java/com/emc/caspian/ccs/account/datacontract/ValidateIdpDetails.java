package com.emc.caspian.ccs.account.datacontract;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ValidateIdpDetails {

    public ValidateIdpDetails() {
        this.code = null;
        this.message = null;
        this.description = null;
        this.field_name = null;
    }
    @JsonProperty("code")
    public String getCode() {
      return code;
    }

    @JsonProperty("code")
    public void setCode(String code) {
      this.code = code;
    }
      
    @JsonProperty("message")
    public String getMessage() {
      return message;
    }

    @JsonProperty("message")
    public void setMessage(String message) {
      this.message = message;
    }

    @JsonProperty("description")
    public String getDescription() {
      return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
      this.description = description;
    }

    @JsonProperty("field_name")
    public String getField_name() {
      return field_name;
    }

    @JsonProperty("field_name")
    public void setField_name(String field_name) {
      this.field_name = field_name;
    }

    private String code;
    private String message;
    private String description;
    private String field_name;
}
