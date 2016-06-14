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



var config = require('../configuration')
    ,fs    = require('fs')
    ,path  = require('path')
    ,elasticsearch = require('../elasticsearch')
    ,lifecycle = require('../lifecycle-manager/lifecycle')
    ,path      = require('path')


/**
*
*
*  Cache entry data-structure:
*
*  { "createdAt" : timestamp, "query" : {},"servers" : [], "archiveUrlInfos" : [ { "url" : "http://10.63.13.68:6556/api/logs/archive/dksdkfbds", "status" : "available/inprogress/not-started"}], "tags" : [], "isDeleted" : true/false, "localfspath": filepath of archive, "lifecycleConfiguration" : { "id" : "archive-id", "rule" : {} }}
*
*
**/


var ExportJobStatus = {
    EXPORT_JOB_NOT_STARTED: "Export Job Not Started",
    EXPORT_JOB_INPROGRESS : "Export Job In progress",
    EXPORT_JOB_COMPLETE : "Export Job Complete",
    EXPORT_JOB_DOESNOT_EXIST : "No Export Job Exists"
}



var CacheEntry = function() {
  var self = this;

  self.cacheEntryInfoInstance = {};
  self.cacheEntryInfoInstance.servers = [];
  self.cacheEntryInfoInstance.archiveUrlInfos = [];
  self.cacheEntryInfoInstance.tags = [""];
  self.cacheEntryInfoInstance.isDeleted = false;
  self.elasticsearch = elasticsearch;
}



CacheEntry.prototype.SetArchiveId = function(id) {
    var self = this;

    self.cacheEntryInfoInstance.archiveId = id;
}


CacheEntry.prototype.GetArchiveId = function() {
    var self = this;

    return self.cacheEntryInfoInstance.archiveId;
}


CacheEntry.prototype.SetCreatedAt = function(timestamp){
  var self = this;
  self.cacheEntryInfoInstance.createdAt = timestamp;
}


CacheEntry.prototype.GetCreatedAt = function(){
  var self = this;
  return self.cacheEntryInfoInstance.createdAt
}



CacheEntry.prototype.SetQueryFilter = function(query){
  var self = this
  if (query != undefined & query != ""){
      self.cacheEntryInfoInstance.query_filter = query;
  }
}

CacheEntry.prototype.GetQueryFilter = function(){
  var self = this;
  return self.cacheEntryInfoInstance.query_filter;

}

CacheEntry.prototype.AddServer = function(serverAddress) {
  var self = this;

  if ( !self.IsServerAlreadyPresent(serverAddress)) {
    self.cacheEntryInfoInstance.servers.push(serverAddress)
  }
}


CacheEntry.prototype.AddServers = function(servers) {
  var self = this;

  for (var index in servers) {
    self.AddServer(servers[index])
  }
}


CacheEntry.prototype.GetServers = function() {
  var self = this;

  return self.cacheEntryInfoInstance.servers;
}


CacheEntry.prototype.DeleteServer = function(serverAddress) {
  var self = this;

  if ( self.IsServerAlreadyPresent(serverAddress)) {
    var newserverList = []
    var oldservers = self.GetServers();
    for (var index in oldservers) {
      if (oldservers[index] == serverAddress){
         continue
      }
      newserverList.push(oldservers[index])
    }
    self.cacheEntryInfoInstance.servers = newserverList;
  }
}


CacheEntry.prototype.IsServerAlreadyPresent = function(serverAddress) {
  var self = this;
  if (self.cacheEntryInfoInstance.servers == undefined) {
    return false
  }

  var servers = self.cacheEntryInfoInstance.servers

  for (var index in servers) {
    if (servers[index] == serverAddress){
      return true;
    }
  }

  return false;
}


CacheEntry.prototype.AddArchiveUrlInfo = function(archiveUrlInfo) {
  var self = this;

  if ( ! self.IsArchiveUrlInfoAlreadyPresent(archiveUrlInfo)) {
      self.cacheEntryInfoInstance.archiveUrlInfos.push(archiveUrlInfo);
  }
}


