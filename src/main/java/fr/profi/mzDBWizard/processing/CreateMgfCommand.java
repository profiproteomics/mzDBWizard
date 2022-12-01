package fr.profi.mzDBWizard.processing;

import fr.profi.mgfboost.ui.command.ui.MzdbCreateMgfPanel;
import fr.profi.mzDBWizard.configuration.ConfigurationManager;
import fr.profi.mzknife.CommandArguments;


public class CreateMgfCommand {

    private CommandArguments.MzDBCreateMgfCommand command;
    private MzdbCreateMgfPanel configurationPanel = null;

    private static CreateMgfCommand instance;
    private CreateMgfCommand(){
        command = new CommandArguments.MzDBCreateMgfCommand();
        initCommand();
    }

    private void initCommand(){
        command.precMzComputation = ConfigurationManager.getPrecursorComputationMethod().name();
        command.mzTolPPM = ConfigurationManager.getMzTolerance();
        command.intensityCutoff = ConfigurationManager.getIntensityCutoff();
//        command.exportProlineTitle = ConfigurationManager.
    }

    public static CreateMgfCommand getInstance(){
        if(instance == null)
            instance = new CreateMgfCommand();
        return instance;
    }


    public MzdbCreateMgfPanel getConfigurationPanel() {
        if (configurationPanel == null) {
            configurationPanel = new MzdbCreateMgfPanel(command, false);
        }
        return configurationPanel;
    }

    public boolean buildCommand() {
        return getConfigurationPanel().buildCommand(command);
    }

    public CommandArguments.MzDBCreateMgfCommand getCommand(){
        return command;
    }

    public void showError(){
        configurationPanel.showErrorMessage();
    }

}
