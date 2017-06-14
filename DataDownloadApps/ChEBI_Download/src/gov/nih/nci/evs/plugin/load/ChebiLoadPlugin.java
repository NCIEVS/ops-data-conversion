package gov.nih.nci.evs.plugin.load;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.nci.evs.dda.PluginInterface;

public class ChebiLoadPlugin implements PluginInterface {

    private Logger LOGGER;
    private boolean hasError = false;
    private String auxPath = "";
    private String pluginDir = "";
    private String inputFile = "";
    private String lbRuntimePath = "";
    private String version = "";
    private String dateString = "";
    URI manifestFile=null;
    URI preferencesFile = null;
    
    
    @Override
    public void setLogger(Logger logger) {
       this.LOGGER = logger;

    }

    @Override
    public String getPluginName() {
        
        return "ChebiLoadPlugin";
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public void setAuxilliaryPath(String path) {
        this.auxPath = path;

    }

    @Override
    public void execute(String pluginDir) {
        this.pluginDir = pluginDir;
        calculateVersion();
        calculateDateString();
        updateMetadata();
        updateManifest();
        if(!hasError){
        callLoadOBO();}
        else {
            LOGGER.severe("Load not processed due to previous error");
        }
    }

    private void callLoadOBO() {
        String jar = lbRuntimePath+"runtime/lbRuntime.jar"+ File.pathSeparatorChar + lbRuntimePath + "runtime/lbPatch.jar";
        String config="-DLG_CONFIG_FILE="+lbRuntimePath + "resources/config/lbconfig.props";
        String inputURI = convertToURI(inputFile);
        ProcessBuilder pb = new ProcessBuilder("java",config,"-Xmx1000m" ,"-XX:MaxPermSize=256M","-cp",jar,"org.LexGrid.LexBIG.admin.LoadOBO","-in", inputURI, "-mf", manifestFile.toString(),  "-a");
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
        
    }
    
    private String convertToURI(String filePath){
        String newURI = null;
        filePath = filePath.replace("\\", "/");
        filePath = "file:///" + filePath;
        File f = new File(filePath);
        newURI = f.toURI().toString();
        return filePath;
    }
    
    private void calculateVersion(){
        //this will be a combination of the month and year
//        String dateFormat = "MMMMyyyy";
//        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//        
//        version = sdf.format(cal.getTime());
        
        int dotLocation = inputFile.lastIndexOf(".");
        int scoreLocation = inputFile.lastIndexOf("_");
        version = inputFile.substring(scoreLocation+1,dotLocation-1);
    }
    
    private void calculateDateString(){
        //sed it out of filename?
        //if sed not possible, use current date
        String dateFormat = "yyyy-MM-dd";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        
        dateString = sdf.format(cal.getTime());
    }
    
    private void updateMetadata(){
        //update with date in 1 place and version in 2 places.
        //read in manifest template and put out the manifest
        
        try {
            FileReader metaIn;
            metaIn = new FileReader(auxPath+"/ChEBI_metadata_template.xml");

            BufferedReader br = new BufferedReader(metaIn);
            FileWriter metaOut = new FileWriter(auxPath+"/ChEBI_metadata.xml");
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
            FileReader mfIn = new FileReader(auxPath+"/ChEBI_manifest_template.xml");
            BufferedReader br = new BufferedReader(mfIn);
            FileWriter mfOut = new FileWriter(auxPath+"/ChEBI_manifest.xml");
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
    }
    
    
    @Override
    public String getOutputFile() {
        
        return null;
    }

    @Override
    public void setInputFile(String file) {
        this.inputFile= file;

    }

    @Override
    public void setLbRuntimePath(String lbRuntimePath) {
       this.lbRuntimePath = lbRuntimePath;

    }

}
