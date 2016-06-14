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
*  Controller for start/stop/restart/reload of logcourier-service
*
*/

package main;

import 	"github.com/revel/config"

type LogCourierServiceController struct {
	LogCourierLoggerPtr       LoggerInterface
    ConfigurationFileListPtr  ConfigurationFilesInterface
	StartCmd                  string
	StopCmd                   string
	RestartCmd                string
	ReloadCmd                 string
	StatusCmd                 string
}


func (lcscontroller *LogCourierServiceController) init(ConfigurationFileListPtr ConfigurationFilesInterface) {
	lcscontroller.LogCourierLoggerPtr = new(LcLogger);
	lcscontroller.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "logcourier-service-controller");
    lcscontroller.ConfigurationFileListPtr = ConfigurationFileListPtr;
	lcscontroller.StartCmd = "service logcourier-service start"
	lcscontroller.StopCmd = "service logcourier-service stop"
	lcscontroller.RestartCmd = "service logcourier-service restart"
	lcscontroller.ReloadCmd = "service logcourier-service reload"
	lcscontroller.StatusCmd = "service logcourier-service status"
	lcscontroller.ReadlcrestServiceConfigForLogCourier();
	
}

//readlcrestServiceConfig reads the lcrest-conf.conf file to initialized our log-courier-rest service for 
// running log-courier binary
func (lcscontroller *LogCourierServiceController) ReadlcrestServiceConfigForLogCourier(){
	if c, err := config.ReadDefault(lcscontroller.ConfigurationFileListPtr.GetLcRestConfFile()); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("unable to read lcrest-conf.conf file for LOGCOURIER executable switching to defualt configPath:%s ", ConfigurationFileList.LogCouierConfFile);
	}else {
		if temp, err := c.String("LOGCOURIER", "configPath"); err == nil && temp != "LOG_COURIER_CONF_FILE_PATH"{
			lcscontroller.ConfigurationFileListPtr.SetLogCourierConfFile(temp);
		}
		
		if temp, err := c.String("LOGCOURIER", "startCmd"); err == nil {
			lcscontroller.StartCmd = temp;
		}
		
		if temp, err := c.String("LOGCOURIER", "stopCmd"); err == nil {
			lcscontroller.StopCmd = temp;
		}
		
		if temp, err := c.String("LOGCOURIER", "restartCmd"); err == nil {
			lcscontroller.RestartCmd= temp;
		}
		
		if temp, err := c.String("LOGCOURIER", "reloadCmd"); err == nil {
			lcscontroller.ReloadCmd = temp;
		}
		
		if temp, err := c.String("LOGCOURIER", "statusCmd"); err == nil {
			lcscontroller.StatusCmd = temp;
		}
	}
}


func (lcscontroller *LogCourierServiceController) GetLogCourierConfFile() (confFile string){
	confFile = lcscontroller.ConfigurationFileListPtr.GetLogCourierConfFile();
	return confFile;
}


func (lcscontroller *LogCourierServiceController) StartLogCourierService() {
	if err := CmdExecNoWait(lcscontroller.StartCmd); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("Unable to start LogCourier service [Error:", err.Error(), "]");
	}else{
		lcscontroller.LogCourierLoggerPtr.Info("LogCourier service started successfully...");
	}
}

func (lcscontroller *LogCourierServiceController) StopLogCourierService() {
	if output, err := CmdExec(lcscontroller.StopCmd); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("Unable to stop LogCourier service [Error:", err.Error(), "]");
	}else{
		lcscontroller.LogCourierLoggerPtr.Info("LogCourier service stopped successfully...[", output, "]");
	}
}

func (lcscontroller *LogCourierServiceController) RestartLogCourierService() {
	if err := CmdExecNoWait(lcscontroller.RestartCmd); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("Unable to restart LogCourier service [Error:", err.Error(), "]");
	}else{
		lcscontroller.LogCourierLoggerPtr.Info("LogCourier service restarted successfully...");
	}
}

func (lcscontroller *LogCourierServiceController) ReloadLogCourierService() {
	if _, err := CmdExec(lcscontroller.ReloadCmd); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("Unable to reload LogCourier service [Error:", err.Error(), "]");
		lcscontroller.RestartLogCourierService();
	}else{
		lcscontroller.LogCourierLoggerPtr.Info("LogCourier service reloaded successfully...");
	}
}

func (lcscontroller *LogCourierServiceController) StatusLogCourierService() (status string){
	if output, err := CmdExec(lcscontroller.StatusCmd); err != nil {
		lcscontroller.LogCourierLoggerPtr.Error("Unable to get running status of LogCourier service [Error:", err.Error(), "]");
		status = "Unknown"
	}else{
		lcscontroller.LogCourierLoggerPtr.Info("LogCourier service status successfully fetched [", output, "]");
		status = output
	}
	return status
}

var LogCourierServiceControllerInstance LogCourierServiceController;