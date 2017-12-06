package gov.nih.nci.evs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

public class WriteSystemRelease {

	
    private static void updateSystemRelease(String newLine) throws FileNotFoundException {
        //Open the SystemReleaseHistory file and write out a new line for this month
        try {
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("./NCISystemReleaseHistory.txt", true)));
            writer.println(newLine);
            writer.close();
            
        } catch (IOException e) {
            System.out.println("Unable to write to NCISystemReleaseHistory.txt");
            e.printStackTrace();
        }

    }

    private static Date[] parseSystemReleaseFile(
            Vector<String> systemReleaseFile) {
        //go through the SystemReleaseFile and pull out the publication dates
        //load these dates into an array for use in building the concept history publish files.
        Date[] dates = new Date[systemReleaseFile.size()+1];
        try {
            int i=0;
            for(String historyRow: systemReleaseFile){
                String[] history = historyRow.split("\\|");
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
                Date date = sdf.parse(history[0]);
                dates[i] = date;
                i++;
            }

        } catch (Exception e){
            System.out.println("Unable to parse NCISystemReleaseHistory.txt");
            e.printStackTrace();

        }

        return dates;
    }
    
    private static Vector<String> readSystemReleaseFile() {
        //This file has version numbers and publication dates for all versions of NCIt
        // The file will be assumed to be in the local directory
        //and to be named "NCISystemReleaseHistory.txt"
        Vector<String> v = new Vector<String>();
        FileReader fr = null;
        BufferedReader buff = null;
        try {
            fr = new FileReader("./NCISystemReleaseHistory.txt");
            buff = new BufferedReader(fr);
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) {
                    eof = true;
                } else {
                    v.add(line);
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // Closing the streams
            try {
                buff.close();
                fr.close();
            } catch (Exception e) {
                System.out.println("NCISystemReleaseHistory.txt not found or unreadable");
                e.printStackTrace();
            }
        }

        if (!v.isEmpty())
            return v;
        return null;
    }
    
    private static String ConstructReleaseFileText(String publishDate, String version) throws ParseException{
        //Sample text
        //Editing of NCI Thesaurus 14.03e was completed on March 31, 2014.  Version 14.03e was March's fifth build in our development cycle.
        String textLine = "Editing of NCI Thesaurus "+ version + " was completed on ";
        try {

            //format the publication date for output to the file
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(publishDate);
            sdf.applyLocalizedPattern("MMMM dd, yyyy");
            String dateString = sdf.format(date);
            
            
            textLine = textLine + dateString + ".  Version " + version + " was ";
            
            //Determine the month for output to the publication file
            sdf.applyLocalizedPattern("MMMM");
            String month = sdf.format(date);
            textLine = textLine + month + "'s ";

            //To determine what build this is, for the "March's fourth build..." part
            char versionLetter = version.charAt(version.length()-1);
            String versionOrder = "";
            switch(versionLetter){
                case 'a': versionOrder = "first";
                break;
                case 'b': versionOrder = "second";
                break;
                case 'c': versionOrder = "third";
                break;
                case 'd': versionOrder = "fourth";
                break;
                case 'e': versionOrder = "fifth";
                break;
                case 'f': versionOrder = "sixth";
                break;
                case 'g': versionOrder = "seventh";
                break;
                case 'h': versionOrder = "eighth";
                default: versionOrder = "unknown";
                break;
            }
            
            textLine = textLine + versionOrder + " build in our development cycle.";

        } catch (ParseException e) {
            System.out.println("Unable to generate text for SystemRelease file");
            throw e;
        }
        return textLine;

    }
    
    private static Date[] readAndWriteSystemReleaseFile(
            Vector<String> readSystemReleaseFile, String publishDate, String version) throws ParseException, FileNotFoundException {
        //Take the version and write to the SystemRelease file.
        String urlBit = "http://nci.nih.gov/";
        String urlStub = "http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#:";
        //First check to see if the line is already there.
        String last = readSystemReleaseFile.lastElement();
        if (!last.contains(version)){
            //Grab the previous version out of the last line.
            String[] lastline = last.split("\\|");
            String previousVersion = lastline[3];
            
            //Format the editaction date for the SystemRelease file
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date = sdf.parse(publishDate);
            sdf.applyLocalizedPattern("dd-MMM-yy");
            String dateString = sdf.format(date);

            //Need to construct the new line with date|url|version|old url|text
            String outputLine = dateString + "|" + urlBit + "|" + urlStub +  version + "|" + version + "|" +  urlStub + previousVersion + "|" + ConstructReleaseFileText(publishDate, version);
            readSystemReleaseFile.add(outputLine);
            updateSystemRelease(outputLine);
        }
        
        

        //Then send to the read.
        return parseSystemReleaseFile(readSystemReleaseFile);
    }




}
