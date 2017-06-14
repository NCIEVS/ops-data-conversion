package gov.nih.nci.evs.dda;

import org.apache.commons.net.ftp.FTPFile;


public class FTPFileFilter implements org.apache.commons.net.ftp.FTPFileFilter{
String fileName;
    public FTPFileFilter(String fileName) {
       this.fileName=fileName;
    }
    
    public boolean accept(FTPFile ftpFile){
        String searchString = fileName;
        return ftpFile.isFile() && ftpFile.getName().contains(searchString);
    }
}