CacheEntry.prototype.AddArchiveUrlInfos = function(archiveUrlInfos) {
  var self = this;

  if (archiveUrlInfos == undefined) {
    archiveUrlInfos = []
  }

  for (var index in archiveUrlInfos) {
    self.AddArchiveUrlInfo(archiveUrlInfos[index])
  }
}


CacheEntry.prototype.GetArchiveUrlInfos = function() {
  var self = this;

  return self.cacheEntryInfoInstance.archiveUrlInfos;
}


CacheEntry.prototype.UpdateArchiveUrlInfo = function(archiveUrlInfo) {
    var self = this;

    if (archiveUrlInfo == undefined || archiveUrlInfo.url == undefined || archiveUrlInfo.url == ""){
        return
    }

    if (! self.IsArchiveUrlInfoAlreadyPresent(archiveUrlInfo)) {
        self.cacheEntryInfoInstance.archiveUrlInfos.push(archiveUrlInfo)
        return
    }


    for (var index in self.cacheEntryInfoInstance.archiveUrlInfos){
        if ( self.cacheEntryInfoInstance.archiveUrlInfos[index].url == archiveUrlInfo.url) {
            self.cacheEntryInfoInstance.archiveUrlInfos[index] = archiveUrlInfo
            break;
        }
    }
}


CacheEntry.prototype.DeleteArchiveUrlInfo = function(archiveUrlInfo) {
  var self = this;

  if ( self.IsArchiveUrlInfoAlreadyPresent(archiveUrlInfo)) {
      var newarchiveUrlsList = []
      var oldarchiveUrlInfosList = self.GetArchiveUrlInfos();
      for ( var index in oldarchiveUrlInfosList){
          if (oldarchiveUrlInfosList[index].url == archiveUrlInfo.url){
            continue;
          }
          newarchiveUrlsList.push(oldarchiveUrlInfosList[index]);
      }
      self.cacheEntryInfoInstance.archiveUrlInfos = newarchiveUrlsList;
  }
}

CacheEntry.prototype.IsArchiveUrlInfoAlreadyPresent = function(archiveUrlInfo){
  var self = this;

  var archiveUrlInfos = self.GetArchiveUrlInfos();
  for (var index in archiveUrlInfos){
      if (archiveUrlInfos[index].url == archiveUrlInfo.url){
         return true;
      }
  }
  return false;
}


CacheEntry.prototype.IsAnyArchiveUrlInfoPresent = function(){
    var self = this

    var archiveUrlInfos = self.GetArchiveUrlInfos()
    for (var index in archiveUrlInfos){
        return true
    }

    return false
}


CacheEntry.prototype.GetArchiveUrlInfoForServer = function(serverAddress) {
    var self = this;

    var archiveUrlInfo = BuildArchiveUrlInfo(self.GetArchiveId(), serverAddress, "");
    var archiveUrlInfos = self.GetArchiveUrlInfos();

    for (var index in archiveUrlInfos){
        if ( archiveUrlInfos[index].url == archiveUrlInfo.url){
            archiveUrlInfo = archiveUrlInfos[index]
            return archiveUrlInfo
        }
    }

    return undefined
}


CacheEntry.prototype.AddTag = function(tag) {
   var self = this;

   if (self.cacheEntryInfoInstance.tags == undefined){
      self.cacheEntryInfoInstance.tags = [];
   }

   var tags = self.cacheEntryInfoInstance.tags;
   for (var index in tags) {
     if ( tags[index] == tag) {
       return
     }
   }

   self.cacheEntryInfoInstance.tags.push(tag);
}


CacheEntry.prototype.AddTags = function(tags) {
  var self = this;

  if (tags == undefined ) {
    tags = [];
  }

  for(var index in tags) {
    self.AddTag(tags[index])
  }

}


