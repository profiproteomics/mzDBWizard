package fr.profi.mzDBWizard.processing.threading;

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.filelookup.WatcherExecution;
import fr.profi.mzDBWizard.processing.threading.queue.TaskManagerThread;
import fr.profi.mzDBWizard.processing.threading.task.*;
import fr.profi.mzDBWizard.processing.threading.task.callback.ConvertRawFile2MzdbCallback;
import fr.profi.mzDBWizard.processing.threading.task.callback.GenerateMgfFromMzdbCallback;
import fr.profi.mzDBWizard.processing.threading.task.callback.SplitExplorisMzdbCallback;
import fr.profi.mzDBWizard.processing.threading.task.callback.UploadMzdbCallback;

import java.io.File;

public class FileProcessingExec {

  public  static final String MZDB_SUFFIX=".mzdb";
  public  static final String MGF_SUFFIX=".mgf";
  public  static final String RAW_SUFFIX=".raw";
  public  static final String WIFF_SUFFIX=".wiff";
  public  static final String SPLIT_SUFFIX=".split.mzdb";

  public static void launchRawFileTasks(File rawFile){
    if (ConfigurationManager.getConvertMzdbOperation()) {
      TaskManagerThread.getTaskManagerThread().addTask(new ConvertRawFile2MzdbTask(new ConvertRawFile2MzdbCallback(), rawFile));
    }
  }


  public static void launchMzdbFileTasks(File mzdbFile){
    FileProcessingExec.launchMzdbFileTasks(mzdbFile,false);
  }

  public static void launchMzdbFileTasks(File mzdbFile, boolean fromSplitTask) {

    if(!fromSplitTask && mzdbFile.getName().endsWith(SPLIT_SUFFIX)) {
      fromSplitTask=true;
    }

    if (ConfigurationManager.getSplitMzdbOperation() && !fromSplitTask) {
      SplitExplorisMzdbCallback callback = new SplitExplorisMzdbCallback();
      TaskManagerThread.getTaskManagerThread().addTask(new SplitExplorisMzdbTask(callback, mzdbFile));

    }
    else if (ConfigurationManager.getGenerateMgfOperation()) {
      GenerateMgfFromMzdbCallback callback = new GenerateMgfFromMzdbCallback();
      TaskManagerThread.getTaskManagerThread().addTask(new GenerateMgfFromMzdbTask(callback, mzdbFile));
      callback.setMzdbFile(mzdbFile);

    }
    else if (ConfigurationManager.getUploadOperation()) {
      UploadMzdbCallback callback = new UploadMzdbCallback();
      callback.setMzdbFile(mzdbFile);
      TaskManagerThread.getTaskManagerThread().addTask(new UploadMzdbTask(callback, mzdbFile, WatcherExecution.getInstance().getMonitoringDirectory(), ConfigurationManager.getMountingPointLabel()));

    }
    else if (ConfigurationManager.getDeleteMzdb()) {
      TaskManagerThread.getTaskManagerThread().addTask(new DeleteFileTask(null, mzdbFile));
    }
  }

}
