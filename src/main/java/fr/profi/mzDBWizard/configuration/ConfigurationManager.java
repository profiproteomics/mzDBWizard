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
import fr.profi.mzDBWizard.processing.jms.queue.JMSConnectionManager;
import fr.profi.mzDBWizard.util.MzDBUtil;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * Class used to manage the configuration of the application, load and save its properties
 *
 * @author AK249877
 */
public class ConfigurationManager {

    public enum PrecursorComputationMethod {
        MAIN_PRECURSOR_MZ, SELECTED_ION_MZ, MZDB_ACCESS_REFINED_PRECURSOR_MZ, THERMO_REFINED_PRECURSOR_MZ, PROLINE_REFINED_PRECURSOR_MZ
    }

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("ApplicationProperties");

    public static final String HOST_TO_SELECT =  "<host>";

    private static String jms_server_host = HOST_TO_SELECT;
    
    private static int jms_server_port = 5445;
    
    private static String service_request_queue_name = "ProlineServiceRequestQueue";
    
    private static String input_stream_jms = "JMS_HQ_InputStream";
    
    private static String service_monitoring_notification_topic_name = "ProlineServiceMonitoringNotificationTopic";
    
    private static String proline_node_id_key = "Proline_NodeId";
    
    private static String proline_service_name_key = "Proline_ServiceName";
    
    private static String proline_service_version_key = "Proline_ServiceVersion";
    
    private static boolean delete_raw = false;    
    
    private static boolean delete_mzdb = true;
    
    private static boolean recursive_watching = true;
    
    private static boolean fullscreen = false;
    
    private static boolean restricted = true;
    
    private static ArrayList<String> path_labels;
    
    private static String converter_path = "." + File.separator + "converter" + File.separator + "mzdb_x64_0.9.7" + File.separator + "raw2mzDB.exe";

    private static String converter_options;

    
    private static String monitor_path = "." + File.separator;
    
    private static float mz_tolerance = (float) 10.0;
    
    private static float intensity_cutoff = (float) 0.0;
    
    private static PrecursorComputationMethod precursor_computation_method = PrecursorComputationMethod.THERMO_REFINED_PRECURSOR_MZ;
    
    private static String mounting_point_label = "";
    
    private static boolean mgf_operation = false;
    
    private static boolean convert_operation = true;
    
    private static boolean upload_operation = true;
    
    private static boolean process_pending = false;
    
    private static boolean debug_mode = false;
    
    public static void setConvertOperation(boolean b){
        convert_operation = b;
    }
    
    public static boolean getConvertOperation(){
        return convert_operation;
    }
    
    public static void setMgfOperation(boolean b) {
        mgf_operation = b;
    }
    
    public static boolean getMgfOperation() {
        return mgf_operation;
    }
    
    public static void setUploadOperation(boolean b){
        upload_operation = b;
    }
    
    public static boolean getUploadOperation(){
        return upload_operation;
    }
    
    public static void setJmsServerHost(String s) {
        jms_server_host = s;
    }
    
    public static String getJmsServerHost() {
        return jms_server_host;
    }
    
    public static void setJmsServerPort(int i) {
        jms_server_port = i;
    }
    
    public static int getJmsServerPort() {
        return jms_server_port;
    }
    
    public static void setServiceRequestQueueName(String s) {
        service_request_queue_name = s;
    }
    
    public static String getServiceRequestQueueName() {
        return service_request_queue_name;
    }
    
    public static void setInputStreamJms(String s) {
        input_stream_jms = s;
    }
    
    public static String getInputStreamJms() {
        return input_stream_jms;
    }
    
    public static void setServiceMonitoringNotificationTopicName(String s) {
        service_monitoring_notification_topic_name = s;
    }
    
    public static String getServiceMonitoringNotificationTopicName() {
        return service_monitoring_notification_topic_name;
    }

    public static String getProlineServiceNameKey() {
        return proline_service_name_key;
    }

    public static void setDeleteRaw(boolean b) {
        delete_raw = b;
    }
    
    public static boolean getDeleteRaw() {
        return delete_raw;
    }
    
    public static void setDeleteMzdb(boolean b) {
        delete_mzdb = b;
    }
    
    public static boolean getDeleteMzdb() {
        return delete_mzdb;
    }
    
    public static void setRecursiveWatching(boolean b) {
        recursive_watching = b;
    }
    
    public static boolean getRecursive() {
        return recursive_watching;
    }
    
