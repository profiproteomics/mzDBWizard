package fr.profi.mzDBWizard.processing.threading.task;

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.AbstractTask;

import java.io.File;

public abstract class AbstractFileTask extends AbstractTask {

  private File m_file;

  public AbstractFileTask(AbstractCallback callback, TaskInfo taskInfo, File file) {
    super(callback,taskInfo);
    m_file = file;
  }

  protected abstract boolean runTaskImplementation() throws Exception;

  public File getFile(){
    return m_file;
  }

  @Override
  public String getUniqueKey(){
    return m_file.getName().toLowerCase();
  }


  @Override
  public boolean precheck() throws Exception {
    boolean result = true;
    if(m_file == null ) {
      m_taskError = new TaskError("File is not defined.");
      result = false;

    } else if( !m_file.exists()) {
      m_taskError = new TaskError("File "+ m_file.getAbsolutePath()+" does not exist.");
      result = false;
    }

    return result;
  }

  @Override
  public boolean runTask() {
    try {
      if (!precheck()) {
        return false;
      }

      return runTaskImplementation();
    } catch (Exception e) {
      m_taskError = new TaskError(e);
      return false;
    }
  }
}
