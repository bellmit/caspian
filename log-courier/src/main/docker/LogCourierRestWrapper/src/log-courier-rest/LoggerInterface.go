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
*  Interface for Handle Logging configuration
*
**/

package main;


type LoggerInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface, filename string)
	Info(message ...interface{})
	Warn(message ...interface{})
	Debug(message ...interface{})
	Error(message ...interface{})
	Critical(message ...interface{})
	Trace(message ...interface{})
}