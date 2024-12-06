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

import fr.profi.mgfboost.ui.command.ui.MzdbCreateMgfPanel;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.gui.overview.AttributesTableModel;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTask;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTaskTypeRenderer;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTasksTableModel;
import fr.profi.mzDBWizard.gui.pendingtask.PendingTasksTableModel.Action;
import fr.profi.mzDBWizard.gui.util.ComponentTitledBorder;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.gui.util.GenericTableRenderer;
import fr.profi.mzDBWizard.gui.util.GuiUtil;
import fr.profi.mzDBWizard.processing.CreateMgfCommand;
import fr.profi.mzDBWizard.processing.jms.queue.AbstractJMSCallback;
import fr.profi.mzDBWizard.processing.jms.queue.AccessJMSManagerThread;
import fr.profi.mzDBWizard.processing.jms.queue.JMSConnectionManager;
import fr.profi.mzDBWizard.processing.jms.task.MountingPathJMSTask;
import fr.profi.mzDBWizard.processing.threading.FileProcessingExec;
import fr.profi.mzDBWizard.util.JavaVersion;
import fr.profi.mzDBWizard.util.FileUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.*;

/**
 *
 * This dialog is opened when the application is started.
 * At the beginning, it displays Settings, then it is transformed in a Review dialog after the user clicks on the
 * "Next" button
 *
 * @author AK249877
 */
public class SettingsAndReviewDialog extends JDialog implements ActionListener, KeyListener {

    private static final Logger m_logger = LoggerFactory.getLogger(SettingsAndReviewDialog.class);

    public enum Step {

        CONFIG_STEP, REVIEW_STEP
    }

    public enum Answer {

        CANCEL, OK, NULL_ANSWER
    }

    private HashMap<Step, JPanel> m_steps;
    private JButton[] m_button;
    private Answer m_answer;
    private Step m_currentStep;

    private JPanel m_mainPanel;

    private JCheckBox m_processPendingCheckBox;

    private boolean m_processExisting;

    private PendingTasksTableModel m_pendingTasksModel;
    private JTable m_pendingTasksTable;
    private ArrayList<PendingTask> m_pendingTasks;

    private AttributesTableModel m_configurationTableModel;

    private HashSet<String> m_filesIndex;
    private boolean m_hasDuplicates, m_triggerPermission;

    //MONITOR OPERATION VARIABLES
    private JTextField m_monitoredDirectory;
    private JCheckBox m_recursiveCheckBox;

    //SPLIT MZDB OPERATION VARIABLES
    private JCheckBox m_splitMzdbOperationCheckbox;
    private boolean m_doSplit;

    //MGF OPERATION VARIABLES
    private JCheckBox m_generateMgfOperationCheckbox;
    private boolean m_doGenerateMgf;
//    private JComboBox m_precursorComputationMethodCBox;
//    private JTextField m_mzTolerance, m_intensityCutoff;

    //CONVERSION OPERATION VARIABLES
    private JTextField m_converterTxtField;
    private JTextField m_converterOptionTxtField;
    private JCheckBox m_convertOperationCheckbox;
    private boolean m_doConvert;
    private JButton m_converterButton;

    //UPLOAD OPERATION VARIABLES
    private JCheckBox m_uploadOperationCheckbox;
    private boolean m_doUpload;
    private JTextField m_host;
    private JComboBox m_mountingPointComboBox;
    private JButton m_refreshMountingPointsButton;

    //CLEANUP OPERATION VARIABLES
    private JCheckBox[] m_cleanupOperationCheckboxes;

    private CreateMgfCommand m_createMgfCommand;

    public SettingsAndReviewDialog() {

        setModal(true);

        JavaVersion buildInformation = new JavaVersion();
        setTitle("Welcome to " + buildInformation.getModuleName() + " (" + buildInformation.getVersion() + ")");
        setIconImage(DefaultIcons.getSingleton().getIcon(DefaultIcons.LOGO_ICON).getImage());
        setResizable(true);
        setLayout(new BorderLayout());

        m_answer = Answer.NULL_ANSWER;
        m_createMgfCommand = CreateMgfCommand.getInstance();

        JPanel internalPanel =  init();
        add(internalPanel, BorderLayout.CENTER);
        add(initButtons(), BorderLayout.SOUTH);

        addKeyListener(this);

        setFocusable(true);

        pack();

        if(ConfigurationManager.isReadOnlyCfg()){
           setToReadOnly(internalPanel);
        }
    }

