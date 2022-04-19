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
package fr.profi.mzDBWizard.gui.about;

import fr.profi.mzDBWizard.gui.util.DefaultIcons;
import fr.profi.mzDBWizard.util.BuildInformation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * About Dialog, can be displayed through the menu of the main window.
 *
 * @author AK249877
 */
public class AboutDialog extends JDialog implements ActionListener, KeyListener {

    private static AboutDialog m_instance;

    public static AboutDialog getInstance() {
        if (m_instance == null) {
            m_instance = new AboutDialog();
        }
        return m_instance;
    }

    private AboutDialog() {
        setModal(true);
        setTitle("About");

        setSize(360, 240);
        setMinimumSize(new Dimension(360, 180));
        setResizable(false);

        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(initMainComponent(), BorderLayout.CENTER);
        add(initButtons(), BorderLayout.SOUTH);

        setFocusable(true);
    }

    private JPanel initMainComponent() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BorderLayout());

        BuildInformation buildInformation = new BuildInformation();
        
        String title = buildInformation.getModuleName() + " ("+buildInformation.getVersion()+")";
        
        String body = buildInformation.getModuleName()+" is developed in the context of ProFi (French Proteomics National Infrastructure) by BIG (Grenoble): EDyP Team, BGE Laboratory (U1038 CEA/INSERM/UJF)";

        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));

        SimpleAttributeSet attribs = new SimpleAttributeSet();
        StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);

        JTextPane bodyArea = new JTextPane();
        bodyArea.setText(body);
        bodyArea.setAutoscrolls(true);
        bodyArea.setEditable(false);
        bodyArea.setBackground(null);
        bodyArea.setParagraphAttributes(attribs, true);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(bodyArea, BorderLayout.CENTER);

        addKeyListener(this);

        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (ae.getActionCommand().equalsIgnoreCase("OK")) {
            setVisible(false);
        }
    }

    private JPanel initButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JButton m_okButton = new JButton("OK", DefaultIcons.getSingleton().getIcon(DefaultIcons.TICK_ICON));
        m_okButton.addActionListener(this);
        m_okButton.setActionCommand("OK");

        panel.add(m_okButton);

        return panel;
    }

    @Override
    public void keyTyped(KeyEvent ke) {

    }

    @Override
    public void keyPressed(KeyEvent ke) {
        if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            setVisible(false);
        } else if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
            setVisible(false);
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {

    }

}
