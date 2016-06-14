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



var   lifecycle = require('./lifecycle')
    , CronJob   = require('cron').CronJob
    , cacheModule = require('../cache')
    , Queue = require('../utils/queue').Queue
    , uuid = require('uuid')
    , DistributedLock = require('../lock/DistributedLock')
    , FileCleanup = require('../utils/FileCleanup')
    , config = require('../configuration')



var cache = cacheModule.IdStore


/**
 *
 * This module is used for setting delete marker for all the expired archives using cronjob
 *
 */

var LifecycleManager = function() {
    var self = this

    self.LifecycleJobQueue = new Queue()
}


LifecycleManager.prototype.AddJob = function(Job) {
    var self = this

    if (Job != undefined){
        self.LifecycleJobQueue.enqueue(Job)
    }

}


LifecycleManager.prototype.Run = function() {
     var self = this

     if (self.LifecycleJobQueue != undefined && !self.LifecycleJobQueue.isEmpty()){
         while(true){
             if (self.LifecycleJobQueue.isEmpty()){
                 break
             }

             var job = self.LifecycleJobQueue.dequeue()
             job.Run()
         }
     }

}





var DocumentExpirationJob = function() {
    var self = this

    self.Info = {}
    self.Info.JobId =  uuid.v4();

}

DocumentExpirationJob.prototype.GetJobId = function() {
    var self = this

    if (self.Info.JobId == undefined || self.Info.JobId == ""){
        self.Info.JobId = uuid.v4()
    }

    return self.Info.JobId
}

DocumentExpirationJob.prototype.equals = function(JobId) {
    var self = this

    if (JobId == undefined){
        JobId = ""
    }

    if (self.GetJobId() == JobId){
        return true
    }

    return false;
}


DocumentExpirationJob.prototype.SetRunStrategy = function(strategy) {
    var self = this

    if (strategy == undefined ){
        strategy = new JobRunStrategy()
        strategy.SetType()
        strategy.SetExecutor()
    }

    self.Info.JobRunStrategy = strategy
}


DocumentExpirationJob.prototype.GetRunStrategy = function() {
    var self = this

    return self.Info.JobRunStrategy
}


DocumentExpirationJob.prototype.Run = function() {
    var self = this;

    if (self.GetRunStrategy() == undefined){
        self.SetRunStrategy()
    }

    var runStrategy = self.GetRunStrategy();
    runStrategy.GetExecutor().execute(self.RunExpirationJob)
}


DocumentExpirationJob.prototype.RunExpirationJob = function() {
    var pagesize = 50
    var pagenumber = 0

    console.log("Document Expiration triggered")

    while(true){
        var cacheEntries = cache.GetAllEntries(pagesize, pagenumber,"");
        for (var index in cacheEntries){
            var cacheEntry = cacheEntries[index]

            console.log("In lifecycle manager, the value of cacheentry is: %s",JSON.stringify(cacheEntry.GetData()));

            if (cacheEntry.IsEntryExpired()) {
                cacheEntry.SetDeleteMarker(true)
                cache.UpdateEntry(cacheEntry.GetArchiveId(), cacheEntry)
            }
        }

        if (cacheEntries.length == 0) {
            break
        }

        pagenumber += 1
    }
}



var BuildScheduledDocumentExpirationJob = function(crontimeString) {
    var docExpirationJob = new DocumentExpirationJob()
    var strategy = new JobRunStrategy()
    var executor = new ScheduledExecutor()
    executor.SetCronTime(crontimeString)
    strategy.SetType(JobRunStrategyType.CRONJOB_TYPE_RUN)
    strategy.SetExecutor(executor)
    docExpirationJob.SetRunStrategy(strategy)

    return docExpirationJob
}


var ArchiveCleanupJob = function() {
    var self = this

    self.Info = {}
    self.Info.JobId =  uuid.v4();
}

ArchiveCleanupJob.prototype.GetJobId = function(){
    var self = this

    if (self.Info.JobId == undefined || self.Info.JobId == ""){
        self.Info.JobId = uuid.v4()
    }

    return self.Info.JobId
}

ArchiveCleanupJob.prototype.equals = function() {
    var self = this

    if (JobId == undefined){
        JobId = ""
    }

    if (self.GetJobId() == JobId){
        return true
    }

    return false;
}


ArchiveCleanupJob.prototype.SetRunStrategy = function(strategy) {
    var self = this

    if (strategy == undefined ){
        strategy = new JobRunStrategy()
        strategy.SetType()
        strategy.SetExecutor()
    }

    self.Info.JobRunStrategy = strategy
}

ArchiveCleanupJob.prototype.GetRunStrategy = function(){
    var self = this

    return self.Info.JobRunStrategy
}

ArchiveCleanupJob.prototype.Run = function() {
    var self = this;

    if (self.GetRunStrategy() == undefined){
        self.SetRunStrategy()
    }

    var runStrategy = self.GetRunStrategy();
    runStrategy.GetExecutor().execute(self.RunAllDeletedArchiveCleanupJob)
}



