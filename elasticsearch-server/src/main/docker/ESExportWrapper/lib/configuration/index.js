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


var nconf = require('nconf');

function Config(){
  nconf.argv().env("_");
  var environment = nconf.get("NODE:ENV") || "development";
  nconf.file(environment, "config/" + environment + ".json");
  nconf.file("default", "config/default.json");
}

Config.prototype.get = function(key) {
  return nconf.get(key);
};

module.exports = new Config();
