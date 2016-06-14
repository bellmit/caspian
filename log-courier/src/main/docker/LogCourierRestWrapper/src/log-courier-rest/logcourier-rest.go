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
* EXPOSING REST API for LOG COURIER MANAGEMENT
*
**/

package main

import ( 
	"github.com/ant0ine/go-json-rest/rest"
	"net/http"
	"errors"
	"strconv"
	)


type LogCourierRestApi struct {
	LogCourierLoggerPtr        LoggerInterface;
	Api                        *rest.Api;
	ServicesInstancePtr        ServicesInterface;
	ContainerListInMemoryPtr   ContainerListInterface;
	LogFileScannerInstancePtr  LogFileScannerInterface;
	ConfigCachePtr             InMemoryConfigInterface;
}


func (lcrestApi *LogCourierRestApi) init(ConfigurationFileListPtr ConfigurationFilesInterface, Api *rest.Api, ServicesInstancePtr ServicesInterface, ContainerListInMemoryPtr   ContainerListInterface, LogFileScannerInstancePtr  LogFileScannerInterface, ConfigCachePtr  InMemoryConfigInterface){
    lcrestApi.LogCourierLoggerPtr = new(LcLogger);
    lcrestApi.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "logcourier-rest-module");
    lcrestApi.SetApi(Api);
    lcrestApi.ServicesInstancePtr = ServicesInstancePtr;
	lcrestApi.ContainerListInMemoryPtr = ContainerListInMemoryPtr;
	lcrestApi.LogFileScannerInstancePtr = LogFileScannerInstancePtr;
	lcrestApi.ConfigCachePtr = ConfigCachePtr;
}

func (lcrestApi *LogCourierRestApi) GetApi() (Api *rest.Api) {
	if (lcrestApi.Api == nil) {
		lcrestApi.Api = rest.NewApi()
	 	lcrestApi.Api.Use(rest.DefaultDevStack...)
	}
	return lcrestApi.Api;
}


func (lcrestApi *LogCourierRestApi) SetApi(Api *rest.Api){
	if (Api == nil) {
		lcrestApi.Api = rest.NewApi()
		lcrestApi.Api.Use(rest.DefaultDevStack...)
	}else {
		lcrestApi.Api = Api;
	}
}


func (lcrestApi *LogCourierRestApi) RegisterAPI(){
	 if lcrestApi.Api == nil {
	 	lcrestApi.Api = rest.NewApi()
	 	lcrestApi.Api.Use(rest.DefaultDevStack...)
	 	}
     
     router, err := rest.MakeRouter(
     	rest.Get("/api/log-paths", lcrestApi.GetFullContainerConfiguration),
     	rest.Delete("/api/log-paths", lcrestApi.DeleteFullContainerConfiguration),
     	rest.Get("/api/log-paths/#containerName", lcrestApi.GetContainerConfiguration),
     	rest.Post("/api/log-paths", lcrestApi.CreateContainerConfigurationByExtractingContainerName),
     	rest.Put("/api/log-paths", lcrestApi.CreateContainerConfigurationByExtractingContainerName),
     	rest.Post("/api/log-paths/#containerName", lcrestApi.CreateContainerConfiguration),
     	rest.Put("/api/log-paths/#containerName", lcrestApi.UpdateContainerConfiguration),
     	rest.Delete("/api/log-paths/#containerName", lcrestApi.DeleteContainerConfiguration),
     	rest.Get("/api/#containerName/refresh", lcrestApi.RefreshContainerConfiguration),
     	rest.Get("/api/log-path-dirs", lcrestApi.GetLogPathDirs),
     	rest.Post("/api/log-path-dirs", lcrestApi.AddLogPathDirs),
     	rest.Put("/api/log-path-dirs", lcrestApi.UpdateLogPathDirs),
     	rest.Get("/api/log-path-patterns", lcrestApi.GetLogPathPatterns),
     	rest.Post("/api/log-path-patterns", lcrestApi.AddLogPathPatterns),
     	rest.Put("/api/log-path-patterns", lcrestApi.UpdateLogPathPatterns),
     	rest.Put("/api/synch-cache", lcrestApi.RefreshCache),
    )
    if err != nil {
        lcrestApi.LogCourierLoggerPtr.Error(err.Error());
        panic(err);
    }
    lcrestApi.Api.SetApp(router)
}


//RegisterAppication function registers our Rest service on the running HTTP server.
func (lcrestApi *LogCourierRestApi) RegisterApplication(){
   lcrestApi.RegisterAPI();
}

