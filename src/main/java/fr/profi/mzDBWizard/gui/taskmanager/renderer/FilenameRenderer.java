/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.taskmanager.renderer;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import fr.profi.mzDBWizard.processing.info.TaskInfo;

/**
 *
 * @author AK249877
 */
public class FilenameRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        TaskInfo taskInfo = ((TaskInfo) value);
        setText(taskInfo.getTaskDescription());

        setBackground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);

        if (isSelected && getBackground() != null) {
            setBackground(getBackground().darker());
        }

        return this;
    }

}
