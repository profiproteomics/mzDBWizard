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

import fr.profi.mzDBWizard.gui.log.LogsDialog;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.gui.taskmanager.renderer.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.info.TaskInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Panel containing all the tasks : done, failed, running...
 * There is a popup menu and buttons to see logs or clear them
 *
 * @author AK249877
 */
public class TaskManagerPanel extends JPanel implements ActionListener {

    private TaskManagerTableModel m_taskManagerModel;
    private JTable m_table;
    private JPopupMenu m_popupMenu;
    private JMenuItem logsItem, clearItem, clearAllItem;
    private JButton logsButton, clearButton;

    private final Logger logger = LoggerFactory.getLogger(getClass().toString());

    public TaskManagerPanel() {

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        setLayout(new BorderLayout());
        add(initTasksPanel(), BorderLayout.CENTER);

    }

    private JPanel initTasksPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        m_taskManagerModel = new TaskManagerTableModel();
        m_table = new JTable(m_taskManagerModel);

        m_table.setFillsViewportHeight(true);

        m_table.getColumnModel().getColumn(TaskManagerTableModel.TASK_TYPE_INDEX).setCellRenderer(new TaskTypeRenderer());
        m_table.getColumnModel().getColumn(TaskManagerTableModel.FILE_INDEX).setCellRenderer(new FilenameRenderer());
        m_table.getColumnModel().getColumn(TaskManagerTableModel.STATE_INDEX).setCellRenderer(new ExecutionStateRenderer());
        m_table.getColumnModel().getColumn(TaskManagerTableModel.DISPATCH_TIME_INDEX).setCellRenderer(new DateTimeRenderer());
        m_table.getColumnModel().getColumn(TaskManagerTableModel.ERROR_LIST_INDEX).setCellRenderer(new ErrorsNumberRenderer());

        m_table.setRowHeight(20);

        //m_table.addMouseListener(this);

        m_table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_table.setComponentPopupMenu(addPopupMenu());

        JScrollPane scrollPane = new JScrollPane(m_table);
        
        scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5), BorderFactory.createLineBorder(Color.BLACK)));

        panel.add(addToolBar(), BorderLayout.WEST);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }


    private JToolBar addToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        logsButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.DOCUMENT_ICON));
        logsButton.setFocusable(false);
        logsButton.addActionListener(this);
        logsButton.setActionCommand("Logs");
        logsButton.setToolTipText("Click to show the logs of the selected task");
        logsButton.setFocusPainted(false);
        logsButton.setOpaque(true);
        logsButton.setBorderPainted(false);
        toolbar.add(logsButton);

        clearButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.BROOM_ICON));
        clearButton.setFocusable(false);
        clearButton.addActionListener(this);
        clearButton.setActionCommand("Clear");
        clearButton.setToolTipText("Click to clear the selected task");
        clearButton.setFocusPainted(false);
        clearButton.setOpaque(true);
        clearButton.setBorderPainted(false);
        toolbar.add(clearButton);

        updateButtons();

        ListSelectionModel cellSelectionModel = m_table.getSelectionModel();

        cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateButtons();
            }

        });

        return toolbar;
    }

    private JPopupMenu addPopupMenu() {
        m_popupMenu = new JPopupMenu();

        logsItem = new JMenuItem("Logs", DefaultIcons.getSingleton().getIcon(DefaultIcons.DOCUMENT_ICON));
        logsItem.addActionListener(this);
        logsItem.setActionCommand("Logs");
        m_popupMenu.add(logsItem);

        clearItem = new JMenuItem("Clear", DefaultIcons.getSingleton().getIcon(DefaultIcons.BROOM_ICON));
        clearItem.addActionListener(this);
        clearItem.setActionCommand("Clear");
        m_popupMenu.add(clearItem);


        clearAllItem = new JMenuItem("Clear All");
        clearAllItem.addActionListener(this);
        clearAllItem.setActionCommand("Clear All");
        m_popupMenu.add(clearAllItem);


        m_popupMenu.addPopupMenuListener(new PopupMenuListener() {

            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {

                        int[] selectedRows = m_table.getSelectedRows();
                        int nbSelectedRows = selectedRows.length;

                        int rowAtPoint = m_table.rowAtPoint(SwingUtilities.convertPoint(m_popupMenu, new Point(0, 0), m_table));
                        if (rowAtPoint != -1) {
                            boolean rowIsSelected = false;
                            for (int rowSelected : selectedRows) {
                                if (rowAtPoint == rowSelected) {
                                    rowIsSelected = true;
                                    break;
                                }
                            }
                            if (!rowIsSelected) {
                                m_table.setRowSelectionInterval(rowAtPoint, rowAtPoint);
                            }
                        }

                        // update actions

                        // update logs
                        logsItem.setEnabled(nbSelectedRows == 1);

                        // update clear
                        boolean clear = (nbSelectedRows>=1);
                        for (int row : selectedRows) {
                            row = m_table.convertRowIndexToModel(row);
                            TaskInfo info = m_taskManagerModel.getTaskInfo(row);
                            if (info.isFinished() || info.isAborted()) {
                                continue;
                            }
                            clear = false;
                            break;
                        }
                        clearItem.setEnabled(clear);

                        // update clear all
                        boolean clearAll = false;
                        int nbInfo = m_taskManagerModel.getRowCount();
                        for (int i=0;i<nbInfo;i++) {
                            TaskInfo info = m_taskManagerModel.getTaskInfo(i);
                            if (info.isFinished() || info.isAborted()) {
                                clearAll = true;
                                break;
                            }
                        }
                        clearAllItem.setEnabled(clearAll);

                    }
                });
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                // TODO Auto-generated method stub

            }
        });


        return m_popupMenu;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        int[] selectedRows = m_table.getSelectedRows();
        if (selectedRows.length == 0) {
            return;
        }

        if (ae.getActionCommand().equalsIgnoreCase("Logs")) {
            int row = m_table.getSelectedRow();
            row = m_table.convertRowIndexToModel(row);
            TaskInfo taskInfo = m_taskManagerModel.getTaskInfo(row);
            LogsDialog.setParameters(this, taskInfo, 2);
            LogsDialog logsDialog = LogsDialog.getInstance();
            logsDialog.updateLogsDialog();
            Thread thread = new Thread(logsDialog);
            thread.start();
            logsDialog.setVisible(true);
            logsDialog.setModal(true);
        } else if (ae.getActionCommand().equalsIgnoreCase("Clear")) {

            for (int row : selectedRows) {
                row = m_table.convertRowIndexToModel(row);
                TaskInfo info = m_taskManagerModel.getTaskInfo(row);
                TaskInfoManager.getTaskInfoManager().cancel(info.getSourceTaskInfo());
            }
        } else if (ae.getActionCommand().equalsIgnoreCase("Clear All")) {
            int nbInfo = m_taskManagerModel.getRowCount();
            for (int i=0;i<nbInfo;i++) {
                TaskInfo info = m_taskManagerModel.getTaskInfo(i);
                if (info.isFinished() || info.isAborted()) {
                    TaskInfoManager.getTaskInfoManager().cancel(info.getSourceTaskInfo());
                }
            }
        }

    }



    private void updateButtons() {

        int[] selectedRows = m_table.getSelectedRows();
        int nbSelectedRows = selectedRows.length;

        // update logs
        logsButton.setEnabled(nbSelectedRows == 1);

        // update clear
        boolean clear = (nbSelectedRows>=1);
        for (int row : selectedRows) {
            row = m_table.convertRowIndexToModel(row);
            TaskInfo info = m_taskManagerModel.getTaskInfo(row);
            if (info.isFinished() || info.isAborted()) {
                continue;
            }
            clear = false;
            break;
        }
        clearButton.setEnabled(clear);

    }

}