//GetFullContainerConfiguration function is used as a handler method to handle GET request 
//for /api/log-paths and send container configuration for all containers
func (lcrestApi *LogCourierRestApi) GetFullContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	    w.WriteJson(lcrestApi.ContainerListInMemoryPtr.getAllContainerEntries())
}

//DeleteFullContainerConfiguration function is used as a handler method to handle DELETE request 
//for /api/log-paths and delete container configuration for all containers
func (lcrestApi *LogCourierRestApi) DeleteFullContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	if containers, err := lcrestApi.ContainerListInMemoryPtr.deleteAllContainerEntries(); err != nil {
		rest.Error(w, err.Error(), http.StatusInternalServerError)
	}else {
		var config *Config = new(Config);
		config.Files = []FileConfig{};
		for _, container := range containers {
			config.Files = append(config.Files, container.ContainerConfig);
			}
		scanlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles();
		scanlogFilesObj.cleanAllLogFiles();
		lcrestApi.ConfigCachePtr.deleteConfiguration(config);
	    w.WriteJson(map[string]string{"status" : "deleted"}) 
	}
}


//GetContainerConfiguration function is used as a handler method to handle GET request
//for /api/log-paths/#containerName and send container configuration for a specific container
func (lcrestApi *LogCourierRestApi) GetContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	containerName := r.PathParam("containerName");
	if container, err := lcrestApi.ContainerListInMemoryPtr.getContainerEntry(containerName); err != nil {
		rest.Error(w, err.Error(), http.StatusInternalServerError)
	}else {
	    w.WriteJson(&container)
	}
}


//CreateContainerConfigurationByExtractingContainerName function is used as handler method to handle POST request
//for /api/log-paths and create a container configutration by extracting container name from the paths present in the payload
func (lcrestApi *LogCourierRestApi) CreateContainerConfigurationByExtractingContainerName(w rest.ResponseWriter, r *rest.Request) {
	var paths []string = []string{};
	err := r.DecodeJsonPayload(&paths);
    if err != nil {
        rest.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
    var containers []Container;
	if containers, err = lcrestApi.ContainerListInMemoryPtr.updateContainerEntryUsingLogFiles(paths, []string{}); err != nil {
        rest.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
	config := new(Config);
	config.Files = []FileConfig{};
	for _, container := range containers {
		config.Files = append(config.Files, container.ContainerConfig);
	}
	scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
	scannedlogFilesObj.mergeLogFiles(paths, []string{});
	lcrestApi.ConfigCachePtr.updateConfiguration(config);
	w.WriteJson(&containers)
}


//CreateContainerConfiguration function is used as a handler method to handle POST request
//for /ap/log-paths/#containerName and create a container configuration for a specific container
func (lcrestApi *LogCourierRestApi) CreateContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	containerName := r.PathParam("containerName");
	var paths []string = []string{};
	err := r.DecodeJsonPayload(&paths);
	if err != nil {
        rest.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }

	if container, err := lcrestApi.ContainerListInMemoryPtr.getContainerEntry(containerName); err == nil {
		scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
		scannedlogFilesObj.removeLogFiles(container.ContainerConfig.Paths);
	}
	
	lcrestApi.ContainerListInMemoryPtr.addContainerEntry(containerName, paths)
	if container, createErr := lcrestApi.ContainerListInMemoryPtr.getContainerEntry(containerName); createErr != nil {
		rest.Error(w, err.Error(), http.StatusInternalServerError)
	}else {
		config := new(Config);
	    config.Files = []FileConfig{container.ContainerConfig};
	    scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
	    scannedlogFilesObj.mergeLogFiles(paths, []string{});
	    lcrestApi.ConfigCachePtr.updateAndRemoveConfiguration(config);
	    w.WriteJson(&container)
	}
}


//UpdateContainerConfiguration function is used as a handler method to handler PUT request
//for /api/log-paths/#containerName and update container configuration for a specific container with new paths
func (lcrestApi *LogCourierRestApi) UpdateContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	containerName := r.PathParam("containerName");
	var paths []string = []string{};
	err := r.DecodeJsonPayload(&paths);
	if err != nil {
        rest.Error(w, err.Error(), http.StatusInternalServerError)
        return
    }
	lcrestApi.ContainerListInMemoryPtr.updateContainerEntry(containerName, paths, []string{})
	if container, createErr := lcrestApi.ContainerListInMemoryPtr.getContainerEntry(containerName); createErr != nil {
		rest.Error(w, err.Error(), http.StatusInternalServerError)
	}else {
		config := new(Config);
	    config.Files = []FileConfig{container.ContainerConfig};
	    scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
	    scannedlogFilesObj.mergeLogFiles(paths, []string{});
	    lcrestApi.ConfigCachePtr.updateConfiguration(config);
	    w.WriteJson(&container)
	}
}


