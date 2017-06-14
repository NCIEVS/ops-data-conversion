//using PluginDemo from JavaRanch

package gov.nih.nci.evs.dda;

import gov.nih.nci.evs.MyLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DataDownloadApp {
    //This class deals with user interaction.  It passes actual control of the app to ApplicationController
    private final static Logger LOGGER = Logger.getLogger(DataDownloadApp.class.getName());

    //The plugins information from the plugin.config file goes into the pluginMap
    HashMap<String,PluginData> pluginMap = new HashMap<String,PluginData>();
    boolean doDownload = false;
    boolean doProcessing = false;
    boolean doLoad = false;
    String vocabName= null;
    String lbConfigPath = null;
    PluginData pluginData = null;
    String pluginPath = null;
    String auxPath = null;  //option to add an auxilliary path of any kind
    String inputFile = ""; //location and name of the file to be processed or loaded.
    String lbRuntimePath = ""; //location of the lbRuntime for lexevs loads.  

    /**
     * @param args
     */
    public static void main(String[] args) {



        try {
            MyLogger.setup();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        DataDownloadApp dda = new DataDownloadApp(args);
        //test method to check if logger debugging
        //        dda.logSomething();


        //pass in vocab name and binary for actions?
        //HGNC,1,1,0 for run HGNC download, process but not load
        //Or have parameters for each.  -l=load, -p=process, -d=download
        //So args must be 2 to 4 characters in length?  Or just show the list if no vocab name?

        //User calls  "DDA -d" and we prompt for a vocab
        //User calls "DDA HGNC -d -p" we download then process HGNC
        //User calls "DDA Hugo -d -p" we prompt them to select a vocab, then download and process
        //User calls "DDA HGNC -d -l" we warn them that HGNC needs processing and close
        //User calls "DDA HGNC" we warn them that they must chose an action, print options and close
        //User passes no vocab and no parameter we give them a list of options and close

        //        ApplicationController ac = new ApplicationController(dda.pluginMap,dda.doDownload,dda.doProcessing,dda.doLoad);


    }


    private void logSomething(){
        //Debugger to check that I have the logger set up correctly
        LOGGER.setLevel(Level.SEVERE);
        LOGGER.severe("severe");
        LOGGER.info("Info");

        LOGGER.setLevel(Level.INFO);
        LOGGER.severe("severe");
        LOGGER.info("Info");
    }


    public DataDownloadApp(String[] args){
        if (args.length==0){
            printArgs();
        }
        //        try {
        //            MyLogger.setup();
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //        logSomething();

        String vocabName = getListOfActions(args);
        readPluginList();
        pluginData = pluginMap.get(vocabName);
        if(pluginData==null){
            //print list of available vocabs and ask which they want.
            pluginData = displayVocabList();
        }

        //TODO If user chose download and load, check to see if there is a process plugin listed
        //Suggest they use it if there are any

        if(auxPath==null){
            ApplicationController ac = new ApplicationController(pluginData,pluginPath,doDownload,doProcessing,doLoad,inputFile);
        } else
        {
            ApplicationController ac = new ApplicationController(pluginData,pluginPath,doDownload,doProcessing,doLoad,inputFile,auxPath, lbRuntimePath);
        }

    }

    private String getListOfActions(String[] args){
        try {
            if (args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    String option = args[i];
                    if (option.equalsIgnoreCase("-d")) {
                        doDownload = true;
                    } else if (option.equalsIgnoreCase("-p")) {
                        doProcessing = true;
                    } else if (option.equalsIgnoreCase("-l")) {
                        doLoad = true;
                    } else if (option.equalsIgnoreCase("-f")){
                        pluginPath = args[++i];
                        lbConfigPath = pluginPath + "/lbconfig.props";
                    } else if (option.equalsIgnoreCase("-a")){
                        auxPath = args[++i];
                    } else if (option.equalsIgnoreCase("-i")){
                        inputFile = args[++i];
                    } else if (option.equalsIgnoreCase("-e")){
                        lbRuntimePath = args[++i];
                    }
                }

                //check vs list of vocabs in plugin

            }
            //check to see if any of the "do" booleans are true.
            if(!doDownload&&!doProcessing&&!doLoad){
                System.out.println("You have not chosen an action to perform");
                LOGGER.severe("No action chosen to be performed");
                printArgs();
            }
            
            if(doLoad && lbRuntimePath.equals("")){
                System.out.println("You must provide a location for the lbRuntime when doing loads");
                LOGGER.severe("Load selected but no lbRuntime provided");
                printArgs();
            }

            String upperName = args[0].toUpperCase();
            LOGGER.info("Vocabulary Name entered = " + upperName);
            return upperName;
        }
        catch(Exception e){
            LOGGER.severe("Error when selecting vocabulary");
            printArgs();
            return null;
        }
    }

    private PluginData displayVocabList()  {
        try{
            Set<String> tempList =  pluginMap.keySet();
            TreeSet<String> vocabList = new TreeSet<String>(tempList);
            Integer i=0;

            //Display a numbered list of possible vocabularies
            System.out.println("Please select a vocabulary to work with");
            for(String vocab:vocabList){
                System.out.println(i+ " -- "+ vocab);
                i++;
            }

            //get choice from keyboard
            InputStreamReader in = new InputStreamReader(System.in);
            BufferedReader keyboardEntry = new BufferedReader(in);
            String s = keyboardEntry.readLine();

            LOGGER.info("User Input: " + s);
            //should be a single digit number.
            String entry = s.substring(0,1);
            Integer choice = new Integer(entry);

            //Use the number to grab the vocab Name
            String[] vocabs = vocabList.toArray(new String[vocabList.size()]);
            String vocabChoice = vocabs[choice];
            if(vocabChoice != null){
                LOGGER.info("User chose "+ choice +"   "+ vocabChoice+ " from vocabulary list");
                return pluginMap.get(vocabChoice);}


        }
        catch (Exception e){
            System.out.println("Invalid entry");
            LOGGER.severe("Problem selecting vocabulary from list");
            System.exit(1);
        }
        return null;
    }

    private void readPluginList(){
        //This method will read in the plugin config file.
        FileReader fr=null;
        BufferedReader bfr=null;
        try {
            fr = new FileReader(pluginPath + "/plugin.config");
            bfr = new BufferedReader(fr);
            boolean eof = false;
            while (!eof) {
                String line = bfr.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    parsePluginLine(line);
                }
            }
            bfr.close();
            fr.close();
        } catch (IOException e) {
            // Exceptions in accessing the file
            LOGGER.severe("Unable to read from plugin.config file");
            e.printStackTrace();
        } catch (Exception e) {
            // Exceptions with string or object processing
            LOGGER.severe("Unable to create list of plugins");
            e.printStackTrace();
        }finally {
            // Closing the streams
            try {
                if (bfr != null) {
                    bfr.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (Exception e) {
                System.out.println("plugin.config not found or unreadable");
                e.printStackTrace();
            }
        }

    }

    private void parsePluginLine(String line) throws Exception{
        //Take the pipe-delimited string and load it into plugin object
        String[] parsedLine = line.split("\\|");
        PluginData pd = new PluginData();
        pd.setVocabName(parsedLine[0]);
        pd.setDownloadPluginName(parsedLine[1]);
        pd.setProcessPluginName(parsedLine[2]);
        pd.setLoadPluginName(parsedLine[3]);
        pluginMap.put(pd.getVocabName().toUpperCase(), pd);
    }

    /**
     * Prints the help.
     */
    public static void printArgs() {
        System.out.println("");
        System.out.println("Usage: DataDownloadApp [Vocab Name] [OPTIONS] ");
        System.out.println("  At least one action must be chosen: download, process or load to LexEVS");
        System.out.println(" ");
        System.out
        .println("  [vocab name]\tTells what vocabulary we are working with");
        System.out.println("  -f [dir] The location of the plugins directory (default ./plugins");
        System.out.println("  -a \t\tThe location of any config file or other aux info");
        System.out.println("  -i \t\tInput file.  For process and load this will tell where to find the file");
        System.out.println("  -e, \t\tLexBIG path location (Ex: /LexBIG/6.1/).  Required if doing a load");
        System.out.println("  -d, \t\tDownload data file from web");
        System.out.println("  -p, \t\tProcess downloaded data file");
        System.out
        .println("  -l, \t\tLoad to LexEVS");
        System.out.println("");
        System.exit(1);
    }
}