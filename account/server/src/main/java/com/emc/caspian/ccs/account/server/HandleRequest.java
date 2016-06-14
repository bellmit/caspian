package com.emc.caspian.ccs.account.server;

import com.emc.caspian.ccs.account.controller.AccountProtocol;

public interface HandleRequest<T extends AccountProtocol.Response> {

  T processRequest() throws Exception;
}
