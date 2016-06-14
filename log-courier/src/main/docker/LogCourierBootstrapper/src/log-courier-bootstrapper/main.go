package main;

import (
	"log"
	"flag"
	"path/filepath"
	"os"
	"github.com/revel/config"
	"strings"
	"regexp"
	)


type ConfigFileList struct {
	LogfileToserviceMappingConfigFilePath string
	ParserSettingsConfigFilePath string
	LogCourierConfigFilePath string
}

var parserConfigurationFiles  ConfigFileList;

type LogFilePath struct{
	Path string
	IsDeleted bool
}

var LogFilePathsList []LogFilePath = []LogFilePath{};

type ServiceAndVariableList struct {
    ServiceNameList             map[string][]string
	VariableList                map[string]string
}

var variableList  =  map[string]string{ "MaxContainers": "1000", 
	                        				"NUM": "1",
		             						"NUM_INCR": "1",                                                               
											"UNIQUE_ID": "DYNAMIC_NUM",																				  
											"IP": "CONTAINER_HOST_IPADDR"};																				 
																					  
var serviceGroups map[string][]string = map[string][]string {"caspian_platform_services" : []string{},
	          												 "caspian_common_services" : []string{},                                                              	       
															 "caspian_cloud_compute_services": []string{}};	                                                                        	       
	                                                                        	       
var serviceAndVariableList ServiceAndVariableList = ServiceAndVariableList{VariableList : variableList,
	                                                                       ServiceNameList: serviceGroups};                                                                    	
	                                                                        

var logNormalizationProperties []string = []string{ "device", "devtype", "part" , "parttype"};
var logPathPatternTag string = "logpath_pattern"

type ServicesMetaData struct {
	ServiceName              string 
	AdditionProperties       map[string]string
	PathPattern              string
	Paths                    []string
}

var serviceMetaDataList []ServicesMetaData = []ServicesMetaData{};



func main(){
	Setup();
	readSettingsFile()
	readPathConfigFile()
	resolveVariableInServiceMetaDataList()
	classifyLogPaths()
	persistPathConfiguration()
}

func Setup(){
	dir, _ := filepath.Abs(filepath.Dir(os.Args[0]))
	mappingConfigPath := flag.String("logpathtoserviceconfig", dir + "/" + "../src/conf/lc-pathconfig.conf", "log path file location to service name mapping configuration-file  absolute path")
	settingsConfigPath := flag.String("parsersettings", dir + "/" + "../src/conf/lc-settings.conf", "parser settings configuration file absolute path");
	lcConfigPath := flag.String("lcconfig", "/opt/log-management/log-courier/log-courier.conf", "log-courier configuration file absolute path");
	logfilePaths := flag.String("logfilepaths", "", "log folder absolute path which contains logs");
	flag.Parse();
	
	if filepath.IsAbs(*mappingConfigPath) == false {
		*mappingConfigPath,_ = filepath.Abs(*mappingConfigPath)
	}
	
	if filepath.IsAbs(*settingsConfigPath) == false {
		*settingsConfigPath,_ = filepath.Abs(*settingsConfigPath)
	}
	
	if filepath.IsAbs(*lcConfigPath) == false {
		*lcConfigPath,_ = filepath.Abs(*lcConfigPath)
	}
    
    parserConfigurationFiles.LogfileToserviceMappingConfigFilePath = *mappingConfigPath;
    parserConfigurationFiles.ParserSettingsConfigFilePath = *settingsConfigPath;
    parserConfigurationFiles.LogCourierConfigFilePath = *lcConfigPath;
    pathlist := []string{};
    err := parseStringToArrayOfString(*logfilePaths, &pathlist)
	if err != nil {
		log.Println("Failed parse string to array of string: %s\n", err)
		return ;
	}
	log.Printf("logpathlist:%s", pathlist);
	for _, path := range pathlist {
		newPath := new(LogFilePath);
		newPath.Path = path;
		newPath.IsDeleted = false;
		LogFilePathsList = append(LogFilePathsList, *newPath);
	}
	log.Printf("LogPath to service mapping configuration file:(%s) loaded", parserConfigurationFiles.LogfileToserviceMappingConfigFilePath);
	log.Printf("Parser settings configuration file:(%s) loaded", parserConfigurationFiles.ParserSettingsConfigFilePath);
    log.Printf("log file paths successfully read and parsed:%s", LogFilePathsList);
}


