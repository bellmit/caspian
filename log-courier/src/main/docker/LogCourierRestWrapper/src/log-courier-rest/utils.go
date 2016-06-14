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
* LogCourier utility module
*
**/

package main

import (
	"os/exec"
	"strings"
	"strconv"
	)



func CmdExecNoWait(cmd string)(err error){
	parts := strings.Fields(cmd)
    err = exec.Command(parts[0], parts[1:]...).Start();
    if err != nil {
        mainLogger.Error("Error executing command:", cmd," reason:", err.Error())
    }
    return err
}

func CmdExec(cmd string)(output string, err error){
	parts := strings.Fields(cmd)
	out := []byte{};
    out, err = exec.Command(parts[0], parts[1:]...).Output();
    if err != nil {
        mainLogger.Error("Error executing command:", cmd," reason:", err.Error())
        return "",err
    }
    output = string(out)
    return output, nil
}




func GetAddedAndRemovedSetFromSet(existingSet []string, newInputSet []string) ( addedSet [] string, removedSet []string, err error) {
	err = nil;
	addedSet = []string{}
	removedSet = []string{}
	numOfItems := len(existingSet);
    alreadyPresent := false;
    if ( numOfItems != 0 ) {
  	for _, newItem := range newInputSet {
  		for _, existingItem := range existingSet{
  			if (newItem == existingItem) {
  				alreadyPresent = true;
  				break;
  			}
  		}
  		if (alreadyPresent == false) {
  			addedSet = append(addedSet, newItem);
  		}else{
  			alreadyPresent = false;
  		}
  	}

  	//collecting removed files
  	for _, existingItem := range existingSet {
  		for _, newItem := range newInputSet {
  			if (existingItem == newItem) {
  				alreadyPresent = true;
  				break;
  			}
  		}
  		if (alreadyPresent == false) {
  			removedSet = append(removedSet, existingItem);
  		}else{
  			alreadyPresent = false;
  		}
  	}
  }else{
  	addedSet = newInputSet;
  }

  return addedSet, removedSet, err;
}


func ArrayToString(array []string, joinString string) (concatenatedString string){
	temp := "";
	for index, item := range array {
		if (index == 0 ) {
			temp = item;
		}else{
			temp = temp + joinString + item;
		}
	}
	concatenatedString = temp;
	return concatenatedString;
}


func StringToArray( input string, delimiter string) (output [] string) {
	input = strings.TrimSpace(input);
	terms := strings.Split(input, delimiter);
	for index, term := range terms {
		terms[index] = strings.TrimSpace(term);
	}
	output = terms;
	return output
}


func ConvertStringToInt(input string) (output int, err error) {
	output, err = strconv.Atoi(input);
	return output, err;
}

func ParseStringToArrayOfString(stringToBeParsed string, arrayOfString *[]string) (err error){
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

	//Trim white space and remove empty path
	for index:=0; index < len(terms) ; index++ {
		temp := strings.TrimSpace(terms[index]);
		if (temp == "") {
			terms = append(terms[:index], terms[index+1:]...);
			index--;
		}
	}


	*arrayOfString = terms;
	if err != nil {
		mainLogger.Error("Failed unmarshalling json: ", err)
		return err;
	}


	return nil;
}


func ConvertTimeIntervalToSeconds(interval int, interval_unit string) (seconds int){
  seconds = 0;
	switch interval_unit {
		case "minutes","minute","min":
				seconds = interval * 60;
				break
		case "hours","hour","hrs","hr":
				seconds = interval * 60 * 60;
				break
		case "days","day":
				seconds = interval * 60 * 60 * 24;
				break
		case "weeks","week":
				seconds = interval * 60 * 60 * 24 * 7;
				break
		case "seconds","second","sec":
				seconds = interval;
     }

		return seconds;
}
