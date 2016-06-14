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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.caspian.ccs.imagerepo.api.datamodel.Task;
import com.emc.caspian.fabric.config.Configuration;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * TaskEngine
 *
 * @author shrids
 *
 */
public final class TaskEngine {

    public static final String SECTION = "service.task";
    public static final Integer WORKER_THREADS = Configuration.make(Integer.class, SECTION + ".max_worker_threads", "10").value();

    private ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(WORKER_THREADS));

    // TODO: Clean up scheduled Single thread executor to clean up all the tasks after expiry time..

    /**
     * Initiates an orderly shutdown of the taskEngine, all previously submitted tasks are executed.
     * No new tasks are accepted.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks, halts the processing of waiting tasks, and
     * returns a list of the tasks that were awaiting execution.
     *
     * @return
     */
    public List<Runnable> shutdownNow() {
        return executor.shutdownNow();
    }

    /**
     * Returns true if this executor has been shut down.
     *
     * @return
     */
    public boolean isShutdown() {
        return executor.isShutdown();
    }

    /**
     * Returns true if all tasks have completed following shut down. Note that isTerminated is never
     * true unless either shutdown or shutdownNow was called first.
     *
     * @return
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown request, or the timeout
     * occurs, or the current thread is interrupted, whichever happens first.
     *
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executor.awaitTermination(timeout, unit);
    }

    private void submit(final CallableTask task) {
        // Submit the function to the executor.
        ListenableFuture<Task> result = executor.submit(task);

        Futures.addCallback(result, new FutureCallback<Task>() {

            @Override
            public void onSuccess(Task result) {
                _log.info("Task ID:{} execution is successfull", result.getId());
                try {
                    task.onSuccess();
                } catch (Exception e) {
                    _log.error("Exception during execution of OnSuccess() function", e);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                _log.error("Task execution is a Failure", t);
                try {
                    task.onFailure(t);
                } catch (Exception e) {
                    _log.error("Exception during execution of OnFailure() function", e);
                }
            }
        });
    }

    /**
     * Submit a task to the TaskEngine. Performs the following 1. Checks if the task is Valid. 2.
     * Invokes onSubmit() for a given task. 3. Submits the task to the executor.
     *
     * @param taskData
     * @return
     */
    public <T extends Task> String submit(final T taskData) {
        CallableTask task = TaskFactory.getTask(taskData);
        String taskID = null;
        try {
            taskID = task.onSubmit();
        } catch (Exception e) {
            _log.error("Exception occured during execution of onSubmit() function", e);
            // unrecoverable exception
            throw new RuntimeException("Error during execution of onSubmit() function");
        }
        submit(task);
        return taskID;
    }

    private static final Logger _log = LoggerFactory.getLogger(TaskEngine.class);
}
