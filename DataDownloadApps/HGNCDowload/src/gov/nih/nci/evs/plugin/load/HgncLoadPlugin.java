package gov.nih.nci.evs.plugin.load;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import gov.nih.nci.evs.dda.PluginInterface;

public class HgncLoadPlugin implements PluginInterface {
    
//    @echo off
//    REM Loads an OWL file. You can provide a manifest file to configure coding scheme
//    REM meta data.
//    REM
//    REM Options:
//    REM   -in,--input <uri> URI or path specifying location of the source file
//    REM   -mf,--manifest <uri> URI or path specifying location of the manifest file
//    REM   -lp,--loaderPrefs<uri> URI or path specifying location of the loader preference file
//    REM   -v, --validate <int> Perform validation of the candidate
//    REM         resource without loading data.  If specified, the '-a' and '-t'
//    REM         options are ignored.  Supported levels of validation include:
//    REM         0 = Verify document is well-formed
//    REM         1 = Verify document is valid
//    REM   -a, --activate ActivateScheme on successful load; if unspecified the vocabulary is loaded but not activated
//    REM   -t, --tag <id> An optional tag ID (e.g. 'PRODUCTION' or 'TEST') to assign. 
//    REM
//    REM Example: LoadOWL -in "file:///path/to/somefile.owl" -a
//    REM          LoadOWL -in "file:///path/to/somefile.owl" -v 0
//    REM
//    java -Xmx1000m -XX:MaxPermSize=256M -cp "..\runtime\lbPatch.jar;..\runtime\lbRuntime.jar" org.LexGrid.LexBIG.admin.LoadOWL %*
    
    
    //Main program has lbConfig.props pointing at indexes and lbRuntime.jar (and patch)
    //Auxilliary path is where to find manifest and PF?
    

    private Logger LOGGER;
    boolean hasError = false;
    String inputFile = null;
    String version = null;
    String dateString = null;
    String pluginDir = "";
    String auxPath = "";
    URI manifestFile=null;
    URI preferencesFile = null;
    String lbRuntimePath = "";
    @Override
    public void setLogger(Logger logger) {
        LOGGER = logger;
    }