    public static void setFullscreen(boolean b) {
        fullscreen = b;
    }
    
    public static boolean getFullscreen() {
        return fullscreen;
    }
    
    public static void setRestricted(boolean b) {
        restricted = b;
    }
    
    public static boolean getRestricted() {
        return restricted;
    }
    
    public static void setPathLabels(ArrayList<String> labels) {
        path_labels = labels;
    }
    
    public static ArrayList<String> getPathLabels() {
        return path_labels;
    }


    public static void setConverterOptions(String s) {
        converter_options = s;
    }

    public static String getConverterOptions() {
        return converter_options;
    }

    public static void setConverterPath(String s) {
        converter_path = s;
    }
    
    public static String getConverterPath() {
        return converter_path;
    }
    
    public static void setMonitorPath(String s) {
        monitor_path = s;
    }
    
    public static String getMonitorPath() {
        return monitor_path;
    }
    
    public static void setMzTolerance(float f) {
        mz_tolerance = f;
    }
    
    public static float getMzTolerance() {
        return mz_tolerance;
    }
    
    public static void setIntensityCutoff(float f) {
        intensity_cutoff = f;
    }
    
    public static float getIntensityCutoff() {
        return intensity_cutoff;
    }
    
    public static void setPrecursorComputationMethod(PrecursorComputationMethod method) {
        precursor_computation_method = method;
    }
    
    public static void setMountingPointLabel(String label) {
        mounting_point_label = label;
    }
    
    
    public static String getMountingPointLabel() {
        return mounting_point_label;
    }
    
    public static void setPrecursorComputationMethod(String s) {
        for (PrecursorComputationMethod method : PrecursorComputationMethod.values()) {
            if (method.toString().equalsIgnoreCase(s)) {
                ConfigurationManager.setPrecursorComputationMethod(method);
            }
        }
    }

    public static PrecursorComputationMethod getPrecursorComputationMethod() {
        return precursor_computation_method;
    }
    
    public static void setProcessPending(boolean b){
        process_pending = b;
    }
    
    public static boolean getProcessPending(){
        return process_pending;
    }
    
    public static void setDebugMode(boolean b){
        debug_mode = b;
    }
    
    public static boolean getDebugMode(){
        return debug_mode;
    }
    
