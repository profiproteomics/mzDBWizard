/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.taskmanager.renderer;

import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.processing.info.TaskInfo;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author AK249877
 */
public class TaskTypeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        TaskInfo taskInfo = (TaskInfo) value;
        int taskType = taskInfo.getTaskType();
        switch (taskType) {
            case TaskInfo.CONVERTER_TASK:
                setToolTipText("Converter");
                setText("Converter");
                setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.GEAR_ICON));
                break;
            case TaskInfo.UPLOAD_TASK:
                setToolTipText("Uploader");
                setText("Uploader");
                setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.UPLOAD_ICON));
                break;
            case TaskInfo.DELETE_TASK:
                setToolTipText("Delete File");
                setText("Delete File");
                setIcon(null);
                break;
            case TaskInfo.MOUNTING_POINT_TASK:
                setToolTipText("Mounting Point Directory");
                setText("Mounting Point");
                setIcon(null);
                break;
            default:
                setToolTipText("");
                setText("");
                setIcon(null);
                break;
        }

        setBackground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);

        if (isSelected && getBackground() != null) {
            setBackground(getBackground().darker());
        }

        return this;
    }

}
