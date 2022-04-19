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
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSCallback;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSTask;
import fr.profi.mzDBWizard.processing.jms.queue.AccessJMSManagerThread;
import fr.profi.mzDBWizard.processing.jms.queue.JMSConnectionManager;
import org.apache.commons.io.FilenameUtils;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.*;
import java.nio.file.Path;

/**
 *
 * JMS Task to upload a file to the server
 *
 */
public class UploadFileJMSTask extends AbstractJMSTask {

    private static final String SERVICE_NAME = "proline/misc/FileUpload";


    private File m_file2Upload;
    private final Path m_monitorDirectoryPath;
    private final String m_pathLabel;


    public UploadFileJMSTask(AbstractJMSCallback callback, File file2Upload, Path monitorDirectoryPath, String pathLabel) {
        super(callback, new TaskInfo("Upload File", TaskInfo.UPLOAD_TASK, false, TaskInfo.VisibilityEnum.HIDDEN));

        m_file2Upload = file2Upload;
        m_monitorDirectoryPath = monitorDirectoryPath;
        m_pathLabel = pathLabel;
    }


    @Override
    public void taskRun() throws JMSException {
        InputStream in = null;
        try {
            final BytesMessage message = AccessJMSManagerThread.getAccessJMSManagerThread().getSession().createBytesMessage();

            File uploadFile = m_file2Upload;

            String log = "Prepare to upload mzDB file " + m_file2Upload.getAbsolutePath();
            m_logger.debug(log);
            m_taskInfo.addLog(log);



            // Upload File on server side
            message.setJMSReplyTo(m_replyQueue);
            message.setStringProperty(JMSConnectionManager.PROLINE_SERVICE_NAME_KEY, SERVICE_NAME);

            //JPM.WART : rename file extension : we always want .mzdb (and not .mzDB)
            String destFileName = uploadFile.getAbsolutePath();
            if (destFileName.endsWith(".mzDB")) {
                destFileName = destFileName.substring(0,destFileName.length()-2)+"db";
            }

            message.setStringProperty("dest_file_name", FilenameUtils.getName(destFileName));

            //this needs checking!
            String destFolderPath = m_pathLabel + File.separator + destFileName.substring(destFileName.indexOf(m_monitorDirectoryPath.toAbsolutePath().toString()) + m_monitorDirectoryPath.toAbsolutePath().toString().length(), destFileName.lastIndexOf(FilenameUtils.getName(destFileName)));
            message.setStringProperty("dest_folder_path", destFolderPath);


            addSourceToMessage(message);
            addDescriptionToMessage(message);

            in = new FileInputStream(uploadFile);
            BufferedInputStream inBuf = new BufferedInputStream(in);
            message.setObjectProperty(JMSConnectionManager.HORNET_Q_INPUT_STREAM_KEY, inBuf);
            setTaskInfoRequest("Call Service proline/misc/FileUpload with dest_file_name " + uploadFile.getName());
            //  Send the Message
            m_producer.send(message);


            m_logger.info("Message [{}] sent", message.getJMSMessageID());
            m_taskInfo.addLog("Message ["+message.getJMSMessageID()+"] sent");

            m_taskInfo.setJmsMessageID(message.getJMSMessageID());
        } catch (FileNotFoundException ex) {
            throw new JMSException(ex.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void taskDone(Message jmsMessage) throws Exception {

            final TextMessage textMessage = (TextMessage) jmsMessage;
            final String jsonString = textMessage.getText();

            final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);
            if (jsonMessage instanceof JSONRPC2Notification) {
                String log = "JSON Notification method: " + ((JSONRPC2Notification) jsonMessage).getMethod() + " instead of JSON Response";
                m_logger.warn(log);
                m_taskInfo.addWarning(log);
                throw new Exception("Invalid JSONRPC2Message type");

            } else if (jsonMessage instanceof JSONRPC2Response) {
                final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;
                m_logger.debug("JSON Response Id: " + jsonResponse.getID());

                final JSONRPC2Error jsonError = jsonResponse.getError();

                if (jsonError != null) {
                    m_logger.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                    m_logger.error("JSON Throwable", jsonError);
                    m_taskInfo.addWarning("JSON Error code "+ jsonError.getMessage()+", message : \"+jsonError.getCode()+\"");
                    throw jsonError;
                }

                final Object result = jsonResponse.getResult();
                if ((result == null) || (!String.class.isInstance(result))) {
                    m_logger.error(getClass().getSimpleName() + " failed : No returned values");
                    throw new Exception("Invalid result " + result);
                }

                String log = "Uploading for file: " + m_file2Upload.getAbsolutePath() + " has come to its end.";
                m_logger.info(log);
                m_taskInfo.addLog(log);

            }

            m_currentState = JMSState.STATE_DONE;

    }


}
