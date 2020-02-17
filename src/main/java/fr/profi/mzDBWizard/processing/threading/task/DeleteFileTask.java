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
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.AbstractTask;
import fr.profi.mzDBWizard.processing.threading.task.callback.DeleteFileCallback;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.util.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

/**
 *
 * Task to delete a file
 *
 * @author JPM235353
 */
public class DeleteFileTask extends AbstractTask {

    private final Logger m_logger = LoggerFactory.getLogger(getClass().toString());

    private File m_file;

    public DeleteFileTask(AbstractCallback callback, File f) {
        super(callback, new TaskInfo("Delete file : "+f.getName(), TaskInfo.DELETE_TASK, true, null, TaskInfo.VisibilityEnum.VISIBLE_IF_ERROR));

        m_file = f;
    }


    public String getUniqueKey() {
        return m_file.getName().toLowerCase();
    }


    @Override
    public int getType() {
        return WorkerPool.DELETE_THREAD;
    }

    @Override
    public boolean precheck() throws Exception {

        if (!m_file.exists()) {
            m_taskError = new TaskError("File does not exist : "+m_file.getAbsolutePath());
            return false;
        }


        // check that the raw file has been completely copied on the disk
        FileUtility.checkFileFinalization(m_file);

        // try to force writable
        m_file.setWritable(true);

        if (!m_file.canWrite()) {
            m_taskError = new TaskError("File is not writable : "+m_file.getAbsolutePath());
            return false;
        }


        return true;
    }

    @Override
    public boolean runTask() {

        try {
            return runTaskImplementation();
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }
    }

    private boolean runTaskImplementation() throws Exception {

        if (m_callback instanceof DeleteFileCallback) {
            ((DeleteFileCallback) m_callback).setFile(m_file);
        }

        String log = "Starting to delete "+m_file.getAbsolutePath() + " file.";
        m_logger.info(log);
        m_taskInfo.addLog(log);

        if (!precheck()) {
            return false;
        }

        return  deleteFile(m_file);

    }

    private boolean deleteFile(File f) {
        return deleteFile(f, 0);
    }
    private boolean deleteFile(File f, int retry) {

        final int MAX_RETRY = 5;

        try {
            Files.delete(f.toPath());

        } catch (NoSuchFileException x) {
            m_taskError = new TaskError("Trying to delete file " + f.getAbsolutePath() + ", which does not exist!");
            m_logger.error(m_taskError.getErrorTitle(), x);
            return false;
        } catch (DirectoryNotEmptyException x) {
            m_taskError = new TaskError("Directory " + f.getAbsolutePath() + " is not empty!");
            m_logger.error(m_taskError.getErrorTitle(), x);
            return false;
        } catch (IOException x) {
            if (retry < MAX_RETRY) {
                try {
                    Thread.sleep(1000*retry); // for MAX_RETRY == 5 : waits 1+2+3+4 = 10 seconds
                } catch (InterruptedException e) {}

                return deleteFile(f, retry+1);
            } else {
                m_taskError = new TaskError("You do not have the right to delete: " + f.toPath().toString() + "!");
                m_logger.error(m_taskError.getErrorTitle(), x);
                return false;
            }
        }

        String log = "Finished to delete "+m_file.getAbsolutePath() + " file.";
        m_logger.info(log);
        m_taskInfo.addLog(log);

        return true;
    }

}
