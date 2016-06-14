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
 * Created by Nikita Juneja on 1/20/16.
 */


var fs = require('fs')
    , path = require('path')
    , exec = require('child_process').exec

var JSONmaker = function () {
}

JSONmaker.prototype.ConvertSplittedFilesToValidJSONs = function (dirName, callback, callback_context) {

    console.log("inside ConvertSplittedFilesToValidJSONs");

    fs.readdir(dirName, function (err, files) {
        if (err) {
            console.log(err);
        }

        var splittedFiles = files.map(function (file) {
            return path.join(dirName, file);
        }).filter(function (file) {
            return fs.statSync(file).isFile();
        });

        modifyAllFilesExceptFirst(splittedFiles);
    });

    var modifyAllFilesExceptFirst = function(splittedFiles) {

        var child = exec("for f in " + dirName + "/*; do if [[ ${f: -25} != *"+splittedFiles[0].toString().slice(-25)+" ]]; then sed -i -e '1i[\' $f; sed -i -e '0,/,{/ s/,{/{/' $f; echo ']' >> $f; fi; done", function (error, stdout, stderr) {
            if (error !== null) {
                console.log('exec error: ' + error);
            }

            console.log(stdout);

            // Get array of filenames in the directory
            fs.readdir(dirName, function (err, files) {
                if (err) {
                    console.log(err);
                }

                var splittedFiles = files.map(function (file) {
                    return path.join(dirName, file);
                }).filter(function (file) {
                    return fs.statSync(file).isFile();
                });

                modifyFirstFile(splittedFiles);
            });
        });
    }
    var modifyFirstFile = function (splittedFiles) {
        // For first file, append a line in the end containing ']'
        var child = exec("echo ']' >> " + splittedFiles[0], function (error, stdout, stderr) {
            if (error !== null) {
                console.log('exec error: ' + error);
            }

            console.log(stdout);
            if (splittedFiles.length > 1)
                modifyLastFile(splittedFiles);
            else
                callCallback();
        });
    }

    var modifyLastFile = function (splittedFiles) {

        // For last file, remove comma from first line and insert a line with '[' in beginning
        var child1 = exec("sed -i '$d' " + splittedFiles[splittedFiles.length - 1], function (error, stdout, stderr) {
            if (error !== null) {
                console.log('exec error: ' + error);
            }

            console.log(stdout);
            callCallback();
        });
    }

    var callCallback = function () {
        if (callback_context == undefined) {

            callback(dirName);
        } else {
            callback.call(callback_context, dirName)
        }
    }
}
module.exports = new JSONmaker();
