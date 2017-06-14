package gov.nih.nci.evs.plugin.download;

import gov.nih.nci.evs.dda.PluginInterface;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class HgncDownloadPlugin implements PluginInterface {

    boolean hasError=false;
    private static Logger LOGGER = null;
    private String saveTo;
    private String downloadedFile;
    private String unzippedFile;
    
    @Override
    public String getPluginName() {

        return "HgncDownloadPlugin";
    }

    @Override
    public boolean hasError() {

        return hasError;
    }
    
    public void setLogger(Logger logger){
        LOGGER = logger;
    }


    @Override
    public void execute(String pluginDir) {
        // TODO check to see if anything on the column definitions page has changed.
        //  if it has, warn user and quit.  Tell them the new hash has been saved so rerun to proceed.
        // save new hash
        // http://www.genenames.org/help/download#Curated_Field_Definitions
        this.saveTo=pluginDir + "/data/";
        String hash = readWebPage();
        if (hash!=null){
            if(checkHash(hash)){
                writeHash(hash);
                LOGGER.warning("Column definitions changed.  Please review before processing");
                hasError=true;
            }
            else {
                LOGGER.info("No changes in Column definitions");
            }
        }

        //If all looks good, go grab HGNC
        //ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc_complete_set.txt.gz
        downloadHgnc();
        
        LOGGER.info("Download process complete");
    }

    private boolean checkHash(String newHash){

        FileReader fr = null;
        BufferedReader bfr = null;

        //check if hgnc_hash.txt exists
        //if not, get hash and save.
        try {
            fr = new FileReader("./hgnc_hash.txt");
            bfr = new BufferedReader(fr);
            String line = bfr.readLine();
            bfr.close();
            fr.close();
            if(line.equals(newHash))
                return false;
            else
                return true;


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            LOGGER.severe("hgnc_hash.txt not found. Writing a new one.  Please check columns manually");
//            e.printStackTrace();
            hasError = true;
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
            LOGGER.severe("Unable to read hgnc_hash.txt");
            hasError = true;
            return true;
        }

    }

    private String readWebPage(){
        //Navigate to http://www.genenames.org/help/download#Curated_Field_Definitions
        // and read the page into local store or stream
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            InputStream is = new URL("http://www.genenames.org/help/download#Curated_Field_Definitions").openStream();

            byte[] dataBytes = new byte[1024];
            int nread = 0;
            while ((nread = is.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            };
            byte[] mdbytes = md.digest();

            //            BufferedReader dis = new BufferedReader(new InputStreamReader(is));
            //            String s = null;
            //            while((s = dis.readLine())!=null){
            //                System.out.println(s);
            //            }

            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<mdbytes.length;i++) {
                hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
            }
            //            System.out.println("Hex format : " + hexString.toString());
            String hash = hexString.toString();

            return hash;

        } catch (IOException e) {
            // TODO Auto-generated catch block
            hasError = true;
            LOGGER.severe("Trouble reading Curated Field Definition web page");
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e){
            hasError = true;
            LOGGER.severe("Unable to calculate hash due to unrecognized algorithm");
            e.printStackTrace();
        }
        return null;
    }



    private void writeHash(String hash){
        //write the hashcode to the hgnc_hash.txt is the local directory
        try {
            PrintWriter pw = new PrintWriter("hgnc_hash.txt");
            pw.println(hash);
            pw.close();
        } catch (FileNotFoundException e) {
            LOGGER.severe("Unable to print to hgnc_hash.txt");
            hasError=true;
            e.printStackTrace();
        }

    }

    private void downloadHgnc(){
        //        ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc_complete_set.txt.gz
        boolean downloadError = false;
        
        String urlString = "ftp://ftp.ebi.ac.uk/pub/databases/genenames/hgnc_complete_set.txt.gz";

        String dateFormat = "yy_MM_dd";
        try {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            downloadedFile = "hgnc_"+sdf.format(cal.getTime())+".gz";
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream in = conn.getInputStream();
            downloadedFile = saveTo + downloadedFile;
            FileOutputStream out = new FileOutputStream(downloadedFile);
            byte[] b = new byte[1024];
            int count;
            while ((count = in.read(b))>=0){
                out.write(b, 0, count);
            }
            out.flush();
            out.close();
            in.close();
            
            unzipHgnc();
        } catch (MalformedURLException e) {

            LOGGER.severe("URL not valid " + urlString);
            downloadError=true;
        } catch (IOException e) {
            LOGGER.severe("Problem creating file at " + saveTo);
            downloadError=true;
        }

        hasError = downloadError;
    }
    
    private void unzipHgnc(){
        byte[] buffer = new byte[1024];
        String dateFormat = "yy_MM_dd";
        try{
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            FileInputStream fileIn = new FileInputStream(downloadedFile);
            GZIPInputStream gZipInputStream = new GZIPInputStream(fileIn);
            unzippedFile = "hgnc_complete_"+sdf.format(cal.getTime())+".txt";
            unzippedFile = saveTo + unzippedFile;
            FileOutputStream fileOutputStream = new FileOutputStream(unzippedFile);
            int bytes_read;
            while((bytes_read = gZipInputStream.read(buffer)) > 0){
                fileOutputStream.write(buffer, 0, bytes_read);
            }
            gZipInputStream.close();
            fileOutputStream.close();
            
            LOGGER.info("Successfully unzipped :" + unzippedFile);
        } catch (IOException e){
            LOGGER.severe("Unable to unzip downloaded hgnc file "+ downloadedFile + " to "+ unzippedFile);
            hasError = true;
        }
    }
    
    @Override
    public void setAuxilliaryPath(String path) {
        saveTo = path + "/download/data/";
    }

    @Override
    public String getOutputFile() {
        // Get the path of the file we just downloaded
        return unzippedFile;
    }

    @Override
    public void setInputFile(String file) {
        // Not relevant to this plugin
        
    }

    @Override
    public void setLbRuntimePath(String lbRuntimePath) {
        // Not relevant for download
        
    }
}
