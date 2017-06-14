package gov.nih.nci.evs.dda;

public class PluginData {

    private String vocabName=null;
    private String downloadPluginName=null;
    private String processPluginName=null;
    private String loadPluginName=null;
    
    
    public String getVocabName() {
        return vocabName;
    }
    public void setVocabName(String vocabName) {
        this.vocabName = vocabName;
    }
    public String getDownloadPluginName() {
        return downloadPluginName;
    }
    public void setDownloadPluginName(String downloadPluginName) {
        this.downloadPluginName = downloadPluginName;
    }
    public String getProcessPluginName() {
        return processPluginName;
    }
    public void setProcessPluginName(String processPluginName) {
        this.processPluginName = processPluginName;
    }
    public String getLoadPluginName() {
        return loadPluginName;
    }
    public void setLoadPluginName(String loadPluginName) {
        this.loadPluginName = loadPluginName;
    }
    public boolean requiresProcessing(){
        //check to see if a processPluginName is assigned
        //If none, then no processing is required.
        if (processPluginName==null) return false;
        return true;
    }
    
}
