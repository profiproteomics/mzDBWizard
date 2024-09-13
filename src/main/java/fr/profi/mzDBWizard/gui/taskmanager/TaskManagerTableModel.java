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
package fr.profi.mzDBWizard.gui.taskmanager;

import fr.profi.mzDBWizard.gui.overview.AttributeEntry;
import fr.profi.mzDBWizard.gui.overview.OverviewScrollPane;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.info.TaskInfoListener;
import fr.profi.mzDBWizard.processing.info.TaskInfoManager;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * Table Model for tasks
 *
 * @author AK249877
 */
public class TaskManagerTableModel extends AbstractTableModel implements TaskInfoListener {

    public static final int TASK_TYPE_INDEX = 0;
    public static final int FILE_INDEX = 1;
    public static final int STATE_INDEX = 2;
    public static final int DISPATCH_TIME_INDEX = 3;
    public static final int ERROR_LIST_INDEX = 4;
    private static final int COLUMN_COUNT = 5;

    private ArrayList<TaskInfo> m_infoList = new ArrayList<>(5000);

    public TaskManagerTableModel() {
        TaskInfoManager.getTaskInfoManager().setListener(this);
    }

    @Override
    public void update() {

        SwingUtilities.invokeLater(() -> {

            if (TaskInfoManager.getTaskInfoManager().copyData(m_infoList, false)) {

                ArrayList<AttributeEntry> attributes = TaskInfoManager.getTaskInfoManager().getExecutionModelData();

                OverviewScrollPane.getSingleton().update(attributes);

                fireTableDataChanged();
            }
        });

    }

    @Override
    public int getRowCount() {
        return m_infoList.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(int row, int column) {

        return m_infoList.get(row);

    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case TaskManagerTableModel.FILE_INDEX:
                return "File";
            case TaskManagerTableModel.TASK_TYPE_INDEX:
                return "Type";
            case TaskManagerTableModel.DISPATCH_TIME_INDEX:
                return "Dispatch Time";
            case TaskManagerTableModel.STATE_INDEX:
                return "State";
            case TaskManagerTableModel.ERROR_LIST_INDEX:
                return "Logs";
            default:
                return "??";
        }
    }

    public TaskInfo getTaskInfo(int row) {
        return m_infoList.get(row);
    }

}
