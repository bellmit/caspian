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
* Handles loading, parsing and updating log-courier configuration file
*
**/
package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"regexp"
	"time"
	"reflect"
	//"log"
	"errors"
	//"sync"
)

const configFileSizeLimit = 10 << 20

var defaultConfig = &struct {
	netTimeout   int64
	fileDeadtime string
}{
	netTimeout:   15,
	fileDeadtime: "24h",
}

var ConfigCache InMemoryConfig= InMemoryConfig { IsInit: false };


//Config data type that is read from the configuration file used by log-courier binary
type Config struct {
	General GeneralConfig     `json:"general"`
	Network NetworkConfig     `json:"network"`
	Files   []FileConfig      `json:"files"`
}


func (toConfig *Config) isFieldExistsInFileConfig(field *map[string]string) (index int, isPresent bool){
	isPresent = true;
	index = -1;
	length := len(toConfig.Files); 
	if (length == 0) {
		isPresent = false;
	}
	for i:= 0; i < length; i++ {
		for k, v := range *field {
			if val, ok := toConfig.Files[i].Fields[k]; ok {
				if (val == v) {
					isPresent = isPresent && true;
				}else {
					isPresent = false;
				    break;
				}
			}else {
				isPresent = false;
				break;
			}
		}
		if (isPresent == true) {
			index = i;
		}
	}
	return index, isPresent;
}


//mergeFileConfigForType method merges the two FileConfig data-type by comparing their part field value
func (toConfig *Config) mergeFileConfigForPart(fromConfig *Config) {
	for i:= 0; i < len(fromConfig.Files); i++ {
		if (len(toConfig.Files) == 0) {
			    toConfig.Files = append(toConfig.Files, fromConfig.Files[i]);
				continue;
				}
		for j:= 0 ; j < len(toConfig.Files); j++ {
			if (fromConfig.Files[i].Fields["part"] == toConfig.Files[j].Fields["part"]) {
				toConfig.Files[j].Paths = append(toConfig.Files[j].Paths, fromConfig.Files[i].Paths...);
				if (fromConfig.Files[i].DeadTime != "" ) {
					toConfig.Files[j].DeadTime = fromConfig.Files[i].DeadTime;
				}
				for k, v := range fromConfig.Files[i].Fields {
					toConfig.Files[j].Fields[k] = v;
				}
				toConfig.Files[j].Paths = removeDuplicatePathEntries(toConfig.Files[j].Paths);
				break;
			}
			if (j+1 == len(toConfig.Files)) {
				toConfig.Files = append(toConfig.Files, fromConfig.Files[i]);
				break;
			}
		}
	}
	
}


//deleteFileConfig method deletes the fromConfig.FileConfig's present inside toConfig
func (toConfig *Config) deleteFileConfigIfPartMatches(fromConfig *Config) (err error, isdeleted bool){
	isdeleted = false;
	for i:= 0; i < len(fromConfig.Files); i++ {
		for j:= 0 ; j < len(toConfig.Files); j++ {
			if (fromConfig.Files[i].Fields["part"] == toConfig.Files[j].Fields["part"]) {	
					toConfig.Files = append(toConfig.Files[:j], toConfig.Files[j+1:]...);
					j--;
					isdeleted = true;
					return nil, isdeleted
				}
		}
	}
	return errors.New("Could not find any matching Paths to delete"), isdeleted
}

func (toConfig *Config) mergeNetworkConfig(fromConfig *Config) {
	toConfig.Network.mergeServers(&fromConfig.Network);
}


//GeneralConfig data-type present in the configuration file used by log-courier binary.
// At present we are only interested in two configurations-
// 1. AdminEnabled - to enable lc_admin
// 2. Host - to send host ip information along with collected logs
type GeneralConfig struct {
	AdminEnabled     bool          `json:"admin enabled"`
	Host             string        `json:"host"`
}


