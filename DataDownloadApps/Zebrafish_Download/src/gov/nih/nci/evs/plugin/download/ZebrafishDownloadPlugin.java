package gov.nih.nci.evs.plugin.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Logger;

import gov.nih.nci.evs.dda.PluginInterface;

public class ZebrafishDownloadPlugin implements PluginInterface {
    // http://obo.cvs.sourceforge.net/viewvc/obo/obo/ontology/anatomy/gross_anatomy/animal_gross_anatomy/fish/zebrafish_anatomy.obo
    // http://www.berkeleybop.org/ontologies/zfa.obo

    boolean hasError = false;
    private static Logger LOGGER = null;
    private String auxPath = "";
    private String pluginDir = "";
    URL goURL = null;
    private String outputFile = "";
    private String saveTo;
    private String version;

    @Override
    public void setLogger(Logger logger) {
        this.LOGGER = logger;

    }

    @Override
    public String getPluginName() {

        return pluginDir;
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
        this.pluginDir = pluginDir;
        this.saveTo = pluginDir + "\\data\\";
        try {

            downloadZFINobo();

            // downloadZFINowl();

        } catch (Exception e) {
            LOGGER.severe("Invalid URL ");
            e.printStackTrace();
        }

        LOGGER.info("Download process complete");
    }

    private void downloadZFINobo() {
        String bopURL = "http://www.berkeleybop.org/ontologies/";
        String zfinPath = "http://www.berkeleybop.org/ontologies/zfa.obo";
        try {
            File file = new File(saveTo + "/zfa.obo");
            FileWriter out = new FileWriter(file);
            URL zfinURL = new URL(zfinPath);
            URLConnection connection = zfinURL.openConnection();
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.contains("data-version")) {
                    String temp = inputLine;
                    temp = temp.substring(temp.lastIndexOf("/") + 1);
                    version = temp.trim();
                }
                out.write(inputLine + "\n");
            }
            in.close();
            out.close();
            File file2 = new File(saveTo + "zfa_"+version + ".obo");
            file.renameTo(file2);
            outputFile = file2.getAbsolutePath();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public String getOutputFile() {

        return outputFile;
    }

    @Override
    public void setInputFile(String file) {
        // not needed for download

    }

    @Override
    public void setLbRuntimePath(String lbRuntimePath) {
        // irrelevant for download

    }

}
