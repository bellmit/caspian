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
* Interface for EXPOSING REST API for LOG COURIER MANAGEMENT
*
**/

package main;

import "github.com/ant0ine/go-json-rest/rest"

type LogCourierRestApiInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface, Api *rest.Api, ServicesInstancePtr ServicesInterface, ContainerListInMemoryPtr   ContainerListInterface, LogFileScannerInstancePtr  LogFileScannerInterface, ConfigCachePtr  InMemoryConfigInterface)
    GetApi() (Api *rest.Api)
    SetApi(Api *rest.Api)
    RegisterAPI()
    RegisterApplication()
    GetFullContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    DeleteFullContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    GetContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    CreateContainerConfigurationByExtractingContainerName(w rest.ResponseWriter, r *rest.Request)
    CreateContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    UpdateContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    DeleteContainerConfiguration(w rest.ResponseWriter, r *rest.Request) 
    RefreshContainerConfiguration(w rest.ResponseWriter, r *rest.Request)
    GetLogPathDirs(w rest.ResponseWriter, r *rest.Request)
    AddLogPathDirs(w rest.ResponseWriter, r *rest.Request) 
    UpdateLogPathDirs(w rest.ResponseWriter, r *rest.Request)
    GetLogPathPatterns(w rest.ResponseWriter, r *rest.Request)
    AddLogPathPatterns(w rest.ResponseWriter, r *rest.Request)
    UpdateLogPathPatterns(w rest.ResponseWriter, r *rest.Request)
}