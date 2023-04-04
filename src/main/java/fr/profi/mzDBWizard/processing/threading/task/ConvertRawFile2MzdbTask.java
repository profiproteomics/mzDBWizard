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
package fr.profi.mzDBWizard.processing.threading.task;

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.processing.threading.task.callback.ConvertRawFile2MzdbCallback;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzDBWizard.util.GenericUtil;
import fr.profi.mzDBWizard.util.MzDBUtil;
import fr.profi.mzdb.util.patch.DIAIsolationWindowsPatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * Task to convert a raw file to a mzdb file
 *
 * @author JPM235353
 */
public class ConvertRawFile2MzdbTask extends AbstractFileTask {

    private Process m_conversionProcess = null;

    private boolean m_testMode = false;
    private String m_outputTempFilePath = null;
    private String m_outputMzdbFilePath = null;

    public ConvertRawFile2MzdbTask(AbstractCallback callback, File f) {
        super(callback, new TaskInfo("Convert to mzdb : "+f.getName(), TaskInfo.CONVERTER_TASK, true,  TaskInfo.VisibilityEnum.VISIBLE), f);
    }

    public ConvertRawFile2MzdbTask(AbstractCallback callback, File f, boolean testMode) {
        super(callback, new TaskInfo("Convert to mzdb : "+f.getName(), TaskInfo.CONVERTER_TASK, true,  testMode ? TaskInfo.VisibilityEnum.VISIBLE_IF_ERROR: TaskInfo.VisibilityEnum.VISIBLE), f);
        m_testMode = testMode;
    }

    @Override
    public int getType() {
        return WorkerPool.CONVERTER_THREAD;
    }


    @Override
    public boolean precheck() throws Exception {

        boolean superResult = super.precheck();
        if(!superResult)
            return false;

        if(m_testMode){
            // check that test mzdb file does not exist
            File mzdbFile = new File(MzDBUtil.TEST_MZDB);
            if (mzdbFile!= null && mzdbFile.exists()) {
                m_taskError = new TaskError("Test mzdb file corresponding to " + getFile().getAbsolutePath() + " already exists.");
                return false;
            }
            m_outputMzdbFilePath = mzdbFile.getAbsolutePath();

            // delete mzdb tmp file if necessary
            File tempFile = new File(MzDBUtil.TEST_MZDB_TMP);
            if (tempFile.exists()) {
                FileUtility.forceDeleteFile(tempFile);
            }
            m_outputTempFilePath = tempFile.getAbsolutePath();
        } else {
            // check that corresponding converted file does not exist
            String path = getFile().getAbsolutePath();
            int index = path.lastIndexOf(".");
            String mzdbFilePath = path.substring(0, index + 1) + "mzdb";
            File mzdbFile = new File(mzdbFilePath);
            if (mzdbFile.exists()) {
                m_taskError = new TaskError("Mzdb file corresponding to " + getFile().getAbsolutePath() + " already exists.");
                return false;
            }
            m_outputMzdbFilePath = mzdbFilePath;

            // delete mzdb tmp file if necessary
            String mzdbTmpFilePath = path.substring(0, index + 1) + "mzdb.tmp";
            File tempFile = new File(mzdbTmpFilePath);
            if (tempFile.exists()) {
                FileUtility.forceDeleteFile(tempFile);
            }
            m_outputTempFilePath = mzdbTmpFilePath;
        }

        return true;
    }

