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

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.StatusPrinter;
import fr.profi.mzDBWizard.configuration.CurrentExecution;
import fr.profi.mzDBWizard.processing.threading.AbstractCallback;
import fr.profi.mzDBWizard.util.FileManager;
import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.gui.MainFrame;
import fr.profi.mzDBWizard.gui.SettingsAndReviewDialog;
import fr.profi.mzDBWizard.processing.threading.task.ConvertRawFile2MzdbTask;
import fr.profi.mzDBWizard.processing.threading.queue.TaskManagerThread;
import fr.profi.mzDBWizard.util.FileUtility;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import java.io.File;
import org.slf4j.LoggerFactory;
import java.awt.Dimension;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;


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


        //VDS : can't we use of logback.properties ? More easier to change at runtime => generated log file : Proline_Studio ?! FILE "21 juillet 2017.log" => better "mzdbWizard_20170721.log" ?! 
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(loggerContext);
        fileAppender.setName("timestamp");
        fileAppender.setAppend(true);

        String dateString = DateFormat.getDateInstance(DateFormat.SHORT).format(System.currentTimeMillis()).replaceAll("/", "");

        String filename = "log" + File.separator + "mzdbWizard_" + dateString + ".log";

        fileAppender.setFile(filename);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%date{dd MMM yyyy HH:mm:ss.SSS} [%thread] %-5level %logger{36} %mdc - %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        Logger logbackLogger = loggerContext.getLogger("ROOT");
        logbackLogger.setLevel(ch.qos.logback.classic.Level.ALL);
        logbackLogger.addAppender(fileAppender);

        // OPTIONAL: print logback internal status messages
        StatusPrinter.print(loggerContext);

        ConfigurationManager.loadProperties();

        javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {


                SettingsAndReviewDialog settingsAndReviewDialog = new SettingsAndReviewDialog();

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

                    ConfigurationManager.setConverterPath(settingsAndReviewDialog.getConverterURL());

                    CurrentExecution.initInstance(new File(settingsAndReviewDialog.getDirectoryURL()), settingsAndReviewDialog.getConfiguration());

                    File mzdbFile = FileManager.getSingleton().getFile(FileManager.TEST_MZDB);
                    if ((mzdbFile != null) && (mzdbFile.exists())) {
                        FileUtility.forceDeleteFile(mzdbFile);
                    }

                    File tempFile = FileManager.getSingleton().getFile(FileManager.TEST_MZDB_TMP);
                    if ((tempFile != null) && (tempFile.exists())) {
                        FileUtility.forceDeleteFile(tempFile);
                    }

                    if (settingsAndReviewDialog.getConfiguration().getConvert()) {

                        //JPM.TODO : TEST with another thread
                        File rawFile = FileManager.getSingleton().getFile(FileManager.TEST_RAW);

                        if ((rawFile != null) && (rawFile.exists())) {


                            AbstractCallback callback = new AbstractCallback() {

                                @Override
                                public boolean mustBeCalledInAWT() {
                                    return false;
                                }

                                @Override
                                public void run(boolean success, long taskId) {
                                    if (success) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                waitDialog.setVisible(false);
                                                MainFrame frame = new MainFrame(new Dimension(1280, 720), new Dimension(1280, 720));
                                                frame.setVisible(true);
                                            }
                                        });
                                    } else {
                                        LoggerFactory.getLogger(getClass().toString()).error("Something is wrong with raw2mzDB.exe. Call your administrator!");
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
                                MainFrame frame = new MainFrame(new Dimension(1280, 720), new Dimension(1280, 720));
                                frame.setVisible(true);
                            }
                        });
                    }

                } else {
                    System.exit(0);
                }

            }});
        
    }

}
