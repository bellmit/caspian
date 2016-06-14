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


/*
* 
*  Module to handle death of child processes if pid of log-courier-rest=1
*
*/

package main

import "os"
import "os/signal"
import "syscall"


//  Handle death of child (SIGCHLD) messages. 
//  Pushes the signal onto the notifications channel if there is a waiter.
func sigChildHandler(notifications chan os.Signal) {
	var sigs = make(chan os.Signal, 3)
	signal.Notify(sigs, syscall.SIGCHLD)
    mainLogger.Info("SIGCHLD signal generated and notified to the parent process");
	for {
		var sig = <- sigs
		select {
		case notifications <-sig:  /*  published it.  */
		default:
		}
	}

}

func reapChildren() {
	var notifications = make(chan os.Signal, 1)

    mainLogger.Info("Calling signal child handler")
	go sigChildHandler(notifications)

	for {
		var sig = <-notifications
		mainLogger.Info("Received signal:", sig)
		for {
			var wstatus syscall.WaitStatus

			pid, err := syscall.Wait4(-1, &wstatus, 0, nil)
			for syscall.EINTR == err {
				pid, err = syscall.Wait4(-1, &wstatus, 0, nil)
			}

			if syscall.ECHILD == err {
				break
			}

			mainLogger.Info("Reaper cleanup: pid=",pid , "wstatus=", wstatus)

		}
	}

}


func Reap() {
	 if 1 == os.Getpid() {
	 	  mainLogger.Info("Starting ChildReaper thread");
		  go reapChildren()
	 }
}