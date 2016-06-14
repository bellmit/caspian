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
* operations associated with configuration creation/updating/deletion for specific services
*
**/


package main

import (
	"strings"
	"github.com/revel/config"
	"regexp"
	);


type NormalizationFieldTags struct {
	DefaultFieldTags           []string
	SkipLogPathPatternTag      string
	ContainerNamePatternTag    string
}

type ServiceMetaData struct {
	ServiceName string
	SkipLogPathPattern string
	ContainerNamePattern string
	Properties map[string]string
}

type Services struct {
	LogCourierLoggerPtr        LoggerInterface;
	ConfigurationFileListPtr   ConfigurationFilesInterface;
	ServicesMetaDataList       []ServiceMetaData;
	FieldTags                  NormalizationFieldTags;
	DynamicVariableValues      map[string]string
}


func (services *Services) init(ConfigurationFileListPtr ConfigurationFilesInterface) {
	services.LogCourierLoggerPtr = new(LcLogger);
	services.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "services-module");
	services.ConfigurationFileListPtr = ConfigurationFileListPtr;
	services.ServicesMetaDataList = [] ServiceMetaData{}
	services.FieldTags = NormalizationFieldTags{};
	services.FieldTags.DefaultFieldTags = []string{ "device", "devtype", "part" , "parttype"};
	services.FieldTags.SkipLogPathPatternTag = "skip_logpath_pattern"
	services.FieldTags.ContainerNamePatternTag = "container_name_pattern"
	services.DynamicVariableValues = map[string]string{ "NUM" : "1",
										"CONTAINERNAME" : "@CONTAINERNAME",
										"ACCOUNT": "@ACCOUNT",
										"CLUSTERID": "@CLUSTERID",
										"IP": "CONTAINER_HOST_ADDRESS",
										"NODETYPE": "platform",
										"HOSTNAME" : "Caspian"};
	services.ReadSettingsFile();
	services.ReadPathConfigFile();
}


func (services *Services) ReadSettingsFile(){
	if c, err := config.ReadDefault(services.ConfigurationFileListPtr.GetLcSettingsConfFile()); err != nil {
		services.LogCourierLoggerPtr.Error("unable to read lc-settings.conf file for LOGCOURIER Bootstrapper so switching to default value");
	}else {
		for _, section := range c.Sections() {
			if (section == "default" ) {
				if options, err := c.Options(section); err == nil {
					for _, option := range options {
						if value, err := c.String(section, option); err == nil {
							services.DynamicVariableValues[option] = strings.TrimSpace(value);
						}
					}
				}
			}

			if (section == "log-normalization") {
				if options, err := c.Options(section); err == nil {
					for _, option := range options {
						if value, err := c.String(section, option); err == nil {
							 value = strings.TrimSpace(value);
							 temp := []string{};
			                 err = ParseStringToArrayOfString(value, &temp)
				             if err != nil {
		                         services.LogCourierLoggerPtr.Error("Failed parse string to array of string:", err)
		                     }else{
		             	         services.FieldTags.DefaultFieldTags = temp;
		                     }
						  }
					  }
				 }
			 }
		}
	}
	services.LogCourierLoggerPtr.Info("Dynamic variables stored for resolving:", services.DynamicVariableValues);
	services.LogCourierLoggerPtr.Info("log-Normalization properties:", services.FieldTags.DefaultFieldTags);
}


