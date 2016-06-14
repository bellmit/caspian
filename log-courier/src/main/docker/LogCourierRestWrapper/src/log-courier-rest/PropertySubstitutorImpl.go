

package main;

import (
  "strings"
  "github.com/revel/config"
  "regexp"
  )

type SubstitutionRule struct {
     Name                     string
     Type                     string
     Properties               []string
     Source                   string
     Target                   string
     ContainerNamePattern     string
     SkipContainerNamePattern string
}

type PropertySubstitutorImpl struct{
     LogCourierLoggerPtr        LoggerInterface;
     ConfigurationFileListPtr   ConfigurationFilesInterface;
     Rules                      []SubstitutionRule
}


func (psubs *PropertySubstitutorImpl) init(ConfigurationFileListPtr ConfigurationFilesInterface) {
      psubs.LogCourierLoggerPtr = new(LcLogger);
      psubs.LogCourierLoggerPtr.init(ConfigurationFileListPtr, "property-substitutor-module");
      psubs.ConfigurationFileListPtr = ConfigurationFileListPtr;
      psubs.Rules = [] SubstitutionRule{}
      psubs.loadConfiguration(ConfigurationFileListPtr.GetLcPropertySubstitutorConfFile())
}

func (psubs *PropertySubstitutorImpl) loadConfiguration(configFile string){
      if c, err := config.ReadDefault(configFile); err != nil {
        psubs.LogCourierLoggerPtr.Error("unable to read lc-settings.conf file for LOGCOURIER Bootstrapper so switching to default value");
      }else {
        for _, substitutionRuleName := range c.Sections() {
            if (substitutionRuleName == "DEFAULT") {
              continue;
            }

            properties, err := c.SectionOptions(substitutionRuleName);
            if (err != nil){
              continue;
            }

            subsObject := SubstitutionRule{};
            subsObject.Name = substitutionRuleName;
            for _,property := range properties {
               property = strings.TrimSpace(property)
               if value, err := c.String(subsObject.Name, property); err == nil {
                       value = strings.Trim(value, "\"")
                       switch property {
                         case "type": subsObject.Type = value
                                      break;
                         case "source": subsObject.Source = value
                                      break;

                         case "target": subsObject.Target = value
                                      break;

                         case "properties": properties := StringToArray(value, ",")
                                            subsObject.Properties = properties
                                            break;

                         case "container_name_pattern": subsObject.ContainerNamePattern = value
                                      break;

                          case "skip_logpath_pattern": subsObject.SkipContainerNamePattern = value
                                      break;

                       }
   		         }
            }
              psubs.Rules = append(psubs.Rules, subsObject)
        }
      }
}


func (psubs *PropertySubstitutorImpl) RunSubstitutor(input map[string]string, containerName string)(output map[string]string) {
          output = input;

          for _, Rule := range psubs.Rules {
            containerNamePatternObject, err := regexp.Compile(Rule.ContainerNamePattern);
            if (err != nil) {
              psubs.LogCourierLoggerPtr.Error("Unable to compile path pattern:", Rule.ContainerNamePattern," for substitution rule name:", Rule.Name);
              continue;
            }

            skipContainerNamePatternObject, err := regexp.Compile(Rule.SkipContainerNamePattern);
            if (err != nil) {
              psubs.LogCourierLoggerPtr.Error("Unable to compile skiplogpath pattern:", Rule.SkipContainerNamePattern," for substitution rule name:", Rule.Name);
              continue;
            }

            isMatched := false;
            if foundBool := containerNamePatternObject.MatchString(containerName); foundBool {
              if (Rule.SkipContainerNamePattern != "") {
                if isSkipPattern := skipContainerNamePatternObject.MatchString(containerName); !isSkipPattern {
                 isMatched = true;
                 }
              }else{
                isMatched = true;
              }
           }

          if (isMatched) {
            sourceObject, err := regexp.Compile(Rule.Source);
            if (err != nil) {
              psubs.LogCourierLoggerPtr.Error("Unable to compile substitution express :", Rule.Source)
              break;
            }
            for _, property := range Rule.Properties {
              if property_value, ok := output[property]; ok {
                 output[property] = sourceObject.ReplaceAllString(property_value, Rule.Target);
              }
            }
            
          }
        }
          return output;
}


func NewPropertySubstitutorInstance(ConfigurationFileListPtr ConfigurationFilesInterface) (substitutor *PropertySubstitutorImpl){
     substitutor = &PropertySubstitutorImpl{};
     substitutor.init(ConfigurationFileListPtr);
     return substitutor;
}
