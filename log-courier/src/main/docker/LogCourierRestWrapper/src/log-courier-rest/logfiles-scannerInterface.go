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
* Interface for sanning log files in the system
*
**/

package main;


type LogFileScannerInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface, scriptFilepath string, configFilePath string)
	GetScannedLogFiles()(scannedlogfilelist *ScannedLogFiles)
	getDirAndPattern() (dirs []string, patterns []string)
	addLogFilesDirs(logfilesdirs []string)
	updateLogFilesDirs(logfilesdirs []string)
	addLogFilesPatterns(logfilepatterns []string)
	updateLogFilesPatterns(logfilepatterns []string)
	scanLogFiles()(logfiles []string, err error)
	scanLogFilesForContainer(containerName string) (logfiles []string, err error)
	persistConfiguration()(err error)
}