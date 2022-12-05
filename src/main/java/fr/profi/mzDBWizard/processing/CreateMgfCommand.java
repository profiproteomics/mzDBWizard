package fr.profi.mzDBWizard.processing;

import fr.profi.mgfboost.ui.command.MzdbCreateMgfCommand;
import fr.profi.mgfboost.ui.command.ui.MzdbCreateMgfPanel;
import fr.profi.mzknife.CommandArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public class CreateMgfCommand extends MzdbCreateMgfCommand {

    // Command Properties Keys
    public static final String INTENSITY_CUTOFF_KEY = "PROCESS_MGF_INTENSITY_CUTOFF";
    public static final String PCLEAN_CONFIG_NAME_KEY = "PCLEAN_CONFIG_NAME";
    public static final String PRECURSOR_COMPUTATION_METHOD_KEY = "PROCESS_MGF_PRECURSOR_COMPUTATION_METHOD";
    public static final String EXPORT_PROLINE_TITLE_KEY = "PROCESS_MGF_EXPORT_PROLINE_TITLE";
    public static final String MZ_TOLERANCE_KEY = "PROCESS_MGF_MZ_TOLERANCE";
    public static final String PCLEAN_LABEL_METHOD_NAME_KEY = "PCLEAN_LABEL_METHOD_NAME";
    public static final String PROCESS_MGF_PCLEAN_KEY = "PROCESS_MGF_PCLEAN";

    //Command PropertiesDefautl Values
    private static float mz_tolerance = (float) 10.0;
    private static float intensity_cutoff = (float) 0.0;
    private static String precursor_computation_method = "main_precursor_mz";
    private static boolean generate_mgf_operation = false;
    private static boolean exportProlineTitle = true;
    private static boolean processPClean = true;
    private static String pCleanLabelMethodName = "";
    private static String pCleanConfigName = "";

    private static CreateMgfCommand instance;
    private final Logger logger = LoggerFactory.getLogger(CreateMgfCommand.class);

    private CreateMgfCommand(){
        super();
    }

    public static CreateMgfCommand getInstance(){
        if(instance == null)
            instance = new CreateMgfCommand();
        return instance;
    }

    @Override
    public MzdbCreateMgfPanel getConfigurationPanel() {
        if (configurationPanel == null) {
            configurationPanel = new MzdbCreateMgfPanel(command, false);
        }
        return configurationPanel;
    }

    public CommandArguments.MzDBCreateMgfCommand getCommand(){
        return command;
    }

    public Properties getCommandProperties(){
        Properties prop = new Properties();
        prop.setProperty(MZ_TOLERANCE_KEY, String.valueOf(command.mzTolPPM));
        prop.setProperty(INTENSITY_CUTOFF_KEY, String.valueOf(command.intensityCutoff));
        prop.setProperty(PRECURSOR_COMPUTATION_METHOD_KEY, command.precMzComputation);
        prop.setProperty(EXPORT_PROLINE_TITLE_KEY, String.valueOf(command.exportProlineTitle));
        prop.setProperty(PROCESS_MGF_PCLEAN_KEY, String.valueOf(command.pClean));
        prop.setProperty(PCLEAN_LABEL_METHOD_NAME_KEY, command.pCleanLabelMethodName);
        prop.setProperty(PCLEAN_CONFIG_NAME_KEY, command.pCleanConfig != null ? command.pCleanConfig.getConfigCommandValue(): "");
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

        command.exportProlineTitle = Boolean.parseBoolean( prop.getOrDefault(EXPORT_PROLINE_TITLE_KEY, "false").toString());
        logger.debug("exportProlineTitle: "+command.exportProlineTitle);

        command.pClean = Boolean.parseBoolean(prop.getOrDefault(PROCESS_MGF_PCLEAN_KEY, "false").toString());
        logger.debug("pClean: "+command.pClean);

        command.pCleanLabelMethodName  = prop.getOrDefault(PCLEAN_LABEL_METHOD_NAME_KEY, "").toString();
        logger.debug("pCleanLabelMethodName: "+ pCleanLabelMethodName);

        command.pCleanConfig =CommandArguments.PCleanConfig.LABEL_FREE;
        if(prop.containsKey(PCLEAN_CONFIG_NAME_KEY)){
            CommandArguments.PCleanConfig cfg = CommandArguments.PCleanConfig.getConfigFor(prop.getProperty(PCLEAN_CONFIG_NAME_KEY));
            if(cfg != null)
                command.pCleanConfig = cfg;
        }
        logger.debug("pCleanConfig: "+ (command.pCleanConfig != null ? command.pCleanConfig.getConfigCommandValue() : ""));

    }
}
