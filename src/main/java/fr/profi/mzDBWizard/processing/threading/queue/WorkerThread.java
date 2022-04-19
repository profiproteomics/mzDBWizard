/*
 * Copyright (C) 2019
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */
package fr.profi.mzDBWizard.processing.threading.queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Thread which executes a task
 *
 * @author JPM235353
 */
public class WorkerThread extends Thread {

    private final Logger m_logger = LoggerFactory.getLogger(getClass().toString());

    private AbstractTask m_action = null;

    private static int m_threadCounter = 0;

    private WorkerPool m_workerPool;

    public WorkerThread(WorkerPool workerPool) {
        super("WorkerThread"+m_threadCounter); // for debug, set specific name
        m_threadCounter++;

        m_workerPool = workerPool;

    }

    public synchronized boolean isAvailable() {
        return (m_action == null);
    }

    public synchronized void setAction(AbstractTask action) {

        m_action = action;
        notifyAll();

    }

    @Override
    public void run() {
        try {
            while (true) {

                AbstractTask action;

                synchronized (this) {

                    while (true) {

                        if (m_action != null) {
                            action = m_action;
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                action.getTaskInfo().setRunning(true);


                // execute action
                try {
                    boolean success = action.runTask();

                    // call callback code (if there is not a consecutive task)
                    action.callback(success);



                } finally {
                    TaskManagerThread.getTaskManagerThread().actionDone(action);
                }

                synchronized(this) {
                    m_action = null;
                }

                m_workerPool.threadFinished();
            }


        } catch (Throwable t) {
            m_logger.debug("Unexpected exception in main loop of WorkerThread", t);
        }

    }

}
