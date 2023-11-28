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
import fr.profi.mzDBWizard.processing.CreateMgfCommand;
import fr.profi.mzDBWizard.processing.jms.queue.JMSConnectionManager;
import fr.profi.mzDBWizard.processing.threading.FileProcessingExec;
import fr.profi.mzDBWizard.util.MzDBUtil;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

/**
 *
 * Class used to manage the configuration of the application, load and save its properties
 *
 * @author AK249877
 */
public class ConfigurationManager {

    public static final String PROCESS_GENERATE_MGF_KEY = "PROCESS_GENERATE_MGF";

    public static final String FULLSCREEN_KEY = "FULLSCREEN";
    public static final String READ_ONLY_KEY = "READ_ONLY";
    public static final String DEBUG_MODE_KEY = "DEBUG_MODE";

    public static final String DELETE_RAW_KEY = "DELETE_RAW";
    public static final String DELETE_MZDB_KEY = "DELETE_MZDB";

    public static final String MONITOR_PATH_KEY = "MONITOR_PATH";
    public static final String RECURSIVE_WATCHING_KEY = "RECURSIVE_WATCHING";

    public static final String PROCESS_CONVERT_KEY = "PROCESS_CONVERT";
    public static final String CONVERTER_PATH_KEY = "CONVERTER_PATH";
    public static final String CONVERTER_OPTIONS_KEY = "CONVERTER_OPTIONS";
    public static final String PROCESS_SPLIT_MZDB_KEY = "PROCESS_SPLIT_MZDB";

    public static final String PROCESS_UPLOAD_KEY = "PROCESS_UPLOAD";
    public static final String MOUNTING_POINT_LABEL_KEY = "MOUNTING_POINT_LABEL";
    public static final String JMS_SERVER_PORT_KEY = "JMS_SERVER_PORT";
    public static final String JMS_SERVER_HOST_KEY = "JMS_SERVER_HOST";
    public static final String SERVICE_REQUEST_QUEUE_NAME_KEY = "SERVICE_REQUEST_QUEUE_NAME";

    public static final String PROCESS_PENDING_KEY = "PROCESS_PENDING";


//    public enum PrecursorComputationMethod {
//        MAIN_PRECURSOR_MZ, SELECTED_ION_MZ, MZDB_ACCESS_REFINED_PRECURSOR_MZ, THERMO_REFINED_PRECURSOR_MZ, PROLINE_REFINED_PRECURSOR_MZ, MGF_BOOST
//    }
//    String mgfBoostVDS="mgf_boost";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("ApplicationProperties");

    private static boolean fullscreen = false;
    private static boolean read_only_cfg = false;

    //JMS & Upload Options
    public static final String HOST_TO_SELECT =  "<host>";
    private static String jms_server_host = HOST_TO_SELECT;
    private static int jms_server_port = 5445;
    private static String service_request_queue_name = JMSConnectionManager.DEFAULT_SERVICE_REQUEST_QUEUE_NAME;
    private static ArrayList<String> mounting_point_path_labels;
    private static boolean upload_operation = true;

    //File manage Options
    private static boolean delete_raw = false;
    private static boolean delete_mzdb = true;
    private static boolean recursive_watching = true;
    private static String monitor_path = "." + File.separator;
    private static String mounting_point_label = "";

    // Converter Options
    private static String converter_path = "." + File.separator + "converter" + File.separator +"raw2mzDB.exe";
    private static String converter_options;
    private static boolean convert_mzdb_operation = true;
    private static boolean split_mzdb_operation = true;

    //MGF Generation Options
//    private static float mz_tolerance = (float) 10.0;
//    private static float intensity_cutoff = (float) 0.0;
//    private static PrecursorComputationMethod precursor_computation_method = PrecursorComputationMethod.THERMO_REFINED_PRECURSOR_MZ;
    private static boolean generate_mgf_operation = false;
//    private static boolean exportProlineTitle = true;
//    private static boolean processPClean = true;
//    private static String pCleanLabelMethodName = "";
//    private static String pCleanConfigName = "";


    private static boolean process_pending = false;
    private static boolean debug_mode = false;
    
    public static void setConvertMzdbOperation(boolean b){
        convert_mzdb_operation = b;
    }
    
    public static boolean getConvertMzdbOperation(){
        return convert_mzdb_operation;
    }
    
    public static void setProcessGenerateMgf(boolean b) {
        generate_mgf_operation = b;
    }

