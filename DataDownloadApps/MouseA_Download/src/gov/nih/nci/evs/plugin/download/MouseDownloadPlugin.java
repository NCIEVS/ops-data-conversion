package gov.nih.nci.evs.plugin.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import gov.nih.nci.evs.dda.PluginInterface;

public class MouseDownloadPlugin implements PluginInterface {

    boolean hasError = false;
    private static Logger LOGGER = null;
    private String auxPath = "";
    private String pluginDir = "";
    URL maURL= null;
    private String outputFile = "";
    private String saveTo;
    
    @Override
    public void setLogger(Logger logger) {
        this.LOGGER = logger;

    }

    @Override
    public String getPluginName() {
        return "MouseDownloadPlugin";
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
        // TODO Auto-generated method stub
//ftp://ftp.informatics.jax.org/pub/reports/adult_mouse_anatomy.obo
        this.pluginDir = pluginDir;
        this.saveTo=pluginDir + "\\data\\";
        
        try{
            
            maURL = new URL("ftp://ftp.informatics.jax.org/pub/reports/");
            
            downloadMAobo();
            
        } catch (MalformedURLException e) {
            LOGGER.severe("Invalid URL ");
            e.printStackTrace();
        }
    }

    private void downloadMAobo() {

        String maString = "ftp.informatics.jax.org";
        String maDir = "pub/reports";
       FTPClient ftpClient = new FTPClient();
       
       try{
           ftpClient.connect(maString);
           logFTPserver(ftpClient);
           int replyCode = ftpClient.getReplyCode();
           if (!FTPReply.isPositiveCompletion(replyCode)){
               LOGGER.severe("Connect failed: "+ replyCode);
               return;
           }
           boolean loginSuccess = ftpClient.login("anonymous", "");
           logFTPserver(ftpClient);
           if(!loginSuccess){
               LOGGER.severe("Could not login to server");
               return;
           } 
               LOGGER.info("Login successful");
               ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
               ftpClient.enterLocalPassiveMode();
               ftpClient.setControlKeepAliveTimeout(300);
           FTPFile[] files = ftpClient.listFiles(maDir,filter);
           String fileName="";
           if(files==null || files.length<1){
               LOGGER.severe("No files found for " + getMADateString());
               hasError = true;
               return;
           }
           else if(files!= null && files.length==1){
//           for (FTPFile file:files){
//               System.out.println(file.getName());
//               
//           }
            fileName= files[0].getName();}
           else if (files.length>1){
               LOGGER.warning("More than one file returned");
               fileName= files[0].getName();
           }
           String downloadFile = saveTo + fileName;
           FileOutputStream out = new FileOutputStream(downloadFile);
           String remotePath = maDir+"/" + fileName;
           
           hasError = ftpClient.retrieveFile(remotePath, out);
           String replyString = ftpClient.getReplyString();
           out.close();
           ftpClient.logout();
           ftpClient.disconnect();
           renameOBO();
           if(hasError){
               LOGGER.severe("File not retrieved");
               
           }
           
       }catch(IOException e){
           hasError=true;
               LOGGER.severe("Error when downloading MA " + e.getMessage());
               e.printStackTrace();
           } finally {
               if (ftpClient.isConnected()){
                   try{
                       ftpClient.logout();
                       ftpClient.disconnect();
                   }catch (IOException e){
                       hasError = true;
                       LOGGER.severe("Trouble disconnecting " + e.getMessage());
                   }
               }
           }
       
    }
    
    private void renameOBO() throws IOException {
        String version = getMAVersion();
        File file = new File(saveTo+"adult_mouse_anatomy.obo");
        String newName = saveTo + "adult_mouse_anatomy_"+version+".obo";
        File file2 = new File(newName);
        if (file2.exists()) {
            LOGGER.warning(newName + " exists");
            hasError = true;
            throw new java.io.IOException("file exists");
            }
        hasError = file.renameTo(file2);
        outputFile = newName;
    }

    private String getMAVersion() {
        // extract the version from the file, rename chebi.obo with this version.
        String version = "";
        int i=0;
        FileReader file;
        try {
            file = new FileReader(saveTo + "adult_mouse_anatomy.obo");

        BufferedReader bfr = new BufferedReader(file);
        String line = "";
        while ((line = bfr.readLine())!=null){
            i++;
            
            //The version and date will appear within the first 10 lines of the OBO file.
            if (i>10) {
                break;
            }
            
            if (line.contains("date")){
               
                version = (line.substring(6,16).trim());
                version = version.replace(":", "-");
            }

        }
        
        
        bfr.close();
       
        } catch (IOException e) {
            // TODO Auto-generated catch block
            hasError = true;
            LOGGER.severe("Issue reading downloaded file");
            e.printStackTrace();
        } 
        return version;
    }
    
    private void logFTPserver(FTPClient ftpClient){
        String[] replies = ftpClient.getReplyStrings();
        if (replies !=null && replies.length>0){
            for (String reply:replies){
                LOGGER.info(reply);
            }
        }
    }
    
    
    @Override
    public String getOutputFile() {
        return outputFile;
    }

    @Override
    public void setInputFile(String file) {
        // not relevant for download

    }

    @Override
    public void setLbRuntimePath(String lbRuntimePath) {
        // not relevant for download

    }
    
    private String getMADateString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        String dateString = sdf.format(cal.getTime());
        dateString = dateString + "-01";
        return dateString;
    }
    
    FTPFileFilter filter = new FTPFileFilter() {
        public boolean accept(FTPFile ftpFile){
            String searchString = "adult_mouse_anatomy.obo";
            return ftpFile.isFile() && ftpFile.getName().contains(searchString);
        }
    };
    
}
