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



/*
*
*  Interface for controlling of start/stop/restart/reload of logcourier-service
*
*/


package main;


type LogCourierServiceControllerInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface)
	GetLogCourierConfFile()(confFile string)
	StartLogCourierService()
	StopLogCourierService()
	RestartLogCourierService()
	ReloadLogCourierService()
	StatusLogCourierService()(status string)
}