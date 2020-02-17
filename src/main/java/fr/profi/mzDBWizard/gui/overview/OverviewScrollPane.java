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
package fr.profi.mzDBWizard.gui.overview;

import fr.profi.mzDBWizard.processing.info.TaskInfoManager;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author AK249877
 */
public class OverviewScrollPane extends JScrollPane {

    private AttributesTableModel m_executionTableModel;
    private JTable m_executionTable;

    private static OverviewScrollPane m_singleton = null;

    private OverviewScrollPane() {
        init();
    }

    public static OverviewScrollPane getSingleton() {
        if (m_singleton == null) {
            m_singleton = new OverviewScrollPane();
        }

        return m_singleton;
    }

    private void init() {

        m_executionTableModel = new AttributesTableModel(TaskInfoManager.getTaskInfoManager().getExecutionModelData());
        m_executionTable = new JTable(m_executionTableModel);
        
        m_executionTable.setPreferredScrollableViewportSize(new Dimension(m_executionTable.getWidth(), 130));
        
        m_executionTable.setTableHeader(null);
        m_executionTable.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
        m_executionTable.setRowHeight(20);
        m_executionTable.setRowSelectionAllowed(true);
        m_executionTable.setDefaultRenderer(Object.class, new ExecutionTableRenderer());

        getViewport().add(m_executionTable);
        
        m_executionTable.setMinimumSize(new Dimension(m_executionTable.getWidth(), 130));
        setMinimumSize(new Dimension(m_executionTable.getWidth(), 130));
        setPreferredSize(new Dimension(m_executionTable.getWidth(), 130));

        validate();
        repaint();
    }

    public void update(ArrayList<AttributeEntry> attributes) {
        int[] selection = m_executionTable.getSelectedRows();

        m_executionTableModel.update(attributes);

        if (selection != null && selection.length > 0) {
            m_executionTable.setRowSelectionInterval(selection[0], selection[selection.length - 1]);
        }
    }


}
