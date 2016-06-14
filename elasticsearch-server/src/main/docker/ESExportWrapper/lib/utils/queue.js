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





var Queue = function() {
    var self =  this;
    self.itemList = []
    self.head = -1
}


Queue.prototype.enqueue = function(item) {
    var self = this

   // console.log("Enqueue is called!!")
    if (self.itemList == undefined){
        self.itemList = []
        self.head = -1
    }

    self.itemList.push(item)
    self.head += 1

}


Queue.prototype.dequeue = function() {
    var self = this


    if (self.itemList == undefined || self.size() == 0){
        return undefined
    }

    var item = self.itemList[0]

    var newList = []
    var oldList = self.itemList
    for (var index in oldList){
        if (index == 0){
            continue
        }
        newList.push(oldList[index])
    }

    self.itemList = newList
    self.head -= 1;

    return item

}

Queue.prototype.size = function() {
    var self = this


    return self.head + 1

}

Queue.prototype.isEmpty = function() {
    var self = this;

    if (self.size() == 0) {
        return true
    }

    return false;
}


Queue.prototype.clear = function(){
    var self = this
    self.head = -1
    self.itemList = []
}


Queue.prototype.peek = function(index) {
    var self = this

    if (index < 0 && index >= self.size()) {
       return undefined
    }


    return self.itemList[index]
}


Queue.prototype.serialize = function(separator){
    var self = this

    if (separator == undefined){
        separator = " "
    }

    var serialstring = ""
    var count = 0;

    while(true){
        if (self.size() == count){
            break
        }

        if (serialstring == ""){
            serialstring = self.peek(count)
        }else{
            serialstring = serialstring + separator + self.peek(count)
        }
        count += 1;
    }

    return serialstring
}


module.exports.Queue = Queue