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
* Created by Nikita Juneja
*/

var exec = require('child_process').exec
    ,fs = require('fs')
    ,config = require('../configuration')
    ,path = require('path')
    ,JSONmaker = require('../valid-json-maker')

var fileSplitter = function() {
};

fileSplitter.prototype.SplitFile = function(inputFilePath, outputDirectoryName, callback, callback_context) {

    var tmpFilePath = outputDirectoryName+"/"+path.basename(inputFilePath)+".tmp";
    var lineSplitCount = config.get("static_values:export_file_split:no_lines");
    var splitPrefix = outputDirectoryName+"/"+"exportedJSON_";

    //Creating output directory and changing permissions
    var child1 = exec("mkdir "+outputDirectoryName+";chmod 777 "+outputDirectoryName);
    var child2 = exec("chmod 777 "+inputFilePath+".tmp");

    // Moving tmp file to the directory
    var child3 = exec("mv "+inputFilePath+".tmp "+tmpFilePath, function (error, stdout, stderr) {
        if (error !== null) {
            console.log('exec error: ' + error);
        }
    });

// Splitting tmp file inside the directory
    var child4 = exec("split -l"+lineSplitCount+" --verbose --suffix-length=5 --numeric-suffixes "+tmpFilePath+" "+splitPrefix, function (error, stdout, stderr) {
        if (error !== null) {
            console.log('exec error: ' + error);
        }

        console.log(stdout);
        console.log("Calling ConvertSplittedFilesToValidJSONs");

        JSONmaker.ConvertSplittedFilesToValidJSONs(outputDirectoryName, callback, callback_context);

    });

    // Removing the big tmp file
    var child5 = exec("rm "+tmpFilePath, function (error, stdout, stderr) {
        if (error !== null) {
            console.log('exec error: ' + error);
        }
    });
};

module.exports = new fileSplitter();
