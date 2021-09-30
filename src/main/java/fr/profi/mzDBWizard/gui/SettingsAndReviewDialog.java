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

import fr.profi.mzDBWizard.gui.overview.AttributesTableModel;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTask;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTaskTypeRenderer;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTasksTableModel;
import fr.profi.mzDBWizard.gui.util.ComponentTitledBorder;
import fr.profi.mzDBWizard.gui.util.GenericTableRenderer;
import fr.profi.mzDBWizard.processing.jms.task.MountingPathJMSTask;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTasksTableModel.Action;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSCallback;
import fr.profi.mzDBWizard.processing.jms.queue.AccessJMSManagerThread;
import fr.profi.mzDBWizard.processing.jms.queue.JMSConnectionManager;
import fr.profi.mzDBWizard.util.BuildInformation;
import fr.profi.mzDBWizard.configuration.Configuration;
import fr.profi.mzDBWizard.configuration.Configuration.PrecursorComputationMethod;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * This dialog is opened when the application is started.
 * At the beginning, it displays Settings, then it is transformed in a Review dialog after the user clicks on the
 * "Next" button
 *
 * @author AK249877
 */
public class SettingsAndReviewDialog extends JDialog implements ActionListener, KeyListener {

    public enum Step {

        CONFIG_STEP, REVIEW_STEP
    }

    public enum Answer {

        CANCEL, OK, NULL_ANSWER
    }

    private HashMap<Step, JPanel> m_steps;
    private JButton m_button[];
    private Answer m_answer;
    private Step m_currentStep;

    private JPanel m_mainPanel;

    private JCheckBox m_processPendingCheckBox;

    private boolean m_processExisting;

    private PendingTasksTableModel m_pendingTasksModel;
    private JTable m_pendingTasksTable;
    private ArrayList<PendingTask> m_pendingTasks;

    private Configuration m_configuration;
    private AttributesTableModel m_configurationTableModel;
    private JTable m_configurationTable;


    private HashSet<String> m_filesIndex;
    private boolean m_hasDuplicates, m_triggerPermission;

    //MONITOR OPERATION VARIABLES
    private JTextField m_monitoredDirectory;
    private JCheckBox m_recursiveCheckBox;

    //MGF OPERATION VARIABLES
    private JCheckBox m_mgfOperationCheckbox;
    private boolean m_mgfOperation;
    private JComboBox m_precursorComputationMethod;
    private JTextField m_mzTolerance, m_intensityCutoff;

    //CONVERSION OPERATION VARIABLES
    private JTextField m_converterTxtField;
    private JTextField m_converterOptionTxtField;
    private JCheckBox m_convertOperationCheckbox;
    private boolean m_doConvert;
    private JButton m_converterButton;

    //UPLOAD OPERATION VARIABLES
    private JCheckBox m_uploadOperationCheckbox;
    private boolean m_uploadOperation;
    private JTextField m_host;
    private JComboBox m_mountingPointComboBox;
    private JButton m_refreshMountingPointsButton;

    //CLEANUP OPERATION VARIABLES
    private JCheckBox m_cleanupOperationCheckboxes[];

