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
package fr.profi.mzDBWizard.configuration;

import fr.profi.mzDBWizard.gui.overview.AttributeEntry;
import java.util.ArrayList;

/**
 * Class containing the parameters of the current running application which can be converted to a model used to be displayed in a table
 *
 * @author AK249877
 */
public class Configuration {
    
    public enum PrecursorComputationMethod {
        MAIN_PRECURSOR_MZ, SELECTED_ION_MZ, MZDB_ACCESS_REFINED_PRECURSOR_MZ, THERMO_REFINED_PRECURSOR_MZ, PROLINE_REFINED_PRECURSOR_MZ
    }
    
    private String m_monitoredUrl, m_converterUrl, m_converterOptions, m_host, m_mountingPoint;
    private boolean m_recursiveMonitoring, m_convert, m_exportMgf, m_uploadMzdb, m_deleteRaw, m_deleteMzdb, m_processPending;
    private PrecursorComputationMethod m_precursorComputationMethod;
    private float m_mzTolerance, m_intensityCutoff;
    
    public void setMonitoredUrl(String s){
        m_monitoredUrl = s;
    }

    
    public void setConverterUrl(String s){
        m_converterUrl = s;
    }

    public void setConverterOptions(String s){
        m_converterOptions = s;
    }

    public void setHost(String s){
        m_host = s;
    }
    
    public String getHost(){
        return m_host;
    }
    
    public void setMountingPoint(String s){
        m_mountingPoint = s;
    }
    
    public String getMountingPoint(){
        return m_mountingPoint;
    }
    
    public void setRecursiveWatching(boolean b){
        m_recursiveMonitoring = b;
    }

    
    public void setConvert(boolean b){
        m_convert = b;
    }
    
    public boolean getConvert(){
        return m_convert;
    }
    
    public void setExportMgf(boolean b){
        m_exportMgf = b;
    }

    
    public void setUploadMzdb(boolean b){
        m_uploadMzdb = b;
    }

    
    public void setDeleteRaw(boolean b){
        m_deleteRaw = b;
    }

    
    public void setDeleteMzdb(boolean b){
        m_deleteMzdb = b;
    }

    
    public void setPrecursorComputationMethod(PrecursorComputationMethod m){
        m_precursorComputationMethod = m;
    }

    
    public void setMzTolerance(float f){
        m_mzTolerance = f;
    }

    
    public void setIntensityCutoff(float f){
        m_intensityCutoff = f;
    }

    
    public void setProcessPending(boolean b){
        m_processPending = b;
    }
    
    public boolean getProcessPending(){
        return m_processPending;
    }
    
    public ArrayList<AttributeEntry> getConfigurationModelData(){
        ArrayList<AttributeEntry> modelData = new ArrayList<AttributeEntry>();
        
        modelData.add(new AttributeEntry("Monitored URL", m_monitoredUrl));
        modelData.add(new AttributeEntry("Recursive Monitoring", String.valueOf(m_recursiveMonitoring)));
        modelData.add(new AttributeEntry("Process Pending", String.valueOf(m_processPending)));
        modelData.add(new AttributeEntry("Convert", String.valueOf(m_convert)));
        if(m_convert){
            modelData.add(new AttributeEntry("Converter", m_converterUrl));
            modelData.add(new AttributeEntry("Converter Options", m_converterOptions));
        }
        modelData.add(new AttributeEntry("Export mgf", String.valueOf(m_exportMgf)));
        if(m_exportMgf){
            modelData.add(new AttributeEntry("m/z tolerance", String.valueOf(m_mzTolerance)));
            modelData.add(new AttributeEntry("Intensity cutoff", String.valueOf(m_intensityCutoff)));
            modelData.add(new AttributeEntry("Precursor m/z computation method", m_precursorComputationMethod.toString()));
        }
        modelData.add(new AttributeEntry("Upload", String.valueOf(m_uploadMzdb)));
        if(m_uploadMzdb){
            modelData.add(new AttributeEntry("Host", m_host));
            modelData.add(new AttributeEntry("Mounting Point", m_mountingPoint));
        }
        modelData.add(new AttributeEntry("Delete raw", String.valueOf(m_deleteRaw)));
        modelData.add(new AttributeEntry("Delete mzdb", String.valueOf(m_deleteMzdb)));
        
        return modelData;
    }
}
