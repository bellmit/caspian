/**
 *  Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 *
 * This software contains the intellectual property of EMC Corporation
 * or is licensed to EMC Corporation from third parties.  Use of this
 * software and the intellectual property contained therein is expressly
 * limited to the terms and conditions of the License Agreement under which
 * it is provided by or on behalf of EMC.
 */
package com.emc.caspian.ccs.taskEngine;

import java.util.concurrent.Callable;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.fabric.config.Configuration;

/**
 * Interface to be implemented by all supported tasks.
 *
 * @author shrids
 *
 */
public interface CallableTask extends Callable<Task> {

    /**
     * Function to be executed before submitting the task to the task engine.
     *
     * @throws Exception
     * @return return the task ID which can be used to track the task.
     */
    public String onSubmit() throws Exception;

    /**
     * This function is invoked by the Task Engine on a successful execution of the task
     *
     * @throws Exception
     */
    public void onSuccess() throws Exception;

    /**
     * This function is invoked by the task engine in case of a failure of the task. All the error
     *
     * @param t
     * @throws Exception
     */
    public void onFailure(Throwable t) throws Exception;

    /*
     * Configuration used by all tasks.
     */
    static final String SECTION = "service.task";
    static final Long TASK_TTL_HOURS = Configuration.make(Long.class, SECTION + ".ttl_hours", "1").value();
    static final long TASK_TTL_MS = TASK_TTL_HOURS * 60 * 60 * 1000;

}