CacheEntry.prototype.GetTags = function() {
   var self = this;

   if (self.cacheEntryInfoInstance.tags == undefined) {
     self.cacheEntryInfoInstance.tags = [];
   }

   return self.cacheEntryInfoInstance.tags;
}


CacheEntry.prototype.DeleteAllTags = function() {
   var self = this;

   self.cacheEntryInfoInstance.tags = [];
}


CacheEntry.prototype.SetDeleteMarker = function(isDeleted) {
  var self = this;

  if (isDeleted == undefined) {
    isDeleted = true;
  }

  self.cacheEntryInfoInstance.isDeleted = isDeleted;
}


CacheEntry.prototype.IsEntryMarkedForDeletion = function() {
  var self = this;

  return self.cacheEntryInfoInstance.isDeleted;
}



CacheEntry.prototype.SetArchiveLocalFilesystemPath = function(path) {
  var self = this;

  self.cacheEntryInfoInstance.localfsPath = path;
}


CacheEntry.prototype.GetArchiveLocalFilesystemPath = function() {
  var self = this;

  return self.cacheEntryInfoInstance.localfsPath;
}


CacheEntry.prototype.AddLifecycleConfiguration = function(lifecycleConfiguration) {
   var self = this

   if (lifecycleConfiguration != undefined) {
     self.cacheEntryInfoInstance.lifecycleConfiguration = lifecycleConfiguration;
   }
}

CacheEntry.prototype.UpdateLifecycleConfiguration = function(lifecycleConfiguration) {
   var self = this;

   if(lifecycleConfiguration != undefined){
     self.cacheEntryInfoInstance.lifecycleConfiguration = lifecycleConfiguration;
   }
}

CacheEntry.prototype.GetLifecycleConfiguration = function() {
   var self = this;

   return self.cacheEntryInfoInstance.lifecycleConfiguration;
}

CacheEntry.prototype.DeleteLifecycleConfiguration = function() {
   var self = this;

   self.cacheEntryInfoInstance.lifecycleConfiguration = undefined;
}


CacheEntry.prototype.IsEntryExpired = function() {
  var self = this;
  var isExpired = false;

  var expiryDuration = 0
  var lifecycleConfiguration = new lifecycle.LifecycleConfiguration(self.GetArchiveId());
  lifecycleConfiguration.SetLifecycleConfiguration(self.GetLifecycleConfiguration());
  var lifecycleRule = new lifecycle.LifecycleRule();
  lifecycleRule.SetRule(lifecycleConfiguration.GetRule())
    
  if (lifecycleRule.GetExpiration().expiration.days != -1){
      expiryDuration = lifecycleRule.GetExpiration().expiration.days * 86400;
  }else {
    return isExpired;
  }


  var createdAtTimestamp = self.GetCreatedAt();
  if (createdAtTimestamp == undefined) {
    return isExpired;
  }

  var entryTimestamp = new Date(createdAtTimestamp)
  var currentTimestamp = new Date()

  var diff = Math.round((currentTimestamp.getTime() - entryTimestamp.getTime())/1000);

  console.log("Id:%s time diff:%s",self.GetArchiveId(),diff);

    if (diff > expiryDuration) {
    isExpired = true;
  }

  return isExpired;
}


CacheEntry.prototype.IsEntryReadyForTransition = function() {
 var self = this;
 var isTransitionReady = false;

 var transitionDuration = 0
 var lifecycleConfiguration = new lifecycle.LifecycleConfiguration(self.GetArchiveId());
 lifecycleConfiguration.SetLifecycleConfiguration(self.GetLifecycleConfiguration());
 var lifecycleRule = new lifecycle.LifecycleRule();
 lifecycleRule.SetRule(lifecycleConfiguration.GetRule())

 if (lifecycleRule.GetTransition().transition.days != undefined){
      transitionDuration = lifecycleRule.transition.days * 86400;
 }else {
   return isTransitionReady;
 }


 var createdAtTimestamp = self.GetCreatedAt();
 if (createdAtTimestamp == undefined) {
   return isTransitionReady;
 }

 var entryTimestamp = new Date(createdAtTimestamp)
 var currentTimestamp = new Date()

 var diff = Math.round((currentTimestamp.getTime() - entryTimestamp.getTime())/1000);

 if (diff > transitionDuration) {
   isTransitionReady = true;
 }

 return isTransitionReady;
}


