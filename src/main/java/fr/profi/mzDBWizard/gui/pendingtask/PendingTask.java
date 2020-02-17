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
package fr.profi.mzDBWizard.gui.pendingtask;

/**
 * A Pending Task is potential task corresponding to a raw or mzdb file which is already present in the watched
 * directory when the application is started.
 *
 * @author AK249877
 */
public class PendingTask {

        private final String m_url;
        private final PendingTasksTableModel.Action m_taskType;

        public PendingTask(String url, PendingTasksTableModel.Action taskType) {
            m_url = url;
            m_taskType = taskType;
        }


        public String getUrl() {
            return m_url;
        }

        public PendingTasksTableModel.Action getTaskType() {
            return m_taskType;
        }
    }