ArchiveCleanupJob.prototype.RunAllDeletedArchiveCleanupJob = function() {
    var pagesize = -1
    var pagenumber = -1

    console.log("Document cleanup triggered")
    var localendpoint = config.get("environment:container_host_address");
    var cacheEntries = cache.GetAllEntries(pagesize, pagenumber,"", true);
        for (var index in cacheEntries){
            var cacheEntry = cacheEntries[index]
            var archiveUrlInfo = cacheEntry.GetArchiveUrlInfoForServer(localendpoint);
            if (archiveUrlInfo != undefined){
                if(!cacheEntry.IsEntryExpired() && archiveUrlInfo.status !=cacheModule.ExportJobStatus.EXPORT_JOB_COMPLETE){
                    continue;
                }
                DistributedLock.acquire(cacheEntry.GetArchiveId())
                cacheEntry.DeleteArchiveUrlInfo(archiveUrlInfo)
                cacheEntry.DeleteServer(localendpoint)
                if (!cacheEntry.IsAnyArchiveUrlInfoPresent()){
                    var error = FileCleanup.cleanSynchronous(cacheEntry.GetArchiveLocalFilesystemPath())
                    if (error == undefined){
                        cache.RemoveEntry(cacheEntry.GetArchiveId())
                    }

                }else{
                    var error = FileCleanup.cleanSynchronous(cacheEntry.GetArchiveLocalFilesystemPath())
                    if (error == undefined){
                        cache.UpdateEntry(cacheEntry.GetArchiveId(), cacheEntry)
                    }
                }
                DistributedLock.release(cacheEntry.GetArchiveId())
            }
        }

}


var BuildScheduledArchiveCleanupJob = function(crontimeString) {
    var archiveleanupJob = new ArchiveCleanupJob()
    var strategy = new JobRunStrategy()
    var executor = new ScheduledExecutor()
    executor.SetCronTime(crontimeString)
    strategy.SetType(JobRunStrategyType.CRONJOB_TYPE_RUN)
    strategy.SetExecutor(executor)
    archiveleanupJob.SetRunStrategy(strategy)

    return archiveleanupJob
}






var JobRunStrategyType = {
    ONE_TIME_RUN : "ONE_TIME_RUN",
    CRONJOB_TYPE_RUN : "CRONJOB_TYPE_RUN"
}

var JobRunStrategy = function() {
    var self = this

    self.strategyInfo = {}

}

JobRunStrategy.prototype.SetType = function(type) {
    var self = this

    if (type == undefined || type == "") {
        type = JobRunStrategyType.ONE_TIME_RUN
    }

    self.strategyInfo.type = type

}

JobRunStrategy.prototype.GetType = function() {
    var self = this

    return self.strategyInfo.type
}


JobRunStrategy.prototype.SetExecutor = function(executor){
    var self = this

    if (executor == undefined || executor == ""){
        executor = new OneTimeExecutor()
    }

    self.strategyInfo.executor = executor


}

JobRunStrategy.prototype.GetExecutor = function() {
   var self = this
   return self.strategyInfo.executor
}



var ScheduledExecutor = function() {
   var self = this
   self.crontime = "* * * * * *"
}


/**
 *  scheduledTimestampInfo should be of the form '* * * * * *'
 *  Asterisk.   E.g. *
 *   Ranges.    E.g. 1-3,5
 *   Steps.     E.g. * /2
 *
 *   Meaning of different * from left to right
 *
 *   Seconds: 0-59
 *   Minutes: 0-59
 *   Hours: 0-23
 *   Day of Month: 1-31
 *   Months: 0-11
 *   Day of Week: 0-6
 *
 */
ScheduledExecutor.prototype.execute = function(scheduledFunction){
    var self = this

    //console.log("Scheduled function name:%s", JSON.stringify(scheduledFunction.getName()))
    var job = new CronJob(self.crontime, scheduledFunction, null, false, 'UTC');
    self.cronjob = job;
    job.start()
}

ScheduledExecutor.prototype.SetCronTime = function(timestampInfo){
    var self = this
    if (timestampInfo == undefined || timestampInfo == ""){
        timestampInfo = "* * * * * *"
    }
    self.crontime = timestampInfo
}

ScheduledExecutor.prototype.stop = function() {
    var self = this

    if (self.cronjob != undefined){
        self.cronjob.stop()
    }
}


var OneTimeExecutor = function(){

}

OneTimeExecutor.prototype.execute = function(OnetimeFunction){
    if (OnetimeFunction != undefined) {
        OnetimeFunction()
    }
}

OneTimeExecutor.prototype.stop = function() {
    var self = this;

    return
}



module.exports.LifecycleManager = new LifecycleManager()
module.exports.BuildScheduledDocumentExpirationJob = BuildScheduledDocumentExpirationJob
module.exports.BuildScheduledArchiveCleanupJob = BuildScheduledArchiveCleanupJob