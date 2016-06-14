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
* Scanning log files in the system
*
**/


package main

import (	
	"sync"
	"github.com/revel/config"
	"errors"
)


type ScannedLogFiles struct {
	LogFiles     []string;
	mu           sync.RWMutex;
}

func (slf *ScannedLogFiles) init() {
	slf.LogFiles = [] string {};
}


func (slf *ScannedLogFiles) cleanAllLogFiles() {
	slf.mu.Lock()
    defer slf.mu.Unlock()
    slf.LogFiles = [] string{}
}

func (slf *ScannedLogFiles) addLogFiles(logfiles []string) (err error){
	return slf.mergeLogFiles(logfiles, []string{});
}


func (slf *ScannedLogFiles) removeLogFiles(logfiles []string) (err error){
	return slf.mergeLogFiles([]string{}, logfiles);
}

func (slf *ScannedLogFiles) mergeLogFiles(addedlogfiles []string, removedlogfiles []string) (err error) {
  err = nil;
  slf.mu.Lock()
  defer slf.mu.Unlock()
  
  if (len(addedlogfiles) != 0 ) {
  	slf.LogFiles = append(slf.LogFiles, addedlogfiles...);
  }
  
  if (len(removedlogfiles) != 0 ) {
  	for _, toBeRemovedlogfile := range removedlogfiles {
  		for index, presentLogFile := range slf.LogFiles {
  			if (presentLogFile == toBeRemovedlogfile) {
  				slf.LogFiles = append(slf.LogFiles[:index], slf.LogFiles[index+1:]...);
  				break;
  			}
  		}
  	}
  }
  return err;	
}




func (slf *ScannedLogFiles) getNewlyAddedAndRemovedLogFiles(logfiles []string) (addedlogfiles []string, removedlogfiles []string, err error) {
  err = nil;
  addedlogfiles = []string{};
  removedlogfiles = []string{};
  
  slf.mu.Lock()
  defer slf.mu.Unlock()
  addedlogfiles, removedlogfiles, err = GetAddedAndRemovedSetFromSet(slf.LogFiles, logfiles);
  return addedlogfiles, removedlogfiles, err;
}



type LogFileScanner struct {
	LogCourierLoggerPtr      LoggerInterface
	LogfileScannerScriptFile string
	LogfileScannerConfigFile string
	LogfilesDirs             []string
	LogFilesPattern          []string
	ScannedLogFilesList      *ScannedLogFiles
	mu                       sync.RWMutex;
}

func (lfscanner *LogFileScanner) init(ConfigurationFileListPtr ConfigurationFilesInterface, scriptFilepath string, configFilePath string){
	lfscanner.LogCourierLoggerPtr = new(LcLogger);
	lfscanner.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "logfiles-scanner-module");
    lfscanner.LogfileScannerConfigFile = configFilePath;
    lfscanner.LogfileScannerScriptFile = scriptFilepath;
	lfscanner.LogfilesDirs = [] string{"/:*caspian*"};
	lfscanner.LogFilesPattern = [] string {"*.log"};
	lfscanner.ScannedLogFilesList = &ScannedLogFiles{};
	lfscanner.ScannedLogFilesList.init();
	lfscanner.loadConfiguration();
}

func (lfscanner *LogFileScanner) loadConfiguration() {
	
	if c, err := config.ReadDefault(lfscanner.LogfileScannerConfigFile); err != nil {
		lfscanner.LogCourierLoggerPtr.Error("unable to read logpath-pattern.conf file which used for running logfilescanner", lfscanner.LogfileScannerConfigFile);
	}else {
		if temp, err := c.String("LOG_FILES_SCANNING_CONF", "logpath_patterns"); err == nil {
			ParseStringToArrayOfString(temp, &lfscanner.LogFilesPattern);
		}
		
		if temp, err := c.String("LOG_FILES_SCANNING_CONF", "logpath_dirs"); err == nil {
			ParseStringToArrayOfString(temp, &lfscanner.LogfilesDirs);
		}else {
			lfscanner.LogCourierLoggerPtr.Error("Encountered error while fetching logpath_dirs information reason:" + err.Error());
		}
	}
}

func (lfscanner *LogFileScanner) GetScannedLogFiles()(scannedlogfilelist *ScannedLogFiles) {
	scannedlogfilelist = lfscanner.ScannedLogFilesList;
	return scannedlogfilelist;
}

