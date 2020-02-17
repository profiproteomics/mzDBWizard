/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.util;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author AK249877
 */
  public class GenericTableRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            setHorizontalAlignment(SwingConstants.CENTER);

            setText(value.toString());

            setHorizontalAlignment(SwingConstants.CENTER);

            setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);

            if (table.isEnabled()) {

                if (isSelected && getBackground() != null) {
                    setBackground(getBackground().darker());
                }

            } else {
                setBackground(getBackground().darker());
            }

            return this;
        }
    }
