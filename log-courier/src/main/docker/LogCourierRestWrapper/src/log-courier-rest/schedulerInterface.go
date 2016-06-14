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
*
*  Scheduler Interface used for implementing any scheduler
*
*/


package main

import scheduler "github.com/jasonlvhit/gocron"



type SchedulerInterface interface {
	NumberOfJobs() (numOfJobs int)
	Every(interval int, interval_unit string, job_func interface{}, params ...interface{}) (instance *scheduler.Job)
	Remove(j interface{})
	Clear()
	RunAll()
	RunAllwithDelay(seconds int)
	Start()
}
