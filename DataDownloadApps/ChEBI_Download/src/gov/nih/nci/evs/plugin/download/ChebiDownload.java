package gov.nih.nci.evs.plugin.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
//import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;

import gov.nih.nci.evs.dda.PluginInterface;
import gov.nih.nci.evs.dda.FTPFileFilter;

//check ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology for chebi.obo.  Download
// open file and look for 
// format-version: 1.2
// data-version: 117
// date: 06:07:2014 11:17
//make version number out of this and append to output file name.

public class ChebiDownload implements PluginInterface {
    Logger LOGGER;
    boolean hasError = false;
    String auxPath = "";
    String outputFile = "";
    String inputFile = "";
    String pluginDir = "";
    String saveTo = "";
    URL ChebiURL;
    
    @Override
    public void setLogger(Logger logger) {
        this.LOGGER= logger;

    }

    @Override
    public String getPluginName() {
        
        return "ChebiDownload";
    }

    @Override
    public boolean hasError() {
        return hasError;
    }

    @Override
    public void setAuxilliaryPath(String path) {
       this.auxPath=path;

    }

    @Override
    public void execute(String pluginDir) {
        this.pluginDir=pluginDir;
        this.saveTo=pluginDir + "\\data\\";

        try{
            ChebiURL = new URL("ftp://ftp.ebi.ac.uk/pub/databases/chebi/ontology");
            
            downloadChebiOBO();
            
            downloadChebiOWL();
            
        }catch (MalformedURLException e) {
            LOGGER.severe("Invalid URL ");
            e.printStackTrace();
        }
    }

    private void downloadChebiOWL() {
        //This is for the day when we start downloading ChEBI owl full time
        //For now, this is just for testing the code to do so
        
    }

    private void downloadChebiOBO() {
        String chebiString = "ftp.ebi.ac.uk";
        String chebiDir = "pub/databases/chebi/ontology";
        FTPClient ftpClient = new FTPClient();
        try{
            ftpClient.connect(chebiString);
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
//            LOGGER.info("Login successful");
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setControlKeepAliveTimeout(300);
            FTPFileFilter filter = new FTPFileFilter("chebi.obo");
            FTPFile[] files = ftpClient.listFiles(chebiDir,filter);
            String fileName="";
            if(files==null || files.length<1){
                LOGGER.severe("No files found for chebi.obo");
                hasError = true;
                return;
            }
            else if(files!= null && files.length==1){
               fileName= files[0].getName();}
              else if (files.length>1){
                  LOGGER.warning("More than one file returned");
                  fileName= files[0].getName();
              }
            
            String downloadFile = saveTo + fileName;
            FileOutputStream out = new FileOutputStream(downloadFile);
            String remotePath = chebiDir+"/" + fileName;
            hasError = ftpClient.retrieveFile(remotePath, out);
            String replyString = ftpClient.getReplyString();
            out.close();
            ftpClient.logout();
            ftpClient.disconnect();
            if(hasError){
                LOGGER.severe("File not retrieved");
                
            }
            
            renameOBO();
        }catch (IOException e){
            hasError=true;
            LOGGER.severe("Error when downloading ChEBI " + e.getMessage());
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
        String version = getChebiVersion();
        File file = new File(saveTo+"chebi.obo");
        String newName = saveTo + "chebi_v"+version+".obo";
        File file2 = new File(newName);
        if (file2.exists()) {
            LOGGER.warning(newName + " exists");
            hasError = true;
            throw new java.io.IOException("file exists");
            }
        hasError = file.renameTo(file2);
        outputFile = newName;
    }

    private String getChebiVersion() {
        // extract the version from the file, rename chebi.obo with this version.
        String version = "";
        int i=0;
        FileReader file;
        try {
            file = new FileReader(saveTo + "chebi.obo");

        BufferedReader bfr = new BufferedReader(file);
        String line = "";
        while ((line = bfr.readLine())!=null){
            i++;
            
            //The version and date will appear within the first 10 lines of the OBO file.
            if (i>10) {
                break;
            }
            
            if (line.contains("data-version")){
                //data-version: 117
                version = (line.substring(line.indexOf(":")+1).trim());
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
    
//  private void GoGet(){
//  //check the directory vs local date to see if new version up
//  //files are at http://www.geneontology.org/ontology-archive/
//  String goString = "ftp://ftp.geneontology.org/go/ontology-archive";
//  try {
//      
//      goURL= new URL(goString);
//  } catch (MalformedURLException e) {
//      e.printStackTrace();
//      LOGGER.severe("Unable to connect to GO download site");
//  }
//  //get a list of the gene-ontology-edit.obo files.
//  try {
//      Document doc = Jsoup.connect(goString).get();
//      Elements links = doc.getElementsByTag("a");
//      for (Element link : links) {
////        System.out.println(link.attr("href") + " - " + link.text());
//          if(link.text().startsWith("gene_ontology_edit.obo")){
//              System.out.println(link.attr("href") + " - " + link.text());
//          }
//      }
//      int elID = links.size()-1;
//      Element goDownload = links.get(elID);
//      String latestFile = goDownload.text();
//      System.out.println("Latest GO file "+ latestFile);
//      LOGGER.info("Downloading GO file " + latestFile);
//  } catch (IOException ex) {
//      ex.printStackTrace();
//  }
//  
//  //download zip file and unpack. 
//  
//  //place in DL_Folder
//}

    
    @Override
    public String getOutputFile() {
       return outputFile;
    }

    @Override
    public void setInputFile(String file) {
        this.inputFile=file;

    }

    @Override
    public void setLbRuntimePath(String lbRuntimePath) {
        // Not relevant for download

    }

//    FTPFileFilter filter = new FTPFileFilter() {
//        public boolean accept(FTPFile ftpFile){
//            String searchString = "chebi.obo";
//            return ftpFile.isFile() && ftpFile.getName().contains(searchString);
//        }
//    };
}
