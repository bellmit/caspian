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



var   CronJob   = require('cron').CronJob
     ,config = require('nconf')
     ,exec   = require('child_process').exec



var scheduler = function() {
    self = this;
    self.Jobs = []
}


scheduler.prototype.AddJob = function(job) {
   self = this;

   for ( index in self.Jobs ) {
     if ( job.name == self.Jobs[index].name ) {
         console.log("cannot add two jobs with the same:%s", job.name);
         return
     }
   }

   var Job = {}
   Job.name = job.name;
   Job.properties = {}
   Job.properties.type = job.properties.type;

   if (Job.properties.type == "command-executor" ) {
       Job.properties.command = job.properties.command
   }


   Job.timer = job.timer;
   if ( job.timeZone == undefined) {
      job.timeZone = "UTC";
   }

   Job.timeZone = job.timeZone

   if (job.start == undefined ){
      job.start = false;
   }

   Job.start = job.start
   var scheduledFunction = function() {
                              self = this;
                              if (self.properties.type == "command-executor" ) {
                              var child = exec(self.properties.command, function (error, stdout, stderr) {
                                              if (error !== null) {
                                                  console.log('exec error: ' + error);
                                              }else{
                                                console.log("command:'%s' and output:%s", self.properties.command, stdout);
                                              }
                                           });

                              }
                           }

   var cronjobObject =  new CronJob({
                             cronTime: Job.timer,
                             onTick: scheduledFunction,
                             start: Job.start,
                             timeZone: Job.timeZone
                        });
   cronjobObject.context = Job;
   Job.cronjobObject = cronjobObject;

   self.Jobs.push(Job)
   return Job
}



scheduler.prototype.DeleteJob = function(jobname) {
   self = this

   for ( index in self.Jobs ) {
     if ( jobname == self.Jobs[index].name ) {
        if (index > -1) {
           if (self.Jobs[index].cronjobObject) {
             self.Jobs[index].cronjobObject.stop();
           }
           self.Jobs.splice(index, 1);
           return self.Jobs[index]
        }
     }
   }

  console.log("No Job found with name:%s .", jobname)
  return undefined
}


scheduler.prototype.GetJob = function(jobname) {
   self = this;

   for ( index in self.Jobs ) {
     if ( jobname == self.Jobs[index].name ) {
       return self.Jobs[index];
     }
   }

   console.log("No Job found with name:%s .", jobname)
   return undefined
}


scheduler.prototype.RunJob = function(job) {
  self = this;

  for ( index in self.Jobs ) {
    if ( job.name == self.Jobs[index].name ) {
      if (!self.Jobs[index].cronjobObject.running) {
        self.Jobs[index].cronjobObject.start();
      }
      return "success";
    }
  }

  console.log("Job:%s submitted for execution is not registered with out scheduler.", job.name)
  return "failure"
}


scheduler.prototype.RunAll = function() {
   self = this;

   for ( index in self.Jobs) {
        self.Jobs[index].cronjobObject.start();
   }

   return "success"
}

module.exports = new scheduler();
