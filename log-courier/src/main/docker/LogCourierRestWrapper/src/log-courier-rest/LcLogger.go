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
*  Handle Logging configuration
*
**/

package main;

import (
	"strings"
	"regexp"
	"strconv"
)

import log "github.com/cihub/seelog"
import "github.com/revel/config"


var LogFileSizePattern = "[1-9][0-9]*|[B]|[kK]B|[mM]B|[gG]B|[tT]B"

type LogRotationMetadata struct {
	RotationType  string
	MaxRollNumber int
	Properties map[string]string
}

type LcLogger struct {
	ConfigurationFileListPtr ConfigurationFilesInterface;
	IsLogRotationEnabled  bool
	LogsFolder            string
	LogFiles              []string
	LogMinLevel           string
	LogMaxLevel           string
	LogFormat             string
	LogConfigurationFile  string
	LogRotationAttributes LogRotationMetadata
	Logger                log.LoggerInterface
	RelativeFileName      string
}


func (logger *LcLogger) init(ConfigurationFileListPtr ConfigurationFilesInterface, filename string) {
	logger.RelativeFileName = filename;
	logger.ConfigurationFileListPtr = ConfigurationFileListPtr;
	logger.IsLogRotationEnabled = false;
	logger.LogsFolder = "/var/log/log-courier-rest"
	logger.LogFiles = []string{}
	logger.LogMinLevel = "INFO"
	logger.LogMaxLevel = "ERROR"
	logger.LogFormat = "%Date %Time [%LEVEL] %Msg%n"
	logger.LogRotationAttributes = LogRotationMetadata{RotationType: "size", MaxRollNumber: 5, Properties : map[string]string{}};
	
	if c, err := config.ReadDefault(logger.ConfigurationFileListPtr.GetLcLoggerConfFile()); err != nil {
		log.Errorf("unable to read lc-logger.conf file configPath:%s ", logger.ConfigurationFileListPtr.GetLcLoggerConfFile());
	}else {
		logger.LogConfigurationFile = logger.ConfigurationFileListPtr.GetLcLoggerConfFile();
		
		if temp, err := c.String("LC_LOGGER", "logrotation"); err == nil {
			if (strings.ToLower(strings.TrimSpace(temp)) == "enabled") {
				logger.IsLogRotationEnabled = true
			}else {
				logger.IsLogRotationEnabled = false;
			}
		}
		
		if temp, err := c.String("LC_LOGGER", "logfolder"); err == nil {
			logger.LogsFolder = temp;
		}
		
		if temp, err := c.String("LC_LOGGER", "minloglevel"); err == nil {
			logger.LogMinLevel = strings.ToUpper(strings.TrimSpace(temp));
		}
		
		if temp, err := c.String("LC_LOGGER", "maxloglevel"); err == nil {
			logger.LogMaxLevel = strings.ToUpper(strings.TrimSpace(temp));
		}
		
		if temp, err := c.String("LC_LOGGER", "logformat"); err == nil {
			logger.LogFormat = temp;
		}
		
		if temp, err := c.String("LC_LOGGER", "rotation_interval_type"); err == nil {
			logger.LogRotationAttributes.RotationType = strings.ToLower(strings.TrimSpace(temp));
		}
		
		if temp, err := c.String("LC_LOGGER", "max_rotated_files"); err == nil {
			if logger.LogRotationAttributes.MaxRollNumber,err = strconv.Atoi(temp) ; err != nil {
				logger.LogRotationAttributes.MaxRollNumber = 5;
				
			}
		}
		
		if (logger.LogRotationAttributes.RotationType == "size") {
			logger.LogRotationAttributes.Properties["rotation_size_limit_inbytes"] = "26214400"
			if temp, err := c.String("LC_LOGGER", "rotation_size_limit"); err == nil {
			   patternObj, _ := regexp.Compile(LogFileSizePattern);
			   if foundBool := patternObj.MatchString(temp); foundBool {
				  	sizeAttribs := patternObj.FindAllString(temp,-1);
				  	var bytes int = 0;
				  	var multiplier int = 1;
				  	var err error;
			  	    if bytes, err =  strconv.Atoi(sizeAttribs[0]); err != nil {
			  		       bytes = 26214400
			  	     }else {
			  	 	       multiplierStr := strings.ToUpper(sizeAttribs[1]);
			  			   switch multiplierStr {
			  					case "B"  : multiplier = 1;
			  					case "KB" : multiplier = 1024;
			  					case "MB" : multiplier = 1024*1024;
			  					case "GB" : multiplier = 1024*1024*1024;
			  					case "TB" : multiplier = 1024*1024*1024*1024;
			  					default:
			  			        		    multiplier = 1;
			  			        		    }
	  			     bytes = bytes * multiplier;
	  			     logger.LogRotationAttributes.Properties["rotation_size_limit_inbytes"] = strconv.Itoa(bytes);
			  	}
			  }
		    }
		}
	}
	
	logger.LogFiles = append(logger.LogFiles, logger.LogsFolder + "/" + logger.RelativeFileName + ".log");
	seelogStart := `<seelog minlevel="`+strings.ToLower(logger.LogMinLevel)+`" maxlevel="`+strings.ToLower(logger.LogMaxLevel)+`">`
	outputsStart := `<outputs formatid="common">`
	rollingSetting := ``;
	if (logger.IsLogRotationEnabled) {
		if (logger.LogRotationAttributes.RotationType == "size") {
			rollingSetting = `<rollingfile type="size" filename="`+logger.LogFiles[0]+`" maxsize="`+
			logger.LogRotationAttributes.Properties["rotation_size_limit_inbytes"]+`" maxrolls="`+strconv.Itoa(logger.LogRotationAttributes.MaxRollNumber)+`"/>`;
		}
	}
	formatsStart := `<formats>`
	format1 := `<format id="common" format="`+logger.LogFormat+`" />`
	formatsEnd := `</formats>`
	outputsEnd := `</outputs>`;
	seelogEnd := `</seelog>`;
	
	appconfig := seelogStart + outputsStart + rollingSetting +  outputsEnd + formatsStart + format1 + formatsEnd + seelogEnd;
	logger.Logger , _ = log.LoggerFromConfigAsBytes([]byte(appconfig));  
	      
}


func (logger *LcLogger) Info(message ...interface{}) {
	logger.Logger.Info(message);
	logger.Logger.Flush();
}

func (logger *LcLogger) Warn(message ...interface{}) {
	logger.Logger.Warn( message);
	logger.Logger.Flush();
}

func (logger *LcLogger) Debug(message ...interface{}) {
	logger.Logger.Debug(message);
	logger.Logger.Flush();
}

func (logger *LcLogger) Error(message ...interface{}) {
    logger.Logger.Error(message)
    logger.Logger.Flush();	
}

func (logger *LcLogger) Critical(message ...interface{}) {
	logger.Logger.Critical(message);
	logger.Logger.Flush();
}

func (logger *LcLogger) Trace(message ...interface{}) {
	logger.Logger.Trace(message);
	logger.Logger.Flush();
}