CacheEntry.prototype.GetData = function() {
  var self = this;

  return self.cacheEntryInfoInstance;
}

/*
CacheEntry.prototype.MergeEntries = function( newEntry ) {
  var self = this;

  var newEntryInfoInstance = newEntry.cacheEntryInfoInstance;

  if (newEntryInfoInstance.servers != undefined && newEntryInfoInstance.GetServers().length != 0) {
    for (var index in newEntryInfoInstance.servers){
       self.AddServer(newEntryInfoInstance.servers[index]);
    }
  }

  if (newEntryInfoInstance.archiveUrls != undefined && newEntryInfoInstance.GetArchiveUrls().length != 0){
    for ( var index in newEntryInfoInstance.archiveUrls) {
      self.AddArchiveUrl(newEntryInfoInstance.archiveUrls[index])
    }
  }

  if (newEntryInfoInstance.tags != undefined && newEntryInfoInstance.GetTags().length != 0) {
    for (var index in newEntryInfoInstance.tags) {
      self.AddTag(newEntryInfoInstance.tags[index])
    }
  }

  if (newEntryInfoInstance.lifecycleConfiguration != undefined && newEntryInfoInstance.lifecycleConfiguration.expiration != undefined) {
      self.cacheEntryInfoInstance.lifecycleConfiguration.expiration = newEntryInfoInstance.lifecycleConfiguration.expiration;
  }

  if (newEntryInfoInstance.lifecycleConfiguration != undefined && newEntryInfoInstance.lifecycleConfiguration.transition != undefined) {
    self.cacheEntryInfoInstance.lifecycleConfiguration.transition = newEntryInfoInstance.lifecycleConfiguration.transition;
  }

}
*/

var BuildCacheEntryInstance = function(id, query, serverAddress, tag, expiryDays) {
    var cacheEntryInstance = new CacheEntry();


    if (id != undefined && id != ""){
      cacheEntryInstance.SetArchiveId(id)
    }


    if (query != undefined && id != ""){
        cacheEntryInstance.SetQueryFilter(query);
    }

    if (serverAddress != undefined){
       cacheEntryInstance.AddServer(serverAddress)
    }

    if (tag != undefined) {
         cacheEntryInstance.AddTag(tag);
    }

    if (id != undefined){
           var archiveUrlInfo = BuildArchiveUrlInfo(id, config.get("environment:container_host_address"), ExportJobStatus.EXPORT_JOB_NOT_STARTED);
           cacheEntryInstance.AddArchiveUrlInfo(archiveUrlInfo);

           var archiveLocalFSPath = BuildArchiveLocalFSPathFromId(id)
           cacheEntryInstance.SetArchiveLocalFilesystemPath(archiveLocalFSPath)
    }

    var lifecycleConfiguration = lifecycle.BuildLifecycleConfigurationObjectForExpiration(id, lifecycle.LifecycleConfigurationStatus.LIFECYCLE_ENABLED, expiryDays)

    cacheEntryInstance.AddLifecycleConfiguration(lifecycleConfiguration);

    return cacheEntryInstance;
}

var BuildArchiveUrlInfo = function(id, server, status) {
  if (server == undefined || server == "") {
    server = config.get("environment:container_host_address")
  }
  var archiveUrl = "http://" + server + ":" + config.get("express:port") + "/api/logs/archive/" + id;
  var archiveUrlInfo = {}
  archiveUrlInfo.url = archiveUrl
  if (status == undefined || status == "") {
      status = ExportJobStatus.EXPORT_JOB_NOT_STARTED
  }
  archiveUrlInfo.status = status
  return archiveUrlInfo
}


