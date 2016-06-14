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
* Web server operations
*
*
**/

package main


import (
	"net/http"
	"github.com/revel/config"
	"github.com/ant0ine/go-json-rest/rest"
)


type RestServer struct {
	LogCourierLoggerPtr      LoggerInterface;
	ConfigurationFileListPtr ConfigurationFilesInterface
	Api           *rest.Api
	Port          string
	IpAddress     string
}

func (serv *RestServer) init(ConfigurationFileListPtr ConfigurationFilesInterface, Api *rest.Api) {
	serv.LogCourierLoggerPtr = new(LcLogger);
	serv.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "server-module");
	serv.ConfigurationFileListPtr = ConfigurationFileListPtr;
	serv.Api = Api;
	serv.Port = "6555"
	serv.IpAddress = "0.0.0.0"
	serv.readlcrestServerConfig()
}


//readlcrestServiceConfig reads the lcrest-conf.conf file to initialized our log-courier-rest service for running 
// our HTTP REST service
func (serv *RestServer) readlcrestServerConfig(){
	if c, err := config.ReadDefault(serv.ConfigurationFileListPtr.GetLcRestConfFile()); err != nil {
		serv.LogCourierLoggerPtr.Error("unable to read lcrest-conf.conf file- switching to defualt Address: %s Port: %s ", 
			serv.IpAddress, serv.Port);
	}else {
		if temp, err := c.String("REST", "host"); err == nil && temp != "" && temp != "REST_HOST_ADDRESS"{
			serv.IpAddress = temp;
		}
		
		if  temp, err := c.String("REST", "port"); err == nil && temp != "" && temp != "REST_PORT"{
			serv.Port = temp;
		}		
    }
}

//Start function runs the HTTP server on the port specified by Port variable(default port is 6555)
// It starts a REST service on /log-courier/rest/
func (serv *RestServer) StartRestServer() {
	serv.LogCourierLoggerPtr.Info("Starting Rest Service at ", serv.IpAddress, ":", serv.Port);
	http.ListenAndServe(serv.IpAddress + ":" + serv.Port, serv.Api.MakeHandler());
    serv.LogCourierLoggerPtr.Critical("Unexpected shutdown of LogCourier Rest");
}

var RestServerInstance RestServer;
