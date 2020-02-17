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
package fr.profi.mzDBWizard.filelookup;

 import fr.profi.mzDBWizard.configuration.CurrentExecution;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import fr.profi.mzDBWizard.processing.threading.task.ConvertMzdb2MgfTask;
import fr.profi.mzDBWizard.processing.threading.task.DeleteFileTask;
import fr.profi.mzDBWizard.processing.threading.task.UploadMzdbTask;
import fr.profi.mzDBWizard.processing.threading.task.callback.ConvertMzdb2MgfCallback;
import fr.profi.mzDBWizard.processing.threading.task.callback.ConvertRawFile2MzdbCallback;
import fr.profi.mzDBWizard.processing.threading.task.ConvertRawFile2MzdbTask;
import fr.profi.mzDBWizard.processing.threading.task.callback.DeleteFileCallback;
import fr.profi.mzDBWizard.processing.threading.task.callback.UploadMzdbCallback;
import fr.profi.mzDBWizard.processing.threading.queue.TaskManagerThread;
import org.slf4j.LoggerFactory;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import java.io.File;

/**
 *
 *  Class used to look up for new file in the selected directory and potentially sub-directories.
 *  We are interested in raw and mzdb files.
 *
 * @author AK249877
 */
public class DirectoryWatcher implements Runnable  {

    private final WatchService m_watcher;

    private final Map<WatchKey, Path> m_keys;

    private boolean m_trace;

    private boolean m_run = true;


    private final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass().toString());


    private final CurrentExecution m_executionSession;

    public DirectoryWatcher() throws IOException {
        m_executionSession = CurrentExecution.getInstance();

        m_watcher = FileSystems.getDefault().newWatchService();
        m_keys = new HashMap<>();

        m_trace = m_executionSession.getConfiguration().getProcessPending();


        if (ConfigurationManager.getRecursive()) {
            registerDirectoriesRecursively(m_executionSession.getMonitoringDirectory().toPath());
        } else {
            registerSingleDirectory(m_executionSession.getMonitoringDirectory().toPath());
        }
        
        m_trace = true;

    }

    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    private void registerSingleDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(m_watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (m_trace) {
            Path prev = m_keys.get(key);
            if (prev == null) {

                File[] listOfFiles = dir.toFile().listFiles();

                logger.debug("Directory " + dir.toString() + " was registered. The folder contains " + listOfFiles.length + " files.");

                for (int i = 0; i < listOfFiles.length; i++) {
                    File f = listOfFiles[i];
                    if (f.isFile()) {

                        String lowerPath = f.getAbsolutePath().toLowerCase();
                        if (lowerPath.endsWith(".raw") || lowerPath.endsWith(".wiff")) {
                            // we have a raw file
                            if (ConfigurationManager.getConvertOperation()) {
                                TaskManagerThread.getTaskManagerThread().addTask(new ConvertRawFile2MzdbTask(new ConvertRawFile2MzdbCallback(), f));

                            }
                        } else if (lowerPath.endsWith(".mzdb")) {
                            // we have a mzdb file

                            if (ConfigurationManager.getMgfOperation()) {
                                ConvertMzdb2MgfCallback callback = new ConvertMzdb2MgfCallback();
                                TaskManagerThread.getTaskManagerThread().addTask(new ConvertMzdb2MgfTask(callback, f));
                                callback.setMzdbFile(f);

                            }
                            else if (ConfigurationManager.getUploadOperation()) {
                                UploadMzdbCallback callback = new UploadMzdbCallback();
                                callback.setMzdbFile(f);
                                TaskManagerThread.getTaskManagerThread().addTask(new UploadMzdbTask(callback, f, getPath(), ConfigurationManager.getMountingPointLabel()));

                            }
                            else if (ConfigurationManager.getDeleteMzdb()) {
                                TaskManagerThread.getTaskManagerThread().addTask(new DeleteFileTask(new DeleteFileCallback(), f));
                            }
                        }


                    }
                }

            }
        }
        m_keys.put(key, dir);
    }

    private void registerDirectoriesRecursively(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerSingleDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    void watch() {
        while (m_run) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = m_watcher.take();
            } catch (InterruptedException x) {
                m_run = false;
                return;
            }

            Path dir = m_keys.get(key);
            if (dir == null) {
                logger.error("WatchKey: " + key.toString() + " was not recognized!");
                continue;
            }


            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                    continue;
                }

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                if (kind == ENTRY_CREATE) {
                    logger.info(event.kind().name() + " : " + child.toString());
                }

                if (Files.isDirectory(child, NOFOLLOW_LINKS)) {

                    if (ConfigurationManager.getRecursive()) {
                        if (kind == ENTRY_CREATE) {
                            try {
                                registerDirectoriesRecursively(child);
                            } catch (IOException ex) {
                                logger.error("IOException on recursive directory registering!", ex);
                            }
                        }
                    } else {
                        try {
                            registerSingleDirectory(child);
                        } catch (IOException ex) {
                            logger.error("IOException on single directory registering!", ex);
                        }
                    }
                } else {

                    String fileName = child.toString();
                    String fileNameLowerCase = fileName.toLowerCase();

                    if (!fileNameLowerCase.endsWith(".raw") && !fileNameLowerCase.endsWith(".wiff") && !fileNameLowerCase.endsWith(".mzdb")) {
                        continue;
                    }

                    File f = new File(fileName);
                    if (kind == ENTRY_CREATE) {

                            if (fileNameLowerCase.endsWith(".raw") || fileNameLowerCase.endsWith(".wiff")) {
                                // we have a raw file

                                if (ConfigurationManager.getConvertOperation()) {
                                    TaskManagerThread.getTaskManagerThread().addTask(new ConvertRawFile2MzdbTask(new ConvertRawFile2MzdbCallback(), f));
                                }
                            } else if (fileNameLowerCase.endsWith(".mzdb")) {
                                // we have a mzdb file
                                if (ConfigurationManager.getMgfOperation()) {
                                    ConvertMzdb2MgfCallback callback = new ConvertMzdb2MgfCallback();
                                    TaskManagerThread.getTaskManagerThread().addTask(new ConvertMzdb2MgfTask(callback, f));
                                    callback.setMzdbFile(f);
                                }
                                else if (ConfigurationManager.getUploadOperation()) {
                                    UploadMzdbCallback callback = new UploadMzdbCallback();
                                    callback.setMzdbFile(f);
                                    TaskManagerThread.getTaskManagerThread().addTask(new UploadMzdbTask(callback, f, getPath(), ConfigurationManager.getMountingPointLabel()));
                                }
                                else if (ConfigurationManager.getDeleteMzdb()) {
                                    TaskManagerThread.getTaskManagerThread().addTask(new DeleteFileTask(new DeleteFileCallback(), f));
                                }
                            }



                        

                    }

                }

            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                m_keys.remove(key);

                // all directories are inaccessible
                if (m_keys.isEmpty()) {
                    m_run = false;
                    break;
                }
            }

            try {
                // force that events are grouped
                Thread.sleep(1000);
            } catch (InterruptedException ie) {

            }

        }
    }

    @Override
    public void run() {
        watch();
    }


    public Path getPath() {
        return m_executionSession.getMonitoringDirectory().toPath();
    }

}