func readSettingsFile(){
	if c, err := config.ReadDefault(parserConfigurationFiles.ParserSettingsConfigFilePath); err != nil {
		log.Printf("unable to read lc-settings.conf file for LOGCOURIER Bootstrapper so switching to default value");
	}else {
		for k, _ := range serviceAndVariableList.VariableList {
			if value, err := c.String("default", k); err == nil {
			serviceAndVariableList.VariableList[k] = value;
		    }
	      }
		
		for k, _ := range serviceAndVariableList.ServiceNameList {
			if value, err := c.String("service-list", k); err == nil {
				 log.Printf("service-list:%s", value);
				 temp := []string{};
			     err = parseStringToArrayOfString(value, &temp)
				 if err != nil {
		             log.Println("Failed parse string to array of string: %s\n", err)
		             }else{
		             	serviceAndVariableList.ServiceNameList[k] = temp;
		             }
		      }
	       }
		
		if properties, err := c.String("log-normalization", "extra_properties"); err == nil {
			     log.Printf("Log Normalization extra properties:%s", properties);
			     temp := []string{};
			     err = parseStringToArrayOfString(properties, &temp)
				 if err != nil {
		             log.Println("Failed parse string to array of string: %s\n", err)
		             }else{
		             	logNormalizationProperties = temp;
		             }
			
		    }	
		}	
	
	log.Printf("settings:%s", serviceAndVariableList.VariableList);
	log.Printf("log-Normalization properties:%s", logNormalizationProperties);
	log.Printf("service-list:%s", serviceAndVariableList.ServiceNameList);
}


func readPathConfigFile(){
	if c, err := config.ReadDefault(parserConfigurationFiles.LogfileToserviceMappingConfigFilePath); err != nil {
		log.Printf("unable to read "+ parserConfigurationFiles.LogfileToserviceMappingConfigFilePath + " file for LOGCOURIER Bootstrapper so switching to default value");
	}else {
		for service_group, _ := range serviceAndVariableList.ServiceNameList { 
			for _, service_name := range serviceAndVariableList.ServiceNameList[service_group] {
				servicemetadata := new(ServicesMetaData);
				servicemetadata.ServiceName = service_name;
				servicemetadata.PathPattern = "";
				servicemetadata.Paths = []string{};
				servicemetadata.AdditionProperties = make(map[string]string);
				for _, property := range logNormalizationProperties {
				    if value, err := c.String(service_name, property); err == nil {
			        servicemetadata.AdditionProperties[property] = value;
		          }
				}
				if value, err := c.String(service_name, logPathPatternTag); err == nil {
			        servicemetadata.PathPattern = value;
		          }
				serviceMetaDataList = append(serviceMetaDataList, *servicemetadata)
	       	  }
		}
		
	}
	
	log.Printf("service log normalization configuration:%s", serviceMetaDataList);
}



func parseStringToArrayOfString(stringToBeParsed string, arrayOfString *[]string) (err error){
	if (stringToBeParsed == "") {
		*arrayOfString = []string{};
		return nil;
	}
	//Removing whitespaces
	terms := strings.Split(stringToBeParsed, " ")
	for index, _ := range terms {
		terms[index] = strings.TrimSpace(terms[index]);
	}
	stringToBeParsed = strings.Join(terms, "");
	
	//Parsing on comma
	terms = strings.Split(stringToBeParsed, ",")
	for index, _ := range terms {
		terms[index] = strings.TrimSpace(terms[index]);
	}
	*arrayOfString = terms;
	if err != nil {
		log.Println("Failed unmarshalling json: %s\n", err)
		return err;
	}
	
	return nil;	
}

func resolveVariable(stringToBeParsed string)(resolvedstring string){
	
	terms := strings.Split(stringToBeParsed, "#");
	for index, term := range terms {
		if val, ok := serviceAndVariableList.VariableList[term]; ok {
			terms[index] = val;
		}
	}
	
   return strings.Join(terms, "");
}