var BuildArchiveLocalFSPathFromId = function(id){
    var archiveLocalfsPath = config.get("static_values:elastic_index_store:directory") + "/" + id + ".json.tar.bz2";
    if ( path.resolve( archiveLocalfsPath ) != path.normalize(archiveLocalfsPath ) ) {
         archiveLocalfsPath = path.resolve(archiveLocalfsPath)
    }

    return archiveLocalfsPath
}


var BuildArchiveLocalFSPathForElasticdumpFromId = function(id){
    var archiveLocalfsPathForElasticdump = config.get("static_values:elastic_index_store:directory") + "/" + id + ".json";
    if ( path.resolve( archiveLocalfsPathForElasticdump ) != path.normalize(archiveLocalfsPathForElasticdump ) ) {
        archiveLocalfsPathForElasticdump = path.resolve(archiveLocalfsPathForElasticdump)
    }

    return archiveLocalfsPathForElasticdump
}


var BuildCacheEntryInstanceFromESDoc = function(document) {
    var cacheEntryInstance = new CacheEntry();
    var data = undefined
    if (document != undefined && document != ""){
        data = JSON.parse(document);
    }

    //console("ES doc data for build cache Entry:%s", document..)

    if (data != undefined ){


        if (data.archiveId != undefined ){
            cacheEntryInstance.SetArchiveId(data.archiveId)
        }

        if (data.createdAt != undefined ){
             cacheEntryInstance.SetCreatedAt(data.createdAt)
        }

        if (data.servers != undefined) {
          cacheEntryInstance.AddServers(data.servers)
        }

        if (data.archiveUrlInfos != undefined) {
          cacheEntryInstance.AddArchiveUrlInfos(data.archiveUrlInfos)
        }

        if (data.tags != undefined) {
          cacheEntryInstance.AddTags(data.tags)
        }

        if (data.isDeleted != undefined){
          cacheEntryInstance.SetDeleteMarker(data.isDeleted);
        }

        if (data.localfsPath != undefined) {
          cacheEntryInstance.SetArchiveLocalFilesystemPath(data.localfsPath)
        }

        if (data.lifecycleConfiguration != undefined){
          cacheEntryInstance.AddLifecycleConfiguration(data.lifecycleConfiguration)
        }

        if (data.query_filter != undefined ){
            cacheEntryInstance.SetQueryFilter(data.query_filter)
        }
    }

    return cacheEntryInstance
}


var IdStore = function() {
  var self = this;
  self.elasticsearch = new elasticsearch();
}




IdStore.prototype.AddEntry = function(id, cacheEntry) {
   var self = this;

   self.elasticsearch.putDocument(undefined, undefined, id, cacheEntry.GetData());
}


IdStore.prototype.UpdateEntry = function(id, updateCacheEntry){
  var self = this;

  self.elasticsearch.putDocument(undefined, undefined, id, updateCacheEntry.GetData());
}


IdStore.prototype.GetEntry = function(id) {
  var self = this;

  var doc = self.elasticsearch.getDocument(undefined, undefined, id);
    console.log("Document for id %s is doc:%s", id, doc)
  if (doc != undefined) {
    var cacheEntry = BuildCacheEntryInstanceFromESDoc(doc)
    if (!cacheEntry.IsEntryMarkedForDeletion()) {
         return cacheEntry;
    }
  }

  return undefined;
}


IdStore.prototype.IsEntryAlreadyPresent = function(id) {
  var self = this;

  var doc = self.elasticsearch.getDocument(undefined, undefined, id);
  if (doc != "") {
       var cacheEntry = BuildCacheEntryInstanceFromESDoc(doc);
       if (cacheEntry.IsEntryMarkedForDeletion()) {
         return false;
       }else {
         return true;
       }
  }else {
    return false;
  }
}


