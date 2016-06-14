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






var  Queue = require('../utils/queue').Queue
    ,fs    = require('fs')
    ,request = require('request')
    ,cacheModule = require('../cache')
    ,cache       = cacheModule.IdStore
    ,config = require('../configuration')
    ,uuid  = require('uuid')
    ,ExportJobStatus = require('../cache').ExportJobStatus



var RemoteArchiveDownloader = function() {
    var self = this

    self.DownloadJobQueue = new Queue()
}



RemoteArchiveDownloader.prototype.AddJob = function(Job){
    var self = this
   //   var JobInfo = { "jobid" : archiveId, "archiveId" : archiveId, "downloadUrl" : remote_url, "output" : { "type" : "file" , "filename" : outputFilename} }

    if (self.DownloadJobQueue == undefined) {
        self.DownloadJobQueue = []
    }

    if (Job!= undefined && !self.IsJobAlreadyExists(Job.GetJobId())) {
        console.log("remote archive download job has been enqueued in the waiting queue. The job is requested for archiveId:%s and the corresponding jobid is:%s", Job.GetArchiveId(), Job.GetJobId())
        self.DownloadJobQueue.enqueue(Job)
    }

    return Job
}


RemoteArchiveDownloader.prototype.IsJobAlreadyExists = function(jobId) {
    var self = this
    var index = 0;
    var job = undefined
    while(true){
       if (index == self.DownloadJobQueue.size()){
           break;
       }

       job = self.DownloadJobQueue.peek(index)

       if (job.GetJobId() != undefined && job.GetJobId() == jobId) {
           console.log("There is already a remote archive download job for archiveId:%s . So job with jobid:%s is rejected !!!", job.GetArchiveId(), job.GetJobId())
           return true
       }

       index++
    }

    return false;

}


RemoteArchiveDownloader.prototype.RunAllJobs = function() {
   var self = this;

    console.log("Running all remote archive download job!!")
    while(true){
        if (self.DownloadJobQueue.size() == 0)  {
            break;
        }

        var Job = self.DownloadJobQueue.dequeue()

        if (Job == undefined){
            break
        }
        self.RunJob(Job)
    }
}


RemoteArchiveDownloader.prototype.RunJob = function(Job) {

    var _type = Job.GetOutputType()
    var outstream =   undefined

    if (_type != undefined && _type == "file"){
        outstream = fs.createWriteStream(Job.GetOutputFilename() + ".remote.tmp")
    }


    var RemoveArchiveUrlInfoEntry = function(archiveId) {

        var cacheEntryInstance = cache.GetEntry(archiveId)
        if (cacheEntryInstance != undefined && cacheEntryInstance.GetArchiveId() == archiveId) {
            var archiveInfo = cacheModule.BuildArchiveUrlInfo(cacheEntryInstance.GetArchiveId(), config.get("environment:container_host_address"), "")
            cacheEntryInstance.DeleteArchiveUrlInfo(archiveInfo)
            if (cacheEntryInstance.IsAnyArchiveUrlInfoPresent()) {
                cache.UpdateEntry(cacheEntryInstance.GetArchiveId(), cacheEntryInstance);
            }else{
                cache.RemoveEntry(cacheEntryInstance.GetArchiveId())
            }
        }

    }


    var  AddArchiveUrlInfoEntry = function(archiveId) {

        var cacheEntryInstance = cache.GetEntry(archiveId)
        if (cacheEntryInstance != undefined && cacheEntryInstance.GetArchiveId() == archiveId) {
            var archiveInfo = cacheModule.BuildArchiveUrlInfo(Job.GetArchiveId(), config.get("environment:container_host_address"), cacheModule.ExportJobStatus.EXPORT_JOB_COMPLETE)
            cacheEntryInstance.UpdateArchiveUrlInfo(archiveInfo)
            cache.UpdateEntry(cacheEntryInstance.GetArchiveId(), cacheEntryInstance)
        }

    }


   // var outputStream = fs.createWriteStream(Job.output.filename + '.tmp')
    var sendreq = request.get(Job.GetDownloadUrl())

    sendreq.on('response', function(response){
        if (response.statusCode !== 200) {
            RemoveArchiveUrlInfoEntry(Job.GetArchiveId())
        }else {
            response.pipe(outstream)
        }
    });


    sendreq.on('error', function(err){
        console.log("Error:" + err.message)
        RemoveArchiveUrlInfoEntry(Job.GetArchiveId())
    })


    outstream.on('finish', function(){
        outstream.close()
        var tempFilename = Job.GetOutputFilename() + '.remote.tmp'
        var finalFilename = Job.GetOutputFilename()

        fs.rename(tempFilename, finalFilename, function (err) {
            var cacheEntryInstance = cache.GetEntry(Job.GetArchiveId())
            console.log("Renaming file:%s to :%s", tempFilename, finalFilename)
            if (err) {
                console.log("Unable to rename a file from:%s to:%s", tempFilename, finalFilename)
                RemoveArchiveUrlInfoEntry(Job.GetArchiveId())
                if (fs.existsSync(tempFilename)){
                    fs.unlink(tempFilename);
                }
                return
            }
            AddArchiveUrlInfoEntry(Job.GetArchiveId())
            console.log('renamed complete');
        });

    })

    outstream.on('error', function(err){
        RemoveArchiveUrlInfoEntry(Job.GetArchiveId())

    })
}



