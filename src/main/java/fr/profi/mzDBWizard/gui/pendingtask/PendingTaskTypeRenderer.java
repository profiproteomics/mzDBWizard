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

import fr.profi.mzDBWizard.gui.util.DefaultIcons;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *  Renderer used for PendingTask in the corresponding Table
 *
 * @author AK249877
 */
public class PendingTaskTypeRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

        if (value instanceof PendingTasksTableModel.Action) {

            PendingTasksTableModel.Action type = (PendingTasksTableModel.Action) value;

            switch (type) {
                case CONVERSION:
                    setToolTipText("Conversion");
                    setText("Conversion");
                    setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.GEAR_ICON));
                    break;
                case UPLOAD:
                    setToolTipText("Upload");
                    setText("Upload");
                    setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.UPLOAD_ICON));
                    break;
                default:
                    setToolTipText("UNKNOWN TYPE");
                    setText("UNKNOWN TYPE");
            }

        }

        setBackground(row % 2 == 0 ? Color.LIGHT_GRAY : Color.WHITE);
        
        setHorizontalAlignment(SwingConstants.CENTER);

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