func resolveVariableInServiceMetaDataList(){
	
	for _, service_metadata := range serviceMetaDataList{
		service_metadata.ServiceName = resolveVariable(service_metadata.ServiceName);
		for k, v := range service_metadata.AdditionProperties {
			service_metadata.AdditionProperties[k] = resolveVariable(v);
		}
	}
	
	log.Printf("Resolved Variable Service MetadataList:%s", serviceMetaDataList);
}


func classifyLogPaths(){
	
	totalPath := len(LogFilePathsList);
	for serviceIndex, service_metadata := range serviceMetaDataList {
		if (totalPath == 0) {
			log.Printf("All log file paths consumed");	
			break;
		}
		if (service_metadata.ServiceName == "Caspian_Others"){
			continue;
		}
		pattern := service_metadata.PathPattern;
		pattern = strings.Trim(pattern, "\"");
		rp1, err := regexp.Compile(pattern);
		if (err != nil) {
			log.Printf("Unable to compile path pattern:%s for service name:%s", pattern, service_metadata.ServiceName);
			continue;
		}
		
		for index, pathItem := range LogFilePathsList {
			if (pathItem.IsDeleted == false){
				if foundBool := rp1.MatchString(pathItem.Path); foundBool {
					log.Printf("Match found service-name:%s pattern:%s path:%s", service_metadata.ServiceName, pattern, pathItem.Path);
					service_metadata.Paths = append(service_metadata.Paths, pathItem.Path);
					pathItem.IsDeleted = true;
					LogFilePathsList[index] = pathItem;
					totalPath--;
			     }
			}
		}
		serviceMetaDataList[serviceIndex] = service_metadata;
	}
	
	if (totalPath != 0 ) {
		for serviceIndex, service_metadata := range serviceMetaDataList {
			if (service_metadata.ServiceName == "Caspian_Others"){
				pattern := service_metadata.PathPattern;
				pattern = strings.Trim(pattern, "\"");
				rp1, err := regexp.Compile(pattern);
				if (err != nil) {
					log.Printf("Unable to compile path pattern:%s for service name:%s", pattern, service_metadata.ServiceName);
					continue;
				}
				for index, pathItem := range LogFilePathsList {
			        if (pathItem.IsDeleted == false){
				       if foundBool := rp1.MatchString(pathItem.Path); foundBool {
					     log.Printf("Mapping Unknown path pattern service-name:%s pattern:%s path:%s", service_metadata.ServiceName, pattern, pathItem.Path);
					     service_metadata.Paths = append(service_metadata.Paths, pathItem.Path);
					     pathItem.IsDeleted = true;
					     LogFilePathsList[index] = pathItem;
					     totalPath--;
			            }		
		            }
			        if (totalPath == 0) {
			        	break;
			        }
		        }
				serviceMetaDataList[serviceIndex] = service_metadata;
				break;
              }
			}
		}
	
	log.Printf("Service MetadataList with logpath:%s", serviceMetaDataList);
}

func persistPathConfiguration(){
	var oldconfig Config;
	var err error;
	if oldconfig, err = UnSafeLoadConfiguration(parserConfigurationFiles.LogCourierConfigFilePath); err != nil {
		log.Printf("Error loading log-courier configuration file:%s", parserConfigurationFiles.LogCourierConfigFilePath);
		return;
	}
	var newconfig = new (Config);
	newconfig.Files = []FileConfig{};
	for _, service_metadata := range serviceMetaDataList {
		if (len(service_metadata.Paths) != 0 ) {
			newfileconfig := new(FileConfig);
			newfileconfig.Paths = service_metadata.Paths;
			newfileconfig.DeadTime = defaultConfig.fileDeadtime;
			newfileconfig.Fields = make(map[string]string);
			for k, v := range service_metadata.AdditionProperties{
				newfileconfig.Fields[k] = v;
			}
			newconfig.Files = append(newconfig.Files, *newfileconfig);
		}
	}
	UnSafeUpdateConfiguration(&oldconfig, newconfig);
}