var DownloadJobBuilderFromCacheEntry = function(cacheEntry){
    // { "jobid" : archiveId, "archiveId" : archiveId, "downloadUrl" : remote_url, "output" : { "type" : "file" , "filename" : outputFilename} }
    var Job = new RemoteArchiveDownloadJob()

    var archiveUrlInfo
    for (var index in cacheEntry.GetArchiveUrlInfos()){
        archiveUrlInfo = cacheEntry.GetArchiveUrlInfos()[index]
        if (archiveUrlInfo.status = ExportJobStatus.EXPORT_JOB_COMPLETE){
            break
        }
    }

     if (archiveUrlInfo == undefined){
         return undefined
     }

    Job.SetJobId(cacheEntry.GetArchiveId())
    Job.SetArchiveId(cacheEntry.GetArchiveId())
    Job.SetDownloadUrl(archiveUrlInfo.url)
    Job.SetOutputType("file")
    Job.SetOutputFilename(cacheEntry.GetArchiveLocalFilesystemPath())

    console.log("Remote archive job is built. Details are:%s", JSON.stringify(Job.jobinfo))

    return Job
}


var RemoteArchiveDownloadJob = function() {
    var self = this;

    self.jobinfo = {}
}

RemoteArchiveDownloadJob.prototype.SetJobId = function(jobid){
    var self = this;

    if (jobid == undefined || jobid == ""){
        jobid = uuid.v1()
    }

    self.jobinfo.jobid = jobid

}

RemoteArchiveDownloadJob.prototype.GetJobId = function(){
    var self = this;

    return self.jobinfo.jobid
}

RemoteArchiveDownloadJob.prototype.SetArchiveId = function(archiveId){
    var self = this
    self.jobinfo.archiveId = archiveId
}

RemoteArchiveDownloadJob.prototype.GetArchiveId = function(){
    var self = this
    return self.jobinfo.archiveId;

}

RemoteArchiveDownloadJob.prototype.SetDownloadUrl = function(url){
    var self = this;

    self.jobinfo.downloadUrl = url

}

RemoteArchiveDownloadJob.prototype.GetDownloadUrl = function(){
    var self = this

    return self.jobinfo.downloadUrl

}

RemoteArchiveDownloadJob.prototype.SetOutputType = function(outputType){
    var self = this

    if (self.jobinfo.output == undefined || self.jobinfo.output == ""){
        self.jobinfo.output = {}
    }

    if (outputType == undefined || outputType == ""){
        outputType = "file"
    }

    self.jobinfo.output.type = outputType

}

RemoteArchiveDownloadJob.prototype.GetOutputType = function(){
    var self = this
    if (self.jobinfo.output != undefined){
        return self.jobinfo.output.type
    }else{
        return undefined
    }

}

RemoteArchiveDownloadJob.prototype.SetOutputFilename = function(filename){
    var self = this

    if (filename == undefined || filename == ""){
        return
    }

    if (self.jobinfo.output == undefined || self.jobinfo.output == ""){
        self.jobinfo.output = {}
    }

    if (self.jobinfo.output.type == undefined || self.jobinfo.output.type == ""){
        self.jobinfo.output.type = "file"
    }

    self.jobinfo.output.filename = filename

}

RemoteArchiveDownloadJob.prototype.GetOutputFilename = function(){
    var self = this;

    if (self.jobinfo.output != undefined){
        return self.jobinfo.output.filename
    }else{
        return undefined
    }

}


module.exports.RemoteArchiveDownloader = new RemoteArchiveDownloader()
module.exports.RemoteArchiveDownloadJobBuilder = DownloadJobBuilderFromCacheEntry;