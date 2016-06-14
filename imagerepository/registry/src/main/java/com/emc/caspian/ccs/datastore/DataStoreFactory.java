/**
 * Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.datastore;

import org.apache.commons.lang.NotImplementedException;

import com.emc.caspian.ccs.datastore.mysql.MySqlStore;
import com.emc.caspian.ccs.imagerepo.api.exceptionhandling.ExceptionToStatus;

/**
 * Created by shivesh on 2/10/15.
 */
public class DataStoreFactory {
    public static DataStore getImageStore(DataStoreType dataStoreType) {
        DataStore imageFileStore;
        switch (dataStoreType) {
        case MYSQL:
            imageFileStore = MySqlStore.getMysqlStoreSingleton();
            break;

        case NOSQL:
        default:
            throw new NotImplementedException();
        }

        return imageFileStore;
    }

    public static ExceptionToStatus getExceptionMapper(DataStoreType mysql) {
        ExceptionToStatus exceptionMapper;
        switch (mysql) {
        case MYSQL:
            exceptionMapper = MySqlStore.getExceptionMapper();
            break;
        case NOSQL:

        default:
            throw new NotImplementedException();
        }
        return exceptionMapper;
    }
}