func (lfscanner *LogFileScanner) getDirAndPattern() (dirs []string, patterns []string) {
	lfscanner.mu.RLock()
	defer lfscanner.mu.RUnlock();
	dirs = lfscanner.LogfilesDirs;
	patterns = lfscanner.LogFilesPattern;
	return dirs, patterns;
}

func (lfscanner *LogFileScanner) addLogFilesDirs(logfilesdirs []string) {
	lfscanner.mu.Lock();
	defer lfscanner.mu.Unlock();
	lfscanner.LogfilesDirs = logfilesdirs;
}


func (lfscanner *LogFileScanner) updateLogFilesDirs(logfilesdirs []string) {
	lfscanner.mu.Lock();
	defer lfscanner.mu.Unlock();
	lfscanner.LogfilesDirs = append(lfscanner.LogfilesDirs, logfilesdirs...);
	lfscanner.LogfilesDirs = removeDuplicatePathEntries(lfscanner.LogfilesDirs);
}

func (lfscanner *LogFileScanner) addLogFilesPatterns(logfilepatterns []string) {
	lfscanner.mu.Lock();
	defer lfscanner.mu.Unlock();
	lfscanner.LogFilesPattern = logfilepatterns;
}

func (lfscanner *LogFileScanner) updateLogFilesPatterns(logfilepatterns []string) {
	lfscanner.mu.Lock();
	defer lfscanner.mu.Unlock();
	lfscanner.LogFilesPattern = append(lfscanner.LogFilesPattern, logfilepatterns...);
	lfscanner.LogFilesPattern = removeDuplicatePathEntries(lfscanner.LogFilesPattern);
}


func (lfscanner *LogFileScanner) scanLogFiles() (logfiles []string, err error){
	return lfscanner.scanLogFilesForContainer("_all");
}

func (lfscanner *LogFileScanner) scanLogFilesForContainer(containerName string) (logfiles []string, err error){
	lfscanner.mu.RLock();
	defer lfscanner.mu.RUnlock();
	logfiles = [] string{};
	if (containerName == "" ) {
		containerName = "_all";
	}
	cmd := lfscanner.LogfileScannerScriptFile + " " + containerName;
	output := "";
	if output, err = CmdExec( cmd ); err != nil {
		lfscanner.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Error scanning the log folder for looking newly added log files [reason:" + err.Error() + "]");
	}else {
		logfiles = [] string{};
	    err = ParseStringToArrayOfString(output, &logfiles)
	    if (err != nil) {
	    	lfscanner.LogCourierLoggerPtr.Error("Error in parsing log files:", logfiles);
	    	logfiles = [] string {}
	    }else {
	    	logfiles = removeDuplicatePathEntries(logfiles)
        }   
    }
	
	return logfiles, err;
}


func (lfscanner *LogFileScanner) persistConfiguration()(err error){
	
	err = nil;
	lfscanner.mu.Lock();
	defer lfscanner.mu.Unlock();
	c := config.New( config.DEFAULT_COMMENT, config.ALTERNATIVE_SEPARATOR, false, true);
	c.AddSection("LOG_FILES_SCANNING_CONF")
	pattern := lfscanner.concatenateArray(lfscanner.LogFilesPattern, ",");
	logfileDirs := lfscanner.concatenateArray(lfscanner.LogfilesDirs, ",");
	c.AddOption("LOG_FILES_SCANNING_CONF", "logpath_patterns", pattern)
	c.AddOption("LOG_FILES_SCANNING_CONF", "logpath_dirs", logfileDirs)
	if err = c.WriteFile(lfscanner.LogfileScannerConfigFile, 0644, ""); err != nil {
		lfscanner.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Error writing the new configuration for logfile scanner script - reason:" + err.Error());
	}
	return err;
}


func (lfscanner *LogFileScanner) concatenateArray(array []string, separator string) (concatenated string){
	size := len(array);
	
	if size == 0 {
		concatenated = "";
	}else if size == 1 {
		concatenated = array[0];
	}else {
		for index, item := range array {
			if (index ==  0) {
				concatenated = item ;
				}else {
					concatenated = concatenated + separator + item;
				}
		}
	}	
	return concatenated;
}

var LogFileScannerInstance LogFileScanner;
