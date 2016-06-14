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


var express = require('express')
  , http = require('http')
  , config = require('../configuration')
  , app = express()
  , elasticdumper = require('../elasticdump')
  , cacheModule   = require('../cache')
  , uuid = require('uuid')
  , fs   = require('fs')
  , path = require('path')
  , request = require('request')
  , url = require('url')
  , sync_request = require('sync-request')
  , response_builder = require('../utils/responseBuilder')
  , export_job_status = cacheModule.ExportJobStatus
  , remoteArchiveDownloader = require('../utils/remoteArchiveDownloader').RemoteArchiveDownloader
  , remoteArchiveDownloadJobBuilder = require('../utils/remoteArchiveDownloader').RemoteArchiveDownloadJobBuilder
    , lifecycle_manager_module = require('../lifecycle-manager')



var cache = cacheModule.IdStore;

var archiveExpirationJob = lifecycle_manager_module.BuildScheduledDocumentExpirationJob(config.get("static_values:expirationjob_cron_timestamp"))
var archiveCleanupJob = lifecycle_manager_module.BuildScheduledArchiveCleanupJob(config.get("static_values:cleanupjob_cron_timestamp"))
lifecycle_manager_module.LifecycleManager.AddJob(archiveExpirationJob)
lifecycle_manager_module.LifecycleManager.AddJob(archiveCleanupJob)
lifecycle_manager_module.LifecycleManager.Run()



var allowCrossDomain = function(req, res, next) {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Methods', 'GET,PUT,POST,DELETE,OPTIONS');
    res.header('Access-Control-Allow-Headers', 'Content-Type, Authorization, Content-Length, X-Requested-With');

    // intercept OPTIONS method
    if ('OPTIONS' == req.method) {
      res.send(200);
    }
    else {
      next();
    }
};



app.configure(function(){
  app.use(express.bodyParser());
  app.use(allowCrossDomain);
  app.use(app.router);
});

app.set('port', config.get("express:port"));

app.get('/api/logs/status', function(req, res){
  res.json(200, 'Export Api working');
});

//Create a new archive
// query param :
//   1. tag : to add tag to the archive. Default value is empty string
//   2. expiry_days: set the expiry duration of archive created on the server in terms of number of days
app.post('/api/logs/archive', function(req, res){
   var tag = ""
   var expiryDays = 1;

   if (req.query.tag != undefined) {
     tag = req.query.tag;
   }

   if (req.query.expiry_days != undefined){
     expiryDays = req.query.expiry_days;
   }

   var queryFilterObject = req.body;
   var id = GetUniqueId();
   var localendpoint = config.get("environment:container_host_address")
   var cacheEntry = cacheModule.BuildCacheEntryInstance(id, queryFilterObject, localendpoint, tag, expiryDays);
   var options = {};
   options.input =  config.get("elasticsearch:url");
   options.output = cacheModule.BuildArchiveLocalFSPathForElasticdumpFromId(cacheEntry.GetArchiveId())
   options.searchBody = queryFilterObject;
   options.archiveId = id;
   var elasticdumperInstance = new elasticdumper(options.input, options.output, options);
   cacheEntry.UpdateArchiveUrlInfo(cacheModule.BuildArchiveUrlInfo(id, localendpoint, export_job_status.EXPORT_JOB_INPROGRESS))
   cache.AddEntry(id, cacheEntry);
   res.send(response_builder.ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry))

});


app.get('/api/logs/archive/:id' , function(req, res) {

    var id    = req.params.id;
    var cacheEntry = cache.GetEntry(id);
    var response = {}

    if (cacheEntry == undefined || cacheEntry.IsEntryMarkedForDeletion() || cacheEntry.GetArchiveUrlInfos().length == 0) {
      response = response_builder.NoArchiveMetadataResponseBuilder(id)
      res.json(response.status.statusCode, response);
      return
    }


    var logfilePathBzip = cacheEntry.GetArchiveLocalFilesystemPath();
    var localendpoint = config.get("environment:container_host_address");


    var localArchiveUrlInfo = cacheEntry.GetArchiveUrlInfoForServer(localendpoint);

    if ( localArchiveUrlInfo != undefined && localArchiveUrlInfo.status == export_job_status.EXPORT_JOB_COMPLETE ) {

            if (fs.statSync(cacheEntry.GetArchiveLocalFilesystemPath())){
                console.log("Archive is available :%s", localArchiveUrlInfo.url)
                res.download(cacheEntry.GetArchiveLocalFilesystemPath())
            }else{
                var archiveUrlInfo = cacheEntry.GetArchiveUrlInfoForServer(localendpoint);
                cacheEntry.DeleteArchiveUrlInfo(archiveUrlInfo)
                cacheEntry.DeleteServer(localendpoint)
                if (!cacheEntry.IsAnyArchiveUrlInfoPresent){
                    cacheEntry.SetDeleteMarker(true)
                }
                cache.UpdateEntry(cacheEntry.GetArchiveId(), cacheEntry)
                var response = response_builder.ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry)
                res.json(response.status.statusCode, response)
                return
            }

            return
    }else {
         var response = response_builder.ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry)
         if (localArchiveUrlInfo != undefined) {
               res.json(response.status.statusCode, response);
               console.log("availablability status of archiveId:%s -%s", id, JSON.stringify(response.status));
               return
         }else {
               var archiveUrlInfos = cacheEntry.GetArchiveUrlInfos()
               for (var index in archiveUrlInfos) {
                   var archiveUrlInfo = archiveUrlInfos[index]
                   var archiveUrlInfos = cacheEntry.GetArchiveUrlInfos()
                   for (var index in archiveUrlInfos) {
                       var archiveUrlInfo = archiveUrlInfos[index]
                       if (archiveUrlInfo.status == export_job_status.EXPORT_JOB_COMPLETE) {
                           cacheEntry.AddServer(localendpoint)
                           cacheEntry.AddArchiveUrlInfo(cacheModule.BuildArchiveUrlInfo(id, localendpoint, export_job_status.EXPORT_JOB_INPROGRESS))
                           cache.UpdateEntry(id, cacheEntry);
                           remoteArchiveDownloader.AddJob(remoteArchiveDownloadJobBuilder(cacheEntry, archiveUrlInfo))
                           remoteArchiveDownloader.RunAllJobs()
                           //DownloadRemoteFileToLocal(id, archiveUrl.url, cacheEntry.GetArchiveLocalFilesystemPath())
                           response = response_builder.ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry)
                           res.json(response.status.statusCode, response);
                           console.log("availablability status of archiveId:%s -%s", id, JSON.stringify(response.status));
                           return
                       }
                   }
               }
               response = response_builder.ArchiveMetadataResponseBuilderFromCacheEntry(cacheEntry)
               res.json(response.status.statusCode, response)
               console.log("availablability status of archiveId:%s -%s", id, JSON.stringify(response.status));
               return
         }
    }

  });


