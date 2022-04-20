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
package fr.profi.mzDBWizard;


import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzDBWizard.filelookup.WatcherExecution;
import fr.profi.mzDBWizard.gui.MainFrame;
import fr.profi.mzDBWizard.gui.SettingsAndReviewDialog;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.processing.threading.queue.TaskManagerThread;
import fr.profi.mzDBWizard.processing.threading.task.ConvertRawFile2MzdbTask;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzDBWizard.util.MzDBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;


/**
 * Main class of the application.
 * It displays the SettingsAndReviewDialog at the beginning.
 * Then a Wait Dialog while a conversion test is done on test.raw
 * The the MainFrame with all the tasks
 *
 * @author AK249877
 */
public class MZDBWizard {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InvocationTargetException, InterruptedException {


        Logger logger = LoggerFactory.getLogger("mzDBWizard");
        logger.info("Start mzDBWWizard. Read configuration");
        ConfigurationManager.loadProperties();

        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {


                SettingsAndReviewDialog settingsAndReviewDialog = new SettingsAndReviewDialog();
                settingsAndReviewDialog.setVisible(true);
                settingsAndReviewDialog.setLocationRelativeTo(null);
                if (settingsAndReviewDialog.getAnswer() == SettingsAndReviewDialog.Answer.OK) {

                    JDialog waitDialog = new JDialog();
                    waitDialog.setUndecorated(true);

                    JLabel waitLabel = new JLabel("Please wait..", DefaultIcons.getSingleton().getIcon(DefaultIcons.HOURGLASS), JLabel.LEFT);
                    waitLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    waitDialog.add(waitLabel);
                    waitDialog.setSize(160, 60);
                    waitDialog.setResizable(false);
                    waitDialog.setLocationRelativeTo(null);
                    waitDialog.setVisible(true);


                    WatcherExecution.initInstance(new File(ConfigurationManager.getMonitorPath()));

                    File mzdbFile = new File(MzDBUtil.TEST_MZDB);
                    if (mzdbFile.exists()) {
                        FileUtility.forceDeleteFile(mzdbFile);
                    }

                    File tempFile = new File(MzDBUtil.TEST_MZDB_TMP);
                    if (tempFile.exists()) {
                        FileUtility.forceDeleteFile(tempFile);
                    }

                    if (ConfigurationManager.getConvertMzdbOperation()) {

                        //JPM.TODO : TEST with another thread
                        File rawFile = new File(MzDBUtil.TEST_RAW);

                        if (rawFile.exists()) {


                            AbstractCallback callback = new AbstractCallback() {

                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return true;
                                }

                                @Override
                                public void run(boolean success, long taskId) {
                                    if (success) {
                                        waitDialog.setVisible(false);
                                        MainFrame frame = new MainFrame();
                                        frame.setVisible(true);
                                        if (!ConfigurationManager.getFullscreen()) {
                                            frame.setLocationRelativeTo(null);
                                        }

                                    } else {
                                        waitDialog.setVisible(false);
                                        JOptionPane.showMessageDialog(null, "Something is wrong with raw2mzDB.exe. See your system administrator...", "Converter Test Error", JOptionPane.ERROR_MESSAGE);
                                        logger.error("Something is wrong with raw2mzDB.exe. Call your administrator!");
                                        System.exit(1);
                                    }
                                }


                            };

                            ConvertRawFile2MzdbTask task = new ConvertRawFile2MzdbTask(callback, rawFile, true);
                            TaskManagerThread.getTaskManagerThread().addTask(task);


                        } else {
                            System.exit(1);
                        }

                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                waitDialog.setVisible(false);
                                MainFrame frame = new MainFrame();
                                frame.setVisible(true);
                                if (!ConfigurationManager.getFullscreen()) {
                                    frame.setLocationRelativeTo(null);
                                }
                            }
                        });
                    }

                } else {
                    System.exit(0);
                }

            }});

    }

}
