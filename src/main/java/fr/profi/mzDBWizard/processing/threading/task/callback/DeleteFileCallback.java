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


import fr.profi.mzDBWizard.processing.threading.AbstractCallback;

import java.io.File;

/**
 *
 * Callback called when the delete task has finished
 *
 * @author JPM235353
 */
public class DeleteFileCallback extends AbstractCallback {

    private File m_file;

    @Override
    public boolean mustBeCalledInAWT() {
        return false;
    }

    @Override
    public void run(boolean success, long taskId) {

        // nothing to do
    }

    public void setFile(File file) {
        m_file = file;
    }

}
