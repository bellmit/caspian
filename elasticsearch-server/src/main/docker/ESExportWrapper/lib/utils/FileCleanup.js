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


var fs  = require('fs')

var FileCleanup = function() {


}


FileCleanup.prototype.cleanSynchronous = function(filepath){
    var self = this;
    try {
        if (fs.statSync(filepath)){
            fs.unlinkSync(filepath)
        }
        return undefined
    }catch (e){
        if (e.code =='ENOENT'){
            console.log("Archive filepath:%s doesnt exists and this is found while cleaning up this archive from local filesystem", filepath)
            return undefined
        }
        return e
    }
}


module.exports = new FileCleanup()