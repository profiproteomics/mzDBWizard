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
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.AbstractTask;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzdb.io.writer.mgf.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * Task to create a mgf file from a mzdb file
 *
 * @author JPM235353
 */
public class GenerateMgfFromMzdbTask extends AbstractFileTask {

    public GenerateMgfFromMzdbTask(AbstractCallback callback, File mzdbFile) {
        super(callback, new TaskInfo("Generate mgf from  "+mzdbFile.getName(), TaskInfo.GENERATE_TASK,true,  TaskInfo.VisibilityEnum.VISIBLE), mzdbFile);
    }

    @Override
    public int getType() {
        return WorkerPool.GENERATE_THREAD;
    }

    @Override
    protected boolean runTaskImplementation() throws Exception {

        logger.info("  -->  Generate MGF file from "+ getFile().getName());
        // check that the source file has been completely copied on the disk
        FileUtility.checkFileFinalization(getFile());

        String log = "Starting to generate " + getFile().getAbsolutePath() + " to .mgf format.";
        logger.info(log);
        m_taskInfo.addLog(log);

        return generateMgf(getFile());

    }


    private boolean generateMgf(File mzdbFile) {
        try {
            MgfWriter writer = new MgfWriter(mzdbFile.getAbsolutePath());
            writer.write(mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", getPrecursorComputer(), ConfigurationManager.getIntensityCutoff(), true);
            writer.getMzDbReader().close();

            String log = " mgf file generated from "+ mzdbFile.getAbsolutePath() +".";
            logger.info(log);
            m_taskInfo.addLog(log);

            return true;
            //m_logs.append(mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".tmp"))).append(" has just been exported in .mgf format." + "\n");
        } catch (SQLiteException | ClassNotFoundException ex) {
            m_taskError = new TaskError("Mgf generate Failure", "SQLiteException or ClassNotFoundException while generating mgf file");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "SQLiteException or ClassNotFoundException while exporting mgf file"));
            logger.error("SQLiteException or ClassNotFoundException while generating mgf file", ex);
        } catch (FileNotFoundException ex) {
            m_taskError = new TaskError("Mgf Generate Failure", "Generation faced an IOException. Check input file's integrity.");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "Converter faced an IOException. Check input file's integrity."));
            logger.error("FileNotFoundException while generating mgf file : " + mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", ex);
        } catch (IOException ex) {
            m_taskError = new TaskError("Mgf Generate Failure", "Generation faced an IOException. Check input file's integrity.");
            //m_errorList.add(new ExecutionError(ExecutionError.ErrorClass.CRITICAL_ERROR, "Mgf Export Failure", "Converter faced an IOException. Check input file's integrity."));
            logger.error("IOException while generating mgf file : " + mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".")) + ".mgf", ex);
        }
        return false;
    }

    private IPrecursorComputation getPrecursorComputer() {

        IPrecursorComputation precComp = null;

        if (ConfigurationManager.getPrecursorComputationMethod().equals(ConfigurationManager.PrecursorComputationMethod.PROLINE_REFINED_PRECURSOR_MZ)) {
            precComp = new IsolationWindowPrecursorExtractor(ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(ConfigurationManager.PrecursorComputationMethod.THERMO_REFINED_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.REFINED_THERMO, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(ConfigurationManager.PrecursorComputationMethod.MZDB_ACCESS_REFINED_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.REFINED, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(ConfigurationManager.PrecursorComputationMethod.SELECTED_ION_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.SELECTED_ION_MZ, ConfigurationManager.getMzTolerance());
        } else if (ConfigurationManager.getPrecursorComputationMethod().equals(ConfigurationManager.PrecursorComputationMethod.MAIN_PRECURSOR_MZ)) {
            precComp = new DefaultPrecursorComputer(PrecursorMzComputationEnum.MAIN_PRECURSOR_MZ, ConfigurationManager.getMzTolerance());
        }

        return precComp;
    }
}
