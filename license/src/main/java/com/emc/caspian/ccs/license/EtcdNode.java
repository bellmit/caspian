/**
 *  Copyright (c) 2015 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.license;

import java.util.List;

public class EtcdNode {
    public String key;
    public long createdIndex;
    public long modifiedIndex;
    public String value;

    // For TTL keys
    public String expiration;
    public Integer ttl;

    // For listings
    public boolean dir;
    public List<EtcdNode> nodes;

}
