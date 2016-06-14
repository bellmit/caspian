/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 *
 **/


/**
*  Lifecycle datastructure:
*
* { "lifecycleConfiguration":
*      "id" : "archiveId",
*      "rule" : { "id": "rule-xyz-1", "status" : "enabled" ,
*                 "transition" : { "days": 20 , "storage" : { "class" : "nfs" , "destination" : "x.y.z.w:/abc/def" , "authentication" : { "type" : "credentials-based", "credentials" : {"username" : "root", "password" : "ChangeMe"}}} } ,
*                 "expiration" : { "days" : 365}
*               }
* }
*
**/


var LifecycleConfiguration = function(archiveId) {
  var self = this;
  self.lifecycleConfigurationInstance = {}
  var metadata = {};
  metadata.id = archiveId;
  metadata.rule = {}
  self.lifecycleConfigurationInstance.lifecycleConfiguration = metadata;
}


LifecycleConfiguration.prototype.getId = function(){
  var self = this;
  return self.lifecycleConfigurationInstance.lifecycleConfiguration.metadata.id;
}

LifecycleConfiguration.prototype.SetRule = function(ruleObject){
  var self = this;

  if (ruleObject != undefined){
    self.lifecycleConfigurationInstance.lifecycleConfiguration.rule = ruleObject;
  }
  return
}


LifecycleConfiguration.prototype.GetRule = function() {
    var self = this

    return self.lifecycleConfigurationInstance.lifecycleConfiguration.rule
}

LifecycleConfiguration.prototype.GetLifecycleConfiguration = function() {
  var self = this;
  return self.lifecycleConfigurationInstance.lifecycleConfiguration;
}

LifecycleConfiguration.prototype.SetLifecycleConfiguration = function(lifecycleConfigurationInstance){
    var self = this

    if (lifecycleConfigurationInstance == undefined){
        lifecycleConfigurationInstance = {}
        lifecycleConfigurationInstance.lifecycleConfiguration = {}
    }

    self.lifecycleConfigurationInstance.lifecycleConfiguration = lifecycleConfigurationInstance
}


var LifecycleRule = function() {
  var self = this;
  self.defaultRuleId = "Rule-1";
  self.ruleInstance = {}
  var ruleMetadata = {};
  self.ruleInstance.rule = ruleMetadata;
  self.ruleInstance.rule.id = self.defaultRuleId
}

LifecycleRule.prototype.SetRule = function(lifecycleRuleInstance) {
    var self = this

    if (lifecycleRuleInstance == undefined){
        lifecycleRuleInstance = {}
    }

    self.ruleInstance.rule = lifecycleRuleInstance
}


LifecycleRule.prototype.GetRule = function() {
  var self = this;
  return self.ruleInstance.rule;
}


LifecycleRule.prototype.SetExpiration = function(ExpirationDays) {
  var self = this;
  self.ruleInstance.rule.expiration = {};
  self.ruleInstance.rule.expiration.days = ExpirationDays;
}


LifecycleRule.prototype.GetExpiration = function() {
   var self = this;

   var expirationInstance = {}
   expirationInstance.expiration = {}
   var days = -1;
   if (self.ruleInstance.rule.expiration != undefined && self.ruleInstance.rule.expiration.days != undefined) {
      days = self.ruleInstance.rule.expiration.days;
   }
   expirationInstance.expiration.days = days;

   return expirationInstance;
}


LifecycleRule.prototype.SetTransition = function(transitionDays, storageInformation){
  var self = this;

  var transitionActionInstance = {};
  if (storageInformaton == undefined){
    //We need to have a storage class definition for transition action
    return;
  }else{
    if (storageInformation.class == "nfs"){
     if (transitionDays != undefined && transitionDays != -1 && storageInformation.destination != undefined && storageInformation.authentication != undefined) {
            if (storageInformation.authentication.type != undefined && storageInformation.authentication.type == "credentials-based") {
              if (storageInformation.authentication.credentials != undefined && storageInformation.authentication.credentials.username != undefined && storageInformation.authentication.credentials.password != undefined){
                self.ruleInstance.rule.transition = storageInformation;
              }
            }
     }
    }
  }
}


LifecycleRule.prototype.GetTransition = function(){
  var self = this;
  var transitionInstance = {}
  transitionInstance.transition = {};
  if (self.ruleInstance.rule.transition != undefined) {
     transitionInstance.transition = self.ruleInstance.rule.transition;
  }
  return transitionInstance;
}

LifecycleRule.prototype.SetStatus = function(ruleId, ruleStatus) {
   var self = this;

    if (ruleStatus == undefined || ruleStatus == ""){
        ruleStatus = LifecycleConfigurationStatus.LIFECYCLE_DISABLED
    }

    if (ruleId == undefined || ruleId == ""){
        ruleId = self.defaultRuleId
    }

   if (self.ruleInstance.rule != undefined && (self.ruleInstance.rule.expiration != undefined || self.ruleInstance.rule.transition != undefined)){

      if (self.ruleInstance.rule.id == ruleId) {
        self.ruleInstance.rule.status = ruleStatus;
      }
   }
}

LifecycleRule.prototype.GetStatus = function(ruleId) {
   var self = this;

   if (ruleId == undefined || ruleId == ""){
     ruleId = self.defaultRuleId;
   }

   if (self.ruleInstance.rule.id == ruleId && self.ruleInstance.rule.status != undefined){
     return self.ruleInstance.rule.status
   }

   return LifecycleConfigurationStatus.LIFECYCLE_DISABLED
}


LifecycleRule.prototype.setStatus = function(status){
  var self = this;
  if (self.configuration.rule == undefined){
    self.configuration.rule = {};
  }

  if (status == undefined || status == ""){
      status = LifecycleConfigurationStatus.LIFECYCLE_DISABLED
  }

  self.configuration.rule.status = status;
}



var LifecycleConfigurationStatus = {
    LIFECYCLE_ENABLED : "enabled",
    LIFECYCLE_DISABLED : "disabled"
}



var BuildExpirationLifecycleRuleObject = function(no_of_days, status){
  if (no_of_days == undefined) {
    no_of_days = 1;
  }

  if (status == undefined || status == ""){
      status = LifecycleConfigurationStatus.LIFECYCLE_DISABLED
  }

  var lifecycleRuleInstance = new LifecycleRule();
  lifecycleRuleInstance.SetExpiration(no_of_days);
  lifecycleRuleInstance.SetStatus("", status);

  return lifecycleRuleInstance.GetRule();
}


var BuildLifecycleConfigurationObjectForExpiration = function(id, status, days) {
  var lifecycleConfigurationObject = new LifecycleConfiguration();
  lifecycleConfigurationObject.SetRule(BuildExpirationLifecycleRuleObject(days, status))

  return lifecycleConfigurationObject.GetLifecycleConfiguration();
}

module.exports.LifecycleConfiguration = LifecycleConfiguration
module.exports.LifecycleRule = LifecycleRule
module.exports.BuildLifecycleConfigurationObjectForExpiration = BuildLifecycleConfigurationObjectForExpiration
module.exports.LifecycleConfigurationStatus = LifecycleConfigurationStatus
