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




var ResponseCode = {
    ARCHIVE_DOESNT_EXIST_ERROR : { "statusCode" : "404", "message" : "archive doesnot exist"},
    ARCHIVE_EXIST_BUT_NOT_AVAILABLE : { "statusCode" : "503", "message" : "archive exists but it is not available yet"},
    ARCHIVE_AVAILABLE : { "statusCode" : "200", "message" : "archive is available" },
    ARCHIVE_LIFECYCLE_ENABLED : { "statusCode" : "200", "message" : "Lifecycle configuration is set and enabled for the archive"},
    ARCHIVE_LIFECYCLE_DISABLED : { "statusCode" : "503", "message" : "Lifecycle configuration is not set and disabled for the archive" }
}


module.exports = ResponseCode;