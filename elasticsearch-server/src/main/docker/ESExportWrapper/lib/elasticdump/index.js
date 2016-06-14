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

var elasticdump = require('elasticdump').elasticdump
   ,fs          = require('fs')
   ,exec        = require('child_process').exec
   ,cacheModule = require('../cache')
   ,splitter    = require('../file-splitter')


var defaults = {
  limit:           100,
  offset:          0,
  debug:           false,
  type:            'data',
  delete:          false,
  all:             false,
  bulk:            false,
  maxSockets:      null,
  input:           null,
  output:          null,
  inputTransport:  null,
  outputTransport: null,
  searchBody:      null,
  sourceOnly:      false,
  jsonLines:       false,
  format:          '',
  'ignore-errors': false,
  scrollTime:      '10m',
  'bulk-use-output-index-name': false,
  timeout:         null,
  skip:            null,
  toLog:           null,
};


var cache = cacheModule.IdStore


var elasticdumper=function(input, output, options) {
  var self  = this;

  self.input   = input;
  self.output  = output;
  self.options = options;

  for ( var i in defaults) {
    if ( this.options[i] == undefined ) {
      this.options[i] = defaults[i];
    }
  }

  if ( self.input == undefined ) {
     self.input = null;
  }

  if ( self.output == undefined ) {
     self.output = null;
  }

  self.options.output = self.output + '.tmp';

  var dumper = new elasticdump(self.input, self.options.output, options);

  dumper.on('log',   function(message){ log('log',   message); });
  dumper.on('debug', function(message){ log('debug', message); });
  dumper.on('error', function(error){   log('log', 'Error Emitted => ' + ( error.message || JSON.stringify(error)) ); });

  dumper.dump(function(error, total_writes){
   // var destinationFile = self.output;
    var archiveUpdator = new ArchiveMetadataUpdator(self.options.archiveId)
    var compressor = new Compressor()
    compressor.SetSuccessCallback(archiveUpdator.SuccessfulArchiveMetadataUpdate, archiveUpdator)
    compressor.SetFailureCallback(archiveUpdator.FailedArchiveMetadataUpdate, archiveUpdator)
    if(error){
       console.log("error dumping elasticsearch indexes")
       fs.writeFile(self.output + ".tmp", "Error dumping requested index!!!", function(err) {
            if(err) {
                //ZipFile(self.output + ".tmp", self.output + ".tar.bz2"); //done
                archiveUpdator.FailedArchiveMetadataUpdate();
                return console.error(err);//done
            }
            console.log("The file was saved!");
            compressor.SetCompressionSourceType("file")
            compressor.Compress(self.output)
            //ZipFile(self.output + ".tmp", self.output + ".tar.bz2");
            //archiveUpdator.SuccessfulArchiveMetadataUpdate()
       });

    }else{
        compressor.SetCompressionSourceType("directory");
        splitter.SplitFile(self.output, self.output, compressor.Compress, compressor);
       // UpdateArchiveMetadata(self.options.archiveId)
    }

  });
}


//var UpdateArchiveMetadata = function(id, path, createdAt) {
//  if (createdAt == undefined) {
//    createdAt = new Date().toISOString();
//  }
//
//  var cacheEntry = cache.GetEntry(id);
//  cacheEntry.SetCreatedAt(createdAt)
//  cacheEntry.UpdateArchiveUrlInfo(cacheModule.BuildArchiveUrlInfo(id, "", cacheModule.ExportJobStatus.EXPORT_JOB_COMPLETE))
//  cache.UpdateEntry(id, cacheEntry);
//}


var Compressor = function() {
    var self = this


}

//type can director or file
Compressor.prototype.SetCompressionSourceType = function(type) {
    var self = this

    if (type == undefined){
        type = "directory"
    }

    self.source_type = type
}


Compressor.prototype.GetCompressionSourceType = function(){
    var self = this

    if (self.source_type == undefined || self.source_type == ""){
        self.source_type = "directory"
    }

    return self.source_type

}

Compressor.prototype.SetSuccessCallback = function(callback, context){
    var self = this;

    if (self.success_callback == undefined){
        self.success_callback = {}
    }
    self.success_callback.callback = callback

    if (context != undefined){
        self.success_callback.context = context
    }
}

Compressor.prototype.SetFailureCallback = function(callback, context){
    var self = this

    if (self.failure_callback == undefined){
        self.failure_callback = {}
    }
    self.failure_callback.callback = callback

    if (context != undefined){
        self.failure_callback.context = context
    }
}

