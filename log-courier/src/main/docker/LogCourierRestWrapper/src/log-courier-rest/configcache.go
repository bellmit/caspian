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
* Cache for fast access of log-courier configuration
*
**/

package main;


import (
	"sync"
	"os"
	"errors"
	"encoding/json"
);

type InMemoryConfig struct {
	LogCourierLoggerPtr             LoggerInterface;
	LogCourierServiceControllerPtr  LogCourierServiceControllerInterface;
	LogCourierConfig               Config
	IsInit                         bool
	mu                             sync.RWMutex
}

func (IMConfig *InMemoryConfig) init(ConfigurationFileListPtr ConfigurationFilesInterface, LogCourierServiceControllerPtr LogCourierServiceControllerInterface){
	IMConfig.LogCourierLoggerPtr = new(LcLogger);
	IMConfig.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "configcache-module");
	IMConfig.LogCourierServiceControllerPtr = LogCourierServiceControllerPtr;
	IMConfig.loadConfigurationFromFile(LogCourierServiceControllerPtr.GetLogCourierConfFile());
	IMConfig.IsInit = true;
}


func (IMConfig *InMemoryConfig) loadConfigurationFromFile(path string)(err error){
	    IMConfig.mu.RLock();
	    defer IMConfig.mu.RUnlock();
		if IMConfig.LogCourierConfig, err = UnsafeLoadConfiguration(path); err != nil {
			IMConfig.LogCourierLoggerPtr.Error("Unable to load config file:", err.Error());
			return errors.New("Unable to read configuration file");
		}
		IMConfig.IsInit = true;
		return nil;
}

func (IMConfig *InMemoryConfig) saveConfigurationToFile(path string)(err error){
	IMConfig.mu.Lock()
	defer IMConfig.mu.Unlock();
	_ = os.Remove(path)
	config_file, err := os.OpenFile(path, os.O_CREATE | os.O_RDWR, 0666);
	if err != nil {
		IMConfig.LogCourierLoggerPtr.Error("Error opening the config file ", err);
		return err;
	}
	output, err := json.Marshal(IMConfig.LogCourierConfig);
	if err != nil {
		IMConfig.LogCourierLoggerPtr.Error("Error marshalling the updated json config object ", err);
		return err;
	}
	
	if size := len(output); size > (configFileSizeLimit) {
		IMConfig.LogCourierLoggerPtr.Error("config file:", path ,"size exceeds reasonable limit:", size ," - aborting")
		return 
	}
	
	config_file.Write(output);
	IMConfig.LogCourierServiceControllerPtr.ReloadLogCourierService()
	return err;
}

func (IMConfig *InMemoryConfig) updateConfiguration(newconfig *Config)(err error){
	if (!IMConfig.IsInit) {
		IMConfig.LogCourierLoggerPtr.Error("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		err = errors.New("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		return err;
	}
	IMConfig.mu.Lock()
	IMConfig.LogCourierConfig.mergeNetworkConfig(newconfig);
	IMConfig.LogCourierConfig.mergeFileConfigForPart(newconfig);
	IMConfig.mu.Unlock();
	if err = IMConfig.saveConfigurationToFile(ConfigurationFileList.LogCouierConfFile); err != nil {
		return errors.New("Unable to save the updated configuration to the conf file");
	}
    return nil
}


func (IMConfig *InMemoryConfig) updateAndRemoveConfiguration(newconfig *Config)(err error){
	if (!IMConfig.IsInit) {
		IMConfig.LogCourierLoggerPtr.Error("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		err = errors.New("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		return err;
	}
	IMConfig.mu.Lock()
	IMConfig.LogCourierConfig.deleteFileConfigIfPartMatches(newconfig);
	IMConfig.LogCourierConfig.mergeFileConfigForPart(newconfig);
	IMConfig.LogCourierConfig.mergeNetworkConfig(newconfig);
	IMConfig.mu.Unlock();
	if err = IMConfig.saveConfigurationToFile(ConfigurationFileList.LogCouierConfFile); err != nil {
		return errors.New("Unable to save the updated configuration to the conf file");
	}
    return nil
}

func (IMConfig *InMemoryConfig) deleteConfiguration(configToBeDeleted *Config)(err error){
	if (!IMConfig.IsInit) {
		IMConfig.LogCourierLoggerPtr.Error("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		err = errors.New("Configuration cache not initialized and cannot call updateConfiguration() method without initializing InMemoryConfiguration cache");
		return err;
	}
	IMConfig.mu.Lock();
	IMConfig.LogCourierConfig.deleteFileConfigIfPartMatches(configToBeDeleted);
	IMConfig.mu.Unlock();
	if err = IMConfig.saveConfigurationToFile(ConfigurationFileList.LogCouierConfFile); err != nil {
		return errors.New("Unable to save the updated configuration to the conf file");
	}
    return nil
}

func (IMConfig *InMemoryConfig) getConfiguration()(err error, config Config) {
	if err := IMConfig.loadConfigurationFromFile(ConfigurationFileList.LogCouierConfFile); err != nil {
		return err,Config{}
	}
	return nil,IMConfig.LogCourierConfig;
}


func (IMConfig *InMemoryConfig) synchConfiguration()(err error){
	IMConfig.LogCourierServiceControllerPtr.RestartLogCourierService();
	err, _ = IMConfig.getConfiguration();
	return err
}