func (services *Services) ReadPathConfigFile(){
	if c, err := config.ReadDefault(services.ConfigurationFileListPtr.GetLcLogPathToServiceConfFile()); err != nil {
		services.LogCourierLoggerPtr.Error("unable to read "+ services.ConfigurationFileListPtr.GetLcLogPathToServiceConfFile() + " file for LOGCOURIER Bootstrapper so switching to default value");
	}else {
		for _, service_name:= range c.Sections() {
			    if (service_name == "DEFAULT" ){
			    	continue;
			    }
				servicemetadata := new(ServiceMetaData);
				servicemetadata.ServiceName = service_name;
				servicemetadata.SkipLogPathPattern = "";
				servicemetadata.Properties = make(map[string]string);
				for _, property := range services.FieldTags.DefaultFieldTags {
				    if value, err := c.String(service_name, property); err == nil {
			        servicemetadata.Properties[property] = services.ResolveVariable(value, services.DynamicVariableValues);
		          }
				}

				if value, err := c.String(service_name, services.FieldTags.SkipLogPathPatternTag); err == nil {
					value = strings.Trim(value, "\"")
			        servicemetadata.SkipLogPathPattern = value;
		          }

				if value, err := c.String(service_name, services.FieldTags.ContainerNamePatternTag); err == nil {
					value = strings.Trim(value, "\"")
			        servicemetadata.ContainerNamePattern = value;
		          }

				services.ServicesMetaDataList = append(services.ServicesMetaDataList, *servicemetadata)
	       	  }
		  }

	services.LogCourierLoggerPtr.Info("service log normalization configuration:", services.ServicesMetaDataList);
}


func (services *Services) GetServicesMetaDataList() (servicesMetaDataList []ServiceMetaData) {
	servicesMetaDataList = services.ServicesMetaDataList
	return servicesMetaDataList;
}

func (services *Services) filterLogFileForServiceName(serviceName string, paths []string) (filteredPaths []string, err error) {
	filteredPaths = []string{};
	err = nil;
	for _, service_metadata := range services.ServicesMetaDataList {

		if (service_metadata.ServiceName == serviceName) {
			containerNamePatternObject, err := regexp.Compile(service_metadata.ContainerNamePattern);
		    if (err != nil) {
			   services.LogCourierLoggerPtr.Error("Unable to compile container name pattern:", service_metadata.ContainerNamePattern," for service name:", service_metadata.ServiceName);
			   break;
		    }

		    logpathskipPatternObject, err := regexp.Compile(service_metadata.SkipLogPathPattern);
		    if (err != nil) {
			   services.LogCourierLoggerPtr.Error("Unable to compile path skiplogpattern:", service_metadata.SkipLogPathPattern, " for service name:", service_metadata.ServiceName);
			   break;
		    }

			for _, path := range paths {
				if foundBool := containerNamePatternObject.MatchString(path); foundBool {
					if (service_metadata.SkipLogPathPattern != "" ) {
						if isSkipPattern := logpathskipPatternObject.MatchString(path); !isSkipPattern {
				             filteredPaths = append(filteredPaths, path);
			            }
					}else{
						filteredPaths = append(filteredPaths, path);
					}
			    }
		    }

			break;
		}
	}
	return filteredPaths, err;
}


func (services *Services) GetServiceNameFromContainerName(containerName string)(serviceName string){
	serviceName = "Caspian_Others";
	for _, service_metadata:= range services.ServicesMetaDataList {
		if (service_metadata.ServiceName == "Caspian_Others") {
			continue;
		}
		containerNamePatternObject, err := regexp.Compile(service_metadata.ContainerNamePattern);
		if (err != nil) {
			services.LogCourierLoggerPtr.Error("Unable to compile container name pattern:", service_metadata.ContainerNamePattern," for service name:", service_metadata.ServiceName);
			continue;
		}
		if foundBool := containerNamePatternObject.MatchString(containerName); foundBool {
			serviceName = service_metadata.ServiceName;
			break;
		}

	}
	return serviceName;
}


func (services *Services) ResolveVariable(stringToBeParsed string, variableValues map[string]string)(resolvedstring string){

	myvarlist := variableValues;
	terms := strings.Split(stringToBeParsed, "@");
	for index, term := range terms {
		for k, v := range myvarlist{
			term = strings.TrimSpace(term);
			term = strings.Replace(term,k,v,1);
			terms[index] = term;
		}
	}

   return strings.Join(terms, "");
}

var ServicesInstance Services;
