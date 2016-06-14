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
* LogCourier REST Service main module
*
**/
package main

import (
	"flag"
	"path/filepath"
	"os"
)



var mainLogger LoggerInterface;


//main it calls all the init functions and starts the http server to REST service
func main() {
	ArgsInit()
	mainLogger = new(LcLogger);
	mainLogger.init(&ConfigurationFileList, "main-module");
	Reap();
	mainLogger.Info("Logger instantiated...");
	LogCourierServiceControllerInstance.init(&ConfigurationFileList);
	ConfigCache.init(&ConfigurationFileList, &LogCourierServiceControllerInstance);
	ServicesInstance.init(&ConfigurationFileList);
	PropertySubstitutor := NewPropertySubstitutorInstance(&ConfigurationFileList);
	if err, config := ConfigCache.getConfiguration(); err == nil {
		ContainerListInMemory.init(&ConfigurationFileList, &Config{}, &ServicesInstance, PropertySubstitutor);
	}else {
		ContainerListInMemory.init(&ConfigurationFileList, &config, &ServicesInstance, PropertySubstitutor);
	}

  LcRestApi.init(&ConfigurationFileList, nil, &ServicesInstance, &ContainerListInMemory, &LogFileScannerInstance, &ConfigCache);
  RestServerInstance.init(&ConfigurationFileList, LcRestApi.Api);
  LogFileScannerInstance.init(&ConfigurationFileList, ConfigurationFileList.LogpathsAggregatorScriptFile, ConfigurationFileList.LogpathsAggregatorScriptConfigFile);
  ProcessInitLogFiles();
  command := "curl -XGET " + "http://127.0.0.1:" + RestServerInstance.Port + "/api/_all/refresh";
  RefreshApiInstance := NewAutoRefreshInstance(NewScheduler(), &ConfigurationFileList,  []string{command});
  RefreshApiInstance.schedule();
  Run();
}



func ArgsInit(){
	dir, _ := filepath.Abs(filepath.Dir(os.Args[0]))
	lcConfigPath := flag.String("lcconfig", "/opt/log-management/log-courier/log-courier.conf", "log-courier configuration file absolute path");
	lcRestConfigPath := flag.String("config", dir + "/" + "../src/conf/lcrest-conf.conf", "lcrest configuration absolute path")
	logpathToServiceMappingConfigPath := flag.String("logpathtoserviceconfig", dir + "/" + "../src/conf/lc-pathconfig.conf", "log path file location to service name mapping configuration-file  absolute path")
	settingsConfigPath := flag.String("parsersettings", dir + "/" + "../src/conf/lc-settings.conf", "parser settings configuration file absolute path");
	logaggregatorScriptPath := flag.String("aggregator-script", dir + "/" + "../scripts/logpathaggregator-run.sh", "logpath aggregator script");
	logaggregatorScriptConfigPath := flag.String("aggregator-script-conf", dir + "/" + "../scripts/logpath-pattern.conf", "logpath aggregator script");
	lcLoggerConfigPath := flag.String("lclogger-conf", dir + "/" + "../src/conf/lc-logger.conf", "log courier rest logger configuration");
	lcPropertySubstitutorConfigPath := flag.String("lcpropsubstituor-conf", dir + "/" + "../src/conf/lc-property-substitutor.conf", "log courier normalization property susbtitutor configuration");


	flag.Parse();

	if filepath.IsAbs(*lcConfigPath) == false {
		*lcConfigPath,_ = filepath.Abs(*lcConfigPath)
	}

	if filepath.IsAbs(*lcRestConfigPath) == false {
		*lcRestConfigPath,_ = filepath.Abs(*lcRestConfigPath)
	}

	if filepath.IsAbs(*logpathToServiceMappingConfigPath) == false {
		*logpathToServiceMappingConfigPath,_ = filepath.Abs(*logpathToServiceMappingConfigPath)
	}

	if filepath.IsAbs(*settingsConfigPath) == false {
		*settingsConfigPath,_ = filepath.Abs(*settingsConfigPath)
	}

	if filepath.IsAbs(*logaggregatorScriptPath) == false {
		*logaggregatorScriptPath,_ = filepath.Abs(*logaggregatorScriptPath)
	}

	if filepath.IsAbs(*lcLoggerConfigPath) == false {
		*lcLoggerConfigPath,_ = filepath.Abs(*lcLoggerConfigPath)
	}

	if filepath.IsAbs(*lcPropertySubstitutorConfigPath) == false {
		*lcPropertySubstitutorConfigPath,_ = filepath.Abs(*lcPropertySubstitutorConfigPath)
	}

 	ConfigurationFileList.LcRestConfFile = *lcRestConfigPath;
	ConfigurationFileList.LcLogPathToServiceConfFile = *logpathToServiceMappingConfigPath;
	ConfigurationFileList.LcSettingsConfFile = *settingsConfigPath;
	ConfigurationFileList.LogpathsAggregatorScriptFile = *logaggregatorScriptPath;
	ConfigurationFileList.LogpathsAggregatorScriptConfigFile = *logaggregatorScriptConfigPath;
	ConfigurationFileList.LcLoggerConfFile = *lcLoggerConfigPath;
	ConfigurationFileList.LcPropertySubstitutorConfFile = *lcPropertySubstitutorConfigPath

}


//Run starts the log-courier and log-courier-rest executable
func Run(){
	LcRestApi.RegisterApplication();
	LogCourierServiceControllerInstance.RestartLogCourierService()
    RestServerInstance.StartRestServer();
}

func ProcessInitLogFiles(){

	InitLogFilePathsList := [] string{};
	if temp, err := LogFileScannerInstance.scanLogFiles(); err != nil {
		mainLogger.Error("Unable to scan log files, Error:", err.Error());
		return;
	}else {
		InitLogFilePathsList = temp;
	}
	LogFileScannerInstance.ScannedLogFilesList.addLogFiles(InitLogFilePathsList);
	mainLogger.Info("Processing Logfile paths:%s", InitLogFilePathsList);
   	var newconfig *Config;
   	var err error = nil;
   	if newconfig, err  = ContainerListInMemory.GenerateConfigurationUsingLogFiles(InitLogFilePathsList, []string{}); err != nil {
   		mainLogger.Error("Unable to Generate configurations from logfiles:", InitLogFilePathsList);
   		return
   	}
   	mainLogger.Info("New configuration:", newconfig.Files);
   	ConfigCache.updateConfiguration(newconfig);
}
