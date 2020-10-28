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

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;


/**
 *
 * Access to Files from ressources
 *
 * @author AK249877
 */
public class FileManager {


    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("ApplicationProperties");

    private static FileManager m_singleton = null;

    private FileManager() {

    }

    public static FileManager getSingleton() {
        if (m_singleton == null) {
            m_singleton = new FileManager();
        }
        return m_singleton;
    }

    public File getFile2(String fileName) {

        try {
            URL url = this.getClass().getResource(fileName);
            URI uri = url.toURI();
            File file = new File(uri);
            return file;
        } catch (Exception e) {
            logger.warn ("getFile2 : File not found: "+fileName );
            e.printStackTrace();
            return null;
        }
    }


    public File getFile(String fileName) {

        try {
            URL url = this.getClass().getResource(".");
            URI uri = url.toURI();
            File directory = new File(uri);
            File file = new File(directory.getAbsolutePath()+File.separator+fileName);
            return file;
        } catch (Exception e) {
            logger.warn ("File not found: "+fileName);
            return null;
        }
    }

    public static final String REF_TEST_MZDB = "./samples/test.mzdb";
    public static final String TEST_RAW = "./samples/test.raw";
    public static final String TEST_MZDB = "test.mzdb";
    public static final String TEST_MZDB_TMP = "test.mzdb.tmp";

//    public static final String CONFIGURATION_FILE = ".."+File.separator+".."+File.separator+".."+File.separator+".."+File.separator+"config"+File.separator+"config.properties";
    public static final String CONFIGURATION_FILE = "./config/config.properties";

}
