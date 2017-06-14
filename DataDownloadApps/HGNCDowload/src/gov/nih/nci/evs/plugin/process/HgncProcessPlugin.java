package gov.nih.nci.evs.plugin.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import gov.nih.nci.evs.dda.PluginInterface;

public class HgncProcessPlugin implements PluginInterface {

    boolean hasError = false;
    private Logger LOGGER = null;
    private String configFilePath = null;
    private String inputFile = null;
    String pluginDir = "";
    String outputFile = "";
    
    
    @Override
    public void setLogger(Logger logger) {
        // TODO Auto-generated method stub
        LOGGER = logger;
    }

    @Override
    public String getPluginName() {
        return "HgncProcessPlugin";
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public void execute(String inPluginDir) {
        // TODO Find the file location
        // Call the hgnc processing app and pass in parameters
        this.pluginDir = inPluginDir;
        callHgncToOwl();
    }

    private boolean unzipData(){
        //find the hgnc file in the data directory and unzip it.
        //Need to  have name of file passed in?
        boolean unzipSuccess = true;
        
        return unzipSuccess;
    }
    
    private String getOutputName(){
        String outputName = "";
        try{
            String dateFormat = "yyyy_MM_dd";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            outputName = "hgnc_"+sdf.format(cal.getTime())+".owl";
            return outputName;
        }catch (Exception e){
            return "Hgnc.owl";
        }
        
    }
    
    private void callHgncToOwl(){
        //pass in location of config file ./config/HgncToOwl.properties
        //pass in Xmx  -Xmx2500M
        //main class  gov.nih.nci.evs.hgnc.HgncToOwl
        //jar  HGNCtoOWL.jar
        
        String jar = pluginDir+"/lib/HGNCtoOWL.jar";
//        String lib = "./lib/*.jar";
        outputFile = pluginDir + "/data/" + getOutputName();
        String inputURI = convertToURI(inputFile);
        String outputURI = convertToURI(outputFile);
        
        ProcessBuilder pb = new ProcessBuilder("java","-jar",jar,"-c",configFilePath, "-s", inputURI,"-t",outputURI);
        pb.redirectErrorStream(true);
        
        InputStream is = null;
                try{
                    Process process = pb.start();
                    is = process.getInputStream();
                    
                    int value;
                    while ((value = is.read())!= -1){
                        char inChar = (char)value;
                        System.out.print(inChar);
                    }
                } catch (IOException e){
                    e.printStackTrace();
                    hasError=true;
                }
        
        //Can pass in SysProp to find properties
//        if (configFileName == null) {
//            final String filename = sysProp.getProperty("HgncToOwl.properties");
//            configFileName = filename;
//        }
        
        
        
    }
    
    private String convertToURI(String filePath){
        String newURI = null;
        filePath = filePath.replace("\\", "/");
        filePath = "file:///" + filePath;
        File f = new File(filePath);
        newURI = f.toURI().toString();
        return filePath;
    }

    @Override
    public void setAuxilliaryPath(String path) {
        configFilePath=path+"/HgncToOwl.properties";
    }

    @Override
    public String getOutputFile() {
        // TODO Auto-generated method stub
        return outputFile;
    }

    @Override
    public void setInputFile(String file) {
        
        this.inputFile = file;
    }

    @Override
    public void setLbRuntimePath(String inlbRuntimePath) {
        
        
    }
}
