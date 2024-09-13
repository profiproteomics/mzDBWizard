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

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 *
 * A threaded task must inherits form AbstractTask
 *
 * @author JPM235353
 *
 */
public abstract class AbstractTask extends AbstractTaskWithInfo {

    protected AbstractCallback m_callback;

    // id of the action
    protected Long m_id;
    private static long m_idIncrement = 0;

    protected TaskError m_taskError = null;
    protected int m_errorId = -1;

    protected final Logger logger = LoggerFactory.getLogger(getClass().toString());


    public AbstractTask(AbstractCallback callback, TaskInfo taskInfo) {
        super(taskInfo);
        m_callback = callback;


        logger.debug("Task Info "+taskInfo.getTaskDescription());


        m_idIncrement++;
        if (m_idIncrement == Long.MAX_VALUE) {
            m_idIncrement = 0;
        }
        m_id = m_idIncrement;

    }

    public abstract int getType();


    /**
     * Returns a unique key to avoid to have multiple times the same task
     * @return
     */
    public abstract String getUniqueKey();

    public abstract boolean precheck() throws Exception;

    /**
     * Method called by the WorkerThread to fetch Data from database
     *
     * @return
     */
    public abstract boolean runTask();

    /**
     * Method called after the data has been fetched
     *
     * @param success boolean indicating if the fetch has succeeded
     */
    public void callback(final boolean success) {
        if (m_callback == null) {

            getTaskInfo().setFinished(success, m_taskError, true);

            return;
        }

        m_callback.setErrorMessage(m_taskError, m_errorId);

        if (m_callback.mustBeCalledInAWT()) {
            // Callback must be executed in the Graphical thread (AWT)
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    m_callback.run(success, m_id);
                    getTaskInfo().setFinished(success, getTaskError(), true);
                }
            });
        } else {
            // Method called in the current thread
            // In this case, we assume the execution is fast.
            m_callback.run(success, m_id);
            getTaskInfo().setFinished(success, getTaskError(), true);
        }


    }

    public TaskError getTaskError() {
        return m_taskError;
    }

}