//NetworkConfig data-type present in the configuration file used by log-courier binary.
// At present we are only interested in servers field;
type NetworkConfig struct {
	Servers        []string `json:"servers"`
	Transport      string   `json:"transport"`
//	SSLCertificate string   `json:"ssl certificate"`
//	SSLKey         string   `json:"ssl key"`
//	SSLCA          string   `json:"ssl ca"`
	Timeout        int64    `json:"timeout"`
	timeout        time.Duration
}

func (toNetworkConfig *NetworkConfig) mergeServers(fromNetworkConfig *NetworkConfig) {
	toNetworkConfig.Servers = append(toNetworkConfig.Servers, fromNetworkConfig.Servers...);
}


//FileConfig data-type present in the configuration file used by log-courier binary.
// This configuration used to by log-courier engine to collect logs as it gives the path of logs.
// 1. Paths - contains the array of string of paths where we are expecting logs
// 2. Fields - adding map[string]string parameters that we want to hard-code along with the collected logs
// 3. DeadTime - DeadTime for the collection of logs. Default value is 24h (24 hour)
type FileConfig struct {
	Paths    []string          `json:"paths"`
	Fields   map[string]string `json:"fields"`
	DeadTime string            `json:"dead time"`
	deadtime time.Duration
}

type Comparator interface {
	compare(a interface{}, b interface{}) bool;
}

//compare method compares two FileConfig data-type values if they are equal or not.
// It is used to remove duplicate FileConfig entry and 
// it is also used to delete a particular FileConfig entry
func (filecfg *FileConfig) compare(secondfilecfg *FileConfig) (isequal bool){
	isequal = false;
	if reflect.DeepEqual(filecfg.Paths, secondfilecfg.Paths) && reflect.DeepEqual(filecfg.Fields, secondfilecfg.Fields) {
		isequal = true;
	}
	return    
}


//mergeFileConfigForType method merges the two FileConfig data-type by comparing their part field value
func (tofilecfg *FileConfig) mergeFileConfigForPart(fromfilecfg *FileConfig) {
		if (fromfilecfg.Fields["part"] == tofilecfg.Fields["part"]) {
			tofilecfg.Paths = append(tofilecfg.Paths, fromfilecfg.Paths...);
			if (fromfilecfg.DeadTime != "" ) {
				tofilecfg.DeadTime = fromfilecfg.DeadTime;
			}
			tofilecfg.Paths = removeDuplicatePathEntries(tofilecfg.Paths);
		}
}


//mergeFileConfigForType method merges the two FileConfig data-type by comparing their part field value
func (tofilecfg *FileConfig) removePathsFromFileConfigIfPartMatches(fromfilecfg *FileConfig) {
		if (fromfilecfg.Fields["part"] == tofilecfg.Fields["part"]) {
			for _, toBeDeletePath := range fromfilecfg.Paths {
				for index, path := range tofilecfg.Paths {
					if (path == toBeDeletePath) {
						tofilecfg.Paths = append(tofilecfg.Paths[:index], tofilecfg.Paths[index+1:]...);
					    break;
					}
				}
			}
		}
}


//mergeFileConfigForType method merges the two FileConfig data-type by comparing their part field value
func (tofilecfg *FileConfig) addPathsToFileConfigIfPartMatches(fromfilecfg *FileConfig) {
		if (fromfilecfg.Fields["part"] == tofilecfg.Fields["part"]) {
			tofilecfg.Paths = append(tofilecfg.Paths, fromfilecfg.Paths...);
			tofilecfg.Paths = removeDuplicatePathEntries(tofilecfg.Paths);
		}
}


func removeDuplicateFileConfigEntries(arrayA []FileConfig) (arrayB []FileConfig) {
	for i:= 0; i < len(arrayA); i++ {
		for j:= i + 1 ; j < len(arrayA); j++ {
			if(arrayA[i].compare(&arrayA[j])) {		
					arrayA = append(arrayA[:j], arrayA[j+1:]...);
					j--;
				}
		}
	}
	return arrayA;
}


func removeDuplicatePathEntries(arrayA []string) (arrayB []string) {
	for i:= 0; i < len(arrayA); i++ {
		for j:= i + 1 ; j < len(arrayA); j++ {
			if(arrayA[i] == arrayA[j]) {		
					arrayA = append(arrayA[:j], arrayA[j+1:]...);
					j--;
				}
		}
	}
	return arrayA;
}

