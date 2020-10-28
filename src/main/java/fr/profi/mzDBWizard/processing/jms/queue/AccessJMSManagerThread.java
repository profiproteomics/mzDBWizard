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
package fr.profi.mzDBWizard.processing.jms.queue;

import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.LinkedList;

/**
 *
 * Management of the task which must be executed in the JMS queue
 *
 * @author VD225637
 */
public class AccessJMSManagerThread extends Thread {

    private static AccessJMSManagerThread m_instance;
    private Connection m_connection;
    private Session m_session;
    private LinkedList<AbstractJMSTask> m_taskList = new LinkedList<>();

    public static AccessJMSManagerThread getAccessJMSManagerThread() {
        if (m_instance == null) {
            m_instance = new AccessJMSManagerThread();
            m_instance.start();
        }
        return m_instance;
    }

    private AccessJMSManagerThread() {
        super("AccessJMSManagerThread"); // useful for debugging
        initSession();
    }

    public Session getSession(){
        return m_session;
    }

    /**
     * Main loop of the thread
     */
    @Override
    public void run() {
        try {
            while (true) {
                AbstractJMSTask task = null;
                synchronized (this) {

                    while (true) {

                        // look for a task to be done
                        if (!m_taskList.isEmpty()) {
                            task = m_taskList.poll();
                            break;
                        }
                        wait();
                    }
                    notifyAll();
                }

                // init session if needed
                initSession();

                // fetch data
                task.askJMS();

            }


        } catch (InterruptedException | JMSException t) {
            LoggerFactory.getLogger("mzDB-Task").debug("Unexpected exception in main loop of AccessServiceThread", t);
            m_instance = null; // reset thread
        }
    }


    private void initSession() {
        if (m_connection == null) {
            try {
                // Get JMS Connection
                m_connection = JMSConnectionManager.getJMSConnectionManager().getJMSConnection(); // JMSConnection.getInstance(ConfigurationManager.getJmsServerHost(), ConfigurationManager.getJmsServerPort()).getConnection();  //
                m_connection.start(); // Explicitely start connection to begin Consumer reception
                m_session = m_connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            } catch (JMSException je) {
                LoggerFactory.getLogger("mzDB-Task").error("Unexpected exception when initializing JMS Connection", je);
            } catch (Exception e) {
                LoggerFactory.getLogger("mzDB-Task").error("Unexpected exception when initializing JMS Connection", e);
            }
        }
    }

    /**
     * Add a task to be done
     *
     * @param task
     */
    public final void addTask(AbstractJMSTask task) {

        // TaskInfoManager.getTaskInfoManager().add(task.getTaskInfo()); //JPM.TODO
        // task is queued
        synchronized (this) {
            m_taskList.add(task);
            notifyAll();
        }
    }

    public void cleanup() {
        if (m_session != null) {
            synchronized (this) {
                try {
                    m_session.close();

                    m_taskList.clear();
                } catch (Exception e) {

                } finally {
                    m_session = null;
                    m_connection = null;
                }
            }
        }

    }
}
