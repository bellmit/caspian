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
* Interface for operations associated with create/update/delete of container configuration
*
**/

package main;

type ContainerListInterface interface {
	init(ConfigurationFileListPtr ConfigurationFilesInterface, config *Config, ServicesInstancePtr ServicesInterface, PropertySubstitutorPtr SubstitutorInterface)
	addContainerEntry(containerName string, path []string)(container Container)
	getContainerEntry(containerName string)(container Container, err error)
	updateContainerEntry(containerName string, toBeAddedPath []string, toBeRemovedPath []string)(container Container)
	getAllContainerEntries()(containers []Container)
	deleteContainerEntry(containerName string)(container Container, err error)
	deleteAllContainerEntries()(containers []Container, err error)
	isContainerEntryPresent(containerName string)(index int, isPresent bool)
	updateContainerEntryUsingLogFiles(addedPaths []string, removedPaths []string)(containers []Container, err error)
	GenerateConfigurationUsingLogFiles(addedlogfilepaths []string, removedlogfilepaths []string) (newconfig *Config, err error)
	GetFileConfigForContainer(containerName string, paths []string)(fileconfig FileConfig)
	GetContainerNameByPath(path string)(containerName string)
}
