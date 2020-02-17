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

/**
 * Attribute entry : Name / Value
 *
 * @author AK249877
 */
public class AttributeEntry {
    
    private String m_name, m_value;
    
    public AttributeEntry(String attribute, String value){
        m_name = attribute;
        m_value = value;
    }
    
    public void setName(String name){
        m_name = name;
    }
    
    public String getName(){
        return m_name;
    }
    
    public void setValue(String value){
        m_value = value;
    }
    
    public String getValue(){
        return m_value;
    }
    
}
