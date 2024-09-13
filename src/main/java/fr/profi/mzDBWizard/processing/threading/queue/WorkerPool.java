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

/**
 *
 * Pool of threads which execute a task
 *
 * @author JPM235353
 */
public class WorkerPool {

    public static final int CONVERTER_THREAD = 0;
    public static final int UPLOAD_THREAD = 1;
    public static final int DELETE_THREAD = 2;
    public static final int GENERATE_THREAD = 3;
    private static final int NB_TYPES_THREAD = 4;

    WorkerThread[] m_threads;

    private static WorkerPool m_workerPool = null;


    public synchronized static WorkerPool getWorkerPool() {
        if (m_workerPool == null) {
            m_workerPool = new WorkerPool();
        }
        return m_workerPool;
    }

    private WorkerPool() {
        m_threads = new WorkerThread[NB_TYPES_THREAD];
    }

    public Object getMutex() {
        return this;
    }

    public synchronized void threadFinished() {
        notifyAll();
    }


    public WorkerThread getWorkerThread(int type) {

        WorkerThread thread = m_threads[type];

        if (thread == null) {
            thread = new WorkerThread(this);
            m_threads[type] = thread;
            thread.start();
            return thread;
        }

        if (thread.isAvailable()) {
            return thread;
        }

        return null;
    }



}
