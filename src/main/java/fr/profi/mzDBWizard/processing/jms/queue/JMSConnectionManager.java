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

import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.swing.event.EventListenerList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VD225637
 */
public class JMSConnectionManager {

    private JMSConnectionManager() {
        m_connectionListenersList = new EventListenerList();
        m_connectionState = ConnectionListener.NOT_CONNECTED;
    }

    // paramaters management

    protected static final Logger m_loggerProline = LoggerFactory.getLogger("mzDB-Task");

    //JMS Constants
    public static final String DEFAULT_SERVICE_REQUEST_QUEUE_NAME = "ProlineServiceRequestQueue";

    public static final String PROLINE_NODE_ID_KEY = "Proline_NodeId";

    public static final String PROLINE_SERVICE_NAME_KEY = "Proline_ServiceName";

    public static final String PROLINE_SERVICE_VERSION_KEY = "Proline_ServiceVersion";

    public static final String PROLINE_SERVICE_SOURCE_KEY = "Proline_ServiceSource";

    public static final String PROLINE_SERVICE_DESCR_KEY = "Proline_ServiceDescription";

    public static final String HORNET_Q_SAVE_STREAM_KEY = "JMS_HQ_SaveStream";

    public static final String HORNET_Q_INPUT_STREAM_KEY = "JMS_HQ_InputStream";

    public static final String JMS_SERVER_HOST_PARAM_KEY = "jms.server.host";

    public static final String JMS_SERVER_PORT_PARAM_KEY = "jms.server.port";

    public static final int JMS_CANCELLED_TASK_ERROR_CODE = -32004;

    public static final int JMS_EXPIRED_MSG_ERROR_CODE = -32003;

    private final EventListenerList m_connectionListenersList;//ConnectionListeners list
    private int m_connectionState;//Connection current state

    public String m_jmsServerHost = null;
    public static int m_jmsServerPort = 5445;

    private Connection m_connection = null;
    private Queue m_serviceQueue = null;
    private Session m_mainSession = null;

    private static JMSConnectionManager m_jmsConnectionManager = null;

    public static synchronized JMSConnectionManager getJMSConnectionManager() {
        if (m_jmsConnectionManager == null) {
            m_jmsConnectionManager = new JMSConnectionManager();
        }
        return m_jmsConnectionManager;
    }


    public int getConnectionState() {
        return m_connectionState;
    }

    private void resetConnObjects() {
        m_connection = null;
        m_serviceQueue = null;
        m_mainSession = null;
        m_connectionState = ConnectionListener.NOT_CONNECTED;
        fireConnectionStateChanged(ConnectionListener.NOT_CONNECTED);
    }

    /**
     * Set the JMS Server Host. Connection and session will be reseted
     *
     * @param jmsHost
     */
    public void setJMSServerHost(String jmsHost) {
        m_jmsServerHost = jmsHost;
        resetConnObjects();
    }

    /**
     * Set the JMS Server Port. Connection and session will be reseted
     *
     * @param jmsPort
     */
    public void setJMSServerPort(int jmsPort) {
        m_jmsServerPort = jmsPort;
        resetConnObjects();
    }

    /**
     * Get Studio JMS Connection. Create one if necessary
     * @return
     * @throws Exception
     */
    public Connection getJMSConnection() throws Exception {

        if (m_connection == null) {
            createConnection();
        }
        return m_connection;
    }

    /**
     * Get Proline Server servuce Queue, create JMS connection if necessary
     * @return
     * @throws Exception
     */
    public Queue getServiceQueue() throws Exception {
        if (m_serviceQueue == null) {
            createConnection();
        }
        return m_serviceQueue;
    }

    private void createConnection() throws JMSException {
        try {
            if (m_jmsServerHost == null) {
                throw new RuntimeException("JMS Host not defined ! ");
            }

            // Step 1. Directly instantiate the JMS Queue object.
            //Get JMS Queue Name from preference
            String queueName = ConfigurationManager.getServiceRequestQueueName();

            m_loggerProline.info(" Use JMS Queure " + queueName);
            m_serviceQueue = HornetQJMSClient.createQueue(queueName);

            // Step 2. Instantiate the TransportConfiguration object which contains the knowledge of what
            // transport to use, the server port etc.
            final Map<String, Object> connectionParams = new HashMap<>();
            /* JMS Server hostname or IP */
            connectionParams.put(TransportConstants.HOST_PROP_NAME, m_jmsServerHost);
            /* JMS port */
            connectionParams.put(TransportConstants.PORT_PROP_NAME, Integer.valueOf(m_jmsServerPort));

            final TransportConfiguration transportConfiguration = new TransportConfiguration(NettyConnectorFactory.class.getName(), connectionParams);

            // Step 3 Directly instantiate the JMS ConnectionFactory object using that TransportConfiguration
            final HornetQConnectionFactory cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
            cf.setReconnectAttempts(10);

            // Step 4.Create a JMS Connection
            m_connection = cf.createConnection();

            // Step 5. Create a JMS Session (Session MUST be confined in current Thread)
            // Not transacted, AUTO_ACKNOWLEDGE
            m_mainSession = m_connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            m_connectionState = ConnectionListener.CONNECTION_DONE;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_DONE);
        } catch (RuntimeException | JMSException je) {
            if (m_connection != null) {
                try {
                    m_connection.close();
                    m_loggerProline.info("JMS Connection closed on error " + je.getMessage());
                } catch (Exception exClose) {
                    m_loggerProline.error("Error closing JMS Connection", exClose);
                } finally {
                    resetConnObjects();
                }
            }
            m_connectionState = ConnectionListener.CONNECTION_FAILED;
            fireConnectionStateChanged(ConnectionListener.CONNECTION_FAILED);
            throw je;
        }
    }

    public void closeConnection() {
        if (m_connection != null) {
            try {

                // need to cleanup jms thread
                AccessJMSManagerThread.getAccessJMSManagerThread().cleanup();
                m_mainSession.close();
                m_connection.close();

                m_loggerProline.info("JMS Connection closed");
            } catch (Exception exClose) {
                m_loggerProline.error("Error closing JMS Connection", exClose);
            } finally {
                m_connection = null;
            }
        }
        resetConnObjects();
        m_jmsServerHost = null;
    }

    //Listener Methods
    public void addConnectionListener(ConnectionListener listener) {
        synchronized (m_connectionListenersList) {
            m_connectionListenersList.add(ConnectionListener.class, listener);
        }
    }

    public void removeConnectionListener(ConnectionListener listener) {
        synchronized (m_connectionListenersList) {
            m_connectionListenersList.remove(ConnectionListener.class, listener);
        }
    }

    protected void fireConnectionStateChanged(int newConnState) {
        synchronized (m_connectionListenersList) {
            ConnectionListener[] allListeners = m_connectionListenersList.getListeners(ConnectionListener.class);
            for (ConnectionListener nextOne : allListeners) {
                nextOne.connectionStateChanged(newConnState);
            }
        }
    }

}
