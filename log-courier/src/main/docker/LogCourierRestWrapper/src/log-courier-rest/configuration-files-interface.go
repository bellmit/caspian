package main;



type ConfigurationFilesInterface interface{
	init(LogCourierConfFile string, LcRestConfFile string, LcLogPathToServiceConfFile string, LcSettingsConfFile string, LcLoggerConfFile string, LogpathsAggregatorScriptFile string, LogpathsAggregatorScriptConfigFile string)
    GetLogCourierConfFile() (confFile string)
    SetLogCourierConfFile(confFile string)
    GetLcRestConfFile() (confFile string)
    SetLcRestConfFile(confFile string)
    GetLcLogPathToServiceConfFile() (confFile string)
    SetLcLogPathToServiceConfFile(confFile string)
    GetLcSettingsConfFile() (confFile string)
    SetLcSettingsConfFile(confFile string)
    GetLcLoggerConfFile() (confFile string)
    SetLcLoggerConfFile(confFile string)
		GetLcPropertySubstitutorConfFile() (confFile string)
		SetLcPropertySubstitutorConfFile(confFile string)
    GetLogpathsAggregatorScriptFile()(confFile string)
    SetLogpathsAggregatorScriptFile(confFile string)
    GetLogpathsAggregatorScriptConfigFile()(confFile string)
    SetLogpathsAggregatorScriptConfigFile(confFile string)
}
