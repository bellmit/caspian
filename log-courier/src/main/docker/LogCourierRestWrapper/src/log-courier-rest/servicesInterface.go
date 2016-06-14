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
* Interface for operations associated with configuration creation/updating/deletion for specific services 
*
**/

package main;


type ServicesInterface interface{
	init(ConfigurationFileListPtr ConfigurationFilesInterface)
	GetServicesMetaDataList() (servicesMetaDataList []ServiceMetaData)
	filterLogFileForServiceName(serviceName string, paths []string) (filteredPaths []string, err error)
	GetServiceNameFromContainerName(containerName string)(serviceName string)
	ResolveVariable(stringToBeParsed string, variableValues map[string]string)(resolvedstring string)
}