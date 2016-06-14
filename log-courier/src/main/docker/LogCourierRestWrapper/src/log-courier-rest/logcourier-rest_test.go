package main


//TODO

//import (
//     //"net/http"
//     //"net/http/httptest"
//     "testing"
//	 "runtime"
//	 "path/filepath"
//	 //"io/ioutil"
//	 "fmt"
//	 //"time"
//)
//
////import "github.com/parnurzeal/gorequest"
//
//
//
//var testRestApi *LogCourierRestApi
//var MockServicesInstancePtr *Services;
//var MockContainerListInMemoryPtr   *ContainerList;
//var MockLogFileScannerInstancePtr  *LogFileScanner;
//var MockConfigCachePtr  *InMemoryConfig;
//var MockServerPtr       *RestServer;
//var MockConfigurationFilesList ConfigurationFiles   
//var MockLogCourierServiceControllerInstancePtr *LogCourierServiceController;
//var MockLogCourierLogger       LcLogger;
//var newconfig *Config
//
//func Setup() {
//	_, filename, _, _ := runtime.Caller(0)
//    dir, _ := filepath.Abs(filepath.Dir(filename))
//   
//	testRestApi = new(LogCourierRestApi);
//	MockServicesInstancePtr = new(Services)
//	MockContainerListInMemoryPtr = new(ContainerList)
//	MockLogFileScannerInstancePtr = new(LogFileScanner);
//	MockConfigCachePtr = new(InMemoryConfig);
//	MockServerPtr      = new(RestServer)
//	MockLogCourierServiceControllerInstancePtr = new(LogCourierServiceController);
//	
//	newconfig = new(Config);
//	var fileconfig FileConfig = FileConfig{};
//	fileconfig.Paths = []string{"/var/log/mockpath2.log"};
//	fileconfig.DeadTime = "24h"
//	fileconfig.Fields = map[string]string {"device" : "mock-device", "devtype" : "mock-devtype", "part" : "mock-part", "parttype" : "mock-parttype" , "category" : "UNKNOWN"};
//	newconfig.Files = []FileConfig{fileconfig};
//	
//	MockConfigurationFilesList.LogCouierConfFile = dir + "/" + "test/conf/log-courier-test.conf"
//	MockConfigurationFilesList.LcRestConfFile =  dir + "/" + "test/conf/lcrest-conf-test.conf"
//	MockConfigurationFilesList.LcLogPathToServiceConfFile =  dir + "/" + "../conf/lc-pathconfig.conf";
//	MockConfigurationFilesList.LcSettingsConfFile = dir + "/" + "../conf/lc-settings.conf"
//	MockConfigurationFilesList.LogpathsAggregatorScriptFile =  dir + "/" + "../../scripts/logpathaggregator-run.sh";
//	MockConfigurationFilesList.LogpathsAggregatorScriptConfigFile = dir + "/" + "test/conf/logpath-pattern-test.conf"
//	MockConfigurationFilesList.LcLoggerConfFile = dir + "/" + "test/conf/lc-logger-test.conf";
//	MockLogCourierLogger.init(&MockConfigurationFilesList)
//	
//	MockLogCourierServiceControllerInstancePtr.init(&MockConfigurationFilesList, &MockLogCourierLogger);
//	MockConfigCachePtr.init(MockLogCourierServiceControllerInstancePtr, &MockLogCourierLogger);
//	MockServicesInstancePtr.init(&MockConfigurationFilesList, &MockLogCourierLogger);
//	
//	var oldconfig Config;
//	var err error;
//	if err, oldconfig = MockConfigCachePtr.getConfiguration(); err == nil {
//    	oldconfig = Config{};
//    }
//	
//	MockContainerListInMemoryPtr.init(&oldconfig, MockServicesInstancePtr, &MockLogCourierLogger);
//
//	testRestApi.init(nil, MockServicesInstancePtr, MockContainerListInMemoryPtr, MockLogFileScannerInstancePtr, MockConfigCachePtr, &MockLogCourierLogger);
//	testRestApi.RegisterApplication();
//	
//	MockServerPtr.init(&MockConfigurationFilesList, testRestApi.Api, &MockLogCourierLogger);
//	go MockServerPtr.StartRestServer();
//}
//
//
//func TestGetFullContainerConfiguration(t *testing.T) {  
//    Setup();
////    uri := "/api/log-paths"
////    
////    
////    request := gorequest.New()
////    request.Get("http://127.0.0.1:6555" + uri).End()
//    }