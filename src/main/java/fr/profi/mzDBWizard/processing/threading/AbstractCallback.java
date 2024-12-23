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
package fr.profi.mzDBWizard.processing.threading;

import fr.profi.mzDBWizard.processing.info.TaskError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * Callback called at the end of the execution of a Threaded Task
 *
 * @author JPM235353
 */
public abstract class AbstractCallback {

    protected Logger m_logger = LoggerFactory.getLogger(this.getClass());
    private TaskError m_taskError = null;
    private int m_errorId = -1;

    /**
     * Returns if the callback must be called
     * in the Graphical Thread (AWT)
     * @return
     */
    public abstract boolean mustBeCalledInAWT();

    /**
     * Method called by the AccessDatabaseThread when the data is fetched
     *
     * @param success   indicates if the loading of data has been a success or not
     * @param taskId    id of the task which has been executed
     */
    public abstract void run(boolean success, long taskId);

    public TaskError getTaskError() {
        return m_taskError;
    }
    public int getErrorId() {
        return m_errorId;
    }
    public void setErrorMessage(TaskError taskError, int errorId) {
        m_taskError = taskError;
        m_errorId = errorId;
    }
}
