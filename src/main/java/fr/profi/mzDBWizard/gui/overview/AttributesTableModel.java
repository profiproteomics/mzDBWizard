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

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Table Model for AttributeEntry ( Name / Value )
 * @author AK249877
 */
public class AttributesTableModel extends AbstractTableModel {

    public static final int ATTRIBUTE_INDEX = 0;
    public static final int VALUE_INDEX = 1;
    
    private ArrayList<AttributeEntry> m_entries;

    public AttributesTableModel(ArrayList<AttributeEntry> entries) {
        m_entries = entries;
        fireTableDataChanged();
    }
    
    

    @Override
    public int getRowCount() {
        return m_entries.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case AttributesTableModel.ATTRIBUTE_INDEX:
                return m_entries.get(row).getName();
            case AttributesTableModel.VALUE_INDEX:
                return m_entries.get(row).getValue();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case AttributesTableModel.ATTRIBUTE_INDEX:
                return "Attribute";
            case AttributesTableModel.VALUE_INDEX:
                return "Value";
            default:
                return "??";
        }
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    public void update(ArrayList<AttributeEntry> entries) {
        m_entries = entries;
        fireTableDataChanged();
    }
}