Compressor.prototype.Compress = function(dirName) {
    var self = this
    console.log("compression job for document dump started now!!");

    console.log("sourcetype:%s", self.source_type)
    if (self.GetCompressionSourceType() == "directory"){
        ZipDir(dirName, self.success_callback, self.failure_callback);
    }else {
        ZipFile(dirName + ".tmp", dirName + ".tar.bz2", self.success_callback, self.failure_callback)
    }

}


var ArchiveMetadataUpdator = function(archiveId) {
    var self = this

    self.archiveId = archiveId
}


ArchiveMetadataUpdator.prototype.GetArchiveId = function(){
    var self = this

    return self.archiveId
}

ArchiveMetadataUpdator.prototype.SuccessfulArchiveMetadataUpdate = function(createdAt){
    var self = this
    if (createdAt == undefined) {
        createdAt = new Date().toISOString();
    }

    var cacheEntry = cache.GetEntry(self.GetArchiveId());
    if (cacheEntry == undefined){
        return
    }
    cacheEntry.SetCreatedAt(createdAt)
    cacheEntry.UpdateArchiveUrlInfo(cacheModule.BuildArchiveUrlInfo(self.GetArchiveId(), "", cacheModule.ExportJobStatus.EXPORT_JOB_COMPLETE))
    cache.UpdateEntry(self.GetArchiveId(), cacheEntry);
}

ArchiveMetadataUpdator.prototype.FailedArchiveMetadataUpdate = function(createdAt) {
    var self = this
    if (createdAt == undefined) {
        createdAt = new Date().toISOString();
    }

    var cacheEntry = cache.GetEntry(self.GetArchiveId());
    if (cacheEntry == undefined){
        return
    }
    //cacheEntry.SetCreatedAt(createdAt)
    cacheEntry.DeleteArchiveUrlInfo(cacheModule.BuildArchiveUrlInfo(self.GetArchiveId(), "", cacheModule.ExportJobStatus.EXPORT_JOB_DOESNOT_EXIST))
    cache.UpdateEntry(self.GetArchiveId(), cacheEntry);
}



var ZipFile = function(inputfile, outputfile, success_callback, failure_callback) {
  var child = exec('bzip2 ' + inputfile + '; mv ' + inputfile + '.bz2 ' + outputfile, function (error, stdout, stderr) {
    if (error !== null) {
      console.log('exec error: ' + error);
      if (failure_callback != undefined){
          var callback = failure_callback.callback
          if (failure_callback.context != undefined){
              callback.call(failure_callback.context)
          }else{
              callback.call()
          }

      }
      return
    }

  if (success_callback != undefined ){
      var callback = success_callback.callback
      if (success_callback.context != undefined){
          callback.call(success_callback.context)
      }else{
          callback.call()
      }
  }

  });
}

var ZipDir = function(inputfile, success_callback, failure_callback) {
  var child = exec('tar cvjf '+inputfile+'.tmp.tar.bz2 '+inputfile+';', function (error, stdout, stderr) {
    if (error !== null) {
      console.log('exec error: ' + error);
        if (failure_callback != undefined){
            var callback = failure_callback.callback
            if (failure_callback.context != undefined){
                callback.call(failure_callback.context)
            }else{
                callback.call()
            }

        }
      return
    }
    console.log(stdout);
    var child2 = exec('mv '+inputfile+'.tmp.tar.bz2 '+inputfile+'.tar.bz2', function (error, stdout, stderr) {
      if (error !== null) {
        console.log('exec error: ' + error);
          if (failure_callback != undefined){
              var callback = failure_callback.callback
              if (failure_callback.context != undefined){
                  callback.call(failure_callback.context)
              }else{
                  callback.call()
              }

          }
        return
      }
      console.log(stdout);
      var child3 = exec('rm -rf '+inputfile+";", function (error, stdout, stderr) {
        if (error !== null) {
          console.log('exec error: ' + error);
            if (failure_callback != undefined){
                var callback = failure_callback.callback
                if (failure_callback.context != undefined){
                    callback.call(failure_callback.context)
                }else{
                    callback.call()
                }

            }
          return
        }
      console.log(stdout);
      if (success_callback != undefined ){
          var callback = success_callback.callback
          if (success_callback.context != undefined){
              callback.call(success_callback.context)
          }else{
              callback.call()
          }
      }
      });
    });
  });
}

var log = function(type, message){
  if(type === 'debug'){
    if(options.debug === true){
      message = "[debug] | " + message;
    }else{
      return false;
    }
  }else{
    message = (new Date().toUTCString()) + " | " + message;
  }
  console.log(message);
};


module.exports = elasticdumper