    public static boolean getProcessGenerateMgf() {
        return generate_mgf_operation;
    }

//    public static boolean getProcessPClean() {
//        return processPClean;
//    }
//
//    public static void setProcessPClean(boolean doProcessPClean) {
//        processPClean = doProcessPClean;
//    }
//
//    public static boolean isProlineTitleExported() {
//        return exportProlineTitle;
//    }
//
//    public static void setExportProlineTitle(boolean doExportProlineTitle) {
//        exportProlineTitle = doExportProlineTitle;
//    }
//
//    public static String getPCleanLabelMethodName() {
//        return pCleanLabelMethodName;
//    }
//
//    public static void setPCleanLabelMethodName(String pCleanLabelMethod) {
//        pCleanLabelMethodName = pCleanLabelMethod;
//    }
//
//    public static CommandArguments.PCleanConfig getPCleanConfig() {
//       return  CommandArguments.PCleanConfig.getConfigFor(pCleanConfigName);
//    }
//
//    public static void setPCleanConfigName(String pCleanConfig) {
//        pCleanConfigName = pCleanConfig;
//    }

    public static void setProcessSplitMzdb(boolean b) {
        split_mzdb_operation = b;
    }

    public static boolean getProcessSplitMzdb() {
        return split_mzdb_operation;
    }
    
    public static void setProcessUpload(boolean b){
        upload_operation = b;
    }
    
