/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.taskmanager.renderer;

import fr.profi.mzDBWizard.gui.util.DefaultColors;
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
public class ExecutionStateRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        setForeground(Color.WHITE);

        if (value instanceof TaskInfo) {

            TaskInfo taskInfo = (TaskInfo) value;
            setText(taskInfo.getPublicStateAsString());

            switch (taskInfo.getPublicState()) {
                case TaskInfo.PUBLIC_STATE_WAITING:
                    setBackground(DefaultColors.AMETHYST);
                    break;
                case TaskInfo.PUBLIC_STATE_RUNNING:
                    setBackground(DefaultColors.TURQUOISE);
                    break;
                case TaskInfo.PUBLIC_STATE_ABORTED:
                    setBackground(DefaultColors.ALIZARIN);
                    break;
                case TaskInfo.PUBLIC_STATE_FINISHED:
                    setBackground(DefaultColors.NEPHRITIS);
                    break;
                case TaskInfo.PUBLIC_STATE_FAILED:
                    setBackground(DefaultColors.SUNFLOWER);
                    break;
                                    /*case FINISHED_WITH_WARNINGS: //JPM.TODO
                    setBackground(DefaultColors.SUNFLOWER);
                    setText("ABNORMAL EXIT, NON CRITICAL");
                    break;*/
            }

        }

        setHorizontalAlignment(SwingConstants.CENTER);

        if (isSelected && getBackground() != null) {
            setBackground(getBackground().darker());
        }

        return this;
    }

}
