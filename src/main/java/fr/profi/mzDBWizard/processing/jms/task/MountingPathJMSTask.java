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
package fr.profi.mzDBWizard.processing.jms.task;

import com.thetransactioncompany.jsonrpc2.*;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSCallback;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSTask;
import fr.profi.mzDBWizard.processing.jms.queue.AccessJMSManagerThread;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * JMS Task to know the mounting points on the server side
 *
 */
public class MountingPathJMSTask extends AbstractJMSTask {

    private static final String FILESYSTEM_SERVICE_NAME = "proline/misc/FileSystem";
    private static final String METHOD_NAME = "retrieve_all_mount_points";

    public MountingPathJMSTask(AbstractJMSCallback callback) {
        super(callback, new TaskInfo("Retrieve Mounting Points", TaskInfo.MOUNTING_POINT_TASK, false, TaskInfo.VisibilityEnum.VISIBLE_IF_ERROR));
    }


    @Override
    public void taskRun() throws JMSException {

        String log = "Look for mounting paths";
        m_logger.debug(log);
        m_taskInfo.addLog(log);

        final JSONRPC2Request jsonRequest = new JSONRPC2Request(METHOD_NAME, m_taskInfo.getId());


        final TextMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createTextMessage(jsonRequest.toJSONString());

        /* ReplyTo = Temporary Destination Queue for Server -> Client response */
        message.setJMSReplyTo(m_replyQueue);
        message.setStringProperty(ConfigurationManager.getProlineServiceNameKey(), FILESYSTEM_SERVICE_NAME);
        //message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_VERSION_KEY, m_version);
        addDescriptionToMessage(message);

        setTaskInfoRequest(message.getText());

        //  Send the Message
        m_producer.send(message);
        m_logger.info("Message [{}] sent", message.getJMSMessageID());
        m_taskInfo.setJmsMessageID(message.getJMSMessageID());
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {

        final TextMessage textMessage = (TextMessage) jmsMessage;
        final String jsonString = textMessage.getText();

        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
        if (jsonMessage instanceof JSONRPC2Notification) {
            m_logger.warn("JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response");
            throw new Exception("Invalid JSONRPC2Message type");

        } else if (jsonMessage instanceof JSONRPC2Response) {

            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
            m_logger.debug("JSON Response Id: " + jsonResponse.getID());

            final JSONRPC2Error jsonError = jsonResponse.getError();

            if (jsonError != null) {
                m_logger.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                m_logger.error("JSON Throwable", jsonError);
                throw jsonError;
            }

            final Object result = jsonResponse.getResult();
            if (result == null || !ArrayList.class.isInstance(result)) {
                m_logger.debug("Invalid result: No projectId returned");
                throw new Exception("Invalid result " + result);
            } else {
                m_logger.debug("Result :\n" + result);
            }


            ArrayList<String> pathLabels = new ArrayList<String>();

            ArrayList resultList = (ArrayList) result;
            for (int i = 0; i < resultList.size(); i++) {

                Map fileMap = (Map) resultList.get(i);
                String label = (String) fileMap.get("label");
                String directoryType = (String) fileMap.get("directory_type");

                //System.out.println("Label:" + label + " and DirectoryType:" + directoryType);

                if (directoryType.equalsIgnoreCase("mzdb_files")) {
                    pathLabels.add(label);
                }
            }

            ConfigurationManager.setMoutingPointPathLabels(pathLabels);


        }
        m_currentState = JMSState.STATE_DONE;

        String log = "Finished to look for mounting paths";
        m_logger.debug(log);
        m_taskInfo.addLog(log);

    }
}