//UnsafeMergeConfig append values to the 'to' config from the 'from' config, erroring
// if a value would be overwritten by the merge.
// This function is not thread safe since it is not taking any lock on the content of the configuration file
func UnsafeMergeConfig(to *Config, from Config) (err error) {

	to.Network.Servers = append(to.Network.Servers, from.Network.Servers...)
	to.Files = append(to.Files, from.Files...)

//	if from.Network.SSLCertificate != "" {
//		if to.Network.SSLCertificate != "" {
//			return fmt.Errorf("SSLCertificate already defined as '%s' in previous config file", to.Network.SSLCertificate)
//		}
//		to.Network.SSLCertificate = from.Network.SSLCertificate
//	}
//	if from.Network.SSLKey != "" {
//		if to.Network.SSLKey != "" {
//			return fmt.Errorf("SSLKey already defined as '%s' in previous config file", to.Network.SSLKey)
//		}
//		to.Network.SSLKey = from.Network.SSLKey
//	}
//	if from.Network.SSLCA != "" {
//		if to.Network.SSLCA != "" {
//			return fmt.Errorf("SSLCA already defined as '%s' in previous config file", to.Network.SSLCA)
//		}
//		to.Network.SSLCA = from.Network.SSLCA
//	}
	if from.Network.Timeout != 0 {
		if to.Network.Timeout != 0 {
			return fmt.Errorf("Timeout already defined as '%d' in previous config file", to.Network.Timeout)
		}
		to.Network.Timeout = from.Network.Timeout
	}
	return nil
}


//UnsafeLoadConfiguration loads the configuration in the memory by parsing and json unmarshalling the contents 
// present in the configuration file used by log-courier binary.
// It is not thread safe since it is not locking the content for reading.
func UnsafeLoadConfiguration(path string) (config Config, err error) {
	config_file, err := os.Open(path)
	if err != nil {
		fmt.Println("Failed to open config file '%s': %s\n", path, err)
		return
	}

	fi, _ := config_file.Stat()
	if size := fi.Size(); size > (configFileSizeLimit) {
		fmt.Println("config file (%q) size exceeds reasonable limit (%d) - aborting", path, size)
		return 
	}

	if fi.Size() == 0 {
		fmt.Println("config file (%q) is empty, skipping", path)
		return
	}

	buffer := make([]byte, fi.Size())
	_, err = config_file.Read(buffer)
	

	buffer, err = StripComments(buffer)
	if err != nil {
		fmt.Println("Failed to strip comments from json: %s\n", err)
		return
	}

	err = json.Unmarshal(buffer, &config)
	if err != nil {
		fmt.Println("Failed unmarshalling json: %s\n", err)
		return
	}

	for k, _ := range config.Files {
		if config.Files[k].DeadTime == "" {
			config.Files[k].DeadTime = defaultConfig.fileDeadtime
		}
		config.Files[k].deadtime, err = time.ParseDuration(config.Files[k].DeadTime)
		if err != nil {
			fmt.Println("Failed to parse dead time duration '%s'. Error was: %s\n", config.Files[k].DeadTime, err)
			return
		}
	}

	return
}

func FinalizeConfig(config *Config) {
	if config.Network.Timeout == 0 {
		config.Network.Timeout = defaultConfig.netTimeout
	}

	config.Network.timeout = time.Duration(config.Network.Timeout) * time.Second
}


//StripComments strips the comments present in the configuration file used by log-courier binary.
//It is required to unmarshal the json object present in the form of string in the configuration file
func StripComments(data []byte) ([]byte, error) {
	data = bytes.Replace(data, []byte("\r"), []byte(""), 0) // Windows
	lines := bytes.Split(data, []byte("\n"))
	filtered := make([][]byte, 0)

	for _, line := range lines {
		match, err := regexp.Match(`^\s*#`, line)
		if err != nil {
			return nil, err
		}
		if !match {
			filtered = append(filtered, line)
		}
	}

	return bytes.Join(filtered, []byte("\n")), nil
}