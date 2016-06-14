var   config    = require('./lib/configuration')
     ,scheduler   = require('./lib/cronjob');


var jobslist = config.get("CronJobs");


var JobRegistration = function(Job) {
   scheduler.AddJob(Job)
}


for ( index in jobslist ) {

   JobRegistration(jobslist[index].Job);

}


scheduler.RunAll();
console.log("Jobs are registered");
