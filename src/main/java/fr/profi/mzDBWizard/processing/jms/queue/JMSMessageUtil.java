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

import com.thetransactioncompany.jsonrpc2.*;
import fr.profi.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author VD225637
 */
public class JMSMessageUtil {
    protected static final Logger m_loggerProline = LoggerFactory.getLogger("mzDB-Task");
    private static final String TAB = "    ";
    private static final String DATE_FORMAT = "%td/%<tm/%<tY %<tH:%<tM:%<tS.%<tL";
    private static final int MESSAGE_BUFFER_SIZE = 2048;


    /**
     * Formats some Header filds, Properties and Body of the given JMS Message to print usefull debug info.
     */
    public static String formatMessage(final Message message) {

        if (message == null) {
            throw new IllegalArgumentException("Message is null");
        }

        final StringBuilder buff = new StringBuilder(MESSAGE_BUFFER_SIZE);

        try {
            buff.append(message.getClass().getName()).append("  ").append(message.getJMSMessageID());
            buff.append(StringUtils.LINE_SEPARATOR);

            buff.append(TAB).append("JMSCorrelationID ");
            append(buff, message.getJMSCorrelationID());
            buff.append(StringUtils.LINE_SEPARATOR);

            buff.append(TAB).append("JMSTimestamp ")
                    .append(String.format(DATE_FORMAT, message.getJMSTimestamp()));
            buff.append(StringUtils.LINE_SEPARATOR);

            buff.append(TAB).append("JMSDestination ");
            append(buff, message.getJMSDestination());
            buff.append(StringUtils.LINE_SEPARATOR);

            buff.append(TAB).append("JMSReplyTo ");
            append(buff, message.getJMSReplyTo());
            buff.append(StringUtils.LINE_SEPARATOR);

            final Enumeration<String> nameEnum = message.getPropertyNames();

            while (nameEnum.hasMoreElements()) {
                final String propertyName = nameEnum.nextElement();
                buff.append(TAB).append('[').append(propertyName).append("] : ");

                final String propertyValue = message.getStringProperty(propertyName);

                if (propertyValue == null) {
                    buff.append("NULL");
                } else {
                    buff.append('[').append(propertyValue).append(']');
                }

                buff.append(StringUtils.LINE_SEPARATOR);
            }

            if (message instanceof TextMessage) {
                buff.append(TAB).append(((TextMessage) message).getText());
            }

            buff.append(StringUtils.LINE_SEPARATOR);
        } catch (Exception ex) {
            m_loggerProline.error("Error retrieving JMS Message header or content", ex);
        }

        return buff.toString();
    }



    private static void append(final StringBuilder sb, final Object obj) {
        assert (sb != null) : "append() sb is null";

        if (obj == null) {
            sb.append("NULL");
        } else {
            sb.append(obj);
        }

    }

    public static void traceJSONResponse(final String jsonString) throws JSONRPC2ParseException {
        final JSONRPC2Message jsonMessage = JSONRPC2Message.parse(jsonString);

        if (jsonMessage instanceof JSONRPC2Notification) {
            final JSONRPC2Notification jsonNotification = (JSONRPC2Notification) jsonMessage;

            m_loggerProline.debug("JSON Notification method: " + jsonNotification.getMethod());

            final Map<String, Object> namedParams = jsonNotification.getNamedParams();

            if ((namedParams != null) && !namedParams.isEmpty()) {
                final StringBuilder buff = new StringBuilder("Params: ");

                boolean first = true;

                final Set<Map.Entry<String, Object>> entries = namedParams.entrySet();

                for (final Map.Entry<String, Object> entry : entries) {

                    if (first) {
                        first = false;
                    } else {
                        buff.append(" | ");
                    }

                    buff.append(entry.getKey());
                    buff.append(" : ").append(entry.getValue());
                }

                m_loggerProline.debug(buff.toString());
            }

        } else if (jsonMessage instanceof JSONRPC2Response) {
            final JSONRPC2Response jsonResponse = (JSONRPC2Response) jsonMessage;

            m_loggerProline.debug("JSON Response Id: " + jsonResponse.getID());

            final JSONRPC2Error jsonError = jsonResponse.getError();

            if (jsonError != null) {
                m_loggerProline.error("JSON Error code {}, message : \"{}\"", jsonError.getCode(), jsonError.getMessage());
                m_loggerProline.error("JSON Throwable", jsonError);
            }

            final Object result = jsonResponse.getResult();

            if (result == null) {
                m_loggerProline.debug("No result");
            } else {
                m_loggerProline.debug("Result :\n" + result);
            }

        }

    }
}
