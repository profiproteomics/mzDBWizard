/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.taskmanager.renderer;

import fr.profi.mzDBWizard.gui.util.DefaultColors;
import fr.profi.mzDBWizard.processing.info.TaskError;
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
public class ErrorsNumberRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        TaskInfo taskInfo = (TaskInfo) value;
        TaskError taskError = taskInfo.getTaskError();
        int errors = (taskError != null) ? 1 : 0;
        int warnings = taskInfo.getWarningCount();
        int logSize = taskInfo.getLogs().size();

        if (errors > 0) {
            setBackground(DefaultColors.ALIZARIN);
            setForeground(Color.WHITE);
            setToolTipText("Critical: " + errors + " , Non-Critical: " + warnings);
            setText("Error");
        } else if (warnings > 0) {
            setBackground(DefaultColors.SUNFLOWER);
            setForeground(Color.WHITE);
            setToolTipText("Warnings: " + warnings);
        } else {
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setToolTipText("No error");
            setText((logSize>0) ? "Info" : "");
        }







        setHorizontalAlignment(SwingConstants.CENTER);

        if (isSelected && getBackground() != null) {
            setBackground(getBackground().darker());
        }

        return this;
    }

}
