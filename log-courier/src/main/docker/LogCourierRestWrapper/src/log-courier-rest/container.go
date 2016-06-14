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
* operations associated with create/update/delete of container configuration
*
**/

package main

import (
	"regexp"
	"sync"
	"errors"
	"strings"
	);


var ContainerHostIpaddress string = "127.0.0.1"
var DefaultContainerName   string = "AnyContainer"

type Container struct {
	Name string     `json:"container-name"`
  ContainerConfig FileConfig `json:"configuration"`
}

type ContainerList struct {
	LogCourierLoggerPtr          LoggerInterface;
	ServicesInstancePtr          ServicesInterface;
	PropertySubstitutorPtr       SubstitutorInterface;
	Containers                   []Container;
	mu                           sync.RWMutex
}


func (cl *ContainerList) init(ConfigurationFileListPtr ConfigurationFilesInterface, config *Config, ServicesInstancePtr ServicesInterface, PropertySubstitutorPtr SubstitutorInterface) {
	cl.LogCourierLoggerPtr = new(LcLogger);
	cl.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "container-module");
	cl.ServicesInstancePtr = ServicesInstancePtr;
	cl.PropertySubstitutorPtr = PropertySubstitutorPtr;
	cl.loadContainerListFromConfig(config);
}

func (cl *ContainerList) loadContainerListFromConfig(config *Config)(err error) {
	err = nil;
	for _, fileconfig := range config.Files {
		if (len(fileconfig.Paths) > 0){
		  	containerName := cl.GetContainerNameByPath(fileconfig.Paths[0]);
		  	cl.updateContainerEntry(containerName, fileconfig.Paths, []string{});
		}
	}
	return err;
}

func (cl *ContainerList) addContainerEntry(containerName string, path []string)(container Container){
	if  index, isPresent := cl.isContainerEntryPresent(containerName); isPresent {
		cl.mu.Lock();
		defer cl.mu.Unlock();
		container.Name = containerName;
		container.ContainerConfig = cl.GetFileConfigForContainer(containerName, path);
		cl.Containers[index] = container;
		return container;
	}else{
		temp := new(Container);
		container = *temp;
		container.Name = containerName;
		container.ContainerConfig = cl.GetFileConfigForContainer(containerName, path);
		cl.mu.Lock();
		defer cl.mu.Unlock();
		cl.Containers = append(cl.Containers, container);
		return container;
	}
}


func (cl *ContainerList) deleteContainerEntry(containerName string)(container Container, err error){
	err = nil;
	container = Container{};
	if index, isPresent := cl.isContainerEntryPresent(containerName); isPresent {
		cl.mu.Lock();
		defer cl.mu.Unlock();
		container = cl.Containers[index];
		cl.Containers = append(cl.Containers[:index],cl.Containers[index+1:]...);
	}else{
		err = errors.New("Configuration for the container:" + containerName +" is not Present. Non-existing container configuration cannot be deleted!!!");
	}
	return container, err;
}

func (cl *ContainerList) getAllContainerEntries()(containers []Container){
	cl.mu.RLock();
	defer cl.mu.RUnlock();
	return cl.Containers;
}

func (cl *ContainerList) deleteAllContainerEntries()(containers []Container, err error){
	err = nil;
	containers = []Container{};

	cl.mu.Lock()
	containers = cl.Containers;
	cl.Containers = []Container{};
	cl.mu.Unlock();

	return containers, err;
}


func (cl *ContainerList) updateContainerEntry(containerName string, toBeAddedPath []string, toBeRemovedPath []string)(container Container){
	if index, isPresent := cl.isContainerEntryPresent(containerName); isPresent {
		cl.mu.Lock();
		defer cl.mu.Unlock();

		if (len(toBeAddedPath) != 0 ) {
			toBeAddedfilecfg := cl.GetFileConfigForContainer(containerName, toBeAddedPath);
		    container = cl.Containers[index]
		    container.ContainerConfig.addPathsToFileConfigIfPartMatches(&toBeAddedfilecfg);
		    cl.Containers[index] = container
		}

		if (len(toBeRemovedPath) != 0 ) {
			toBeRemovedfilecfg := cl.GetFileConfigForContainer(containerName, toBeRemovedPath);
		    container = cl.Containers[index]
		    container.ContainerConfig.removePathsFromFileConfigIfPartMatches(&toBeRemovedfilecfg);
		    if ( len(container.ContainerConfig.Paths) == 0 ) {
		    	cl.Containers = append(cl.Containers[:index], cl.Containers[index+1:]...);
		    }else{
		    	cl.Containers[index] = container
		    }
		}

	}else {
		container = cl.addContainerEntry(containerName, toBeAddedPath);
	}
	return container;
}

