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

import fr.profi.mzDBWizard.util.FileUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author AK249877
 */
public class WatcherPoolMonitor implements Runnable {

    private boolean m_run = false;
    private final int m_delayInSeconds;
    private final File m_lockFile, m_monitoredDirectory;
    private final ThreadPoolExecutor m_executor;
    private final ArrayList<Long> m_dispatchTimes;
    private final org.slf4j.Logger m_logger = LoggerFactory.getLogger(getClass().toString());
    private static DirectoryWatcher m_directoryWatcher;

    public WatcherPoolMonitor(File monitoredDirectory, ThreadPoolExecutor executor, int delayInSeconds) {

        m_monitoredDirectory = monitoredDirectory;

        m_executor = executor;

        m_delayInSeconds = delayInSeconds;

        m_lockFile = new File(monitoredDirectory.getAbsolutePath() + File.separator + "mzdb.lock");

        m_dispatchTimes = new ArrayList<>();
    }

    @Override
    public void run() {
        m_run = true;

        while (m_run) {

            try {
                FileUtility.touch(m_lockFile); //VDS What for ? //JPM what for ? :)
                //AK it serves a mechanism that makes sure that you do not have multiple instances of mzDBWizard running watching the same monitored directory
            } catch (IOException ex) {
                Logger.getLogger(WatcherPoolMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (m_executor.getActiveCount() == 0) {

                m_logger.debug("Dispatching a watcher service for directory " + m_monitoredDirectory.getAbsolutePath());
                try {
                    m_directoryWatcher = new DirectoryWatcher();
                    m_executor.submit(m_directoryWatcher);
                    m_dispatchTimes.add(System.currentTimeMillis());
                } catch (IOException ex) {
                    Logger.getLogger(WatcherPoolMonitor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            try {
                Thread.sleep(m_delayInSeconds * 1000);
            } catch (InterruptedException e) {
                m_logger.error("Interrupted!", e);
                m_run = false;
                return;
            }



        }

        m_run = false;
    }


}
