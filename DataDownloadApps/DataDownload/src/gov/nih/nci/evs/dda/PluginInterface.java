//From plugin demo at JavaCodeRanch

package gov.nih.nci.evs.dda;

import java.util.logging.Logger;

public interface PluginInterface {

    public void setLogger(Logger logger);

    // return the name of this plugin
    public String getPluginName();

    // can be called to determine whether the plugin
    // aborted execution due to an error condition
    public boolean hasError();
  
  //Some plugins may need a path set to a file or directory to either read or store
    public void setAuxilliaryPath(String path);

    public void execute(String pluginDir);
        
    public String getOutputFile();
    
    public void setInputFile(String file);

    public void setLbRuntimePath(String lbRuntimePath);
        
}
