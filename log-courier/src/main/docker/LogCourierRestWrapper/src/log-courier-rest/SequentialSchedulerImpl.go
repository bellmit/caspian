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
*      This file implements the sequential scheduler which can be used to schedule any Job
*
**/

package main

import (
	"strings"
	"time"
)

import scheduler "github.com/jasonlvhit/gocron"


type SequentialJobSchedulerImpl struct {
   InternalScheduler            *scheduler.Scheduler
}

func (sch *SequentialJobSchedulerImpl) NumberOfJobs() (numOfJobs int) {
       return sch.InternalScheduler.Len()
}


func (sch *SequentialJobSchedulerImpl) Every(interval int, interval_unit string, job_func interface{}, params ...interface{}) *scheduler.Job {
	job := sch.InternalScheduler.Every(uint64(interval));
	interval_unit = strings.ToLower(interval_unit);

	switch interval_unit {
		case "minutes","minute","min":
				job = job.Minutes();
				break
		case "hours","hour","hrs","hr":
				job = job.Hours();
				break
		case "days","day":
				job = job.Days();
				break
		case "weeks","week":
				job = job.Weeks();
				break
		case "seconds","second","sec":
				job = job.Seconds();
     }
	if len(params) > 0 {
		job.Do(job_func, params...);
	}else {
		job.Do(job_func)
	}

    return job
}

func (sch *SequentialJobSchedulerImpl) Remove(j interface{}){
	sch.InternalScheduler.Remove(j)
}


func (sch *SequentialJobSchedulerImpl) Clear() {
	sch.InternalScheduler.Clear();
}

func (sch *SequentialJobSchedulerImpl) RunAll() {
	sch.InternalScheduler.RunAll()
}

func (sch *SequentialJobSchedulerImpl) RunAllwithDelay(seconds int) {
	 for {
			sch.InternalScheduler.RunAll();
			time.Sleep(time.Second * time.Duration(seconds))
	  }
}

func (sch *SequentialJobSchedulerImpl) Start(){
	sch.InternalScheduler.Start();
}


func NewScheduler() (instance *SequentialJobSchedulerImpl) {
       return &SequentialJobSchedulerImpl{scheduler.NewScheduler()}
}