IdStore.prototype.GetAllEntries = function(pagesize, pagenumber, tags, getdeleteditems) {
  var self = this;

  var queryObject = BuildESQueryObjectForTagsFilter(tags);
  if (getdeleteditems == true){
      queryObject = BuildESQueryObjectForAllDeletedArchiveWithTagsFilter(tags)
  }

  var cacheEntryArray = [];

  var documents = this.elasticsearch.queryDocuments(undefined, undefined, queryObject, pagesize, pagenumber)
  console.log("documents received from ES:%s", JSON.stringify(documents))
  console.log("No of documents from ES:%d", documents.length)

  for (var index in documents) {
      console.log("doc:%s", documents[index])
    if (documents[index] != "") {
      var cacheEntry = BuildCacheEntryInstanceFromESDoc(JSON.stringify(documents[index]));
      if(getdeleteditems == true){
          if (cacheEntry.IsEntryMarkedForDeletion()) {
              cacheEntryArray.push(cacheEntry)
          }
      }else{
          if (!cacheEntry.IsEntryMarkedForDeletion()) {
              cacheEntryArray.push(cacheEntry)
          }
      }

    }
  }

  return cacheEntryArray;
}


IdStore.prototype.DeleteEntry = function(id){
  var self = this;

  var cacheEntryInstance = self.GetEntry(id);

  if (cacheEntryInstance != undefined) {
      cacheEntryInstance.SetDeleteMarker(true)
      self.UpdateEntry(id, cacheEntryInstance)
  }
}


IdStore.prototype.RemoveEntry = function(id){
  var self = this

  self.elasticsearch.deleteDocument(undefined, undefined, id);
}






//var BuildESQueryObjectForTagsFilter = function(tags) {
///* {"query" : { "query_string" : { "default_field" : "tags", "query" : "this AND that OR thus" }}} */
//
// if (tags == undefined || tags == "") {
//        tags = "*";
// }
//
//var queryObject = {"query" : { "query_string" : { "default_field" : "tags", "query" : tags }}}
//  //var queryObject = JSON.parse(queryString);
//  return queryObject;
//}


var BuildESQueryObjectForTagsFilter = function(tags) {
    /* { "query" :{ "filtered": { "query" : {"query_string" : { "default_field" : "tags", "query" : "*" }}, "filter": { "term":  { "isDeleted": "false" }}}}} */

    if (tags == undefined || tags == "") {
        tags = "*";
    }

    var queryObject = { "query" :{ "filtered": { "query" : {"query_string" : { "default_field" : "tags", "query" : tags }}, "filter": { "term":  { "isDeleted": false }}}}}
    //var queryObject = JSON.parse(queryString);
    return queryObject;
}


var BuildESQueryObjectForAllDeletedArchiveWithTagsFilter = function(tags) {
    /* { "query" :{ "filtered": { "query" : {"query_string" : { "default_field" : "tags", "query" : "*" }}, "filter": { "term":  { "isDeleted": "false" }}}}} */

    if (tags == undefined || tags == "") {
        tags = "*";
    }

    var queryObject = { "query" :{ "filtered": { "query" : {"query_string" : { "default_field" : "tags", "query" : tags }}, "filter": { "term":  { "isDeleted": true }}}}}
    //var queryObject = JSON.parse(queryString);
    return queryObject;
}

var DeleteRemoteFile = function( remote_url) {
    request({
        method: 'DELETE',
        uri: remote_url
    }, function (error, response, body) {
        if (error) {
            console.error('delete failed:', error);
            return "fail"
        }
        console.log('Delete successful!  Server responded with:', body);
        return "success"
    })
}


module.exports.IdStore = new IdStore();
module.exports.BuildCacheEntryInstance = BuildCacheEntryInstance;
module.exports.BuildArchiveUrlInfo = BuildArchiveUrlInfo
module.exports.ExportJobStatus = ExportJobStatus
module.exports.BuildArchiveLocalFSPathFromId =
module.exports.BuildArchiveLocalFSPathForElasticdumpFromId = BuildArchiveLocalFSPathForElasticdumpFromId