    @Override
    public String getPluginName() {
        return "HgncLoadPlugin";
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public void setAuxilliaryPath(String path) {
        this.auxPath = path;
        manifestFile = URI.create(path+"/HGNC_manifest.xml");
        preferencesFile = URI.create(path+"/HGNC_preferences.xml");
    }

    @Override
    public void execute(String pluginDir) {
        
        this.pluginDir = pluginDir;
        calculateVersion();
        calculateDateString();
        updateMetadata();
        updateManifest();
        if(!hasError){
        callLoadOWL();}
        else {
            LOGGER.severe("Load not processed due to previous error");
        }
    }

    @Override
    public String getOutputFile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setInputFile(String file) {
        this.inputFile = file;

    }
    
    private void updateMetadata(){
        //update with date in 1 place and version in 2 places.
        //read in manifest template and put out the manifest
        
        try {
            FileReader metaIn;
            metaIn = new FileReader(auxPath+"/HGNC_metadata_template.xml");

            BufferedReader br = new BufferedReader(metaIn);
            FileWriter metaOut = new FileWriter(auxPath+"/HGNC_metadata.xml");
            BufferedWriter bw = new BufferedWriter(metaOut);
            
            String line = "";
            String dateReg = "releaseDate>template";
            String verReg = "version>template";
            while ((line = br.readLine()) !=null){
//              System.out.println(line);
                                    
                    Pattern date = Pattern.compile(dateReg);
                    
                    // get a matcher object
                    Matcher m = date.matcher(line);
                    line = m.replaceAll("releaseDate>"+dateString);
                    
                    Pattern vers = Pattern.compile(verReg);
                    
                    m = vers.matcher(line);
                    
                    line = m.replaceAll("version>"+version);
//                    System.out.println(line);
                    bw.write(line+"\n");
                }
            br.close();
            bw.close();
            metaIn.close();
            metaOut.close();

        } catch (IOException e) {
            LOGGER.severe("Unable to update metadata due to io error\n" + e.getMessage());
            e.printStackTrace();
            hasError=true;
        }    
//        <met:version_info is_current="true">
//        <met:version>March2014</met:version>
//        <met:status>production</met:status>
//        <met:download_info>
//            <met:download_format>SQL</met:download_format>
//            <met:download_url></met:download_url>
//        </met:download_info>
//        <met:version_releaseDate>2014-03-04-08:00</met:version_releaseDate>
//    </met:version_info>
//    
//    <met:term_browser_version>March2014</met:term_browser_version>
    
    
    }
    
    private void updateManifest(){
        //update version in one place
        
        try{
            FileReader mfIn = new FileReader(auxPath+"/HGNC_manifest_template.xml");
            BufferedReader br = new BufferedReader(mfIn);
            FileWriter mfOut = new FileWriter(auxPath+"/HGNC_manifest.xml");
            BufferedWriter bw = new BufferedWriter(mfOut);
            
            String line = "";
            String dateReg = "Version toOverride=\"true\">template";

            while ((line = br.readLine()) !=null){
//              System.out.println(line);
                                    
                    Pattern date = Pattern.compile(dateReg);
                    
                    // get a matcher object
                    Matcher m = date.matcher(line);
                    line = m.replaceAll("Version toOverride=\"true\">"+version);

//                    System.out.println(line);
                    bw.write(line+"\n");
                }
            br.close();
            bw.close();
            mfIn.close();
            mfOut.close();

        }catch (IOException e){
            e.printStackTrace();
            LOGGER.severe("Unable to update metadata due to io error\n" + e.getMessage());
            hasError=true;
        }
        
        
        
//        <representsVersion toOverride="true">November_2013</representsVersion>
        
    }
    
    private void calculateVersion(){
        //this will be a combination of the month and year
        String dateFormat = "MMMMyyyy";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        version = sdf.format(cal.getTime());
    }
    
    private void calculateDateString(){
        //sed it out of filename?
        //if sed not possible, use current date
        String dateFormat = "yyyy-MM-dd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        dateString = sdf.format(cal.getTime());
    }

    
    private void callLoadOWL(){
        //pass in location of config file ./config/HgncToOwl.properties
        //pass in Xmx  -Xmx2500M
        //main class  gov.nih.nci.evs.hgnc.HgncToOwl
        //jar  HGNCtoOWL.jar
         
        String jar = lbRuntimePath+"runtime/lbRuntime.jar"+ File.pathSeparatorChar + lbRuntimePath + "runtime/lbPatch.jar";
//        String lib = "./lib/lbRuntime.jar";
        String config="-DLG_CONFIG_FILE="+lbRuntimePath + "resources/config/lbconfig.props";
        
        String inputURI = convertToURI(inputFile);
        
//        LoadOWL.main(buildArguments());
        
//        ProcessBuilder pb = new ProcessBuilder("java","-cp",jar,"org.LexGrid.LexBIG.admin.LoadOWL", "-i", inputURI, "-mf", manifestFile.toString(), "-lp", preferencesFile.toString() );
        ProcessBuilder pb = new ProcessBuilder("java",config,"-Xmx1000m" ,"-XX:MaxPermSize=256M","-cp",jar,"org.LexGrid.LexBIG.admin.LoadOWL","-in", inputURI, "-mf", manifestFile.toString(), "-lp", preferencesFile.toString(), "-a");
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
    
    private String[] buildArguments(){
        String inputURI = convertToURI(inputFile);
        String[] sendArgs= new String[]{"-i", inputURI, "-mf", manifestFile.toString(), "-lp", preferencesFile.toString()};
        return sendArgs;
    }
    
    private String convertToURI(String filePath){
        String newURI = null;
        filePath = filePath.replace("\\", "/");
        filePath = "file:///" + filePath;
        File f = new File(filePath);
        newURI = f.toURI().toString();
        return filePath;
    }

    public void setLbRuntimePath(String inlbRuntimePath) {
        this.lbRuntimePath = inlbRuntimePath;
        
    }
}
