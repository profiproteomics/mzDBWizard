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

import fr.profi.mzDBWizard.processing.info.TaskError;
import fr.profi.mzDBWizard.processing.info.TaskInfo;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

/**
 * Dialog to display logs/Warnings/Errors.
 * It is automatically updated.
 *
 * @author AK249877
 */
public class LogsDialog extends JDialog implements Runnable, WindowListener {

    private static int m_delayInSeconds;
    private final JTextArea m_textArea;
    private static TaskInfo m_taskInfo;
    private boolean m_run = true;
    private static LogsDialog m_instance;
    private static JComponent m_parent;

    public static LogsDialog getInstance() {
        if (m_instance == null) {
            m_instance = new LogsDialog();
        }
        return m_instance;
    }

    public static void setParameters(JComponent parent, TaskInfo taskInfo, int delayInSeconds) {
        m_parent = parent;
        m_taskInfo = taskInfo;
        m_delayInSeconds = delayInSeconds;
    }

    public void updateLogsDialog() {
        m_run = true;

        setTitle("Logs - " + m_taskInfo.getTaskDescription());

        StringBuilder sb = new StringBuilder();

        TaskError taskError = m_taskInfo.getTaskError();
        if (taskError != null) {
            sb.append("------------------ ERROR : ").append(taskError.getErrorTitle()).append("\n").append(taskError.getErrorText()).append("\n\n");
        }

        for (String log : m_taskInfo.getLogs()) {
            sb.append(log).append("\n");
        }

        m_textArea.setText(sb.toString());

        repaint();
    }

    private LogsDialog() {
        setLocationRelativeTo(m_parent);
        setTitle("Logs for " + m_taskInfo.getTaskDescription());
        setSize(new Dimension(640, 480));

        setLayout(new BorderLayout());

        m_textArea = new JTextArea();
        m_textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        m_textArea.setEditable(false);

        JScrollPane m_scrollPane = new JScrollPane(m_textArea);

        DefaultCaret caret = (DefaultCaret) m_textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        m_scrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createLineBorder(Color.DARK_GRAY)));
        
        add(m_scrollPane, BorderLayout.CENTER);
    }

    @Override
    public void run() {


        while (m_run) {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateLogsDialog();

                }
            });

            if (m_taskInfo.isFinished() || m_taskInfo.isAborted()) {
                terminate();
                break;
            }

            try {
                Thread.sleep(1000 * m_delayInSeconds);
            } catch (InterruptedException ex) {
                Logger.getLogger(LogsDialog.class.getName()).log(Level.SEVERE, null, ex);
                terminate();
            }

        }
    }

    public void terminate() {
        m_run = false;
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {
        terminate();
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }

}
