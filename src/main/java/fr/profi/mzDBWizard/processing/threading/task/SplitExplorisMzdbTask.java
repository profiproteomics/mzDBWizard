package fr.profi.mzDBWizard.processing.threading.task;

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.FileProcessingExec;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.processing.threading.task.callback.SplitExplorisMzdbCallback;
import fr.profi.mzknife.mzdb.MzDBSplitter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplitExplorisMzdbTask extends AbstractFileTask  {

  List<File> m_outFiles = new ArrayList<>();

  public SplitExplorisMzdbTask(AbstractCallback callback,  File sourceMzdbFile) {
    super(callback, new TaskInfo("Split mzdb "+sourceMzdbFile.getName(), TaskInfo.GENERATE_TASK,true,  TaskInfo.VisibilityEnum.VISIBLE),sourceMzdbFile);
  }

  @Override
  public int getType() {
    return WorkerPool.GENERATE_THREAD;
  }


  private void setCallbackFiles(){
    if(SplitExplorisMzdbCallback.class.isInstance(m_callback)){
      ((SplitExplorisMzdbCallback)m_callback).setMzdbFiles(m_outFiles);
    }
  }


  @Override
  protected boolean runTaskImplementation() throws Exception {
    logger.debug("  -->  Split file "+getFile().getName());

    MzDBSplitter splitter = new MzDBSplitter(getFile());
    splitter.setOutputFileExtension(FileProcessingExec.SPLIT_SUFFIX);
    boolean splitSuccess=  splitter.splitMzDbFile();
    if(!splitSuccess){
      MzDBSplitter.RETURN_CODE returnCode = splitter.getFinishStateCode();
      if(returnCode.equals(MzDBSplitter.RETURN_CODE.NOT_EXPLORIS) || returnCode.equals(MzDBSplitter.RETURN_CODE.NO_CVS)){
        m_taskInfo.addLog("No split done. File is not an Exploris or has no CVs.");
        m_outFiles.add(getFile());
        setCallbackFiles();
        return true;
      }

      m_taskError = new TaskError("Split of mzdb File Failure", "File " + getFile().getAbsolutePath());
      String log2 = "File " + getFile().getAbsolutePath() + " could not be splitted.";
      logger.debug(log2);
      m_taskInfo.addLog(log2);
      setCallbackFiles();
      return false;
    } else {
      m_outFiles = splitter.getOutputMzdbFiles();
      logger.info(" END Splitting {} into {} files : ",  getFile().getName(),m_outFiles.size() );
      StringBuilder msg =  new StringBuilder(" END Splitting ");
      msg.append(getFile().getName()).append(" into ").append(m_outFiles.size()).append("files : ");
      for(File f: m_outFiles){
        logger.info(" Output File: {}", f.getName() );
        msg.append(f.getName()).append(";");
      }

      m_taskInfo.addLog(msg.toString());

      setCallbackFiles();
      return true;
    }

  }
}
