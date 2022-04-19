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
package fr.profi.mzDBWizard.processing.threading.task;

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSCallback;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSTask;
import fr.profi.mzDBWizard.processing.jms.queue.AccessJMSManagerThread;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.jms.task.UploadFileJMSTask;
import fr.profi.mzDBWizard.processing.threading.queue.AbstractTask;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * Task to upload a mzdb file (it uses the corresponding upload JMS Task)
 *
 * @author JPM235353
 */
public class UploadMzdbTask extends AbstractFileTask {

    private Path m_directoryPath;
    private String m_pathLabel;

    public UploadMzdbTask(AbstractCallback callback, File f, File directory, String pathLabel) {
        super(callback, new TaskInfo("Updload : "+f.getName(), TaskInfo.UPLOAD_TASK,true,  TaskInfo.VisibilityEnum.VISIBLE), f);
        m_directoryPath = directory.toPath();
        m_pathLabel = pathLabel;
    }

    @Override
    public int getType() {
        return WorkerPool.UPLOAD_THREAD;
    }

    @Override
    protected boolean runTaskImplementation() {
        logger.info("  -->  Upload file "+getFile().getName());

        // Create task for JMS and waits for its end

        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                // nothing to do for the moment

            }
        };


        UploadFileJMSTask task = new UploadFileJMSTask(callback, getFile(), m_directoryPath, m_pathLabel);
        Object mutex = task.getMutex();
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);

        // wait for the JMS task to finish
        boolean m_uploadResult = false;
        try {
             synchronized (mutex) {
                 while (!task.isTaskFinished()) {
                     mutex.wait();
                 }
                 m_uploadResult = (task.getJMSState() == AbstractJMSTask.JMSState.STATE_DONE);

                 // report error to our Task
                 TaskInfo jmsTaskInfo = task.getTaskInfo();
                 m_taskError = jmsTaskInfo.getTaskError();

                 // report logs to our Task
                 ArrayList<String> logs = jmsTaskInfo.getLogs();
                 if (logs.size()>0) {
                     // we report logs to our TaskInfo
                     m_taskInfo.insertLogs(logs);
                 }

             }
         } catch (InterruptedException e) {
             return false;
         }

        return m_uploadResult;
    }
}
