/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.taskmanager.renderer;

import fr.profi.mzDBWizard.processing.info.TaskInfo;
import java.awt.Color;
import java.awt.Component;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author AK249877
 */
public class DateTimeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        TaskInfo taskInfo = (TaskInfo) value;

        long timeStamp = taskInfo.getStartTimestamp();
        setText((timeStamp!=-1) ? convertLongToDate(timeStamp) : "");

        setHorizontalAlignment(SwingConstants.CENTER);

        setBackground(Color.WHITE);
        setHorizontalAlignment(SwingConstants.CENTER);
        
        if (isSelected && getBackground() != null) {
            setBackground(getBackground().darker());
        }

        return this;
    }

    private String convertLongToDate(long time) {
        Date date = new Date(time);
        return m_format.format(date);
    }
    private static Format m_format = new SimpleDateFormat("yyyy MM dd HH:mm:ss");

}
