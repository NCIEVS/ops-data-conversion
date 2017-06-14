package gov.nih.nci.evs.plugin.test;

import java.util.logging.Logger;


import gov.nih.nci.evs.dda.DataDownloadApp;
import gov.nih.nci.evs.plugin.download.ZebrafishDownloadPlugin;
import gov.nih.nci.evs.plugin.load.ZebrafishLoadPlugin;



public class MockApplicationController {

    String auxPath = null;
    String inputFile = null;
    String pluginDir = null;
    String outputFile = null;
    boolean hasError = false;
    String lbConfigPath = null;
    boolean doDownload = false;
    boolean doProcessing = false;
    boolean doLoad = false;
    String lbRuntimePath = "";
    private final static Logger LOGGER = Logger.getLogger(MockApplicationController.class.getName());
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // This class is just to test and debug the plugins.
        MockApplicationController mock = new MockApplicationController();
        mock.processArgs(args);
    }
    
    private void processArgs(String[] args){
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String option = args[i];
                if (option.equalsIgnoreCase("-d")) {
                    doDownload = true;
                } else if (option.equalsIgnoreCase("-p")) {
                    doProcessing = true;
                } else if (option.equalsIgnoreCase("-l")) {
                    doLoad = true;
                } else if (option.equalsIgnoreCase("-f")){
                    pluginDir = args[++i];
                    lbConfigPath = pluginDir + "/lbconfig.props";
                    setSystemVariable(lbConfigPath);
                } else if (option.equalsIgnoreCase("-a")){
                    auxPath = args[++i];
                } else if (option.equalsIgnoreCase("-i")){
                    inputFile = args[++i];
                }
            }
            if(doDownload) testDownload();
            if(doProcessing) testProcess();
            if(doLoad) testLoader();

        }
    }
    
    private void setSystemVariable(String lbConfigPath2) {
        // TODO Auto-generated method stub
        
        
    }

    private void testDownload(){
        ZebrafishDownloadPlugin plugin = new ZebrafishDownloadPlugin();
//        plugin.setAuxilliaryPath(auxPath);
        plugin.setLogger(LOGGER);
        if (inputFile!= null && inputFile.length()>0){
            plugin.setInputFile(inputFile);
            }
        plugin.execute(pluginDir);
        hasError = plugin.hasError();
        outputFile = plugin.getOutputFile();
    }
    
    private void testProcess(){
        
    }
    
    private void testLoader(){
        ZebrafishLoadPlugin plugin = new ZebrafishLoadPlugin();
        plugin.setAuxilliaryPath(auxPath);
        plugin.setLogger(LOGGER);
        plugin.setLbRuntimePath(lbRuntimePath);
        if (inputFile!= null && inputFile.length()>0){
            plugin.setInputFile(inputFile);
            }
        plugin.execute(pluginDir);
        hasError = plugin.hasError();
    }

}
