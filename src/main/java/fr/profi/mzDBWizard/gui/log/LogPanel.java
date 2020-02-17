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

import fr.profi.mzDBWizard.gui.util.DefaultIcons;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToolBar;

/**
 * Panel to display logs/Warnings/Errors
 * @author AK249877
 */
public class LogPanel extends JPanel implements ActionListener {

    private JTextPane m_textPane;
    private JButton m_clearButton;

    public LogPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        add(addToolBar(), BorderLayout.WEST);
        add(addTextPane(), BorderLayout.CENTER);
    }

    private JPanel addTextPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 1));
        m_textPane = new JTextPane();
        m_textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(m_textPane);
        panel.add(scrollPane, BorderLayout.CENTER);
        JTextPaneAppender appender = new JTextPaneAppender(m_textPane);
        appender.start();
        return panel;
    }

    private JToolBar addToolBar() {
        JToolBar toolbar = new JToolBar();
        toolbar.setOrientation(JToolBar.VERTICAL);
        toolbar.setFloatable(false);
        
        m_clearButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.BROOM_ICON));
        m_clearButton.setFocusPainted(false);
        m_clearButton.setOpaque(true);
        m_clearButton.setBorderPainted(false);
        
        m_clearButton.addActionListener(this);
        m_clearButton.setActionCommand("Clear");
        m_clearButton.setFocusable(false);

        toolbar.add(m_clearButton);

        return toolbar;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equalsIgnoreCase("Clear")) {
            m_textPane.setText("");
        }
    }

}
