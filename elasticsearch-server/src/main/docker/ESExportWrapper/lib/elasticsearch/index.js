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


var  request = require('request')
     , sync_request = require('sync-request')
     , config = require('../configuration')



var elasticsearch = function () {
  var self = this;
  self.endpoint    = "http://127.0.0.1:" + config.get("elasticsearch:port")
  self.index       = ".elasticsearch_export"
  self.type       = "cache"
}


elasticsearch.prototype.initialize = function(endpoint, index_name, document_type){
    var self = this;

    if (endpoint == undefined || endpoint == ""){
        endpoint = "http://127.0.0.1:" + config.get("elasticsearch:port")
    }

    if (index_name == undefined || index_name == ""){
        index_name = ".elasticsearch_export"
    }

    if (document_type == undefined || document_type == ""){
        document_type = "cache"
    }

    self.endpoint = endpoint
    self.index = index_name
    self.type = document_type
}



elasticsearch.prototype.status = function() {
  var self = this;


  var response = sync_request('GET', resource_url);
  if (response.statusCode == 200) {
         console.log(body)
         return "running"
  }else {
        return "not_running"
  }
}


elasticsearch.prototype.isRunning = function() {
  var self = this;

  if ( self.status() == "running") {
    return true
  }else {
    return false
  }

}



elasticsearch.prototype.getDocument = function(index, type, documentId) {
   var self = this;

   if ( index == undefined) {
     index = self.index
   }

   if ( type == undefined) {
     type = self.type
   }

   var resource_url = self.endpoint + '/' + index + '/' + type + '/' + documentId + "/" + "_source"
   var response = sync_request('GET', resource_url);
   if ( response.statusCode == 200) {
           console.log("document for documentid:%s is:%s", documentId, response.body)
           return response.body
    }else {
          if (response.body.error){
              console.error('GET request failed:', response.body.error , " documentId:", documentId)
              return undefined;
           }
           console.log( " empty document for documentId:%s", documentId)
           return undefined;
    }
}


elasticsearch.prototype.putDocument = function( index, type, documentId, data) {
  var self = this;

  if ( index == undefined) {
    index = self.index
  }

  if ( type == undefined) {
    type = self.type
  }

  var es_url = self.endpoint + '/' + index + '/' + type + '/' + documentId
   // console.log("Sending put request:%s", JSON.stringify(data))

  request({
    method: 'PUT',
    uri: es_url,
    json: data
      }, function (error, response, body) {
        if (error) {
          console.error('Put request failed:', error);
          return "fail"
        }

        if (body.error){
          console.error('PUT request failed:', body.error)
          return;
        }
        console.log('PUT request successful!  Server responded with:', body);
        return "success"
  })
}


elasticsearch.prototype.putDocumentSync = function( index, type, documentId, data) {
    var self = this;

    if ( index == undefined) {
        index = self.index
    }

    if ( type == undefined) {
        type = self.type
    }

    var es_url = self.endpoint + '/' + index + '/' + type + '/' + documentId
    var response = sync_request('PUT', es_url, { json: data});
    if (response.statusCode == 200) {
        return undefined
    }else{
        var error = {}
        error.code = response.statusCode
        error.message = "could not put the document on elasticsearch for id:" + documentId
        return error
    }
}


elasticsearch.prototype.postDocument = function(index, type, data) {
  var self = this;

  if ( index == undefined) {
    index = self.index
  }

  if ( type == undefined) {
    type = self.type
  }

  var es_url = self.endpoint + '/' + index + '/' + type

  request({
    method: 'POST',
    uri: es_url,
    json: data
      }, function (error, response, body) {
        if (error) {
          console.error('upload failed:', error);
          return "fail"
        }
        if (body.error){
          console.error('POST request failed:', body.error)
          return;
        }
        console.log('Upload successful!  Server responded with:', body);
        return "success"
  })
}


elasticsearch.prototype.deleteDocument = function(index, type, documentId){
  var self = this;

  if ( index == undefined) {
    index = self.index
  }

  if ( type == undefined) {
    type = self.type
  }

  request({
    method: 'DELETE',
    uri: self.endpoint + '/' + index + '/' + type + "/" + documentId
    }, function (error, response, body) {
        if (error) {
          console.error('delete failed:', error);
          return "fail"
        }
        if (body.error){
          console.error('DELETE request failed:', body.error, " for documentId:", documentId)
          return;
        }
        console.log('Delete successful!  Server responded with:', body);
        return "success"
  })
}



elasticsearch.prototype.queryDocuments = function(index, type, queryObject, pagesize, pagenumber){
  var self = this;

  if ( index == undefined) {
    index = self.index
  }

  if ( type == undefined) {
    type = self.type
  }

 var documents = []
 var es_url = self.endpoint + '/' + index + '/' + type + '/' + '_search';
 var response = sync_request('POST', es_url, { json: queryObject});
 if (response.statusCode == 200) {
    var hits = []
     docResponse = JSON.parse(response.body)
    if (docResponse.hits != undefined && docResponse.hits.hits != undefined && docResponse.hits.hits.length != 0){
      for (var index in docResponse.hits.hits) {
        if (/*docResponse.hits.hits[index]._source.isDeleted == false &&*/ docResponse.hits.hits[index]._source.archiveUrlInfos.length != 0){
          hits.push(docResponse.hits.hits[index]._source)
        }
      }
        console.log("hits:%s", JSON.stringify(hits))
        var size = hits.length;
      if (pagesize != undefined && pagenumber != undefined && pagesize != -1 && pagenumber != -1 && size > 0 ) {
        var startIndex = pagenumber * pagesize - 1;

          if (startIndex < 0) {
              startIndex = 0;
          }

          var endIndex   = ( startIndex + parseInt(pagesize) - 1)
          if (endIndex >= size) {
              endIndex = size - 1;
          }


          console.log("hits length:%d pagenumber:%d pagesize:%d startIndex:%d endIndex:%d", size, pagenumber, pagesize, startIndex, endIndex)

        documents = hits.slice(startIndex, endIndex + 1);
      } else {
        documents = hits
      }
    }
 }

 return documents;

}

module.exports = elasticsearch