    public SettingsAndReviewDialog() {

        setModal(true);

        BuildInformation buildInformation = new BuildInformation();
        setTitle("Welcome to " + buildInformation.getModuleName() + " (" + buildInformation.getVersion() + ")");


        setIconImage(DefaultIcons.getSingleton().getIcon(DefaultIcons.WAND_HAT_ICON).getImage());

        setSize(520, 580);
        setMinimumSize(new Dimension(520, 580));
        setMaximumSize(new Dimension(600, 800));

        setResizable(true);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        m_answer = Answer.NULL_ANSWER;

        add(init(), BorderLayout.CENTER);
        add(initButtons(), BorderLayout.SOUTH);

        addKeyListener(this);

        setFocusable(true);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equalsIgnoreCase("Start")) {
            startAction();
        } else if (ae.getActionCommand().equalsIgnoreCase("Cancel")) {
            cancelAction();
        } else if (ae.getActionCommand().equalsIgnoreCase("Next")) {
            nextAction();
        } else if (ae.getActionCommand().equalsIgnoreCase("Previous")) {
            previousAction();
        }
    }

    private void cancelAction() {
        m_answer = Answer.CANCEL;
        setVisible(false);
    }

    public boolean getProcessExisting() {
        return m_processExisting;
    }

    private void startAction() {
        if (validateJComponents() && ensureUniqueness() && (m_currentStep == Step.REVIEW_STEP)) {
            m_answer = Answer.OK;

            ConfigurationManager.setMonitorPath(m_monitoredDirectory.getText());
            ConfigurationManager.setRecursiveWatching(m_recursiveCheckBox.isSelected());

            m_configuration.setProcessPending(m_processPendingCheckBox.isSelected()); //IMPORTANT!
            ConfigurationManager.setProcessPending(m_processPendingCheckBox.isSelected());

            ConfigurationManager.setConvertOperation(m_convertOperationCheckbox.isSelected());
            if (m_convertOperationCheckbox.isSelected()) {
                ConfigurationManager.setConverterPath(m_converterTxtField.getText());
                ConfigurationManager.setConverterOptions(m_converterOptionTxtField.getText());
            }

            ConfigurationManager.setUploadOperation(m_uploadOperationCheckbox.isSelected());
            if (m_uploadOperationCheckbox.isSelected()) {
                JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(m_host.getText());
                ConfigurationManager.setJmsServerHost(m_host.getText());
                ConfigurationManager.setMountingPointLabel(m_mountingPointComboBox.getSelectedItem().toString());
            }

            ConfigurationManager.setMgfOperation(m_mgfOperationCheckbox.isSelected());
            if (m_mgfOperationCheckbox.isSelected()) {
                ConfigurationManager.setMzTolerance(Float.parseFloat(m_mzTolerance.getText()));
                ConfigurationManager.setIntensityCutoff(Float.parseFloat(m_intensityCutoff.getText()));
                ConfigurationManager.setPrecursorComputationMethod(m_precursorComputationMethod.getSelectedItem().toString());
            }

            ConfigurationManager.setDeleteRaw(m_cleanupOperationCheckboxes[0].isSelected());
            ConfigurationManager.setDeleteMzdb(m_cleanupOperationCheckboxes[1].isSelected());

            ConfigurationManager.setMgfOperation(m_mgfOperationCheckbox.isSelected());

            if (m_uploadOperation) {
                JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(m_host.getText());
                ConfigurationManager.setJmsServerHost(m_host.getText());
                ConfigurationManager.setMountingPointLabel(m_mountingPointComboBox.getSelectedItem().toString());
            }

            ConfigurationManager.saveProperties();
            setVisible(false);
        }
    }

    private void nextAction() {

        if (m_currentStep == Step.CONFIG_STEP && validateMgfConfiguration() && validateUploadConfiguration() && validateOperations()) {
            if (!m_steps.containsKey(SettingsAndReviewDialog.Step.REVIEW_STEP)) {
                m_steps.put(SettingsAndReviewDialog.Step.REVIEW_STEP, initReviewStep());
            }

            reviewStep();
        }
    }

    private void reviewStep() {
        m_mainPanel.removeAll();
        m_currentStep = Step.REVIEW_STEP;
        m_mainPanel.add(m_steps.get(Step.REVIEW_STEP));
        m_mainPanel.revalidate();
        m_mainPanel.repaint();

        File monitoredDirectory = new File(m_monitoredDirectory.getText());

        if (monitoredDirectory.exists()) {
            if (m_pendingTasks == null) {
                m_pendingTasks = new ArrayList<>();
            } else {
                m_pendingTasks.clear();
            }

            m_hasDuplicates = false;
            m_triggerPermission = false;

            m_filesIndex.clear();

            ArrayList<File> recoveredFiles = new ArrayList<>();
            FileUtility.listFiles(monitoredDirectory.getAbsolutePath(), recoveredFiles, true);

            for (int i = 0; i < recoveredFiles.size(); i++) {

                File f = recoveredFiles.get(i);

                String filename = FilenameUtils.removeExtension(f.getAbsolutePath());

                if (m_filesIndex.contains(filename)) {
                    m_hasDuplicates = true;
                } else {
                    m_filesIndex.add(filename);
                }

                if (f.getAbsolutePath().toLowerCase().endsWith(".raw") || f.getAbsolutePath().toLowerCase().endsWith(".wiff") || f.getAbsolutePath().toLowerCase().endsWith(".d")) {
                    if (m_doConvert) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.CONVERSION));
                    }
                } else if (f.getAbsolutePath().toLowerCase().endsWith(".mzdb")) {
                    if (m_uploadOperation) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.UPLOAD));
                    } else if  (m_mgfOperation) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.CONVERSION));
                    }

                }

            }

            m_pendingTasksModel.update(m_pendingTasks);
        }

        updateConfiguration();

        m_configurationTableModel.update(m_configuration.getConfigurationModelData());

        updateButtons();

        m_triggerPermission = true;


        setLocationRelativeTo(null);

        validate();
        pack();
    }

    private void previousAction() {
        m_mainPanel.removeAll();

        if (m_currentStep == Step.REVIEW_STEP) {
            m_currentStep = Step.CONFIG_STEP;
            m_mainPanel.add(m_steps.get(Step.CONFIG_STEP));
        }

        m_mainPanel.revalidate();
        m_mainPanel.repaint();

        setSize(480, 640);
        setMinimumSize(new Dimension(480, 640));
        setMaximumSize(new Dimension(600, 800));

        pack();
        repaint();

        updateButtons();
    }

    private JPanel initButtons() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.insets = new Insets(10,10,10,10);

        panel.add(Box.createHorizontalGlue(), c);

        c.weightx = 0;

        String path = new File(".").getAbsolutePath();
        System.out.println(path);

        //panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        String iconsName[] = {DefaultIcons.PREVIOUS_ICON, DefaultIcons.NEXT_ICON, DefaultIcons.TICK_ICON, DefaultIcons.CROSS_ICON};
        String headings[] = {"Previous", "Next", "Start", "Cancel"};
        m_button = new JButton[headings.length];

        for (int i = 0; i < m_button.length; i++) {

            m_button[i] = new JButton(headings[i], DefaultIcons.getSingleton().getIcon(iconsName[i]));
            m_button[i].setActionCommand(headings[i]);
            m_button[i].addActionListener(this);
            m_button[i].setFocusable(false);
            c.gridx++;
            panel.add(m_button[i], c);
            //c.gridx++;
            //panel.add(Box.createHorizontalStrut(10), c);
        }

        updateButtons();
        return panel;
    }

    private void updateButtons() {
        if (m_currentStep == Step.CONFIG_STEP) {
            m_button[0].setEnabled(false);
            m_button[1].setEnabled(true);
            m_button[2].setEnabled(false);
        } else if (m_currentStep == Step.REVIEW_STEP) {
            m_button[0].setEnabled(true);
            m_button[1].setEnabled(false);
            m_button[2].setEnabled(true);
        }
    }

    private void clearMountingPoints() {
        m_mountingPointComboBox.removeAllItems();
        m_mountingPointComboBox.addItem("Select..");
    }

    private void reloadMountingPoints() {

        Object m_lastMountingPoint = m_mountingPointComboBox.getSelectedItem();

        clearMountingPoints();

        for (int i = 0; i < ConfigurationManager.getPathLabels().size(); i++) {
            m_mountingPointComboBox.addItem(ConfigurationManager.getPathLabels().get(i));
        }

        if (m_lastMountingPoint != null && m_mountingPointComboBox.getSelectedIndex() > 0) {
            m_mountingPointComboBox.setSelectedItem(m_lastMountingPoint);
        } else {
            for (int i = 0; i < m_mountingPointComboBox.getItemCount(); i++) {
                if (m_mountingPointComboBox.getItemAt(i).toString().equalsIgnoreCase(ConfigurationManager.getMountingPointLabel())) {
                    m_mountingPointComboBox.setSelectedIndex(i);
                    break;
                }
            }

        }
    }

    private JPanel initMonitorOperationPanel() {
        JPanel monitorOperationPanel = new JPanel();
        monitorOperationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        JLabel monitoredDirectoryLabel = new JLabel("Directory : ");
        monitoredDirectoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        File directoryFile = new File(ConfigurationManager.getMonitorPath());

        m_monitoredDirectory = new JTextField((directoryFile.exists() ? directoryFile.getAbsolutePath() : System.getProperty("user.dir")));
        m_monitoredDirectory.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        m_monitoredDirectory.setFocusable(false);
        m_monitoredDirectory.setToolTipText("Click to select directory!");
        m_monitoredDirectory.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openMonitoredDirectoryChooserDialog();
            }
        });

        JButton m_directoryButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.OPEN_FOLDER_ICON));
        m_directoryButton.setFocusable(false);
        m_directoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                openMonitoredDirectoryChooserDialog();
            }
        });

        m_recursiveCheckBox = new JCheckBox("Recursively");
        m_recursiveCheckBox.setSelected(ConfigurationManager.getRecursive());

        c.gridy = 0;

        c.gridx = 0;
        c.weightx = 0;
        monitorOperationPanel.add(monitoredDirectoryLabel, c);

        c.gridx = 1;
        c.weightx = 1;
        monitorOperationPanel.add(m_monitoredDirectory, c);

        c.gridx = 2;
        c.weightx = 0;
        monitorOperationPanel.add(m_directoryButton, c);

        c.gridy++;

        c.gridwidth = 2;
        c.gridx = 1;
        c.weightx = 0;
        m_recursiveCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
        monitorOperationPanel.add(m_recursiveCheckBox, c);

        monitorOperationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Monitored directory"));

        return monitorOperationPanel;
    }

    private JPanel initConvertOperationPanel() {
        JPanel convertOperationPanel = new JPanel();
        convertOperationPanel.setLayout(new GridBagLayout());

        JLabel converterLabel = new JLabel("Converter : ");
        File converterFile = new File(ConfigurationManager.getConverterPath());

        m_converterTxtField = new JTextField((converterFile.exists()) ? converterFile.getAbsolutePath() : "Converter not selected!");
        m_converterTxtField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        m_converterTxtField.setBackground(Color.WHITE);
        m_converterTxtField.setFocusable(false);
        m_converterTxtField.setToolTipText("Click to select converter!");

        m_converterTxtField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openConverterChooserDialog();
            }
        });

        m_converterButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.OPEN_FOLDER_ICON));
        m_converterButton.setFocusable(false);

        m_converterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                openConverterChooserDialog();
            }
        });

        m_doConvert = ConfigurationManager.getConvertOperation();
        m_convertOperationCheckbox = new JCheckBox("Convert");
        m_convertOperationCheckbox.setSelected(m_doConvert);
        m_convertOperationCheckbox.setFocusable(false);
        m_convertOperationCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                m_doConvert = m_convertOperationCheckbox.isSelected();
                updateConversionOptions();
            }

        });

        updateConversionOptions();

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weighty = 0;
        c.gridy = 0;

        c.weightx = 0;
        c.gridx = 0;
        convertOperationPanel.add(converterLabel, c);

        c.weightx = 1;
        c.gridx++;
        convertOperationPanel.add(m_converterTxtField, c);

        c.weightx = 0;
        c.gridx++;
        convertOperationPanel.add(m_converterButton, c);

        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        JLabel converterOptionLabel = new JLabel("Options : ");
        convertOperationPanel.add(converterOptionLabel, c);


        c.weightx = 1;
        c.gridx++;
        m_converterOptionTxtField = new JTextField("");
        m_converterOptionTxtField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        convertOperationPanel.add(m_converterOptionTxtField, c);

        convertOperationPanel.setBorder(new ComponentTitledBorder(m_convertOperationCheckbox, convertOperationPanel, BorderFactory.createEtchedBorder()));

        return convertOperationPanel;
    }

    private void updateConversionOptions() {
        m_converterTxtField.setEnabled(m_doConvert);
        m_converterButton.setEnabled(m_doConvert);
    }

    private JPanel initMgfOperationPanel() {
        JPanel mgfOperationPanel = new JPanel();
        mgfOperationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_mgfOperation = ConfigurationManager.getMgfOperation();
        m_mgfOperationCheckbox = new JCheckBox("Export mgf");
        m_mgfOperationCheckbox.setSelected(m_mgfOperation);
        m_mgfOperationCheckbox.setFocusable(false);
        m_mgfOperationCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                m_mgfOperation = m_mgfOperationCheckbox.isSelected();
                updateMgfOptions();
            }

        });

        JLabel mzToleranceLabel = new JLabel("m/z tolerance (ppm) : ");
        mzToleranceLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_mzTolerance = new JTextField(String.valueOf(ConfigurationManager.getMzTolerance()));
        m_mzTolerance.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_mgfOperationCheckbox.setSelected(true);
                repaint();
                m_mzTolerance.requestFocus();
            }
        });
        m_mzTolerance.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        m_mzTolerance.setFocusable(true);
        m_mzTolerance.addKeyListener(this);

        JLabel intensityCutoffLabel = new JLabel("Intensity cutoff : ");
        intensityCutoffLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_intensityCutoff = new JTextField(String.valueOf(ConfigurationManager.getIntensityCutoff()));
        m_intensityCutoff.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_mgfOperationCheckbox.setSelected(true);
                repaint();
                m_intensityCutoff.requestFocus();
            }
        });
        m_intensityCutoff.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        m_intensityCutoff.setFocusable(true);
        m_intensityCutoff.addKeyListener(this);

        JLabel precursorMethodLabel = new JLabel("Precursor m/z computation method : ");
        precursorMethodLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_precursorComputationMethod = new JComboBox();

        m_precursorComputationMethod.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!m_mgfOperation) {
                    m_mgfOperationCheckbox.setSelected(true);
                    repaint();
                    m_precursorComputationMethod.showPopup();
                }
            }
        });

        for (PrecursorComputationMethod method : PrecursorComputationMethod.values()) {
            m_precursorComputationMethod.addItem(method.toString());
        }

        m_precursorComputationMethod.setSelectedItem(ConfigurationManager.getPrecursorComputationMethod().toString());

        updateMgfOptions();

        c.gridy = 0;

        c.weightx = 0;
        c.gridx = 0;
        mgfOperationPanel.add(mzToleranceLabel, c);
        c.weightx = 1;
        c.gridx = 1;
        mgfOperationPanel.add(m_mzTolerance, c);

        c.gridy++;

        c.weightx = 0;
        c.gridx = 0;
        mgfOperationPanel.add(intensityCutoffLabel, c);
        c.weightx = 1;
        c.gridx = 1;
        mgfOperationPanel.add(m_intensityCutoff, c);

        c.gridy++;

        c.weightx = 0;
        c.gridx = 0;
        mgfOperationPanel.add(precursorMethodLabel, c);
        c.weightx = 1;
        c.gridx = 1;
        mgfOperationPanel.add(m_precursorComputationMethod, c);

        mgfOperationPanel.setBorder(new ComponentTitledBorder(m_mgfOperationCheckbox, mgfOperationPanel, BorderFactory.createEtchedBorder()));

        return mgfOperationPanel;
    }

    private void updateMgfOptions() {
        m_mzTolerance.setEnabled(m_mgfOperation);
        m_intensityCutoff.setEnabled(m_mgfOperation);
        m_precursorComputationMethod.setEnabled(m_mgfOperation);
    }

    private JPanel initUploadOperationPanel() {
        JPanel uploadOperationPanel = new JPanel();
        uploadOperationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_uploadOperation = ConfigurationManager.getUploadOperation();
        m_uploadOperationCheckbox = new JCheckBox("Upload");
        m_uploadOperationCheckbox.setSelected(m_uploadOperation);
        m_uploadOperationCheckbox.setFocusable(false);

        m_uploadOperationCheckbox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {

                m_uploadOperation = m_uploadOperationCheckbox.isSelected();
                updateUploadOptions();
                if (m_uploadOperation) {
                    m_uploadOperationCheckbox.setFocusPainted(false);
                    if (m_host != null) {
                        m_host.requestFocus();

                        if (m_uploadOperation && !m_host.getText().equalsIgnoreCase(ConfigurationManager.HOST_TO_SELECT)) {
                            requestMountingPoints();
                        }
                    }
                }
            }

        });

        JLabel hostLabel = new JLabel("Host : ");
        hostLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        m_host = new JTextField(ConfigurationManager.getJmsServerHost());
        m_host.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_uploadOperationCheckbox.setSelected(true);
                repaint();
            }
        });
        m_host.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        m_host.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent ke) {
                clearMountingPoints();
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                ;
            }

            @Override
            public void keyReleased(KeyEvent ke) {
                ;
            }

        });

        JLabel mountingPointLabel = new JLabel("Mounting Point : ");

        m_mountingPointComboBox = new JComboBox();
        m_mountingPointComboBox.setModel(new DefaultComboBoxModel());
        m_mountingPointComboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_uploadOperationCheckbox.setSelected(true);
                repaint();
            }
        });

        clearMountingPoints();

        m_refreshMountingPointsButton = new JButton(DefaultIcons.getSingleton().getIcon(DefaultIcons.REFRESH));
        m_refreshMountingPointsButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {

                if (!m_host.getText().equalsIgnoreCase(ConfigurationManager.HOST_TO_SELECT)) {
                    requestMountingPoints();
                } else {
                    // error message
                    JOptionPane.showMessageDialog(m_refreshMountingPointsButton, "Please, select the host of the server.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        });

        updateUploadOptions();

        if (m_uploadOperation && !m_host.getText().equalsIgnoreCase(ConfigurationManager.HOST_TO_SELECT)) {
            requestMountingPoints();
        }

        c.gridy = 0;

        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0;
        uploadOperationPanel.add(hostLabel, c);

        c.gridwidth = 2;
        c.gridx = 1;
        c.weightx = 1;
        uploadOperationPanel.add(m_host, c);

        c.gridy++;

        c.gridwidth = 1;
        c.gridx = 0;
        c.weightx = 0;
        uploadOperationPanel.add(mountingPointLabel, c);

        c.gridwidth = 1;
        c.gridx = 1;
        c.weightx = 1;
        uploadOperationPanel.add(m_mountingPointComboBox, c);

        c.gridwidth = 1;
        c.gridx = 2;
        c.weightx = 0;
        uploadOperationPanel.add(m_refreshMountingPointsButton, c);

        uploadOperationPanel.setBorder(new ComponentTitledBorder(m_uploadOperationCheckbox, uploadOperationPanel, BorderFactory.createEtchedBorder()));

        return uploadOperationPanel;
    }

    private void requestMountingPoints() {

        m_refreshMountingPointsButton.setEnabled(false);
        m_refreshMountingPointsButton.setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.HOURGLASS));


        JMSConnectionManager.getJMSConnectionManager().setJMSServerHost(m_host.getText().trim());


        AbstractJMSCallback callback = new AbstractJMSCallback() {

            @Override
            public boolean mustBeCalledInAWT() {
                return true;
            }

            @Override
            public void run(boolean success) {
                m_refreshMountingPointsButton.setEnabled(true);
                m_refreshMountingPointsButton.setIcon(DefaultIcons.getSingleton().getIcon(DefaultIcons.REFRESH));
                if (success) {
                    reloadMountingPoints();
                } else {
                    clearMountingPoints();
                }
            }
        };


        MountingPathJMSTask task = new MountingPathJMSTask(callback);
        AccessJMSManagerThread.getAccessJMSManagerThread().addTask(task);



    }

    private void updateUploadOptions() {
        m_host.setEnabled(m_uploadOperation);
        m_mountingPointComboBox.setEnabled(m_uploadOperation);

        repaint();
    }

    private JPanel initCleanupOperationPanel() {
        JPanel cleanupPanel = new JPanel();

        String[] checkHeadings = {"Raw", "mzDB"};

        cleanupPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weighty = 0;
        c.weightx = 0;

        c.gridx = -1;
        c.gridy = 0;

        Boolean[] checkInitialValues = {ConfigurationManager.getDeleteRaw(), ConfigurationManager.getDeleteMzdb()};
        m_cleanupOperationCheckboxes = new JCheckBox[checkHeadings.length];
        for (int i = 0; i < m_cleanupOperationCheckboxes.length; i++) {
            m_cleanupOperationCheckboxes[i] = new JCheckBox(checkHeadings[i]);
            m_cleanupOperationCheckboxes[i].setSelected(checkInitialValues[i]);
            m_cleanupOperationCheckboxes[i].setFocusable(false);

            c.gridx++;
            cleanupPanel.add(m_cleanupOperationCheckboxes[i], c);
        }

        c.weightx = 1;
        c.gridx++;

        cleanupPanel.add(Box.createHorizontalBox(), c);

        cleanupPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Cleanup"));

        return cleanupPanel;
    }

    private JPanel initConfigStep() {
        JPanel configPanel = new JPanel();
        configPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        c.weighty = 0;
        c.weightx = 1;

        c.gridx = 0;
        c.gridy = 0;

        configPanel.add(initMonitorOperationPanel(), c);
        c.gridy++;
        configPanel.add(initConvertOperationPanel(), c);
        c.gridy++;
        configPanel.add(initMgfOperationPanel(), c);
        c.gridy++;
        configPanel.add(initUploadOperationPanel(), c);
        c.gridy++;
        configPanel.add(initCleanupOperationPanel(), c);
        c.gridy++;
        c.weighty = 1;
        configPanel.add(Box.createVerticalBox(), c);

        return configPanel;
    }

    private void updateConfiguration() {
        if (m_configuration == null) {
            m_configuration = new Configuration();
        }

        m_configuration.setMonitoredUrl(m_monitoredDirectory.getText());
        m_configuration.setRecursiveWatching(m_recursiveCheckBox.isSelected());

        m_configuration.setConvert(m_convertOperationCheckbox.isSelected());
        m_configuration.setConverterUrl(m_converterTxtField.getText());

        m_configuration.setExportMgf(m_mgfOperationCheckbox.isSelected());
        m_configuration.setMzTolerance(Float.parseFloat(m_mzTolerance.getText()));
        m_configuration.setIntensityCutoff(Float.parseFloat(m_intensityCutoff.getText()));

        for (PrecursorComputationMethod method : PrecursorComputationMethod.values()) {
            if (method.toString().equalsIgnoreCase(m_precursorComputationMethod.getSelectedItem().toString())) {
                m_configuration.setPrecursorComputationMethod(method);
            }
        }

        m_configuration.setUploadMzdb(m_uploadOperationCheckbox.isSelected());
        m_configuration.setHost(m_host.getText());
        m_configuration.setMountingPoint(m_mountingPointComboBox.getSelectedItem().toString());

        m_configuration.setDeleteRaw(m_cleanupOperationCheckboxes[0].isSelected());
        m_configuration.setDeleteMzdb(m_cleanupOperationCheckboxes[1].isSelected());
    }

    private JPanel initReviewStep() {
        JPanel reviewStepPanel = new JPanel();
        reviewStepPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        updateConfiguration();

        m_configurationTableModel = new AttributesTableModel(m_configuration.getConfigurationModelData());
        m_configurationTable = new JTable(m_configurationTableModel);
        m_configurationTable.setTableHeader(null);
        m_configurationTable.setRowHeight(20);
        m_configurationTable.setRowSelectionAllowed(true);
        m_configurationTable.setDefaultRenderer(Object.class, new GenericTableRenderer());

        JScrollPane configurationScrollPane = new JScrollPane(m_configurationTable);
        configurationScrollPane.getViewport().setBackground(m_configurationTable.getBackground());
        configurationScrollPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Configuration"), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        m_configurationTable.setMinimumSize(new Dimension(m_configurationTable.getWidth(), 270));
        configurationScrollPane.setMinimumSize(new Dimension(m_configurationTable.getWidth(), 270));
        configurationScrollPane.setPreferredSize(new Dimension(m_configurationTable.getWidth(), 270));
        
        m_pendingTasksModel = new PendingTasksTableModel();
        m_pendingTasksTable = new JTable(m_pendingTasksModel);
        m_pendingTasksTable.setRowHeight(20);
        m_pendingTasksTable.setRowSelectionAllowed(true);
        m_pendingTasksTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        m_pendingTasksTable.getColumnModel().getColumn(PendingTasksTableModel.FILE_INDEX).setCellRenderer(new GenericTableRenderer());
        m_pendingTasksTable.getColumnModel().getColumn(PendingTasksTableModel.ACTON_INDEX).setCellRenderer(new PendingTaskTypeRenderer());

        JScrollPane pendingScrollPane = new JScrollPane(m_pendingTasksTable);
        pendingScrollPane.getViewport().setBackground(m_pendingTasksTable.getBackground());

        m_pendingTasksTable.setMinimumSize(new Dimension(m_pendingTasksTable.getWidth(), 270));
        pendingScrollPane.setMinimumSize(new Dimension(m_pendingTasksTable.getWidth(), 270));
        pendingScrollPane.setPreferredSize(new Dimension(m_pendingTasksTable.getWidth(), 270));
        
        m_processPendingCheckBox = new JCheckBox("Process existing files");

        m_processPendingCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {

                m_processExisting = m_processPendingCheckBox.isSelected();
                m_pendingTasksTable.setEnabled(m_processExisting);
                repaint();

                if (m_hasDuplicates && m_triggerPermission && m_processExisting && m_uploadOperation) {
                    if (JOptionPane.showConfirmDialog((Component) null, "Are you sure you want to proceed?", "Raw and Mzdb versions of the same file(s)!", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                        m_triggerPermission = false;
                        m_processPendingCheckBox.setSelected(false);
                        m_triggerPermission = true;
                    } else {
                        m_triggerPermission = false;
                        m_processPendingCheckBox.setSelected(true);
                        m_triggerPermission = true;
                    }
                }

            }

        });

        m_processPendingCheckBox.setSelected(false);
        m_pendingTasksTable.setEnabled(false);

        m_triggerPermission = true;

        pendingScrollPane.setBorder(BorderFactory.createCompoundBorder(new ComponentTitledBorder(m_processPendingCheckBox, pendingScrollPane, BorderFactory.createEtchedBorder()), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        reviewStepPanel.add(configurationScrollPane, c);

        c.gridy++;

        reviewStepPanel.add(pendingScrollPane, c);

        return reviewStepPanel;
    }

    private JPanel init() {
        m_mainPanel = new JPanel();
        m_mainPanel.setLayout(new BorderLayout());

        m_filesIndex = new HashSet<>();
        m_hasDuplicates = false;

        m_steps = new HashMap<Step, JPanel>();
        m_currentStep = Step.CONFIG_STEP;

        m_steps.put(Step.CONFIG_STEP, initConfigStep());

        m_mainPanel.add(m_steps.get(Step.CONFIG_STEP));

        return m_mainPanel;
    }

    private void openConverterChooserDialog() {
        File exe = FileUtility.chooseConverter();
        if (exe != null) {
            m_converterTxtField.setText(exe.getAbsolutePath());
            m_convertOperationCheckbox.setSelected(true);
            repaint();
        }
    }

    private void openMonitoredDirectoryChooserDialog() {
        File dir = FileUtility.chooseDirectory();
        if (dir != null) {
            m_monitoredDirectory.setText(dir.getAbsolutePath());
        }
    }

    private boolean validateJComponents() {
        if (m_monitoredDirectory.getText().length() == 0 || m_converterTxtField.getText().length() == 0) {
            JOptionPane.showMessageDialog(this, "Selected monitored directory or converter is invalid, please review your selections.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean ensureUniqueness() {

        File rootDirectory = new File(m_monitoredDirectory.getText());

        if (!rootDirectory.exists()) {
            return false;
        }

        String[] extensions = {"lock"};

        Collection files = FileUtils.listFiles(rootDirectory, extensions, true);

        for (Iterator iterator = files.iterator(); iterator.hasNext();) {
            File file = (File) iterator.next();

            if (file.getName().equals("mzdb.lock")) {

                long lastModified = file.lastModified();
                long currentTimestamp = System.currentTimeMillis();

                if ((currentTimestamp - lastModified) <= 1000 * 10) {
                    JOptionPane.showMessageDialog(this, "An mzDB-wizard instance is already working on this folder. Choose another one or use the other running instance.", "Error", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }

        return true;

    }

    public Configuration getConfiguration() {
        return m_configuration;
    }

    public Answer getAnswer() {
        return m_answer;
    }

    public String getDirectoryURL() {
        return m_monitoredDirectory.getText();
    }

    public String getConverterURL() {
        return m_converterTxtField.getText();
    }

    public String getPathLabel() {
        return m_mountingPointComboBox.getSelectedItem().toString();
    }

    public boolean isUploading() {
        return m_uploadOperation;
    }

    @Override
    public void keyTyped(KeyEvent ke) {
        ;
    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            startAction();
        } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            cancelAction();
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        ;
    }

    private boolean validateMgfConfiguration() {
        try {
            Double.parseDouble(m_mzTolerance.getText());
            Double.parseDouble(m_intensityCutoff.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check m/z tolerance and intensity cutoff.", "Invalid values", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateUploadConfiguration() {
        if (m_uploadOperationCheckbox.isSelected() && m_mountingPointComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Check your mounting point.", "Invalid values", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateOperations() {
        if (!(m_doConvert || m_mgfOperation || m_uploadOperation)) {
            JOptionPane.showMessageDialog(this, "Select at least one operation.", "Invalid configuration", JOptionPane.ERROR_MESSAGE);
        }
        return m_doConvert || m_mgfOperation || m_uploadOperation;
    }

}