//GET list of archives
// pagesize = -1 and pagenumber = -1 returns all archives
// tag is used to filter the list of archives
app.get('/api/logs/archive', function(req, res){
  var pagesize = 10;
  var pagenumber = 0;
  var tag = "";
  var documentList = [];

  if (req.query.pagesize != undefined) {
     pagesize = req.query.pagesize;
  }

  if (req.query.pagenumber != undefined) {
     pagenumber = req.query.pagenumber;
  }

  if (req.query.tag != undefined) {
     tag = req.query.tag
  }

  var cacheEntriesFromES = cache.GetAllEntries(pagesize, pagenumber, tag);
  console.log("No of cache entries:%d", cacheEntriesFromES.length);

  res.json(response_builder.ArchiveMetadataListResponseBuilderFromCacheEntryList(cacheEntriesFromES));
  return;
});


//Delete an archive and its associated metadata
app.delete('/api/logs/archive/:id' , function(req, res) {
    var id  = req.params.id;

    var cacheEntry = cache.GetEntry(id);

    if (cacheEntry == undefined) {
      var response = response_builder.NoArchiveMetadataResponseBuilder(id)
      res.json(response.status.statusCode, response);
      return
    }

    cacheEntry.SetDeleteMarker(true)
    cache.UpdateEntry(cacheEntry.GetArchiveId(),cacheEntry)

    res.json(200, "Archive deleted")
    return



  });

//Get lifecycle configuration
app.get('/api/logs/archive/:id/lifecycle', function(req, res) {
    var id    = req.params.id;
    var cacheEntry = cache.GetEntry(id);
    var response = response_builder.ArchiveLifecycleResponseBuilderFromCacheEntry(cacheEntry)
    return res.json(response.status.statusCode, response)
});


//Update lifecycle configuration
app.put('/api/logs/archive/:id/lifecycle', function(req, res){
    var id    = req.params.id;
    var cacheEntry = cache.GetEntry(id);


    var response = {}

    if (cacheEntry == undefined || cacheEntry.IsEntryMarkedForDeletion()) {
        response = response_builder.NoArchiveMetadataResponseBuilder(id)
        res.json(response.status.statusCode, response);
        return
    }

    var lifecycleConfiguration = req.body

    cacheEntry.SetLifecycleConfiguration(lifecycleConfiguration)
    cache.UpdateEntry(id, cacheEntry)

    response = response_builder.ArchiveLifecycleResponseBuilderFromCacheEntry(cacheEntry)
    res.json(response.status.statusCode, response)
    return

});


//Get all tags associated with the archive
app.get('/api/logs/archive/:id/tags', function(req, res) {
    var id    = req.params.id;
    var cacheEntry = cache.GetEntry(id);


    var response = {}

    if (cacheEntry == undefined || cacheEntry.IsEntryMarkedForDeletion()) {
        response = response_builder.NoArchiveMetadataResponseBuilder(id)
        res.json(response.status.statusCode, response);
        return
    }

    response = response_builder.ArchiveTagResponseBuilderFromCacheEntry(cacheEntry)
    res.json(response.status.statusCode, response)

    return

});



var GetUniqueId = function() {
    var id = uuid.v4();
    while(true){
        var cacheEntry = cache.GetEntry(id);
        if (cacheEntry == undefined){
            break;
        }else{
            id = uuid.v4();
        }
    }
    return id;
}

http.createServer(app).listen(app.get('port'));

module.exports = app;
