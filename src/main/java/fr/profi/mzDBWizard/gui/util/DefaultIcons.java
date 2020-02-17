/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.gui.util;

import javax.swing.ImageIcon;

/**
 *
 * @author AK249877
 */
public class DefaultIcons {

    private static DefaultIcons m_singleton = null;

    private DefaultIcons() {

    }

    public static DefaultIcons getSingleton() {
        if (m_singleton == null) {
            m_singleton = new DefaultIcons();
        }
        return m_singleton;
    }

    public ImageIcon getIcon(String iconRessource) {
        return new ImageIcon(this.getClass().getResource(iconRessource));
    }

    public static final String BROOM_ICON = "broom.png";
    public static final String WAND_HAT_ICON = "wand-hat.png";
    public static final String BLOCK_ICON = "block.png";
    public static final String RESOURCES_ICON = "resource-monitor.png";
    public static final String INFORMATION_ICON = "information-button.png";
    public static final String DOCUMENT_ICON = "document-horizontal-text.png";
    public static final String FOLDER_ICON = "folder-horizontal.png";
    public static final String CROSS_ICON = "cross.png";
    public static final String TICK_ICON = "tick.png";
    public static final String OPEN_FOLDER_ICON = "folder-horizontal-open.png";
    public static final String APPLICATION_ICON = "application-blue.png";
    public static final String EXCLAMATION_YELLOW_ICON = "exclamation-circle.png";
    public static final String EXCLAMATION_RED_ICON = "exclamation-red.png";
    public static final String GEAR_ICON = "gear.png";
    public static final String UPLOAD_ICON = "upload-cloud.png";
    public static final String SCREWDRIVER_ICON = "screwdriver.png";
    public static final String SERVER_ICON = "database.png";
    public static final String LABEL_ICON = "tag-label.png";
    public static final String NEXT_ICON = "arrow.png";
    public static final String PREVIOUS_ICON = "arrow-180.png";
    public static final String BIG_TICK = "tick_32.png";
    public static final String COUNTER = "counter.png";
    public static final String MONITOR = "monitor.png";
    public static final String NOTEBOOK = "notebook.png";
    public static final String BIG_CROSS = "cross-32.png";
    public static final String BIG_EXCLAMATION = "exclamation-32.png";
    public static final String BIG_DISC = "disc-32.png";
    public static final String SMALL_SERVER = "small-server.png";
    public static final String SMALL_FLASHLIGHT = "small-flashlight.png";
    public static final String SMALL_DOCUMENT_EXPORT = "small-document-export.png";
    public static final String SMALL_FOLDER = "small-folder.png";
    public static final String SMALL_TAG_LABEL = "small-tag-label.png";
    public static final String SMALL_RADIO_BUTTON = "small-ui-radio-button.png";
    public static final String SMALL_RADIO_BUTTON_UNCHECK = "small-ui-radio-button-uncheck.png";
    public static final String HOURGLASS = "hourglass.png";
    public static final String REFRESH = "arrow-circle.png";
}
