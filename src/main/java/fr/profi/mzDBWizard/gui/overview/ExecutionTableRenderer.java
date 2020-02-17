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

import fr.profi.mzDBWizard.gui.util.DefaultColors;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 *  Renderer for the table in OverviewScrollPane. The rendering color depends of the row.
 *
 * @author AK249877
 */
  class ExecutionTableRenderer extends DefaultTableCellRenderer {
      
        private static final Color backgrounds[] = {Color.DARK_GRAY, DefaultColors.TURQUOISE, DefaultColors.NEPHRITIS, DefaultColors.CARROT, DefaultColors.ALIZARIN};

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {

            setHorizontalAlignment(SwingConstants.CENTER);

            setText(value.toString());

            setHorizontalAlignment(SwingConstants.CENTER);

            setForeground(Color.WHITE);
            setBackground(backgrounds[row]);

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