func (cl *ContainerList) getContainerEntry(containerName string)(container Container, err error){

	if index, isPresent := cl.isContainerEntryPresent(containerName); isPresent {
		container = cl.Containers[index];
		err = nil;
	}else {
		err = errors.New("Configuration for the container:" + containerName +" is not Present. Create a new configuration for the container first!!!");
	}
	return container, err;
}

func (cl *ContainerList) isContainerEntryPresent(containerName string)(index int, isPresent bool){
	cl.mu.RLock();
	defer cl.mu.RUnlock();
	for index, container := range cl.Containers {
		if (container.Name == containerName) {
			return index, true;
		}
	}
	return -1,false;
}

//func (cl *ContainerList) getConfigurationForContainer(containerName string)(container Container, err error){
//	container = Container{};
//    if index, isPresent := cl.isContainerEntryPresent(containerName); isPresent {
//		container = cl.Containers[index];
//		err = nil;
//	}else {
//		err = errors.New("Configuration for the container:" + containerName +" is not Present. Create a new configuration for the container first!!!");
//	}
//	return container, err;
//}


func (cl *ContainerList) updateContainerEntryUsingLogFiles(addedPaths []string, removedPaths []string)(containers []Container, err error){
	containers = []Container{}
	addedPaths = removeDuplicatePathEntries(addedPaths);
	removedPaths = removeDuplicatePathEntries(removedPaths);

	if (len(addedPaths) != 0 ){
		for _, path := range addedPaths {
    	    if path == "" {
    	    	continue;
    	    }
			containerName := cl.GetContainerNameByPath(path);
			cl.LogCourierLoggerPtr.Info("Processing logpath:", path, " associated container-name:", containerName);
			container := cl.updateContainerEntry(containerName, []string{path}, []string{});
            containers = cl.AppendContainerToContainersList(containers, container);
			cl.LogCourierLoggerPtr.Info("New configuration:", container.ContainerConfig);
	   }
	}

	if (len(removedPaths) != 0 ){
		for _, path := range removedPaths {
    	    if path == "" {
    	    	continue;
    	    }
			containerName := cl.GetContainerNameByPath(path);
			cl.LogCourierLoggerPtr.Info("Processing logpath:", path," associated container-name:", containerName);
			container := cl.updateContainerEntry(containerName, []string{}, []string{path});
            containers = cl.AppendContainerToContainersList(containers, container);
		    cl.LogCourierLoggerPtr.Info("New configuration:", container.ContainerConfig);
	   }
	}

	return containers, err;
}


func (cl *ContainerList) GenerateConfigurationUsingLogFiles(addedlogfilepaths []string, removedlogfilepaths []string) (newconfig *Config, err error){
	err = nil;
	var containers []Container;
	newconfig = new(Config);
	if containers, err = cl.updateContainerEntryUsingLogFiles(addedlogfilepaths, removedlogfilepaths); err != nil {
		return newconfig , err;
	}
	newconfig.Files = []FileConfig{}
	for _, container := range containers {
		newconfig.Files = append(newconfig.Files, container.ContainerConfig);
	}
	return newconfig, err;
}


func (cl *ContainerList) GetFileConfigForContainer(containerName string, paths []string)(fileconfig FileConfig){
	serviceName := cl.ServicesInstancePtr.GetServiceNameFromContainerName(containerName);
	fileconfig = FileConfig{}
	for _, service_metadata := range cl.ServicesInstancePtr.GetServicesMetaDataList() {
		if (service_metadata.ServiceName == serviceName) {
			fileconfig.Paths = paths;
			fileconfig.Fields = make(map[string]string);
			for k, v := range service_metadata.Properties{
			  fileconfig.Fields[k] = v;
			}
			fileconfig.DeadTime = defaultConfig.fileDeadtime;
			var resolveFieldValues map[string]string = make(map[string]string);
			resolveFieldValues["CONTAINERNAME"] = containerName;
			fileconfig.Fields["part"] = cl.ServicesInstancePtr.ResolveVariable(fileconfig.Fields["part"], resolveFieldValues);
			if (serviceName == "BIGDATA_Ambari_Server" || serviceName == "BIGDATA_Ambari_Agent" || serviceName == "BIGDATA_Hadoop_KDC") {
				account, clusterid := cl.ExtractAccountAndCluster(containerName);
				resolveFieldValues["ACCOUNT"] = account;
				resolveFieldValues["CLUSTERID"] = clusterid;
				cl.LogCourierLoggerPtr.Info("Extracted Information from container:", containerName," account name:", account, " clusterid:", clusterid);
				if _,isFieldExists := fileconfig.Fields["account"]; isFieldExists {
					fileconfig.Fields["account"] = cl.ServicesInstancePtr.ResolveVariable(fileconfig.Fields["account"], resolveFieldValues);
				}
				if _,isFieldExists := fileconfig.Fields["clusterid"]; isFieldExists {
					fileconfig.Fields["clusterid"] = cl.ServicesInstancePtr.ResolveVariable(fileconfig.Fields["clusterid"], resolveFieldValues);
				}
			}
			fileconfig.Fields = cl.PropertySubstitutorPtr.RunSubstitutor(fileconfig.Fields, containerName)
			break;
		}
	}

	return fileconfig;
}