//DeleteContainerConfiguration function is used as a handler method to handle DELETE request
//for /api/log-paths/#containerName and delete container configuration for a specific container
func (lcrestApi *LogCourierRestApi) DeleteContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	containerName := r.PathParam("containerName");
	if container, err := lcrestApi.ContainerListInMemoryPtr.deleteContainerEntry(containerName); err != nil {
		rest.Error(w, err.Error(), http.StatusInternalServerError)
	}else {
		var config *Config = new(Config);
		config.Files = []FileConfig{container.ContainerConfig};
		scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
		scannedlogFilesObj.removeLogFiles(container.ContainerConfig.Paths);
		lcrestApi.ConfigCachePtr.deleteConfiguration(config);
	    w.WriteJson(map[string]string{"status" : "deleted"}) 
	}
}


//RefreshContainerConfiguration function is used as a handler method to handle GET request
//for /api/#containerName/refresh to put new logfiles in the logcourier configuration
func (lcrestApi *LogCourierRestApi) RefreshContainerConfiguration(w rest.ResponseWriter, r *rest.Request) {
	containerName := r.PathParam("containerName");
	if logfiles, err := lcrestApi.LogFileScannerInstancePtr.scanLogFilesForContainer(containerName); err != nil {
		lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Error scanning the log folder for looking newly added log files [reason:" + err.Error() + "]");
		rest.Error(w, err.Error(), http.StatusInternalServerError)
		return;
	}else {
		    addedlogfiles := [] string{};
		    removedlogfiles := [] string{};
		    if (containerName == "_all") {
		    	scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles();
		    	addedlogfiles, removedlogfiles, _ = scannedlogFilesObj.getNewlyAddedAndRemovedLogFiles(logfiles);
		    }else {
		    	if container, err := lcrestApi.ContainerListInMemoryPtr.getContainerEntry(containerName); err == nil {
		    		serviceName := lcrestApi.ServicesInstancePtr.GetServiceNameFromContainerName(containerName);
		    		if logfiles, err = lcrestApi.ServicesInstancePtr.filterLogFileForServiceName(serviceName, logfiles); err != nil {
		    			 lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
						err = errors.New("Error scanning the log folder for looking newly added log files [reason:" + err.Error() + "]");
						rest.Error(w, err.Error(), http.StatusInternalServerError)
						return;
		    		}
					addedlogfiles, removedlogfiles, _ = GetAddedAndRemovedSetFromSet(container.ContainerConfig.Paths, logfiles);
				}else {
					addedlogfiles = logfiles;
				}
		    }
		    
		    if (len(addedlogfiles) != 0 || len(removedlogfiles) != 0) {
		    	var containers []Container;
	       		if containers, err = lcrestApi.ContainerListInMemoryPtr.updateContainerEntryUsingLogFiles(addedlogfiles, removedlogfiles); err != nil {
        	       rest.Error(w, err.Error(), http.StatusInternalServerError)
        	       return
                }
		        config := new(Config);
		        config.Files = []FileConfig{};
		        for _, container := range containers {
			       config.Files = append(config.Files, container.ContainerConfig);
		        }
		        scannedlogFilesObj := lcrestApi.LogFileScannerInstancePtr.GetScannedLogFiles()
		        scannedlogFilesObj.mergeLogFiles(addedlogfiles, removedlogfiles);
		        lcrestApi.ConfigCachePtr.updateConfiguration(config);
		        if (len(addedlogfiles) != 0) {
		        	lcrestApi.LogCourierLoggerPtr.Info("After logfiles scanning- newly added logfiles: ", addedlogfiles);
		        }
		        if (len(removedlogfiles) != 0) {
		        	lcrestApi.LogCourierLoggerPtr.Info("After logfiles scanning- newly removed logfiles: ", removedlogfiles);
		        }
		    }
	    	
		    w.WriteJson(map[string]string{"status" : "configuration successfully refreshed",
		    		                      "Log files added" : strconv.Itoa(len(addedlogfiles)),
		    		                      "Log files removed" : strconv.Itoa(len(removedlogfiles)),
		    		                      "containerName" : containerName,
		    		})
	}
}


//GetLogPathDirs function is used as a handler method to handle GET request
//for /api/log-path-dirs to get directories from we will scan logfiles
func (lcrestApi *LogCourierRestApi) GetLogPathDirs(w rest.ResponseWriter, r *rest.Request) {
	dirs, _ := lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
	w.WriteJson(map[string][]string{"logpath_dirs" : dirs});
}


