package gov.nih.nci.evs.plugin.download;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;



import gov.nih.nci.evs.dda.FTPFileFilter;
import gov.nih.nci.evs.dda.PluginInterface;

public class GoDownloadPlugin implements PluginInterface {

    boolean hasError = false;
    private static Logger LOGGER = null;
    private String auxPath = "";
    private String pluginDir = "";
    URL goURL= null;
    private String outputFile = "";
    private String saveTo;

    @Override
    public void setLogger(Logger logger) {
        LOGGER = logger;
        
    }

    @Override
    public String getPluginName() {
        
        return "GoDownloadPlugin";
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
        this.saveTo=pluginDir + "\\data\\";
        try {
//            quick test that we have a working URL
            goURL = new URL("ftp://ftp.geneontology.org/go/ontology-archive");
            
            downloadGOobo();
            
            downloadGOowl();            
            
            
        } catch (MalformedURLException e) {
            LOGGER.severe("Invalid URL ");
            e.printStackTrace();
        }

        
        LOGGER.info("Download process complete");
    }

    private void downloadGOowl() {
        //This is for the day when we start downloading GO owl full time
        //For now, this is just for testing the code to do so
        
    }

    private void downloadGOobo() {
        // will download OBO from ftp://ftp.geneontology.org/go/ontology-archive
       // file will be gene_ontology_edit.obo.yyyy-MM-dd.gz
        String goString = "ftp.geneontology.org";
        String goDir = "go/ontology-archive";
       FTPClient ftpClient = new FTPClient();
       try{
           ftpClient.connect(goString);
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
               String searchString = "gene_ontology_edit.obo." + getGoDateString();
               FTPFileFilter filter = new FTPFileFilter(searchString);
           FTPFile[] files = ftpClient.listFiles(goDir,filter);
           String fileName="";
           if(files==null || files.length<1){
               LOGGER.severe("No files found for " + getGoDateString());
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
           String remotePath = goDir+"/" + fileName;
           
           hasError = ftpClient.retrieveFile(remotePath, out);
           String replyString = ftpClient.getReplyString();
           out.close();
           ftpClient.logout();
           ftpClient.disconnect();
           unzipGO(downloadFile);
           if(hasError){
               LOGGER.severe("File not retrieved");
               
           }
           
       }catch(IOException e){
           hasError=true;
               LOGGER.severe("Error when downloading GO " + e.getMessage());
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
        
    

    private void logFTPserver(FTPClient ftpClient){
        String[] replies = ftpClient.getReplyStrings();
        if (replies !=null && replies.length>0){
            for (String reply:replies){
                LOGGER.info(reply);
            }
        }
    }
    
    
    
//    private void GoGet(){
//        //check the directory vs local date to see if new version up
//        //files are at http://www.geneontology.org/ontology-archive/
//        String goString = "ftp://ftp.geneontology.org/go/ontology-archive";
//        try {
//            
//            goURL= new URL(goString);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            LOGGER.severe("Unable to connect to GO download site");
//        }
//        //get a list of the gene-ontology-edit.obo files.
//        try {
//            Document doc = Jsoup.connect(goString).get();
//            Elements links = doc.getElementsByTag("a");
//            for (Element link : links) {
////              System.out.println(link.attr("href") + " - " + link.text());
//                if(link.text().startsWith("gene_ontology_edit.obo")){
//                    System.out.println(link.attr("href") + " - " + link.text());
//                }
//            }
//            int elID = links.size()-1;
//            Element goDownload = links.get(elID);
//            String latestFile = goDownload.text();
//            System.out.println("Latest GO file "+ latestFile);
//            LOGGER.info("Downloading GO file " + latestFile);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//        
//        //download zip file and unpack. 
//        
//        //place in DL_Folder
//    }
    
    
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
    
    
    private String getGoDateString(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Calendar cal = Calendar.getInstance();
        String dateString = sdf.format(cal.getTime());
        dateString = dateString + "-01";
        return dateString;
    }
    
    private void unzipGO(String downloadFile){
        byte[] buffer = new byte[1024];
        String unzippedFile = "";
        try{

            FileInputStream fileIn = new FileInputStream(downloadFile);
            //trim off the end .gz
            unzippedFile = downloadFile.replace(".gz", "");
            //remove the .obo from the middle of the file
            unzippedFile = unzippedFile.replace(".obo.", "_");
            //tack the .obo onto the end of the file
            unzippedFile = unzippedFile + ".obo";
            GZIPInputStream gZipInputStream = new GZIPInputStream(fileIn);
//            unzippedFile = "hgnc_complete_"+sdf.format(cal.getTime())+".txt";
//            unzippedFile = saveTo + newFile;
            FileOutputStream fileOutputStream = new FileOutputStream(unzippedFile);
            int bytes_read;
            while((bytes_read = gZipInputStream.read(buffer)) > 0){
                fileOutputStream.write(buffer, 0, bytes_read);
            }
            gZipInputStream.close();
            fileOutputStream.close();
            
            LOGGER.info("Successfully unzipped :" + unzippedFile);
        } catch (IOException e){
            LOGGER.severe("Unable to unzip downloaded hgnc file "+ downloadFile + " to "+ unzippedFile);
            hasError = true;
        }
    }
    
    
//    
//    FTPFileFilter filter = new FTPFileFilter() {
//        public boolean accept(FTPFile ftpFile){
//            String searchString = "gene_ontology_edit.obo." + getGoDateString();
//            return ftpFile.isFile() && ftpFile.getName().contains(searchString);
//        }
//    };


}



