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



var   LockInfoModule = require('./LockInfo')
    , config = require('../configuration/index')




var DistributedLock = function () {
    var self = this;

    this.initialize()

}


DistributedLock.prototype.initialize = function(){

}


DistributedLock.prototype.acquire = function(id){
    var self = this
    var lockinfoEntryInstance
    var localendpoint = config.get("environment:container_host_address")


    console.log("Trying to acquire log for archiveId:%s", id)

    lockinfoEntryInstance = LockInfoModule.LockInfoStore.GetEntry(id)

    if (lockinfoEntryInstance == undefined || lockinfoEntryInstance.IsLockEnabledForServer(localendpoint)){
               lockinfoEntryInstance = LockInfoModule.BuildLockInfoInstance(id, localendpoint, LockInfoModule.EntryLockType.WRITE_LOCK, LockInfoModule.EntryLockStatus.ENTRY_LOCK_ENABLED)
                LockInfoModule.LockInfoStore.UpdateEntry(lockinfoEntryInstance.GetArchiveId(), lockinfoEntryInstance)
                while(true){
                    var updatedlockinfoEntryInstance = LockInfoModule.LockInfoStore.GetEntry(lockinfoEntryInstance.GetArchiveId())
                    if (updatedlockinfoEntryInstance !=  undefined && updatedlockinfoEntryInstance.IsLockEnabledForServer(localendpoint)){
                        return
                    }else{
                        if ( updatedlockinfoEntryInstance == undefined || !updatedlockinfoEntryInstance.IsLockEnabled()) {
                            LockInfoModule.LockInfoStore.UpdateEntry(lockinfoEntryInstance.GetArchiveId(), lockinfoEntryInstance)
                            continue
                        }else{
                            self.wait(lockinfoEntryInstance.GetArchiveId())
                        }
                    }
                }

    }else{
        self.wait(id)
    }
}


DistributedLock.prototype.wait = function(id) {
    var lockinfoEntryInstance
    while(true){

        lockinfoEntryInstance = LockInfoModule.LockInfoStore.GetEntry(id)
        if (lockinfoEntryInstance == undefined || !lockinfoEntryInstance.IsLockEnabled()){
            break;
        }

    }


}


DistributedLock.prototype.release = function(id){

    console.log("Releasing lock for archive Id:%s", id)
    var localendpoint = config.get("environment:container_host_address")
    var lockinfoEntryInstance
    lockinfoEntryInstance = LockInfoModule.LockInfoStore.GetEntry(id)
    if (lockinfoEntryInstance != undefined ){
          if (lockinfoEntryInstance.GetLockInfoType() == LockInfoModule.EntryLockType.WRITE_LOCK){
              LockInfoModule.LockInfoStore.RemoveEntry(id)
          }else if (lockinfoEntryInstance.GetLockInfoType() == LockInfoModule.EntryLockType.READ_LOCK){
                lockinfoEntryInstance.DeleteServer(localendpoint)
                if (!lockinfoEntryInstance.IsAnyServerPresent){
                    LockInfoModule.LockInfoStore.RemoveEntry(id)
                }else{
                    LockInfoModule.LockInfoStore.UpdateEntry(id, lockinfoEntryInstance);
                }
          }
    }

}



module.exports = new DistributedLock()