    private void setToReadOnly(JPanel panel){
        List<JComponent> all = GuiUtil.getAllChildrenOfClass(panel, JComponent.class);
        for(JComponent compo : all){
            if(compo instanceof JPanel && compo.getBorder() instanceof ComponentTitledBorder){
                ((ComponentTitledBorder) compo.getBorder()).setEnabledComponent(false);
            } else if ( (compo instanceof JPanel || compo instanceof JScrollPane) && compo.getBorder() instanceof CompoundBorder){
                if( ((CompoundBorder)compo.getBorder()).getOutsideBorder() instanceof ComponentTitledBorder)
                    ((ComponentTitledBorder) ((CompoundBorder)compo.getBorder()).getOutsideBorder()).setEnabledComponent(false);
            }

            compo.setEnabled(false);
        }
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


    private void startAction() {
        if (validateJComponents() && ensureUniqueness() && (m_currentStep == Step.REVIEW_STEP)) {
            m_answer = Answer.OK;

            updateConfiguration();

            ConfigurationManager.saveProperties(m_createMgfCommand);
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
            FileUtility.listFiles(monitoredDirectory.getAbsolutePath(), recoveredFiles, m_recursiveCheckBox.isSelected());

            for (int i = 0; i < recoveredFiles.size(); i++) {

                File f = recoveredFiles.get(i);

                String filename = FilenameUtils.removeExtension(f.getAbsolutePath());

                if (m_filesIndex.contains(filename)) {
                    m_hasDuplicates = true;
                } else {
                    m_filesIndex.add(filename);
                }

                if (f.getAbsolutePath().toLowerCase().endsWith(FileProcessingExec.RAW_SUFFIX) || f.getAbsolutePath().toLowerCase().endsWith(FileProcessingExec.WIFF_SUFFIX) /*|| f.getAbsolutePath().toLowerCase().endsWith(".d")*/) {
                    if (m_doConvert) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.CONVERSION));
                    }
                } else if (f.getAbsolutePath().toLowerCase().endsWith(FileProcessingExec.MZDB_SUFFIX)) {
                    if (m_doUpload) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.UPLOAD));
                    } else if  (m_doGenerateMgf) {
                        m_pendingTasks.add(new PendingTask(recoveredFiles.get(i).getAbsolutePath(), Action.CONVERSION));
                    }

                }

            }

            m_pendingTasksModel.update(m_pendingTasks);
        }

        updateConfiguration();

        m_configurationTableModel.update(ConfigurationManager.getConfigurationModelData());

        updateButtons();

        m_triggerPermission = true;

        validate();
    }

    private void previousAction() {
        m_mainPanel.removeAll();

        if (m_currentStep == Step.REVIEW_STEP) {
            m_currentStep = Step.CONFIG_STEP;
            m_mainPanel.add(m_steps.get(Step.CONFIG_STEP));
        }

        m_mainPanel.revalidate();
        m_mainPanel.repaint();

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
        String[] iconsName = {DefaultIcons.PREVIOUS_ICON, DefaultIcons.NEXT_ICON, DefaultIcons.TICK_ICON, DefaultIcons.CROSS_ICON};
        String[] headings = {"Previous", "Next", "Start", "Cancel"};
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

        for (int i = 0; i < ConfigurationManager.getMountingPointPathLabels().size(); i++) {
            m_mountingPointComboBox.addItem(ConfigurationManager.getMountingPointPathLabels().get(i));
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

        m_doConvert = ConfigurationManager.getConvertMzdbOperation();
        m_convertOperationCheckbox = new JCheckBox("Convert");
        m_convertOperationCheckbox.setSelected(m_doConvert);
        m_convertOperationCheckbox.setFocusable(false);
        m_convertOperationCheckbox.addItemListener(ie -> {
            m_doConvert = m_convertOperationCheckbox.isSelected();
            enableConversionOptions();
        });

        enableConversionOptions();

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
        String options = (ConfigurationManager.getConverterOptions() != null && !ConfigurationManager.getConverterOptions().isEmpty()) ? ConfigurationManager.getConverterOptions() : "";
        m_converterOptionTxtField = new JTextField(options);
        m_converterOptionTxtField.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        convertOperationPanel.add(m_converterOptionTxtField, c);

        convertOperationPanel.setBorder(new ComponentTitledBorder(m_convertOperationCheckbox, convertOperationPanel, BorderFactory.createEtchedBorder()));

        return convertOperationPanel;
    }

    private void enableConversionOptions() {
        m_converterTxtField.setEnabled(m_doConvert);
        m_converterButton.setEnabled(m_doConvert);
    }

    private JPanel initGenerateMgfOperationPanel() {
        JPanel mgfOperationPanel = new JPanel();
        mgfOperationPanel.setLayout(new GridBagLayout());
        m_doGenerateMgf = ConfigurationManager.getProcessGenerateMgf();
        m_generateMgfOperationCheckbox = new JCheckBox("Generate mgf");
        m_generateMgfOperationCheckbox.setSelected(m_doGenerateMgf);
        m_generateMgfOperationCheckbox.setFocusable(false);
        m_generateMgfOperationCheckbox.addItemListener(ie -> {
            m_doGenerateMgf = m_generateMgfOperationCheckbox.isSelected();
            enableMgfOptions();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);
        c.weightx = 1;
        mgfOperationPanel.add(m_createMgfCommand.getConfigurationPanel(), c);

        mgfOperationPanel.setBorder(new ComponentTitledBorder(m_generateMgfOperationCheckbox, mgfOperationPanel, BorderFactory.createEtchedBorder()));
        enableMgfOptions();
        return mgfOperationPanel;
    }

    private void enableMgfOptions() {
        GuiUtil.enableAllChildrenOfClass(m_createMgfCommand.getConfigurationPanel(), JComponent.class, m_doGenerateMgf);
        if(m_doGenerateMgf)
            ((MzdbCreateMgfPanel)m_createMgfCommand.getConfigurationPanel()).updateComponents();
    }

    private JPanel initUploadOperationPanel() {
        JPanel uploadOperationPanel = new JPanel();
        uploadOperationPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_doUpload = ConfigurationManager.getProcessUpload();
        m_uploadOperationCheckbox = new JCheckBox("Upload");
        m_uploadOperationCheckbox.setSelected(m_doUpload);
        m_uploadOperationCheckbox.setFocusable(false);

        m_uploadOperationCheckbox.addItemListener(ie -> {

            m_doUpload = m_uploadOperationCheckbox.isSelected();
            enableUploadOptions();
            if (m_doUpload) {
                m_uploadOperationCheckbox.setFocusPainted(false);
                if (m_host != null) {
                    m_host.requestFocus();

                    if (m_doUpload && !m_host.getText().equalsIgnoreCase(ConfigurationManager.HOST_TO_SELECT)) {
                        requestMountingPoints();
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
                m_doUpload = true;
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
            }

            @Override
            public void keyReleased(KeyEvent ke) {
            }

        });

        JLabel mountingPointLabel = new JLabel("Mounting Point : ");

        m_mountingPointComboBox = new JComboBox();
        m_mountingPointComboBox.setModel(new DefaultComboBoxModel());
        m_mountingPointComboBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m_uploadOperationCheckbox.setSelected(true);
                m_doUpload=true;
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

        enableUploadOptions();

        if (m_doUpload && !m_host.getText().equalsIgnoreCase(ConfigurationManager.HOST_TO_SELECT)) {
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

        JMSConnectionManager.getJMSConnectionManager().closeConnection(); //In case already connected !
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

    private void enableUploadOptions() {
        m_host.setEnabled(m_doUpload);
        m_mountingPointComboBox.setEnabled(m_doUpload);

        repaint();
    }

    private JPanel initSplitMzdbOperationPanel() {
        JPanel splitMzdbPanel = new JPanel();

        splitMzdbPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        m_doSplit = ConfigurationManager.getProcessSplitMzdb();
        m_splitMzdbOperationCheckbox = new JCheckBox("Split Exploris mzdb");
        m_splitMzdbOperationCheckbox.setSelected(m_doSplit);
        m_splitMzdbOperationCheckbox.setFocusable(false);
        m_splitMzdbOperationCheckbox.addItemListener(ie -> {
            m_doSplit = m_splitMzdbOperationCheckbox.isSelected();
        });

        c.gridy = 0;
        c.weightx = 0;
        c.gridx = 0;
        splitMzdbPanel.add(m_splitMzdbOperationCheckbox, c);

        JLabel splitExtensionLb = new JLabel("Splitted files extension: ");
        splitExtensionLb.setHorizontalAlignment(SwingConstants.RIGHT);

        JTextField splitExtension = new JTextField(FileProcessingExec.SPLIT_SUFFIX);
        splitExtension.setEditable(false);
        c.gridy++;
        splitMzdbPanel.add(splitExtensionLb, c);
        c.weightx = 1;
        c.gridx++;
        splitMzdbPanel.add(splitExtension, c);


        splitMzdbPanel.setBorder(new ComponentTitledBorder(m_splitMzdbOperationCheckbox, splitMzdbPanel, BorderFactory.createEtchedBorder()));

        return splitMzdbPanel;
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
        configPanel.add(initSplitMzdbOperationPanel(), c);
        c.gridy++;
        configPanel.add(initGenerateMgfOperationPanel(), c);
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

        ConfigurationManager.setMonitorPath(m_monitoredDirectory.getText());
        ConfigurationManager.setRecursiveWatching(m_recursiveCheckBox.isSelected());

        ConfigurationManager.setConvertMzdbOperation(m_doConvert);
        ConfigurationManager.setConverterPath(m_converterTxtField.getText());
        ConfigurationManager.setConverterOptions(m_converterOptionTxtField.getText());

        ConfigurationManager.setProcessSplitMzdb(m_doSplit);

        ConfigurationManager.setProcessGenerateMgf(m_doGenerateMgf);
        ConfigurationManager.setProcessUpload(m_doUpload);
        ConfigurationManager.setJmsServerHost(m_host.getText());
        ConfigurationManager.setMountingPointLabel(m_mountingPointComboBox.getSelectedItem().toString());

        ConfigurationManager.setDeleteRaw(m_cleanupOperationCheckboxes[0].isSelected());
        ConfigurationManager.setDeleteMzdb(m_cleanupOperationCheckboxes[1].isSelected());
        if(m_processPendingCheckBox != null)
            ConfigurationManager.setProcessPending(m_processPendingCheckBox.isSelected());
    }


    private JPanel initReviewStep() {
        JPanel reviewStepPanel = new JPanel();
        reviewStepPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new java.awt.Insets(5, 5, 5, 5);

        updateConfiguration();

        m_configurationTableModel = new AttributesTableModel(ConfigurationManager.getConfigurationModelData());
        JTable m_configurationTable = new JTable(m_configurationTableModel);
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
        m_processPendingCheckBox.setSelected(ConfigurationManager.getProcessPending());
        m_processPendingCheckBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {

                m_processExisting = m_processPendingCheckBox.isSelected();
                m_pendingTasksTable.setEnabled(m_processExisting);
                repaint();

                if (m_hasDuplicates && m_triggerPermission && m_processExisting && m_doUpload) {
                    if (JOptionPane.showConfirmDialog((Component) null, "Are you sure you want to proceed?", "Raw and Mzdb versions of the same file(s)!", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                        m_triggerPermission = false;
                        m_processPendingCheckBox.setSelected(false);
                        m_triggerPermission = true;
                    } else {
                        m_triggerPermission = false;
                        m_processPendingCheckBox.setSelected(true);
                        m_triggerPermission = true;
                    }
                    m_processExisting = m_processPendingCheckBox.isSelected();
                }

            }

        });

        m_processPendingCheckBox.setSelected(ConfigurationManager.getProcessPending());
        m_pendingTasksTable.setEnabled(ConfigurationManager.getProcessPending());

        m_triggerPermission = true;

        pendingScrollPane.setBorder(BorderFactory.createCompoundBorder(new ComponentTitledBorder(m_processPendingCheckBox, pendingScrollPane, BorderFactory.createEtchedBorder()), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        reviewStepPanel.add(configurationScrollPane, c);

        c.gridy++;

        reviewStepPanel.add(pendingScrollPane, c);

        if(ConfigurationManager.isReadOnlyCfg()){
            setToReadOnly(reviewStepPanel);
        }
        return reviewStepPanel;
    }

    private JPanel init() {
        m_mainPanel = new JPanel();
        m_mainPanel.setLayout(new BorderLayout());

        m_filesIndex = new HashSet<>();
        m_hasDuplicates = false;

        m_steps = new HashMap<>();
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
            m_doConvert=true;
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


    public Answer getAnswer() {
        return m_answer;
    }


    @Override
    public void keyTyped(KeyEvent ke) {
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
    }

    private boolean validateMgfConfiguration() {
        try {
            if(!m_doGenerateMgf)
                return true;
            if(! m_createMgfCommand.buildCommand()) {
                m_createMgfCommand.showErrorMessage();
                return false;
            }
//            Double.parseDouble(m_mzTolerance.getText());
//            Double.parseDouble(m_intensityCutoff.getText());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check m/z tolerance and intensity cutoff.", "Invalid values", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateUploadConfiguration() {
        if (m_doUpload && m_mountingPointComboBox.getSelectedIndex() == 0) {
            JOptionPane.showMessageDialog(this, "Check your mounting point.", "Invalid values", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean validateOperations() {
        if (!(m_doConvert || m_doGenerateMgf || m_doUpload)) {
            JOptionPane.showMessageDialog(this, "Select at least one operation.", "Invalid configuration", JOptionPane.ERROR_MESSAGE);
        }
        return m_doConvert || m_doGenerateMgf || m_doUpload;
    }

}
