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
package fr.profi.mzDBWizard.processing.threading.task.callback;

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.task.DeleteFileTask;
import fr.profi.mzDBWizard.processing.threading.queue.TaskManagerThread;

import java.io.File;

/**
 *
 * Callback called when the conversion to mzdb task has finished
 *
 * @author JPM235353
 */
public class ConvertRawFile2MzdbCallback extends AbstractCallback {

    private File m_rawFile = null;

    @Override
    public boolean mustBeCalledInAWT() {
        return false;
    }

    @Override
    public void run(boolean success, long taskId) {
        if (!success) {
            return;
        }

        m_logger.debug( " --- Finish raw2mzdb. Del raw ? "+ConfigurationManager.getDeleteRaw());
        // if cleanup is asked for raw file
        if (ConfigurationManager.getDeleteRaw()) {
            TaskManagerThread.getTaskManagerThread().addTask(new DeleteFileTask(null, m_rawFile));
        }


    }

    public void setRawFile(File rawFile) {
        m_rawFile = rawFile;
    }
}