//AddLogPathDirs function is used as a handler method to handle POST request
//for /api/log-path-dirs to add new directory for scanning logfiles inside it
func (lcrestApi *LogCourierRestApi) AddLogPathDirs(w rest.ResponseWriter, r *rest.Request) {
	dirpaths := [] string{};
	if  err := r.DecodeJsonPayload(&dirpaths); err != nil {
		lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Unable to read directory paths send in payload");
		rest.Error(w, err.Error(), http.StatusBadRequest)
		return;
	}else{
		lcrestApi.LogFileScannerInstancePtr.addLogFilesDirs(dirpaths);
		lcrestApi.LogFileScannerInstancePtr.persistConfiguration();
		dirpaths,_ = lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
		w.WriteJson(map[string][]string{"logpath_dirs" : dirpaths});
	}
}


//GetLogPathDirs function is used as a handler method to handle PUT request
//for /api/log-path-dirs to append new directory to the already existing directory for scanning logfiles inside it
func (lcrestApi *LogCourierRestApi) UpdateLogPathDirs(w rest.ResponseWriter, r *rest.Request) {
	dirpaths := [] string{};
	if  err := r.DecodeJsonPayload(&dirpaths); err != nil {
		lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Unable to read directory paths send in payload");
		rest.Error(w, err.Error(), http.StatusBadRequest)
		return;
	}else{
		lcrestApi.LogFileScannerInstancePtr.updateLogFilesDirs(dirpaths);
		lcrestApi.LogFileScannerInstancePtr.persistConfiguration();
		dirpaths,_ = lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
		w.WriteJson(map[string][]string{"logpath_dirs" : dirpaths});
	}
}


//GetLogPathPatterns function is used as a handler method to handle GET request
//for /api/log-path-patterns to get logfile pattern which will be scanned and collected for log aggregation
func (lcrestApi *LogCourierRestApi) GetLogPathPatterns(w rest.ResponseWriter, r *rest.Request) {
	_, dirpatterns := lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
	w.WriteJson(map[string][]string{"logpath_patterns" : dirpatterns});
}


//AddLogPathPatterns function is used as a handler method to handle POST request
//for /api/log-path-patterns to add new logfile pattern which will be scanned and collected for log aggregation
func (lcrestApi *LogCourierRestApi) AddLogPathPatterns(w rest.ResponseWriter, r *rest.Request) {
	dirpatterns := [] string{};
	if  err := r.DecodeJsonPayload(&dirpatterns); err != nil {
		 lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Unable to read directory paths send in payload");
		rest.Error(w, err.Error(), http.StatusBadRequest)
		return;
	}else{
		lcrestApi.LogFileScannerInstancePtr.addLogFilesPatterns(dirpatterns);
		lcrestApi.LogFileScannerInstancePtr.persistConfiguration();
		_, dirpatterns = lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
		w.WriteJson(map[string][]string{"logpath_patterns" : dirpatterns});
	}
}


//UpdateLogPathPatterns function is used as a handler method to handle PUT request
//for /api/log-path-patterns to update logfile pattern which will be scanned and collected for log aggregation
func (lcrestApi *LogCourierRestApi) UpdateLogPathPatterns(w rest.ResponseWriter, r *rest.Request) {
	dirpatterns := [] string{};
	if  err := r.DecodeJsonPayload(&dirpatterns); err != nil {
		 lcrestApi.LogCourierLoggerPtr.Error("Error:", err);
		err = errors.New("Unable to read directory paths send in payload");
		rest.Error(w, err.Error(), http.StatusBadRequest)
		return;
	}else{
		lcrestApi.LogFileScannerInstancePtr.updateLogFilesPatterns(dirpatterns);
		lcrestApi.LogFileScannerInstancePtr.persistConfiguration();
		_, dirpatterns = lcrestApi.LogFileScannerInstancePtr.getDirAndPattern();
		w.WriteJson(map[string][]string{"logpath_patterns" : dirpatterns});
	}
}

//RefreshCache function is used as a handler method handle Put request to refresh cache by synching the cache for 
// and configuration file
func (lcrestApi *LogCourierRestApi) RefreshCache(w rest.ResponseWriter, r *rest.Request) {
	if err := lcrestApi.ConfigCachePtr.synchConfiguration(); err != nil {
	   w.WriteJson(map[string]string{"status" : "cache synch not successful"});	
	}else {		
	    w.WriteJson(map[string]string{"status" : "cache synch successful"});
	}
}

var LcRestApi LogCourierRestApi ;