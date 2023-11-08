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
package fr.profi.mzDBWizard.util;

import com.almworks.sqlite4java.SQLiteException;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzdb.MzDbReader;
import fr.profi.mzdb.MzDbReaderHelper;
import fr.profi.mzdb.model.Spectrum;
import fr.profi.mzdb.model.SpectrumData;
import fr.profi.mzdb.model.SpectrumHeader;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.channels.FileLock;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Util methods for Files
 *
 * @author AK249877
 */
public class FileUtility {
//VDS TODO: use apache commons io instead ? 
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("FileUtil");

    public static void listFiles(String directoryName, ArrayList<File> files, boolean recursive) {
        File directory = new File(directoryName);

        // get all the files from a directory
        File[] fList = directory.listFiles();

        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                if (recursive) {
                    listFiles(file.getAbsolutePath(), files, recursive);
                }
            }
        }
    }

    public static boolean forceDeleteFile(File f) {
        if (f.delete()) {
            return true;
        } else {
            try {
                Process process = new ProcessBuilder("rm" + " " + "-f" + " " + f.getAbsolutePath()).start();
                while (process.isAlive()) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(FileUtility.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
        }
        return false;
    }

    public static boolean deleteFile(File f) {
        try {
            Files.delete(f.toPath());
        } catch (NoSuchFileException x) {
            logger.error("Trying to delete file " + f.getAbsolutePath() + ", which does not exist!", x);
            return false;
        } catch (DirectoryNotEmptyException x) {
            logger.error("Directory " + f.getAbsolutePath() + " is not empty!", x);
            return false;
        } catch (IOException x) {
            logger.error("You do not have the right to delete: " + f.toPath().toString() + "!", x);
            return false;
        }
        return true;
    }

    public static boolean isCompletelyWrittenOld(File file) {
        RandomAccessFile stream = null;
        try {
            stream = new RandomAccessFile(file, "rw");
            return true;
        } catch (Exception e) {
            logger.debug("Skipping file " + file.getName() + " for this iteration due it's not completely written");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.error("Exception during closing file " + file.getName());
                }
            }
        }
        return false;
    }
    public static boolean isCompletelyWritten(File file) {

        try {
            boolean success = file.renameTo(file);
            if(!success)
                logger.debug("1. Skipping file " + file.getName() + " for this iteration due it's not completely written");
            return success;
        } catch (Exception e) {
            logger.debug("2. Skipping file " + file.getName() + " for this iteration due it's not completely written");
        }
        return false;
    }

    /**
     * Wait if necessary until the file is complety written
     */
    public static void checkFileFinalization(File f) throws Exception {
        try {

            int nbTimesFileIdentical = 0;
            long previousSize = 0;
            while (!FileUtility.isCompletelyWritten(f)) {
                Thread.sleep(5000);  // (5s
                long currentSize = f.length();
                if (currentSize == previousSize) {
                    nbTimesFileIdentical++;

                    if (nbTimesFileIdentical == 120) {
                        // 10 minutes : file size is not modified for 10 minutes,
                        // there must be a problem
                        throw new Exception("The file "+f.getAbsolutePath()+" is locked for 10 minutes without being modified");
                    }
                } else {
                    nbTimesFileIdentical = 0;
                }

            }
        } catch (InterruptedException ex) {
            logger.error("File Finalization Interrupted!");
        }
    }

    public static boolean isFileUnlocked(File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file.getAbsolutePath());
            FileLock fl = fos.getChannel().tryLock();
            if (fl != null) {
                return false;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GenericUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(GenericUtil.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                fos.close();
            } catch (IOException ex) {
                Logger.getLogger(GenericUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return true;
    }



    public static void touch(File file) throws IOException {
        long timestamp = System.currentTimeMillis();
        touch(file, timestamp);
    }

    public static void touch(File file, long timestamp) throws IOException {
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }

        file.setLastModified(timestamp);
    }

    public static File chooseDirectory() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setMultiSelectionEnabled(false);

        jFileChooser.setCurrentDirectory(new File(ConfigurationManager.getMonitorPath()));

        int result = jFileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

    public static File chooseConverter() {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.setMultiSelectionEnabled(false);
        jFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".exe", "exe"));
        jFileChooser.setAcceptAllFileFilterUsed(false);
        jFileChooser.setCurrentDirectory(new File(ConfigurationManager.getConverterPath()));

        int result = jFileChooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            return jFileChooser.getSelectedFile();
        } else {
            return null;
        }
    }

     public static boolean verifyMzdbFile(File file) {

        boolean pass = true;

        MzDbReader reader = null;

        try {
            reader = new MzDbReader(file, true);

            SpectrumHeader[] ms2Headers = reader.getMs2SpectrumHeaders();

            SpectrumHeader[] ms1Headers = reader.getMs1SpectrumHeaders();

            if (ms1Headers == null || ms1Headers.length < 1) {
                return false;
            } else {
                    long ms1SpectrumId = ms1Headers[0].getSpectrumId();
                    if (ms1SpectrumId != 0) {
                        Spectrum ms1RawSpectrum = reader.getSpectrum(ms1SpectrumId);
                        if (ms1RawSpectrum != null) {
                            SpectrumData ms1SpectrumData = ms1RawSpectrum.getData();
                            if (ms1SpectrumData != null) {
                                final double[] mzList = ms1SpectrumData.getMzList();
                                if (mzList == null || mzList.length < 1) {
                                    pass = false;
                                }
                            } else {
                                pass = false;
                            }
                        } else {
                            pass = false;
                        }
                    }
            }

            if (ms2Headers != null && ms2Headers.length > 0) {

                long ms2SpectrumId = ms2Headers[0].getSpectrumId();
                
                if (ms2SpectrumId != 0) {
                    
                    Spectrum ms2RawSpectrum = reader.getSpectrum(ms2SpectrumId);

                    if (ms2RawSpectrum != null) {

                        SpectrumData ms2SpectrumData = ms2RawSpectrum.getData();

                        if (ms2SpectrumData != null) {

                            final double[] mzList = ms2SpectrumData.getMzList();

                            if (mzList == null || mzList.length < 1) {
                                pass = false;
                            }

                        } else {
                            pass = false;
                        }

                    } else {
                        pass = false;
                    }

                }

            }

        } catch (FileNotFoundException | SQLiteException e) {
            return false;
        } catch (StreamCorruptedException ex) {
            Logger.getLogger(MzDbReaderHelper.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        return pass;

    }

}
