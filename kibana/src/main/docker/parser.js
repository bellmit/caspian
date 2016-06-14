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

module.exports = function(cookie) {
          var i = cookie.indexOf('X-Auth-Token');
          i = cookie.indexOf('=',i);
          i = i + 1 ;
          if(cookie.indexOf('"',i)==i)
            i=i+1;
          var j = cookie.indexOf(';',i)
          var k = cookie.indexOf('"',i)
          var end = i;
          if(j == -1)
                end = k
          else if(k == -1)
                end = j
          else if(k > j)
                end = j
          else
                end = k
          if (end == -1)
                end = cookie.length
          var authToken = cookie.substring(i, end);
          return authToken;
}

