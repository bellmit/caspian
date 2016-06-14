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
* This file implements the scheduling calling of refresh api using sequential scheduler
*
*/

package main;

import 	"github.com/revel/config"
import (
	"strings"
)

type PollingIntervalInfo struct {
	PollingInterval        int
	PollingIntervalUnit    string
}

func NewPollingIntervalInfo(PollingInterval string) (pi *PollingIntervalInfo){
	pi = &PollingIntervalInfo{}
	var err error = nil;
	interval := StringToArray(PollingInterval, " ");

	if pi.PollingInterval, err = ConvertStringToInt(interval[0]); err != nil {
		pi.PollingInterval = 120
		pi.PollingIntervalUnit = "seconds"
	}else {
		pi.PollingIntervalUnit = interval[1];
	}
	return pi;
}


type AutoRefresh struct {
	LogCourierLoggerPtr         LoggerInterface
	SchedulerImplPtr            SchedulerInterface
	Command                     []string
	RefreshInterval             *PollingIntervalInfo
}

func (ar *AutoRefresh)  init(SchedulerImplPtr SchedulerInterface, ConfigurationFileListPtr ConfigurationFilesInterface, Command []string){
	ar.LogCourierLoggerPtr = new(LcLogger);
	ar.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "auto-refresh");
	ar.ReadSettingsFile(ConfigurationFileListPtr.GetLcSettingsConfFile());
	ar.SchedulerImplPtr = SchedulerImplPtr;
	ar.Command = Command;
}

func (ar *AutoRefresh) ReadSettingsFile(SettingsFile string){
	var PollingInterval = "7 minutes";
	if c, err := config.ReadDefault(SettingsFile); err != nil {
		ar.LogCourierLoggerPtr.Error("unable to read lc-settings.conf file so switching to default value");
	}else {
		for _, section := range c.Sections() {
			if (section == "auto-refresh-api" ) {
				if options, err := c.Options(section); err == nil {
					for _, option := range options {
						if value, err := c.String(section, option); err == nil {
							value = strings.TrimSpace(value);
							if ( option == "refresh_interval" ) {
								PollingInterval = value;
							}
						 }
				     }
			     }
		      }
		  }
	}

	ar.RefreshInterval = NewPollingIntervalInfo(PollingInterval);
}

func (ar *AutoRefresh) schedule(){
	ar.SchedulerImplPtr.Every(ar.RefreshInterval.PollingInterval, ar.RefreshInterval.PollingIntervalUnit, ar.refresh);
	go ar.SchedulerImplPtr.RunAllwithDelay(ConvertTimeIntervalToSeconds(ar.RefreshInterval.PollingInterval, ar.RefreshInterval.PollingIntervalUnit));
}


func (ar *AutoRefresh) refresh() {
	CmdExecNoWait(ArrayToString(ar.Command, " "));
}

func NewAutoRefreshInstance(SchedulerImplPtr SchedulerInterface, ConfigurationFileListPtr ConfigurationFilesInterface, command []string)(autorefreshInstance *AutoRefresh){
	arInstance := &AutoRefresh{};
	arInstance.init(SchedulerImplPtr, ConfigurationFileListPtr, command);
	return arInstance;
}
