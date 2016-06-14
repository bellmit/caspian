package com.emc.caspian.ccs.imagerepo;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Protocol;

public interface HandleRequestDelegate<T extends Protocol.Response>
{
    T process() throws Exception;
}
