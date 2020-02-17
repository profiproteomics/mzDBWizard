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
package fr.profi.mzDBWizard.gui.log;

/**
 *
 * @author AK249877
 */
import static ch.qos.logback.classic.Level.DEBUG;
import static ch.qos.logback.classic.Level.ERROR;
import static ch.qos.logback.classic.Level.INFO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import java.awt.Color;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Appender LogBack for JTextArea (swing component)
 *
 * @author Florent Moisson
 */
public class JTextPaneAppender extends AppenderBase<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JTextPaneAppender.class);

    private final PatternLayoutEncoder m_encoder;

    private final JTextPane m_textPane;

    SimpleAttributeSet debugStyle;
    SimpleAttributeSet infoStyle;
    SimpleAttributeSet errorStyle;

    public JTextPaneAppender(JTextPane textPane) {
        m_textPane = textPane;

        initializeStyles();

        // set ctx & launch
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        setContext(lc);

        // auto-add
        lc.getLogger("ROOT").addAppender(this);

        lc.getLogger("ROOT").setLevel(ch.qos.logback.classic.Level.ALL);

        m_encoder = new PatternLayoutEncoder();

        m_encoder.setContext(lc);
        m_encoder.setPattern("%date{dd MMM yyyy HH:mm:ss.SSS} [%thread] %-5level %logger{36} %mdc - %msg%n");

        m_encoder.start();

    }

    private void initializeStyles() {
        debugStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(debugStyle, "Arial");
        StyleConstants.setForeground(debugStyle, Color.BLUE);

        infoStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(infoStyle, "Arial");
        StyleConstants.setForeground(infoStyle, Color.BLACK);

        errorStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(errorStyle, "Arial Black");
        StyleConstants.setForeground(errorStyle, Color.RED);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void append(ILoggingEvent event) {

        m_encoder.encode(event);

        final String line = event.getMessage();

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                try {
                    if (event.getLevel() == DEBUG) {
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, debugStyle);
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
                    } else if (event.getLevel() == INFO) {
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, infoStyle);
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
                    } else if (event.getLevel() == ERROR) {
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), line, errorStyle);
                        m_textPane.getDocument().insertString(m_textPane.getDocument().getLength(), "\n", null);
                    }
                } catch (BadLocationException ex) {
                    logger.error("BadLocationException exception!");
                }
            }
        }
        );

    }

}
