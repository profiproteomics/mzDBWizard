package fr.profi.mzDBWizard.processing;

import fr.profi.mgfboost.ui.command.MzdbCreateMgfCommand;
import fr.profi.mgfboost.ui.command.ui.AbstractCommandPanel;
import fr.profi.mgfboost.ui.command.ui.MzdbCreateMgfPanel;
import fr.profi.mzknife.CommandArguments;
import fr.profi.mzknife.mzdb.MgfBoostConfigTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class CreateMgfCommand extends MzdbCreateMgfCommand {

    // Command Properties Keys
    private static final String INTENSITY_CUTOFF_KEY = "PROCESS_MGF_INTENSITY_CUTOFF";
    private static final String CLEAN_CONFIG_NAME_KEY = "PROCESS_MGF_CLEAN_CONFIG_NAME";
    private static final String PRECURSOR_COMPUTATION_METHOD_KEY = "PROCESS_MGF_PRECURSOR_COMPUTATION_METHOD";
    private static final String EXPORT_PROLINE_TITLE_KEY = "PROCESS_MGF_EXPORT_PROLINE_TITLE";
    private static final String MZ_TOLERANCE_KEY = "PROCESS_MGF_MZ_TOLERANCE";
    private static final String CLEAN_LABEL_METHOD_NAME_KEY = "PROCESS_MGF_CLEAN_LABEL_METHOD_NAME";
    private static final String CLEAN_METHOD_NAME_KEY = "PROCESS_MGF_CLEAN_METHOD_NAME";

    private static final String MGFBOOST_CONFIG_TEMPLATE_KEY = "PROCESS_MGF_MGFBOOST_CONFIG_NAME";

    //Command PropertiesDefault Values
    private static float mz_tolerance = (float) 10.0;
    private static float intensity_cutoff = (float) 0.0;
    private static String precursor_computation_method = "main_precursor_mz";

    private static CreateMgfCommand instance;
    private final Logger logger = LoggerFactory.getLogger(CreateMgfCommand.class);
    private MgfBoostConfigTemplate boostConfigTemplate;

    private CreateMgfCommand(){
        super();
    }

    public static CreateMgfCommand getInstance(){
        if(instance == null)
            instance = new CreateMgfCommand();
        return instance;
    }

    public AbstractCommandPanel<CommandArguments.MzDBCreateMgfCommand> getConfigurationPanel() {
        if (configurationPanel == null) {
            MzdbCreateMgfPanel panel = new MzdbCreateMgfPanel(false);
            panel.updatePanelFromCommand(command);
            panel.setMgfBoostConfigTemplate(boostConfigTemplate);
            configurationPanel = panel;
        }
        return configurationPanel;
    }

    public Properties getCommandProperties(){
        Properties prop = new Properties();
        prop.setProperty(MZ_TOLERANCE_KEY, String.valueOf(command.mzTolPPM));
        prop.setProperty(INTENSITY_CUTOFF_KEY, String.valueOf(command.intensityCutoff));
        prop.setProperty(PRECURSOR_COMPUTATION_METHOD_KEY, command.precMzComputation);
        prop.setProperty(EXPORT_PROLINE_TITLE_KEY, String.valueOf(command.exportProlineTitle));
        prop.setProperty(CLEAN_METHOD_NAME_KEY, command.cleanMethod);
        prop.setProperty(CLEAN_LABEL_METHOD_NAME_KEY, command.cleanLabelMethodName);
        prop.setProperty(CLEAN_CONFIG_NAME_KEY, command.cleanConfig != null ? command.cleanConfig.getConfigCommandValue(): "");
        if (command.precMzComputation.equalsIgnoreCase("mgf_boost")) {
            prop.setProperty(MGFBOOST_CONFIG_TEMPLATE_KEY, String.valueOf(((MzdbCreateMgfPanel)getConfigurationPanel()).getMgfBoostConfigTemplate()));
        } else {
            prop.setProperty(MGFBOOST_CONFIG_TEMPLATE_KEY, "");
        }
        return prop;
    }

    public void loadProperties(Properties prop){

        command.mzTolPPM = Float.parseFloat(prop.getOrDefault(MZ_TOLERANCE_KEY, mz_tolerance).toString());
        logger.debug("mz_tolerance: "+ command.mzTolPPM);

        command.intensityCutoff = Float.parseFloat(prop.getOrDefault(INTENSITY_CUTOFF_KEY, intensity_cutoff).toString());
        logger.debug("mz_tolerance: "+ command.intensityCutoff);

        if (prop.getProperty(PRECURSOR_COMPUTATION_METHOD_KEY) != null) {
            command.precMzComputation = prop.getProperty(PRECURSOR_COMPUTATION_METHOD_KEY);
        }
        logger.debug("precMzComputation: "+command.precMzComputation);

        if (command.precMzComputation.equalsIgnoreCase("mgf_boost")) {

            String boostConfigTemplateStr = prop.getOrDefault(MGFBOOST_CONFIG_TEMPLATE_KEY, MgfBoostConfigTemplate.DISCOVERY.toString()).toString();
            boostConfigTemplate = MgfBoostConfigTemplate.valueOf(boostConfigTemplateStr);

            command.useHeader = boostConfigTemplate.isUseHeader();
            logger.debug("useHeader: "+ command.useHeader);

            command.useSelectionWindow = boostConfigTemplate.isUseSelectionWindow();
            logger.debug("useSelectionWindow: "+ command.useSelectionWindow);

            command.swMaxPrecursorsCount = boostConfigTemplate.getSwMaxPrecursorsCount();
            logger.debug("swMaxPrecursorsCount: "+ command.swMaxPrecursorsCount);

            command.swIntensityThreshold = boostConfigTemplate.getSwIntensityThreshold();
            logger.debug("swIntensityThreshold: "+ command.swIntensityThreshold);

            command.scanSelectorMode = boostConfigTemplate.getScanSelector();
            logger.debug("scanSelectorMode: "+ command.scanSelectorMode);

            command.pifThreshold = boostConfigTemplate.getPifThreshold();
            logger.debug("pifThreshold: "+ command.pifThreshold);

            command.rankThreshold = boostConfigTemplate.getRankThreshold();
            logger.debug("rankThreshold: "+ command.rankThreshold);

        }

        command.exportProlineTitle = Boolean.parseBoolean( prop.getOrDefault(EXPORT_PROLINE_TITLE_KEY, "false").toString());
        logger.debug("exportProlineTitle: "+command.exportProlineTitle);

        command.cleanMethod = prop.getOrDefault(CLEAN_METHOD_NAME_KEY, "").toString();
        logger.debug("cleanMethod: "+command.cleanMethod);

        command.cleanLabelMethodName = prop.getOrDefault(CLEAN_LABEL_METHOD_NAME_KEY, "").toString();
        logger.debug("cleanLabelMethodName: "+ command.cleanLabelMethodName);

        command.cleanConfig = CommandArguments.CleanConfig.LABEL_FREE;
        if(prop.containsKey(CLEAN_CONFIG_NAME_KEY)){
            CommandArguments.CleanConfig cfg = CommandArguments.CleanConfig.getConfigFromCommandValue(prop.getProperty(CLEAN_CONFIG_NAME_KEY));
            if(cfg != null)
                command.cleanConfig = cfg;
        }
        logger.debug("cleanConfig: "+ (command.cleanConfig != null ? command.cleanConfig.getConfigCommandValue() : ""));

    }

}
