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




var   config = require('../configuration')
    , elasticsearch = require('../elasticsearch');



var EntryLockStatus = {
    ENTRY_LOCK_ENABLED : "enabled",
    ENTRY_LOCK_DISABLED : "disabled"
};


var EntryLockType = {
    WRITE_LOCK : "write",
    READ_LOCK : "read"
};

var LockInfoEntry = function(){
    var self = this;

    self.lockinfoMetadata = {}
};


LockInfoEntry.prototype.SetArchiveId = function(id){
    var self = this;

    var error = undefined;

    if (self.lockinfoMetadata.LockInfo == undefined) {
        self.lockinfoMetadata.LockInfo = {}
    }

    if (id == undefined){
        if (error == undefined){
            error = {}
        }
        error.message = "id cannot be undefined";
        return error
    }

    self.lockinfoMetadata.LockInfo.archiveId = id;

    return error

};

LockInfoEntry.prototype.GetArchiveId = function(id){
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined){
        return undefined
    }

    return  self.lockinfoMetadata.LockInfo.archiveId

};


LockInfoEntry.prototype.AddServers = function(servers){
    var self = this;

    if (servers != undefined){
        for (var index in servers){
            self.AddServer(servers[index])
        }
    }

};


LockInfoEntry.prototype.AddServer = function(server){
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined){
        self.lockinfoMetadata.LockInfo = {}
    }

    if (server == undefined){
        return
    }

    if (server == ""){
        server = config.get("environment:container_host_address")
    }

    if (self.lockinfoMetadata.LockInfo.servers == undefined){
        self.lockinfoMetadata.LockInfo.servers = []
    }

    if (!self.IsServerAlreadyPresent(server)){
        self.lockinfoMetadata.LockInfo.servers.push(server)
    }

};


LockInfoEntry.prototype.GetServers = function(){
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined){
        return undefined
    }

    return self.lockinfoMetadata.LockInfo.servers
};

LockInfoEntry.prototype.IsServerAlreadyPresent = function(serverAddress){
    var self = this;

    if (self.GetServers() == undefined){
        return false
    }

    var servers = self.GetServers();
    for (var index in servers){
        if (servers[index] == serverAddress){
            return true
        }
    }

   return false

};

LockInfoEntry.prototype.IsAnyServerPresent = function(){
    var self = this;

    if (self.GetServers() == undefined){
        return false
    }

    var servers = self.GetServers();
    for (var index in servers){
       return true
    }

    return false
};


LockInfoEntry.prototype.DeleteServer = function(server){
    var self = this;

    if (server == undefined || server == "" || ! self.IsAnyServerPresent() || !self.IsServerAlreadyPresent(server) ){
        return
    }

    var oldservers = self.GetServers();
    var newservers = [];

    for (var index in oldservers){
        if (oldservers[index] == server){
            continue
        }

        newservers.push(oldservers[index])
    }

    self.lockinfoMetadata.LockInfo.servers = newservers

}


LockInfoEntry.prototype.IsLockEnabledForServer = function(serverAddress){
    var self = this;

    if (self.IsServerAlreadyPresent(serverAddress) && self.GetStatus() == EntryLockStatus.ENTRY_LOCK_ENABLED){
        return true
    }

    return false;
}

LockInfoEntry.prototype.IsLockEnabled = function(){
    var self = this

    if (self.IsAnyServerPresent() && self.GetStatus() == EntryLockStatus.ENTRY_LOCK_ENABLED){
        return true
    }

    return false;
}


LockInfoEntry.prototype.GetLockInfoType = function() {

    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined || self.lockinfoMetadata.LockInfo.locktype == ""){
        return undefined
    }

    return self.lockinfoMetadata.LockInfo.locktype

};


LockInfoEntry.prototype.SetLockInfoType = function(locktype) {
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined){
        self.lockinfoMetadata.LockInfo = {}
    }

    if (locktype == undefined || locktype == ""){
        locktype = EntryLockType.WRITE_LOCK
    }

    self.lockinfoMetadata.LockInfo.locktype = locktype
};


LockInfoEntry.prototype.GetStatus = function(){
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined || self.lockinfoMetadata.LockInfo.status == ""){
        return undefined
    }

    return self.lockinfoMetadata.LockInfo.status
};


LockInfoEntry.prototype.SetStatus = function(status){
    var self = this;

    if (self.lockinfoMetadata.LockInfo == undefined){
        self.lockinfoMetadata.LockInfo = {}
    }

    if (status == undefined || status == ""){
        status = EntryLockStatus.ENTRY_LOCK_DISABLED
    }

    self.lockinfoMetadata.LockInfo.status = status
};


LockInfoEntry.prototype.GetData = function(){
    var self = this;

    return self.lockinfoMetadata

};



var BuildLockInfoEntryFromESDoc = function(document) {
    var data = undefined;
    if (document != undefined && document != ""){
        data = JSON.parse(document);
    }

    //console("ES doc data for build cache Entry:%s", document..)

    if (data != undefined && data.LockInfo != undefined){

        var lockinfoEntryInstance = new LockInfoEntry();

        data = data.LockInfo;

        if (data.archiveId != undefined ){
          lockinfoEntryInstance.SetArchiveId(data.archiveId)
        }

        if (data.servers != undefined){
          lockinfoEntryInstance.AddServers(data.servers)
        }

        if (data.locktype != undefined){
           lockinfoEntryInstance.SetLockInfoType(data.locktype)
        }

        if (data.status != undefined){
           lockinfoEntryInstance.SetStatus(data.status)
        }

        return lockinfoEntryInstance
    }

    return undefined
};


var BuildLockInfoInstance = function(archiveId, serverAddress, type, status){
    if (archiveId == undefined || archiveId == ""){
        return undefined
    }

    var LockInfoInstance = new LockInfoEntry()
    LockInfoInstance.SetArchiveId(archiveId)
    LockInfoInstance.AddServer(serverAddress)
    LockInfoInstance.SetLockInfoType(type)
    LockInfoInstance.SetStatus(status)

    return LockInfoInstance
}

var LockInfoStore = function() {
    var self = this;

    self.elasticsearch = new elasticsearch();
    self.elasticsearch.initialize("","","lockinfo");

}


LockInfoStore.prototype.AddEntry = function(id, lockinfoEntry){
    var self = this;

    self.elasticsearch.putDocument(undefined, undefined, id, lockinfoEntry.GetData());

};

LockInfoStore.prototype.GetEntry = function(id){
    var self = this;

    var doc = self.elasticsearch.getDocument(undefined, undefined, id);
    var lockinfoEntryInstance = BuildLockInfoEntryFromESDoc(doc);


    return lockinfoEntryInstance;
};

LockInfoStore.prototype.UpdateEntry = function(id, updatedlockinfoEntry){
    var self = this;

    self.elasticsearch.putDocumentSync(undefined, undefined, id, updatedlockinfoEntry.GetData());
};


LockInfoStore.prototype.RemoveEntry = function(id){
    var self = this;

    var lockinfoEntryInstance = self.GetEntry(id);

    if (lockinfoEntryInstance != undefined && lockinfoEntryInstance.GetArchiveId() == id) {
        self.elasticsearch.deleteDocument(undefined, undefined, lockinfoEntryInstance.GetArchiveId());
    }
};

module.exports.LockInfoEntry = LockInfoEntry;
module.exports.LockInfoStore = new LockInfoStore();
module.exports.EntryLockType = EntryLockType;
module.exports.EntryLockStatus = EntryLockStatus;
module.exports.BuildLockInfoInstance = BuildLockInfoInstance