    public static void loadProperties() {
        Properties prop = new Properties();
        InputStream is = null;
        try {

            File cfgFile =new File(MzDBUtil.CONFIGURATION_FILE);
            is =  new FileInputStream(cfgFile);

            logger.debug("Loading Configuration File: " + cfgFile.getAbsolutePath() + " ..");
            
            prop.load(is);
            
            ConfigurationManager.setJmsServerHost(prop.getProperty("JMS_SERVER_HOST") != null ? prop.getProperty("JMS_SERVER_HOST") : jms_server_host);
            JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(ConfigurationManager.getJmsServerHost());
            logger.debug(jms_server_host);
            
            ConfigurationManager.setJmsServerPort(Integer.parseInt(prop.getProperty("JMS_SERVER_PORT") != null ? prop.getProperty("JMS_SERVER_PORT") : String.valueOf(jms_server_port)));
            logger.debug(String.valueOf(jms_server_port));
            
            ConfigurationManager.setServiceRequestQueueName(prop.getProperty("SERVICE_REQUEST_QUEUE_NAME") != null ? prop.getProperty("SERVICE_REQUEST_QUEUE_NAME") : service_request_queue_name);
            logger.debug(service_request_queue_name);
            
            ConfigurationManager.setServiceMonitoringNotificationTopicName(prop.getProperty("SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME") != null ? prop.getProperty("SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME") : service_monitoring_notification_topic_name);
            logger.debug(service_monitoring_notification_topic_name);
            
            ConfigurationManager.setDeleteRaw(Boolean.parseBoolean(prop.getProperty("DELETE_RAW") != null ? prop.getProperty("DELETE_RAW") : String.valueOf(delete_raw)));
            logger.debug(String.valueOf(delete_raw));
            
            ConfigurationManager.setDeleteMzdb(Boolean.parseBoolean(prop.getProperty("DELETE_MZDB") != null ? prop.getProperty("DELETE_MZDB") : String.valueOf(delete_mzdb)));
            logger.debug(String.valueOf(delete_mzdb));
            
            ConfigurationManager.setRecursiveWatching(Boolean.parseBoolean(prop.getProperty("RECURSIVE_WATCHING") != null ? prop.getProperty("RECURSIVE_WATCHING") : String.valueOf(recursive_watching)));
            logger.debug(String.valueOf(recursive_watching));
            
            ConfigurationManager.setFullscreen(Boolean.parseBoolean(prop.getProperty("FULLSCREEN") != null ? prop.getProperty("FULLSCREEN") : String.valueOf(fullscreen)));
            logger.debug(String.valueOf(fullscreen));

            ConfigurationManager.setRestricted(Boolean.parseBoolean(prop.getProperty("RESTRICTED") != null ? prop.getProperty("RESTRICTED") : String.valueOf(restricted)));
            logger.debug(String.valueOf(restricted));
            
            ConfigurationManager.setConverterPath(prop.getProperty("CONVERTER_PATH") != null ? prop.getProperty("CONVERTER_PATH") : converter_path);
            logger.debug(converter_path);

            ConfigurationManager.setConverterOptions(prop.getProperty("CONVERTER_OPTIONS") != null ? prop.getProperty("CONVERTER_OPTIONS") : "");
            logger.debug(converter_path);

            ConfigurationManager.setMonitorPath(prop.getProperty("MONITOR_PATH") != null ? prop.getProperty("MONITOR_PATH") : monitor_path);
            logger.debug(monitor_path);
            
            ConfigurationManager.setMzTolerance(prop.getProperty("MZ_TOLERANCE") != null ? Float.parseFloat(prop.getProperty("MZ_TOLERANCE")) : mz_tolerance);
            logger.debug(String.valueOf(mz_tolerance));
            
            ConfigurationManager.setIntensityCutoff(prop.getProperty("INTENSITY_CUTOFF") != null ? Float.parseFloat(prop.getProperty("INTENSITY_CUTOFF")) : intensity_cutoff);
            logger.debug(String.valueOf(intensity_cutoff));
            
            if (prop.getProperty("PRECURSOR_COMPUTATION_METHOD") != null) {
                ConfigurationManager.setPrecursorComputationMethod(prop.getProperty("PRECURSOR_COMPUTATION_METHOD"));
            }
            logger.debug(precursor_computation_method.toString());
            
            ConfigurationManager.setMgfOperation(Boolean.parseBoolean(prop.getProperty("MGF_OPERATION") != null ? prop.getProperty("MGF_OPERATION") : String.valueOf(mgf_operation)));
            logger.debug(String.valueOf(mgf_operation));
            
            ConfigurationManager.setConvertOperation(Boolean.parseBoolean(prop.getProperty("CONVERT_OPERATION") != null ? prop.getProperty("CONVERT_OPERATION") : String.valueOf(convert_operation)));
            logger.debug(String.valueOf(convert_operation));
            
            ConfigurationManager.setUploadOperation(Boolean.parseBoolean(prop.getProperty("UPLOAD_OPERATION") != null ? prop.getProperty("UPLOAD_OPERATION") : String.valueOf(upload_operation)));
            logger.debug(String.valueOf(upload_operation));
            
            if (prop.getProperty("MOUNTING_POINT_LABEL") != null) {
                ConfigurationManager.setMountingPointLabel(prop.getProperty("MOUNTING_POINT_LABEL"));
            }
            logger.debug(mounting_point_label);
            
            ConfigurationManager.setProcessPending(Boolean.parseBoolean(prop.getProperty("PROCESS_PENDING") != null ? prop.getProperty("PROCESS_PENDING") : String.valueOf(process_pending)));
            logger.debug(String.valueOf(process_pending));
            
            ConfigurationManager.setDebugMode(Boolean.parseBoolean(prop.getProperty("DEBUG_MODE") != null ? prop.getProperty("DEBUG_MODE") : String.valueOf(debug_mode)));
            logger.debug(String.valueOf(debug_mode));
            
        } catch (FileNotFoundException ex) {
            logger.error("FileNotFoundException while loading properties!", ex);
        } catch (IOException ex) {
            logger.error("IOException while loading properties!", ex);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                logger.error("Finally IOException while loading properties!", ex);
            }
        }
        
    }

    public static void saveProperties() {
        Properties prop = new Properties();
        OutputStream output = null;
        
        try {

            output = new FileOutputStream(MzDBUtil.CONFIGURATION_FILE);

            // set the properties value
            prop.setProperty("JMS_SERVER_HOST", ConfigurationManager.getJmsServerHost());
            prop.setProperty("JMS_SERVER_PORT", String.valueOf(ConfigurationManager.getJmsServerPort()));
            prop.setProperty("SERVICE_REQUEST_QUEUE_NAME", ConfigurationManager.getServiceRequestQueueName());
            prop.setProperty("SERVICE_MONITORING_NOTIFICATION_TOPIC_NAME", ConfigurationManager.getServiceMonitoringNotificationTopicName());
            prop.setProperty("DELETE_RAW", String.valueOf(ConfigurationManager.getDeleteRaw()));
            prop.setProperty("DELETE_MZDB", String.valueOf(ConfigurationManager.getDeleteMzdb()));
            prop.setProperty("RECURSIVE_WATCHING", String.valueOf(ConfigurationManager.getRecursive()));
            prop.setProperty("PROCESS_PENDING", String.valueOf(ConfigurationManager.getProcessPending()));
            prop.setProperty("FULLSCREEN", String.valueOf(ConfigurationManager.getFullscreen()));
            prop.setProperty("RESTRICTED", String.valueOf(ConfigurationManager.getRestricted()));
            prop.setProperty("CONVERTER_PATH", ConfigurationManager.getConverterPath());
            prop.setProperty("CONVERTER_OPTIONS", ConfigurationManager.getConverterOptions());
            prop.setProperty("MONITOR_PATH", ConfigurationManager.getMonitorPath());
            prop.setProperty("MZ_TOLERANCE", String.valueOf(ConfigurationManager.getMzTolerance()));
            prop.setProperty("INTENSITY_CUTOFF", String.valueOf(ConfigurationManager.getIntensityCutoff()));
            prop.setProperty("PRECURSOR_COMPUTATION_METHOD", ConfigurationManager.getPrecursorComputationMethod().toString());
            prop.setProperty("MGF_OPERATION", String.valueOf(ConfigurationManager.getMgfOperation()));
            prop.setProperty("CONVERT_OPERATION", String.valueOf(ConfigurationManager.getConvertOperation()));
            prop.setProperty("UPLOAD_OPERATION", String.valueOf(ConfigurationManager.getUploadOperation()));
            prop.setProperty("MOUNTING_POINT_LABEL", ConfigurationManager.getMountingPointLabel());
            prop.setProperty("DEBUG_MODE", String.valueOf(ConfigurationManager.getDebugMode()));

            // save properties to project root folder
            prop.store(output, null);
            
        } catch (IOException io) {
            logger.error("IOException while saving all properties!", io);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.error("IOException while saving all properties! (Finally)", e);
                }
            }
            
        }
    }


    public static ArrayList<AttributeEntry> getConfigurationModelData(){
        ArrayList<AttributeEntry> modelData = new ArrayList<AttributeEntry>();

        modelData.add(new AttributeEntry("Monitored URL", getMonitorPath()));
        modelData.add(new AttributeEntry("Recursive Monitoring", String.valueOf(getRecursive())));
        modelData.add(new AttributeEntry("Process Pending", String.valueOf(getProcessPending())));
        modelData.add(new AttributeEntry("Convert", String.valueOf(getConvertOperation())));
        if(getConvertOperation()){
            modelData.add(new AttributeEntry("Converter", getConverterPath()));
            modelData.add(new AttributeEntry("Converter Options", getConverterOptions()));
        }
        modelData.add(new AttributeEntry("Export mgf", String.valueOf(getMgfOperation())));
        if(getMgfOperation()){
            modelData.add(new AttributeEntry("m/z tolerance", String.valueOf(getMzTolerance())));
            modelData.add(new AttributeEntry("Intensity cutoff", String.valueOf(getIntensityCutoff())));
            modelData.add(new AttributeEntry("Precursor m/z computation method", getPrecursorComputationMethod().toString()));
        }
        modelData.add(new AttributeEntry("Upload", String.valueOf(getUploadOperation())));
        if(getUploadOperation()){
            modelData.add(new AttributeEntry("Host", getJmsServerHost()));
            modelData.add(new AttributeEntry("Mounting Point", getMountingPointLabel()));
        }
        modelData.add(new AttributeEntry("Delete raw", String.valueOf(getDeleteRaw())));
        modelData.add(new AttributeEntry("Delete mzdb", String.valueOf(getDeleteMzdb())));

        return modelData;
    }
    
}
