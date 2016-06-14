package main;


type ConfigurationFiles struct {
	LogCouierConfFile string
	LcRestConfFile string
	LcLogPathToServiceConfFile string
	LcSettingsConfFile string
	LcLoggerConfFile string
	LcPropertySubstitutorConfFile string
	LogpathsAggregatorScriptFile string
	LogpathsAggregatorScriptConfigFile string
}


func (conf *ConfigurationFiles) init(LogCourierConfFile string, LcRestConfFile string, LcLogPathToServiceConfFile string, LcSettingsConfFile string, LcLoggerConfFile string, LogpathsAggregatorScriptFile string, LogpathsAggregatorScriptConfigFile string) {
	conf.LogCouierConfFile = LogCourierConfFile;
	conf.LcRestConfFile = LcRestConfFile;
	conf.LcLogPathToServiceConfFile = LcLogPathToServiceConfFile;
	conf.LcSettingsConfFile = LcSettingsConfFile;
	conf.LogpathsAggregatorScriptFile = LogpathsAggregatorScriptFile;
	conf.LogpathsAggregatorScriptConfigFile = LogpathsAggregatorScriptConfigFile;
}

func (conf *ConfigurationFiles) GetLogCourierConfFile() (confFile string) {
	confFile = conf.LogCouierConfFile
	return confFile;
}


func (conf *ConfigurationFiles) SetLogCourierConfFile(confFile string){
	conf.LogCouierConfFile = confFile;
}


func (conf *ConfigurationFiles) GetLcRestConfFile() (confFile string) {
	confFile = conf.LcRestConfFile;
	return confFile;
}

func (conf *ConfigurationFiles) SetLcRestConfFile(confFile string){
	conf.LcRestConfFile = confFile;
}


func (conf *ConfigurationFiles) GetLcLogPathToServiceConfFile() (confFile string) {
	confFile = conf.LcLogPathToServiceConfFile;
	return confFile;
}

func (conf *ConfigurationFiles) SetLcLogPathToServiceConfFile(confFile string){
	conf.LcLogPathToServiceConfFile = confFile;
}


func (conf *ConfigurationFiles) GetLcSettingsConfFile() (confFile string) {
	confFile = conf.LcSettingsConfFile;
	return confFile;
}


func (conf *ConfigurationFiles) SetLcSettingsConfFile(confFile string){
	conf.LcSettingsConfFile = confFile;
}

func (conf *ConfigurationFiles) GetLcLoggerConfFile() (confFile string) {
	confFile = conf.LcLoggerConfFile;
	return confFile;
}


func (conf *ConfigurationFiles) SetLcLoggerConfFile(confFile string){
	conf.LcLoggerConfFile = confFile;
}


func (conf *ConfigurationFiles) GetLcPropertySubstitutorConfFile() (confFile string){
  confFile = conf.LcPropertySubstitutorConfFile;
	return confFile;
}

func (conf *ConfigurationFiles) SetLcPropertySubstitutorConfFile(confFile string) {
  conf.LcPropertySubstitutorConfFile = confFile;
}


func (conf *ConfigurationFiles) GetLogpathsAggregatorScriptFile() (confFile string) {
	confFile = conf.LogpathsAggregatorScriptFile;
	return confFile;
}


func (conf *ConfigurationFiles) SetLogpathsAggregatorScriptFile(confFile string){
	conf.LogpathsAggregatorScriptFile = confFile;
}


func (conf *ConfigurationFiles) GetLogpathsAggregatorScriptConfigFile() (confFile string) {
	confFile = conf.LogpathsAggregatorScriptConfigFile;
	return confFile;
}


func (conf *ConfigurationFiles) SetLogpathsAggregatorScriptConfigFile(confFile string){
	conf.LogpathsAggregatorScriptConfigFile = confFile;
}


var ConfigurationFileList ConfigurationFiles;
