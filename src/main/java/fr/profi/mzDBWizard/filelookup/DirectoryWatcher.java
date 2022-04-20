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

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.threading.FileProcessingExec;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.*;

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


    public DirectoryWatcher() throws IOException {
        m_watcher = FileSystems.getDefault().newWatchService();
        m_keys = new HashMap<>();

        m_trace = ConfigurationManager.getProcessPending();


        if (ConfigurationManager.getRecursive()) {
            registerDirectoriesRecursively(WatcherExecution.getInstance().getMonitoringDirectory().toPath());
        } else {
            registerSingleDirectory(WatcherExecution.getInstance().getMonitoringDirectory().toPath());
        }
        
        m_trace = true;

    }


    private void registerSingleDirectory(Path dir) throws IOException {
        WatchKey key = dir.register(m_watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        if (m_trace) {
            Path prev = m_keys.get(key);
            if (prev == null) {

                File[] listOfFiles = dir.toFile().listFiles();

                if(listOfFiles!=null) {
                    logger.debug("Directory " + dir.toString() + " was registered. The folder contains " + listOfFiles.length + " files.");

                    for (int i = 0; i < listOfFiles.length; i++) {
                        File f = listOfFiles[i];
                        if (f.isFile()) {

                            String lowerPath = f.getAbsolutePath().toLowerCase();
                            if (lowerPath.endsWith(FileProcessingExec.RAW_SUFFIX) || lowerPath.endsWith(FileProcessingExec.WIFF_SUFFIX)) {
                                // we have a raw file
                                FileProcessingExec.launchRawFileTasks(f);

                            } else if (lowerPath.endsWith(FileProcessingExec.MZDB_SUFFIX)) {
                                // we have a mzdb file
                                FileProcessingExec.launchMzdbFileTasks(f);
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
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
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

                    if (!fileNameLowerCase.endsWith(FileProcessingExec.RAW_SUFFIX) && !fileNameLowerCase.endsWith(FileProcessingExec.WIFF_SUFFIX) && !fileNameLowerCase.endsWith(FileProcessingExec.MZDB_SUFFIX)) {
                        continue;
                    }

                    File f = new File(fileName);
                    if (kind == ENTRY_CREATE) {
                        if (fileNameLowerCase.endsWith(FileProcessingExec.RAW_SUFFIX) || fileNameLowerCase.endsWith(FileProcessingExec.WIFF_SUFFIX)) {
                            // we have a raw file
                            FileProcessingExec.launchRawFileTasks(f);
                        } else if (fileNameLowerCase.endsWith(FileProcessingExec.MZDB_SUFFIX)) {
                            // we have a mzdb file
                            FileProcessingExec.launchMzdbFileTasks(f);
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
                logger.warn("InterruptedException during Directory watching");
            }

        }
    }

    @Override
    public void run() {
        watch();
    }

}
