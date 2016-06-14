/**
 *
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




var   ResponseCode = require('../utils/responseCode')
   ,  ExportJobStatus = require('../cache').ExportJobStatus
   ,  ArchiveMetadataResponse = require('../utils/archivemetadataResponse').ArchiveMetadataResponse
   ,  NoArchiveMetadataResponse = require('../utils/archivemetadataResponse').NoArchiveMetadataResponse
   ,  ArchiveLifecycleResponse = require('../utils/archivemetadataResponse').ArchiveLifecycleResponse
   ,  NoArchiveLifecycleResponse = require('../utils/archivemetadataResponse').NoArchiveLifecycleResponse
   ,  LifecycleConfigurationStatus = require('../lifecycle-manager/lifecycle').LifecycleConfigurationStatus
    , LifecycleConfiguration = require('../lifecycle-manager/lifecycle').LifecycleConfiguration
    , LifecycleRule = require('../lifecycle-manager/lifecycle').LifecycleRule
    , ArchiveTagResponse = require('../utils/archivemetadataResponse').ArchiveTagResponse


var ArchiveMetadataResponseBuilderFromCacheEntry = function (cacheEntry) {
    var response = {};
   if (cacheEntry != undefined) {

       if (!cacheEntry.IsEntryMarkedForDeletion()) {
           var body = new ArchiveMetadataResponse();
           body.SetArchiveId(cacheEntry.GetArchiveId());
           body.SetQueryFilter(cacheEntry.GetQueryFilter())
           body.SetCreatedAt(cacheEntry.GetCreatedAt())
           body.SetServers(cacheEntry.GetServers())
           body.SetArchiveUrlInfos(cacheEntry.GetArchiveUrlInfos())
           body.SetTags(cacheEntry.GetTags());
           body.SetLifecycleConfiguration(cacheEntry.GetLifecycleConfiguration())
           var status = ResponseCode.ARCHIVE_EXIST_BUT_NOT_AVAILABLE
           console.log("responsecode :%s", JSON.stringify(ResponseCode))
           var archiveUrlInfos = cacheEntry.GetArchiveUrlInfos()
          // console.log("archiveUrlInfos :%s", JSON.stringify(archiveUrlInfos))
           for (var index in archiveUrlInfos){
                if (archiveUrlInfos[index].status == ExportJobStatus.EXPORT_JOB_COMPLETE){
                    status = ResponseCode.ARCHIVE_AVAILABLE
                }
           }

           console.log("status for setting status in response :%s", status)
           body.SetStatus(status);
           response = body.GetArchiveMetadataResponse();
       }else {
           response = NoArchiveMetadataResponseBuilder(cacheEntry.GetArchiveId());
       }

   }else {
       response = NoArchiveMetadataResponseBuilder(cacheEntry.GetArchiveId());
   }
    return response;
}


var ArchiveMetadataListResponseBuilderFromCacheEntryList = function(cacheEntryList) {
    var response = [];

    if (cacheEntryList != undefined && cacheEntryList != "" && cacheEntryList != []) {
        response = []
        var cacheEntry
        var each_response
        for (var index in cacheEntryList) {
            cacheEntry = cacheEntryList[index];
            each_response = ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry);
            if (each_response != {} ) {
                response.push(each_response)
            }
        }
    }

    return response;
}


var NoArchiveMetadataResponseBuilder = function(id) {
    var response = {}

    if (id != undefined && id != ""){
        var body = new NoArchiveMetadataResponse();
        body.SetArchiveId(id)
        body.SetStatus(ResponseCode.ARCHIVE_DOESNT_EXIST_ERROR)
        response = body.GetArchiveMetadataResponse();
    }

    console.log("Building no archive metadata response object for archive id:%s", id)
    return response;
}


var ArchiveLifecycleResponseBuilderFromCacheEntry = function(cacheEntry) {
    var response = {}

    if (cacheEntry == undefined) {
        var body = new NoArchiveMetadataResponse()
        body.SetArchiveId(cacheEntry.GetArchiveId())
        body.SetStatus(ResponseCode.ARCHIVE_DOESNT_EXIST_ERROR)
        response = body.GetArchiveMetadataResponse();
    }else {
        var lifecycleConfigurationObject = new LifecycleConfiguration()
        lifecycleConfigurationObject.SetLifecycleConfiguration(cacheEntry.GetLifecycleConfiguration())
        if (lifecycleConfigurationObject.GetRule() == undefined) {
              response = NoArchiveLifecycleResponseBuilder(cacheEntry.GetArchiveId())
        }else {
            var body = new ArchiveLifecycleResponse();
            body.SetArchiveId(cacheEntry.GetArchiveId())
            body.SetLifecycleConfiguration(lifecycleConfigurationObject.GetLifecycleConfiguration())
            var lifecycleRule = new LifecycleRule()
            lifecycleRule.SetRule(lifecycleConfigurationObject.GetRule())
            if (lifecycleRule.GetStatus("") == LifecycleConfigurationStatus.LIFECYCLE_DISABLED) {
                body.SetStatus(ResponseCode.ARCHIVE_LIFECYCLE_DISABLED)
            }else {
                body.SetStatus(ResponseCode.ARCHIVE_LIFECYCLE_ENABLED)
            }
            response = body.GetArchiveLifecycleResponse()
        }
    }

    return response
}


var NoArchiveLifecycleResponseBuilder = function(id){
    var response = {}
    if (id == undefined){
        id = ""
    }
    var body = new NoArchiveLifecycleResponse();
    body.SetArchiveId(id)
    body.SetStatus(ResponseCode.ARCHIVE_LIFECYCLE_DISABLED)
    response = body.GetArchiveLifecycleResponse()

    return response;
}


var ArchiveTagResponseBuilderFromCacheEntry = function(cacheEntry) {
    var response = {}
    var body = {}
    if (cacheEntry ==  undefined) {
        body = new NoArchiveMetadataResponse("")
        response = body.GetArchiveMetadataResponse()
    }else {
        body = new ArchiveTagResponse()
        body.SetArchiveId(cacheEntry.GetArchiveId())
        body.SetTag(cacheEntry.GetTags())
        response = body.GetArchiveTagResponse();
    }
    return response;
}




module.exports.ArchiveMetadataResponseBuilderFromCacheEntry = ArchiveMetadataResponseBuilderFromCacheEntry;
module.exports.ArchiveMetadataListResponseBuilderFromCacheEntryList = ArchiveMetadataListResponseBuilderFromCacheEntryList
module.exports.NoArchiveMetadataResponseBuilder = NoArchiveMetadataResponseBuilder
module.exports.ArchiveLifecycleResponseBuilderFromCacheEntry = ArchiveLifecycleResponseBuilderFromCacheEntry
module.exports.NoArchiveLifecycleResponseBuilder = NoArchiveLifecycleResponseBuilder
module.exports.ArchiveTagResponseBuilderFromCacheEntry = ArchiveTagResponseBuilderFromCacheEntry
