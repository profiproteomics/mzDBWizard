package fr.profi.mzDBWizard.processing.threading.task;

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.WorkerPool;
import fr.profi.mzDBWizard.processing.threading.task.callback.SplitExplorisMzdbCallback;
import fr.profi.mzknife.filter.MzDBSplitter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplitExplorisMzdbTask extends AbstractFileTask  {

  List<File> m_outFiles = new ArrayList<>();
  public static final String SUFFIX_FILENAME=".split.mzdb";

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
    logger.info("  -->  Split file "+getFile().getName());
    if(getFile().getName().endsWith(SUFFIX_FILENAME)) {
      m_outFiles.add(getFile());
      setCallbackFiles();
      return true;
    }

    MzDBSplitter splitter = new MzDBSplitter(getFile());
    boolean splitSuccess=  splitter.splitMzDbFile();
    if(!splitSuccess){
      MzDBSplitter.RETURN_CODE returnCode = splitter.getFinishStateCode();
      if(returnCode.equals(MzDBSplitter.RETURN_CODE.NO_CVS) || returnCode.equals(MzDBSplitter.RETURN_CODE.NO_CVS)){
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
      for(File f: m_outFiles){
        logger.info(" Output File: {}", f.getName() );
      }
      setCallbackFiles();
      return true;
    }

  }
}
