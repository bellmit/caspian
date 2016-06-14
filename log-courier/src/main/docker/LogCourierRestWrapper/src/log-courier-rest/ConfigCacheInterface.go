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
* Interface for managing/storing configuration in-memory cache for fast access of log-courier configuration
*
**/


package main;


type InMemoryConfigInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface, LogCourierServiceControllerPtr LogCourierServiceControllerInterface)
	saveConfigurationToFile(path string)(err error)
	updateConfiguration(newconfig *Config)(err error)
	updateAndRemoveConfiguration(newconfig *Config)(err error)
	deleteConfiguration(configToBeDeleted *Config)(err error)
	getConfiguration()(err error, config Config)
	synchConfiguration()(err error)
}