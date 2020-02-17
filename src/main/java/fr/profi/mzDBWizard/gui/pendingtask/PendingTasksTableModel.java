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

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 *  Table Model for Pending Tasks
 *
 * @author AK249877
 */
public class PendingTasksTableModel extends AbstractTableModel {

    public enum Action {

        CONVERSION, UPLOAD
    }

    public static final int FILE_INDEX = 0;
    public static final int ACTON_INDEX = 1;

    private ArrayList<PendingTask> m_pendingTasks;

    public PendingTasksTableModel() {
        m_pendingTasks = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        return m_pendingTasks.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case PendingTasksTableModel.FILE_INDEX:
                return m_pendingTasks.get(row).getUrl();
            case PendingTasksTableModel.ACTON_INDEX:
                return m_pendingTasks.get(row).getTaskType();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case PendingTasksTableModel.FILE_INDEX:
                return "File";
            case PendingTasksTableModel.ACTON_INDEX:
                return "Action";
            default:
                return "??";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    public void update(ArrayList<PendingTask> pendingTasks) {
        m_pendingTasks = pendingTasks;
        fireTableDataChanged();
    }

}
