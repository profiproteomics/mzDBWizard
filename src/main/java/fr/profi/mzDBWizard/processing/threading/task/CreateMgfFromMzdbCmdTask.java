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
import fr.profi.mzDBWizard.processing.CreateMgfCommand;
import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzknife.MzDbProcessing;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * Task to create a mgf file from a mzdb file
 *
 * @author JPM235353
 */
public class CreateMgfFromMzdbCmdTask extends AbstractFileTask {


    public CreateMgfFromMzdbCmdTask(AbstractCallback callback, File mzdbFile) {
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
            String outputFileName = FilenameUtils.removeExtension(mzdbFile.getAbsolutePath())+".mgf";
            CreateMgfCommand.getInstance().getCommand().mzdbFile=mzdbFile.getAbsolutePath();
            CreateMgfCommand.getInstance().getCommand().outputFile=outputFileName;
            String log = " mgf file generated from "+ mzdbFile.getAbsolutePath() +".";
            logger.info(log);
            m_taskInfo.addLog(log);
            MzDbProcessing.mzdbcreateMgf(CreateMgfCommand.getInstance().getCommand());
            return true;
            //m_logs.append(mzdbFile.getAbsolutePath().substring(0, mzdbFile.getAbsolutePath().lastIndexOf(".tmp"))).append(" has just been exported in .mgf format." + "\n");
        } catch (SQLiteException  ex) {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
