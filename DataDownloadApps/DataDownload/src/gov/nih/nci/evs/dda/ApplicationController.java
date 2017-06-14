package gov.nih.nci.evs.dda;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

enum pluginType{DOWNLOAD, PROCESS, LOAD};

public class ApplicationController {
//    List<PluginInterface> downloadPlugins;
    PluginInterface downloadPlugin;
//    List<PluginInterface> processPlugins;
//    List<PluginInterface> loadPlugins;
    String pluginDir= null;
    String auxPath;
    String outputFile="";
    String inputFile="";
    String lbRuntimePath="";

    private final static Logger LOGGER = Logger.getLogger(DataDownloadApp.class.getName());
    PluginData pluginData = null;


    public ApplicationController(PluginData inPluginData, String inPluginDir,
            boolean doDownload, boolean doProcessing, boolean doLoad, String inInputFile,String inAuxPath, String inlbRunPath) {

        this.pluginData=inPluginData;
        this.pluginDir=inPluginDir;
        boolean hasError=false;
        this.auxPath = inAuxPath;
        this.inputFile = inInputFile;
        this.lbRuntimePath = inlbRunPath;
        //Start calling plugins and doing processing.
        if(doDownload){
//            hasError = getDownloadPlugin();
            hasError = getPlugin(pluginType.DOWNLOAD);
        }
        if (hasError){
            LOGGER.severe("Error in download plugin.  Please review logs.  Program stopping");
            System.out.println("Error in download plugin.  Please review logs.  Program stopping");
            doProcessing = false;
            doLoad = false;
        }
        
        //call processing plugin
        if(doProcessing){
            hasError = getPlugin(pluginType.PROCESS);
        }
        if (hasError){
            LOGGER.severe("Error in processing plugin.  Please review logs.  Program stopping");
            System.out.println("Error in processing plugin.  Please review logs.  Program stopping");
            doLoad = false;
        }
        
        //call load plugin.
        if(doLoad){
            hasError = getPlugin(pluginType.LOAD);
        }
        if (hasError){
            LOGGER.severe("Error in load plugin.  Please review logs.  Program stopping");
            System.out.println("Error in load plugin.  Please review logs.  Program stopping");
        }

    }
    
    public ApplicationController(PluginData inPluginData, String inPluginDir,
            boolean doDownload, boolean doProcessing, boolean doLoad, String inputFile) {
        //If they don't pass in an auxilliary path, just use current
        this(inPluginData, inPluginDir, doDownload, doProcessing, doLoad, inputFile,inPluginDir,null); 
    }
    
    
    
    private boolean getPlugin(pluginType pType){
        boolean hasError = false;
        String pluginName = null;
        String packageName = "gov.nih.nci.evs.plugin.";
        String fileDir = "";

        switch(pType){
            case DOWNLOAD:
                pluginName = pluginData.getDownloadPluginName();
                packageName = packageName + "download";
                fileDir = pluginDir + "/download";
                break;
            case PROCESS:
                pluginName = pluginData.getProcessPluginName();
                packageName = packageName + "process";
                fileDir = pluginDir + "/process";
                if (outputFile.length()>0){
                    inputFile = outputFile;
                }
                break;
            case LOAD:
                pluginName = pluginData.getLoadPluginName();
                packageName = packageName+"load";
                fileDir = pluginDir + "/load";
                if(outputFile.length()>0){
                    inputFile = outputFile;
                }
                break;
            default:
                LOGGER.severe("No proper action chosen to perform");
                return true;
        }
        
        File dir = new File(fileDir);
        PluginLoader cl = new PluginLoader(dir);
        PluginInterface plugin = null;
        if (dir.exists() && dir.isDirectory()){
            try{
                Class c = cl.loadClass(pluginName, packageName);
                Class[] intf = c.getInterfaces();
                for (int j=0; j<intf.length;j++){
                    if (intf[j].getName().equals("gov.nih.nci.evs.dda.PluginInterface")){
                        plugin = (PluginInterface) c.newInstance();
                    }
                }
                plugin.setAuxilliaryPath(auxPath);
                plugin.setLogger(LOGGER);
                if (inputFile.length()>0){
                    plugin.setInputFile(inputFile);
                    }
                if(lbRuntimePath!=null && lbRuntimePath.length()>0){
                    plugin.setLbRuntimePath(lbRuntimePath);
                }
                plugin.execute(pluginDir);
                hasError = plugin.hasError();
                outputFile = plugin.getOutputFile();

            } 
            catch (InstantiationException e) {
                System.err.println("File "+ pluginName+ " does not contain a valid PluginInterface class.");
                LOGGER.severe("File "+ pluginName  + " does not contain a valid PluginInterface class.");
                hasError = true;
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                System.err.println("IllegalAccess to " + pluginName);
                LOGGER.severe("IllegalAccess to " + pluginName);
                hasError = true;
            } catch (ClassNotFoundException e) {
                System.err.println("Class "+ pluginName + " cannot be found in directory.");
                LOGGER.severe("Class "+ pluginName  + " cannot be found in directory.");
                hasError = true;
            }
        } else if (!dir.exists()){
            LOGGER.severe("Plugins directory does not exist");
            hasError = true;
        } else {
            LOGGER.severe("Plugins address is not a directory");
            hasError = true;
        }
        return hasError;
    }


}