    @Override
    protected boolean runTaskImplementation() throws Exception {

        // check that the raw file has been completely copied on the disk
        FileUtility.checkFileFinalization(getFile());

        // check minimum disk space
        if (!checkSufficientDiskSpace()) {
            m_taskError = new TaskError("Insufficient disk space to convert "+ getFile().getAbsolutePath());
            return false;
        }

        // convert file
        if (!convertFile()) {
            return false;
        }

        //
        try {
            while (/*m_run &&*/ m_conversionProcess != null && m_conversionProcess.isAlive()) {
                Thread.sleep(2000);
            }
        } catch (InterruptedException ex) {
            logger.error("File Finalization Interrupted!");
        }

        if (m_conversionProcess != null && m_conversionProcess.exitValue() == 0) {
            File tmpFile = new File(m_outputTempFilePath);
            File mzdbFile = new File(m_outputMzdbFilePath);
            if (tmpFile.exists()) {
                String log = tmpFile.getAbsolutePath() + " size is " + tmpFile.length() + " bytes";
                logger.debug(log);
                m_taskInfo.addLog(log);

                if (!tmpFile.renameTo(mzdbFile)) {
                    m_taskError = new TaskError("Temp File Renaming Failure", "File " + tmpFile.getAbsolutePath() + " could not be renamed.");
                    String log2 = "File " + tmpFile.getAbsolutePath() + " could not be renamed.";
                    logger.debug(log2);
                    m_taskInfo.addLog(log2);

                    return false;
                }
            } else {
                //JPM.WART : raw2mzdb.exe automatically rename .mzdb.tmp file to .mzDB VDS TODO why rename ?
                mzdbFile.renameTo(mzdbFile);
            }

            if (m_callback instanceof ConvertRawFile2MzdbCallback) {
                ((ConvertRawFile2MzdbCallback) m_callback).setRawFile(getFile());
            }

        } else {

            m_taskError = new TaskError("Converter Failure", "Non-zero exit value.");

            String log = "File converter for file " + getFile().getAbsolutePath() + " is not responding.";
            m_taskInfo.addLog(log);
            logger.info(log);

            return false;
        }

        //Apply patch before ending conversion task
        String log = "NO Patching file " + m_outputMzdbFilePath ;
        DIAIsolationWindowsPatch.patchDIAWindows(m_outputMzdbFilePath);
        m_taskInfo.addLog(log);
        logger.info(log);

        log = "Converting for file: " + getFile().getAbsolutePath() + " has come to its end.";
        m_taskInfo.addLog(log);
        logger.info(log);



        return true;
    }

    private boolean convertFile() {

        try {

            String usedConverter;

            String architecture = GenericUtil.getSystemArchitecture();
            if (architecture.contains("64")) {
                List<String> command = new ArrayList<>();
                command.add(ConfigurationManager.getConverterPath());
                if(ConfigurationManager.getConverterOptions() != null && !ConfigurationManager.getConverterOptions().trim().isEmpty()) {
                    String options = ConfigurationManager.getConverterOptions();
                    String[] eachOptions  = options.split(" ");
                    for (int i = 0; i<eachOptions.length; i++){
                        if(!eachOptions[i].trim().isEmpty())
                            command.add(eachOptions[i].trim());
                    }
                }
                command.add("-i");
                command.add(getFile().getAbsolutePath());
                command.add("-o");
                command.add(m_outputTempFilePath);

                m_conversionProcess = new ProcessBuilder().command(command).start();
                usedConverter = ConfigurationManager.getConverterPath();
            } else {
                m_taskError = new TaskError("This installation package is not supported by this processor type. Contact your administrator.");
                return false;
            }

            m_taskInfo.addLog("------------------------------------------------------------------------");
            m_taskInfo.addLog("CONVERSION");
            m_taskInfo.addLog("------------------------------------------------------------------------");
            m_taskInfo.addLog("");

            String log = "Converter: " + usedConverter;
            logger.info(log);
            m_taskInfo.addLog(log);

            InputStream standardOutputStream = m_conversionProcess.getInputStream();
            BufferedReader standardReader = new BufferedReader(new InputStreamReader(standardOutputStream));
            String line;
            while ((line = standardReader.readLine()) != null) {
                logger.info(line);
                m_taskInfo.addLog(line);

            }

            InputStream errorStream = m_conversionProcess.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String errorLine;

            //m_logs.append("\n").append("------------------------------------------------------------------------").append("\n").append("WARNINGS & ERRORS").append("\n").append("------------------------------------------------------------------------").append("\n").append("\n");

            while ((errorLine = errorReader.readLine()) != null) {
                logger.info("Warning:");
                logger.info(errorLine);
                m_taskInfo.addWarning("warning:");
                m_taskInfo.addLog(errorLine);

                //m_logs.append(errorLine).append("\n");
                //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.NON_CRITICAL_ERROR, "Converter ErrorStream", errorLine));
            }

        } catch (IOException ex) {
            m_taskError = new TaskError("Converter faced an IOException during conversion of " + getFile().getAbsolutePath() + ". Check input file's integrity.");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Converter Failure", "Converter faced an IOException. Check input file's integrity."));
            logger.error("File convertion failed!");
            //m_logs.append("File convertion failed!" + "\n");
            return false;
        }

        return true;
    }



    private boolean checkSufficientDiskSpace() {
        return getFile().getUsableSpace() / 1024 / 1024 > AVAILABLE_SPACE_THRESHOLD;
    }
    private static final int AVAILABLE_SPACE_THRESHOLD = 3000;
}
