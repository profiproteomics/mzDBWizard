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
package fr.profi.mzDBWizard.configuration;

import fr.profi.mzDBWizard.filelookup.WatcherPoolMonitor;
import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 * @author AK249877
 */
public class CurrentExecution {

    private static File m_monitoringDirectory;
    private static ThreadPoolExecutor m_watcherExecutor;
    private static WatcherPoolMonitor m_directoryWatcher;
    private static CurrentExecution m_instance;
    private static Configuration m_configuration;

    public static void initInstance(File monitoringDirectory, Configuration configuration) {
        m_monitoringDirectory = monitoringDirectory;
        m_configuration = configuration;
    }

    public static CurrentExecution getInstance() {
        if (m_instance == null) {
            m_instance = new CurrentExecution();
            m_directoryWatcher = new WatcherPoolMonitor(m_monitoringDirectory, m_watcherExecutor, 10);
            Thread thread = new Thread(m_directoryWatcher);
            thread.start();
        }
        return m_instance;
    }

    private CurrentExecution() {
        m_watcherExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

    public File getMonitoringDirectory() {
        return m_monitoringDirectory;
    }

    public Configuration getConfiguration() {
        return m_configuration;
    }

}
