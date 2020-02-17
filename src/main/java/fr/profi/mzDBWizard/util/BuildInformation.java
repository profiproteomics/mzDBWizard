/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.profi.mzDBWizard.util;

import fr.proline.util.version.IVersion;

/**
 *
 * @author AK249877
 */
public class BuildInformation implements IVersion {

    @Override
    public String getModuleName() {
        return "mzDB-wizard";
    }

    @Override
    public String getVersion() {
        return "0.4.0-SNAPSHOT";
    }
    
}