    public static boolean getProcessUpload(){
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

    public static String getProlineServiceNameKey() {
        return JMSConnectionManager.PROLINE_SERVICE_NAME_KEY;
    }

    public static void setDeleteRaw(boolean b) {
        delete_raw = b;
    }
    
    public static boolean getDeleteRaw() {
        return delete_raw;
    }

    public static boolean isReadOnlyCfg() {
        return read_only_cfg;
    }

    public static void setReadOnlyCfg(boolean read_only_cfg) {
        ConfigurationManager.read_only_cfg = read_only_cfg;
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
    
    public static void setMoutingPointPathLabels(ArrayList<String> labels) {
        mounting_point_path_labels = labels;
    }
    
    public static ArrayList<String> getMountingPointPathLabels() {
        return mounting_point_path_labels;
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
    
//    public static void setMzTolerance(float f) {
//        mz_tolerance = f;
//    }
//
//    public static float getMzTolerance() {
//        return mz_tolerance;
//    }
//
//    public static void setIntensityCutoff(float f) {
//        intensity_cutoff = f;
//    }
//
//    public static float getIntensityCutoff() {
//        return intensity_cutoff;
//    }
//
//    public static void setPrecursorComputationMethod(PrecursorComputationMethod method) {
//        precursor_computation_method = method;
//    }
//
    public static void setMountingPointLabel(String label) {
        mounting_point_label = label;
    }
    
    
    public static String getMountingPointLabel() {
        return mounting_point_label;
    }
    
//    public static void setPrecursorComputationMethod(String s) {
//        for (PrecursorComputationMethod method : PrecursorComputationMethod.values()) {
//            if (method.toString().equalsIgnoreCase(s)) {
//                ConfigurationManager.setPrecursorComputationMethod(method);
//            }
//        }
//    }

//    public static PrecursorComputationMethod getPrecursorComputationMethod() {
//        return precursor_computation_method;
//    }
    
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
            String property = prop.getOrDefault(READ_ONLY_KEY, "false").toString();
            ConfigurationManager.setReadOnlyCfg( Boolean.parseBoolean(property));
            logger.debug("ReadOnly : "+ConfigurationManager.isReadOnlyCfg());

            ConfigurationManager.setJmsServerHost(prop.getOrDefault(JMS_SERVER_HOST_KEY, jms_server_host).toString());
            JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(ConfigurationManager.getJmsServerHost());
            logger.debug("jms_server_host: "+jms_server_host);
            
            ConfigurationManager.setJmsServerPort(Integer.parseInt(prop.getOrDefault(JMS_SERVER_PORT_KEY, jms_server_port).toString()));
            logger.debug("jms_server_port: "+ jms_server_port);
            
            ConfigurationManager.setServiceRequestQueueName(prop.getOrDefault(SERVICE_REQUEST_QUEUE_NAME_KEY,service_request_queue_name).toString());
            logger.debug("service_request_queue_name: "+service_request_queue_name);
            
            ConfigurationManager.setDeleteRaw(Boolean.parseBoolean(prop.getOrDefault(DELETE_RAW_KEY, delete_raw).toString()));
            logger.debug("delete_raw: "+ delete_raw);
            
            ConfigurationManager.setDeleteMzdb(Boolean.parseBoolean(prop.getOrDefault(DELETE_MZDB_KEY, delete_mzdb).toString()));
            logger.debug("delete_mzdb: "+ delete_mzdb);
            
            ConfigurationManager.setRecursiveWatching(Boolean.parseBoolean(prop.getOrDefault(RECURSIVE_WATCHING_KEY, recursive_watching).toString()));
            logger.debug("recursive_watching: "+ recursive_watching);
            
            ConfigurationManager.setFullscreen(Boolean.parseBoolean(prop.getOrDefault(FULLSCREEN_KEY, fullscreen).toString()));
            logger.debug("fullscreen: "+ fullscreen);
            
            ConfigurationManager.setConverterPath(prop.getOrDefault(CONVERTER_PATH_KEY, converter_path).toString());
            logger.debug("converter_path: "+ converter_path);

            ConfigurationManager.setConverterOptions(prop.getOrDefault(CONVERTER_OPTIONS_KEY, "").toString());
            logger.debug("converter_options: "+ converter_options);

            ConfigurationManager.setProcessSplitMzdb(Boolean.parseBoolean(prop.getOrDefault(PROCESS_SPLIT_MZDB_KEY, split_mzdb_operation).toString()));
            logger.debug("split_mzdb_operation: "+ split_mzdb_operation);

            ConfigurationManager.setMonitorPath(prop.getOrDefault(MONITOR_PATH_KEY, monitor_path).toString());
            logger.debug("monitor_path: "+monitor_path);

            ConfigurationManager.setProcessGenerateMgf(Boolean.parseBoolean(prop.getOrDefault(PROCESS_GENERATE_MGF_KEY, generate_mgf_operation).toString()));
            logger.debug("generate_mgf_operation: "+ generate_mgf_operation);

            ConfigurationManager.setConvertMzdbOperation(Boolean.parseBoolean(prop.getOrDefault(PROCESS_CONVERT_KEY, convert_mzdb_operation).toString()));
            logger.debug("convert_mzdb_operation: "+ convert_mzdb_operation);
            
            ConfigurationManager.setProcessUpload(Boolean.parseBoolean(prop.getOrDefault(PROCESS_UPLOAD_KEY,upload_operation).toString()));
            logger.debug("upload_operation: "+ upload_operation);
            
            if (prop.getProperty(ConfigurationManager.MOUNTING_POINT_LABEL_KEY) != null) {
                ConfigurationManager.setMountingPointLabel(prop.getProperty(ConfigurationManager.MOUNTING_POINT_LABEL_KEY));
            }
            logger.debug("mounting_point_label: "+mounting_point_label);
            
            ConfigurationManager.setProcessPending(Boolean.parseBoolean(prop.getOrDefault(PROCESS_PENDING_KEY, process_pending).toString()));
            logger.debug("process_pending: "+ process_pending);
            
            ConfigurationManager.setDebugMode(Boolean.parseBoolean(prop.getOrDefault(DEBUG_MODE_KEY, debug_mode).toString()));
            logger.debug("debug_mode: "+ debug_mode);

            //Load Command properties
            CreateMgfCommand.getInstance().loadProperties(prop);

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

    //Save all properties and those from specified CreateMgfCommand
    public static void saveProperties(CreateMgfCommand command) {

        Properties prop = new Properties();
        OutputStream output = null;
        
        try {
            output = new FileOutputStream(MzDBUtil.CONFIGURATION_FILE);

            // set the properties value
            prop.setProperty(READ_ONLY_KEY, String.valueOf(ConfigurationManager.isReadOnlyCfg()));
            prop.setProperty(JMS_SERVER_HOST_KEY, ConfigurationManager.getJmsServerHost());
            prop.setProperty(JMS_SERVER_PORT_KEY, String.valueOf(ConfigurationManager.getJmsServerPort()));
            prop.setProperty(SERVICE_REQUEST_QUEUE_NAME_KEY, ConfigurationManager.getServiceRequestQueueName());
            prop.setProperty(DELETE_RAW_KEY, String.valueOf(ConfigurationManager.getDeleteRaw()));
            prop.setProperty(DELETE_MZDB_KEY, String.valueOf(ConfigurationManager.getDeleteMzdb()));
            prop.setProperty(RECURSIVE_WATCHING_KEY, String.valueOf(ConfigurationManager.getRecursive()));
            prop.setProperty(PROCESS_PENDING_KEY, String.valueOf(ConfigurationManager.getProcessPending()));
            prop.setProperty(FULLSCREEN_KEY, String.valueOf(ConfigurationManager.getFullscreen()));
            prop.setProperty(CONVERTER_PATH_KEY, ConfigurationManager.getConverterPath());
            prop.setProperty(CONVERTER_OPTIONS_KEY, ConfigurationManager.getConverterOptions());
            prop.setProperty(MONITOR_PATH_KEY, ConfigurationManager.getMonitorPath());
//            prop.setProperty(MZ_TOLERANCE_KEY, String.valueOf(ConfigurationManager.getMzTolerance()));
//            prop.setProperty(INTENSITY_CUTOFF_KEY, String.valueOf(ConfigurationManager.getIntensityCutoff()));
//            prop.setProperty(PRECURSOR_COMPUTATION_METHOD_KEY, ConfigurationManager.getPrecursorComputationMethod().toString());
            prop.setProperty(PROCESS_GENERATE_MGF_KEY, String.valueOf(ConfigurationManager.getProcessGenerateMgf()));
            prop.setProperty(PROCESS_CONVERT_KEY, String.valueOf(ConfigurationManager.getConvertMzdbOperation()));
            prop.setProperty(PROCESS_UPLOAD_KEY, String.valueOf(ConfigurationManager.getProcessUpload()));
            prop.setProperty(PROCESS_SPLIT_MZDB_KEY, String.valueOf(ConfigurationManager.getProcessSplitMzdb()));
            prop.setProperty(MOUNTING_POINT_LABEL_KEY, ConfigurationManager.getMountingPointLabel());
            prop.setProperty(DEBUG_MODE_KEY, String.valueOf(ConfigurationManager.getDebugMode()));
            prop.putAll(command.getCommandProperties());
//            prop.setProperty(EXPORT_PROLINE_TITLE_KEY, String.valueOf(ConfigurationManager.isProlineTitleExported()));
//            prop.setProperty(PROCESS_MGF_PCLEAN_KEY, String.valueOf(ConfigurationManager.getProcessPClean()));
//            prop.setProperty(PCLEAN_LABEL_METHOD_NAME_KEY, ConfigurationManager.getPCleanLabelMethodName());
//            prop.setProperty(PCLEAN_CONFIG_NAME_KEY, ConfigurationManager.getPCleanConfig() != null ? ConfigurationManager.getPCleanConfig().getConfigCommandValue(): "");

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
        modelData.add(new AttributeEntry("Convert mzDb", String.valueOf(getConvertMzdbOperation())));
        if(getConvertMzdbOperation()){
            modelData.add(new AttributeEntry("Converter", getConverterPath()));
            modelData.add(new AttributeEntry("Converter Options", getConverterOptions()));
        }
        modelData.add(new AttributeEntry("Split Exploris mzDb", String.valueOf(getProcessSplitMzdb())));
        if(getProcessSplitMzdb()){
            modelData.add(new AttributeEntry("Split mzDb extension", FileProcessingExec.SPLIT_SUFFIX));
        }
        modelData.add(new AttributeEntry("Export mgf", String.valueOf(getProcessGenerateMgf())));
        if(getProcessGenerateMgf()){
            Properties p = CreateMgfCommand.getInstance().getCommandProperties();
            for(Map.Entry<Object, Object> nextP : p.entrySet()){
                modelData.add(new AttributeEntry(nextP.getKey().toString(), nextP.getValue().toString()));
            }
//            modelData.add(new AttributeEntry("m/z tolerance", String.valueOf(getMzTolerance())));
//            modelData.add(new AttributeEntry("Intensity cutoff", String.valueOf(getIntensityCutoff())));
//            modelData.add(new AttributeEntry("Precursor m/z computation method", getPrecursorComputationMethod().toString()));
//            modelData.add(new AttributeEntry("Export Proline Title", String.valueOf(isProlineTitleExported())));
//            modelData.add(new AttributeEntry("Apply PClean", String.valueOf(getProcessPClean())));
//            if(getProcessPClean()){
//                modelData.add(new AttributeEntry("PClean Config", String.valueOf(getPCleanConfig().getDisplayValue())));
//                modelData.add(new AttributeEntry("PClean Labelling method", getPCleanLabelMethodName()));
//            }
        }
        modelData.add(new AttributeEntry("Upload", String.valueOf(getProcessUpload())));
        if(getProcessUpload()){
            modelData.add(new AttributeEntry("Host", getJmsServerHost()));
            modelData.add(new AttributeEntry("Mounting Point", getMountingPointLabel()));
        }
        modelData.add(new AttributeEntry("Delete raw", String.valueOf(getDeleteRaw())));
        modelData.add(new AttributeEntry("Delete mzdb", String.valueOf(getDeleteMzdb())));

        return modelData;
    }
    
}