func (cl *ContainerList) GetContainerNameByPath(path string)(containerName string){
	containerName = DefaultContainerName;
	trpath := cl.TranslatePathForContainerName(path);
	for _, service_metadata:= range cl.ServicesInstancePtr.GetServicesMetaDataList() {
		if (service_metadata.ServiceName == "Caspian_Others") {
			continue;
		}
		containerNamePatternObject, err := regexp.Compile(service_metadata.ContainerNamePattern);
		if (err != nil) {
			cl.LogCourierLoggerPtr.Error("Unable to compile path pattern:", service_metadata.ContainerNamePattern," for service name:", service_metadata.ServiceName);
			continue;
		}

		logpathskipPatternObject, err := regexp.Compile(service_metadata.SkipLogPathPattern);
		if (err != nil) {
			cl.LogCourierLoggerPtr.Error("Unable to compile skiplogpath pattern:", service_metadata.SkipLogPathPattern," for service name:", service_metadata.ServiceName);
			continue;
		}

		if foundBool := containerNamePatternObject.MatchString(trpath); foundBool {
			isMatched := false;
			if (service_metadata.SkipLogPathPattern != "") {
				if isSkipPattern := logpathskipPatternObject.MatchString(path); !isSkipPattern {
				 isMatched = true;
			   }
			}else{
				isMatched = true;
			}

			if (isMatched) {
				containerNames := containerNamePatternObject.FindAllString(trpath, -1);
				if len(containerNames) > 0 {
					containerNameLength := 0;
					for  _, temp := range containerNames {
						if (len(temp) >= containerNameLength) {
							containerName = temp;
							containerNameLength = len(temp);
						}
					}
			    }
			    break;
			}

		}

	}
	return containerName;
}



func (cl *ContainerList) TranslatePathForContainerName(path string)(translatedpath string){
	translatedpath=path;
	translatedpath=strings.Replace(translatedpath,"_","-",-1);
	terms := strings.Split(path, "/");
	for index, term := range terms {
			term = strings.TrimSpace(term);
			if (term != "") {
				terms[index] = term;
				}
	}

   translatedpath = strings.Join(terms, "_");
   translatedpath = strings.TrimSpace(translatedpath);
   return translatedpath;
}


func (cl *ContainerList) AppendContainerToContainersList(containerList []Container, container Container)(modifiedContainerList []Container){

	if (len(containerList) != 0 ) {
		alreadyPresent := false;
		for index, alreadyPresentContainer := range containerList {
			if (alreadyPresentContainer.Name == container.Name) {
				containerList[index] = container;
				alreadyPresent = true;
				break;
			}
		}

		if (alreadyPresent == false) {
			containerList = append(containerList, container);
		}

	}else {
		containerList = append(containerList, container);
	}

	modifiedContainerList = containerList;
	return modifiedContainerList;
}


func (cl *ContainerList) ExtractAccountAndCluster(containerName string)(account string, clusterid string) {
	account = "";
	clusterid = "";
	additionInfoPattern := "[a-zA-Z0-9][a-zA-Z0-9]*-[0-9abcdef][0-9abcdef-]*$";
	additionalInfoPatternObject, _ := regexp.Compile(additionInfoPattern);
	if foundBool := additionalInfoPatternObject.MatchString(containerName); foundBool {
		extractedValues := additionalInfoPatternObject.FindAllString(containerName, -1);
		if len(extractedValues) > 0 {
			accountAndCluster := extractedValues[0];
			terms := strings.Split(accountAndCluster, "-");
			account = terms[0];
			clusterid = strings.Join(terms[1:],"-");
		}
	}

	return account, clusterid
}




var ContainerListInMemory ContainerList;
