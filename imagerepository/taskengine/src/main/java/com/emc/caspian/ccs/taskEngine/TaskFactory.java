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

import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.ccs.imagerepo.api.datamodel.Task.Type;
import com.emc.caspian.ccs.taskEngine.tasks.ImportTask;

/**
 * TaskFactory to fetch the right implementation for a given task type
 *
 * @author shrids
 *
 */
public final class TaskFactory {

    private TaskFactory() {
        // prevent instantiation;
    }

    public static CallableTask getTask(Task task) {
        Type type = Type.fromValue(task.getType());
        switch (type) {
        case IMPORT:
            return new ImportTask(task);

            // new tasks can be added for future
        default:
            break;
        }

        throw new IllegalArgumentException();
    }
}
