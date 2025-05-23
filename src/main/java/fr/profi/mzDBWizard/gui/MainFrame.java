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
package fr.profi.mzDBWizard.gui;

import fr.profi.mzDBWizard.gui.about.AboutDialog;
import fr.profi.mzDBWizard.gui.log.LogPanel;
import fr.profi.mzDBWizard.gui.overview.AttributesTableModel;
import fr.profi.mzDBWizard.gui.overview.OverviewScrollPane;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.gui.taskmanager.TaskManagerPanel;
import fr.profi.mzDBWizard.gui.util.GenericTableRenderer;
import fr.profi.mzDBWizard.processing.info.TaskInfoManager;
import fr.profi.mzDBWizard.util.JavaVersion;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

/**
 *
 * Main window with a list of tasks finished/running/waiting...
 * This window is displayed after the dialog with the settings and the summary dialog
 *
 * @author AK249877
 */
public class MainFrame extends JFrame implements WindowListener, ActionListener {

    public MainFrame() {

        setIconImage(DefaultIcons.getSingleton().getIcon(DefaultIcons.LOGO_ICON).getImage());

        JavaVersion buildInformation = new JavaVersion();

        setTitle(buildInformation.getModuleName() + " (" + buildInformation.getVersion() + ")");

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        setLayout(new GridLayout(1, 1));

        addWindowListener(this);

        if (ConfigurationManager.getFullscreen()) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        JTabbedPane m_tabbedPane = new JTabbedPane();
        m_tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        TaskManagerPanel m_taskManager = new TaskManagerPanel();

        m_tabbedPane.addTab("Tasks", m_taskManager);
        if (ConfigurationManager.getDebugMode()) {
            m_tabbedPane.addTab("Debug", new LogPanel());
        }

        setJMenuBar(initJMenuBar());

        JSplitPane m_splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, m_tabbedPane, getExecutionPanel());
        m_splitPane.setOneTouchExpandable(true);

        add(m_splitPane);
        pack();
        m_splitPane.setDividerLocation(0.65);

    }

    private JPanel getExecutionPanel() {
        JPanel panel = new JPanel();

        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTH;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 0, 5, 0);

        c.weighty = 0;
        c.weightx = 0.5;

        c.gridx = 0;
        c.gridy = 0;

        OverviewScrollPane executionOverview = OverviewScrollPane.getSingleton();
        executionOverview.setBorder(BorderFactory.createTitledBorder("Current Execution"));
        panel.add(executionOverview, c);

        c.gridy++;
        c.weighty = 1;
        AttributesTableModel m_configurationTableModel = new AttributesTableModel(ConfigurationManager.getConfigurationModelData());
        JTable configurationTable = new JTable(m_configurationTableModel);
        configurationTable.setTableHeader(null);
        configurationTable.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        configurationTable.setRowHeight(20);
        configurationTable.setRowSelectionAllowed(true);
        configurationTable.setDefaultRenderer(Object.class, new GenericTableRenderer());

        JScrollPane confScrollPane = new JScrollPane(configurationTable);
        confScrollPane.setBorder(BorderFactory.createTitledBorder("Configuration"));
        
        panel.add(confScrollPane, c);

        c.gridy++;
        c.weighty = 0;
        panel.add(Box.createVerticalBox(), c);
        return panel;
    }

    private JMenuBar initJMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // FILE MENU ---
        // File Menu, F - Mnemonic
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);


        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_E);
        exitItem.addActionListener(this);
        exitItem.setActionCommand("Exit");
        fileMenu.add(exitItem);

        // HELP MENU ---
        // Help Menu, H - Mnemonic
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        
        //Help->User Guide, U - Mnemonic
        JMenuItem userGuideItem = new JMenuItem("User Guide", KeyEvent.VK_U);
        userGuideItem.addActionListener(this);
        userGuideItem.setActionCommand("User Guide");
        helpMenu.add(userGuideItem);
        
        helpMenu.addSeparator();

        // Help->About, A - Mnemonic
        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(this);
        aboutItem.setActionCommand("About");
        helpMenu.add(aboutItem);

        return menuBar;
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {
        exitAction();
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

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equalsIgnoreCase("Exit")) {
            exitAction();
        } else if (ae.getActionCommand().equalsIgnoreCase("About")) {
            AboutDialog about = AboutDialog.getInstance();
            about.setVisible(true);
        } else if (ae.getActionCommand().equalsIgnoreCase("User Guide")) {
            if (Desktop.isDesktopSupported()) {
                try {
                    File myFile = new File("." + File.separator + "documentation" + File.separator + "user_guide.pdf");
                    Desktop.getDesktop().open(myFile);
                } catch (Exception ex) {
                    LoggerFactory.getLogger(this.getClass()).warn(" Unable to read user_guide.pdf");
                    // no application registered for PDFs
                    JOptionPane.showMessageDialog(this,"Unable to read user guide","Documentation access error",JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exitAction() {

        if (TaskInfoManager.getTaskInfoManager().askBeforeExitingApp()) {
            if (JOptionPane.showConfirmDialog(this, "There are still active tasks.", "Do you really want to exit?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else {
            System.exit(0);
        }

    }

}
