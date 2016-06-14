package com.emc.caspian.ccs.account.server;

import com.emc.caspian.ccs.account.controller.AccountProtocol;

public interface TransformResponse<T extends AccountProtocol.Response> {

  Object transform(T response) throws Exception;

}
