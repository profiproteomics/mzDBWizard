package fr.profi.mzDBWizard.processing.threading.task.callback;

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.FileProcessingExec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SplitExplorisMzdbCallback extends AbstractCallback {

  private List<File> m_mzdbFiles = new ArrayList<>();

  @Override
  public boolean mustBeCalledInAWT() {
    return false;
  }

  @Override
  public void run(boolean success, long taskId) {
    if (!success || m_mzdbFiles == null || m_mzdbFiles.isEmpty()) {
      m_logger.warn(" Can't Process Split Exploris mzdb Callback ! ");
      return;
    }

    m_logger.info( " - Finish Split Exploris mzdb. ");

    for(File file : m_mzdbFiles) {
      FileProcessingExec.launchMzdbFileTasks(file, true);
    }
  }

  public void setMzdbFiles(List<File> mzdbFiles) {
    m_mzdbFiles = mzdbFiles;
  }

}
