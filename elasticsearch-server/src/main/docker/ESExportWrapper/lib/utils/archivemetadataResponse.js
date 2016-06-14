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
 * Created by sharique on 28/12/15.
 *
 * Viewable fields of archive metadata
 *
 * { "archiveId" : "afsbdkfbsdkf",
 *   "created_at" : "28-12-2015T17:00:00"
 *   "servers" : ["10,63.13.68", "10.63.13.69"],
 *   "archiveUrlInfos" : [ {"url" : "http://10.63.13.68:6556/api/logs/archive/afsbdkfbsdkf", "status": "available"}, {"url" : "http://10.63.13.69:6556/api/logs/archive/afsbdkfbsdkf", "status" : "inprogress"}],
 *   "tags" : ["tag1", "tag2", "tag3" ],
 *   "lifecycleConfiguration" :
 *     { "id" : "archiveId",
 *       "rule" :
 *               { "id": "rule-xyz-1",
 *                 "status" : "enabled" ,
 *                 "transition" : { "days": 20 , "storage" : { "class" : "nfs" , "destination" : "x.y.z.w:/abc/def" , "authentication" : { "type" : "credentials-based", "credentials" : {"username" : "root", "password" : "ChangeMe"}}} } ,
 *                 "expiration" : { "days" : 365}
 *               }
 *     }
 * }
 *
 */


var ResponseCode = require('../utils/responseCode')


/**
 *
 * @constructor
 *
 */
var ArchiveMetadataResponse = function() {
   var self = this;
   self.archiveMetadata = {}
}



ArchiveMetadataResponse.prototype.SetArchiveId = function(id) {
    var self = this;
    if (id != undefined && id != "") {
        self.archiveMetadata.archiveId = id;
    }

}


ArchiveMetadataResponse.prototype.SetCreatedAt = function(createdat) {
    var self = this;
    if (createdat != undefined && createdat != "") {
        self.archiveMetadata.created_at = createdat;
    }
}


ArchiveMetadataResponse.prototype.SetQueryFilter = function(query){
    var self = this

    if (  query != undefined && query != ""  ) {
        self.archiveMetadata.query_filter = query
    }
}

ArchiveMetadataResponse.prototype.SetServers = function(servers) {
   var self = this
   if (servers != undefined && servers != "") {
       self.archiveMetadata.servers = servers;
   }else {
       self.archiveMetadata.servers = [];
   }
}


ArchiveMetadataResponse.prototype.SetArchiveUrlInfos = function(archiveUrlInfos) {
  var self = this
  if ( archiveUrlInfos != undefined && archiveUrlInfos != ""){
      self.archiveMetadata.archiveUrlInfos = archiveUrlInfos
  }else {
      self.archiveMetadata.archiveUrlInfos = []
  }
}


ArchiveMetadataResponse.prototype.SetTags = function(tags) {
  var self = this
  if (tags != undefined && tags != ""){
      self.archiveMetadata.tags = tags
  }else {
      self.archiveMetadata.tags = []
  }
}


ArchiveMetadataResponse.prototype.SetLifecycleConfiguration = function(lifecycleConfiguration) {
  var self = this
  if (lifecycleConfiguration != undefined && lifecycleConfiguration != ""){
      self.archiveMetadata.lifecycleConfiguration = lifecycleConfiguration;
  }
}


ArchiveMetadataResponse.prototype.SetStatus = function(status) {
    var self = this;


    console.log("status for setting :%s", status)

    if (status != undefined){
        self.archiveMetadata.status = status
    }else {
        self.archiveMetadata.status = "No information available"
    }
}

ArchiveMetadataResponse.prototype.GetArchiveMetadataResponse = function() {
    var self = this

    return self.archiveMetadata;
}






var NoArchiveMetadataResponse = function() {
    var self = this

    self.archiveMetadata = {}
}

NoArchiveMetadataResponse.prototype.SetArchiveId = function(id) {
    var self = this

    if (id != undefined && id != ""){
        self.archiveMetadata.archiveId = id
    }
}

NoArchiveMetadataResponse.prototype.SetStatus = function(status) {
    var self = this

    if (status != undefined && status != ""){
        self.archiveMetadata.status = status
    }else {
        self.archiveMetadata.status = ResponseCode.ARCHIVE_DOESNT_EXIST_ERROR
    }
}


NoArchiveMetadataResponse.prototype.GetArchiveMetadataResponse = function() {
    var self = this;

    return self.archiveMetadata;
}


var ArchiveLifecycleResponse = function() {
    var self = this

    self.archiveLifecycleResponse = {}
}


ArchiveLifecycleResponse.prototype.SetLifecycleConfiguration = function(lifecycleConfiguration){
    var self = this

    self.archiveLifecycleResponse.lifecycleConfiguration = lifecycleConfiguration
}


ArchiveLifecycleResponse.prototype.SetArchiveId = function(id){
    var self = this

    if (id == undefined) {
        id = ""
    }

    self.archiveLifecycleResponse.archiveId = id
}

ArchiveLifecycleResponse.prototype.SetStatus = function (status){
    var self = this

    if (status == undefined){
        status = ResponseCode.ARCHIVE_LIFECYCLE_DISABLED
    }

    self.archiveLifecycleResponse.status = status
}


ArchiveLifecycleResponse.prototype.GetArchiveLifecycleResponse = function() {
    var self = this

    return self.archiveLifecycleResponse
}



var NoArchiveLifecycleResponse = function() {
    var self = this

    self.archiveLifecycleResonse = {}
}


NoArchiveLifecycleResponse.prototype.SetArchiveId = function(id){
    var self = this

    if ( id == undefined){
        id = ""
    }

    self.archiveLifecycleResonse.archiveId = id
}

NoArchiveLifecycleResponse.prototype.SetStatus = function (status){
    var self = this

    if (status == undefined || status == ""){
        status = ResponseCode.ARCHIVE_LIFECYCLE_DISABLED
    }
    self.archiveLifecycleResonse.status = status
}


NoArchiveLifecycleResponse.prototype.GetArchiveLifecycleResponse = function() {
    var self = this

    return self.archiveLifecycleResonse
}



var ArchiveTagResponse = function() {
    var self = this

    self.archiveTagResponse = {}
}


ArchiveTagResponse.prototype.SetArchiveId = function(id){
    var self = this

    if (id == undefined){
        id = ""
    }

    self.archiveTagResponse.archiveId = id
}

ArchiveTagResponse.prototype.SetTag = function(tags){
    var self = this

    if (tags == undefined){
        tags = ""
    }

    self.archiveTagResponse.tag = tags
}


ArchiveTagResponse.prototype.GetArchiveTagResponse = function(){
    var self = this

    return self.archiveTagResponse
}


module.exports.ArchiveMetadataResponse = ArchiveMetadataResponse;
module.exports.NoArchiveMetadataResponse = NoArchiveMetadataResponse;
module.exports.ArchiveLifecycleResponse = ArchiveLifecycleResponse
module.exports.NoArchiveLifecycleResponse = NoArchiveLifecycleResponse
module.exports.ArchiveTagResponse = ArchiveTagResponse