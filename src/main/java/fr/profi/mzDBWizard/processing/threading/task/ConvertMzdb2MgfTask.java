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

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzDBWizard.configuration.Configuration;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.AbstractTask;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzdb.io.writer.mgf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * Task to create a mgf file from a mzdb file
 *
 * @author JPM235353
 */
public class ConvertMzdb2MgfTask extends AbstractTask {

    private final Logger m_logger = LoggerFactory.getLogger(getClass().toString());

    private File m_mzdbFile;

    public ConvertMzdb2MgfTask(AbstractCallback callback, File mzdbFile) {
        super(callback, new TaskInfo("Convert to mgf : "+mzdbFile.getName(), TaskInfo.CONVERTER_TASK,true, null, TaskInfo.VisibilityEnum.VISIBLE));

        m_mzdbFile = mzdbFile;
    }

    public String getUniqueKey() {
        return m_mzdbFile.getName().toLowerCase();
    }

    @Override
    public int getType() {
        return WorkerPool.CONVERTER_THREAD;
    }

    @Override
    public boolean precheck() {
        //JPM.TODO
        // return  (FileUtility.verifyMzdbFile(m_file))
        return true;
    }

    @Override
    public boolean runTask() {

        try {
            return runTaskImplementation();
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }
    }
    private boolean runTaskImplementation() throws Exception {
        if (!precheck()) {
            return false;
        }

        // check that the raw file has been completely copied on the disk
        FileUtility.checkFileFinalization(m_mzdbFile);

        String log = "Starting to convert " + m_mzdbFile.getAbsolutePath() + " to .mgf format.";
        m_logger.info(log);
        m_taskInfo.addLog(log);

        return exportMgf(m_mzdbFile);

    }


    private boolean exportMgf(File mzdbFile) {
        try {
            MgfWriter writer = new MgfWriter(mzdbFile.getAbsolutePath());
            writer.write(mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", getPrecursorComputer(), ConfigurationManager.getIntensityCutoff(), true);
            writer.getMzDbReader().close();

            String log = mzdbFile.getAbsolutePath() + " has just been exported in .mgf format.";
            m_logger.info(log);
            m_taskInfo.addLog(log);

            return true;
            //m_logs.append(mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".tmp"))).append(" has just been exported in .mgf format." + "\n");
        } catch (SQLiteException | ClassNotFoundException ex) {
            m_taskError = new TaskError("Mgf Export Failure", "SQLiteException or ClassNotFoundException while exporting mgf file");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "SQLiteException or ClassNotFoundException while exporting mgf file"));
            m_logger.error("SQLiteException or ClassNotFoundException while exporting mgf file", ex);
        } catch (FileNotFoundException ex) {
            m_taskError = new TaskError("Mgf Export Failure", "Converter faced an IOException. Check input file's integrity.");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "Converter faced an IOException. Check input file's integrity."));
            m_logger.error("FileNotFoundException while exporting mgf file : " + mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", ex);
        } catch (IOException ex) {
            m_taskError = new TaskError("Mgf Export Failure", "Converter faced an IOException. Check input file's integrity.");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "Converter faced an IOException. Check input file's integrity."));
            m_logger.error("IOException while exporting mgf file : " + mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", ex);
        }
        return false;
    }

    private IPrecursorComputation getPrecursorComputer() {

        IPrecursorComputation precComp = null;

        if (ConfigurationManager.getPrecursorComputationMethod().equals(Configuration.PrecursorComputationMethod.PROLINE_REFINED_PRECURSOR_MZ)) {
            precComp = new IsolationWindowPrecursorExtractor(ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(Configuration.PrecursorComputationMethod.THERMO_REFINED_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.REFINED_THERMO, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(Configuration.PrecursorComputationMethod.MZDB_ACCESS_REFINED_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.REFINED, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(Configuration.PrecursorComputationMethod.SELECTED_ION_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.SELECTED_ION_MZ, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(Configuration.PrecursorComputationMethod.MAIN_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.MAIN_PRECURSOR_MZ, ConfigurationManager.getMzTolerance());
        }

        return precComp;
    }
}
