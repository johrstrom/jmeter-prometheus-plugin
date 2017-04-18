package com.github.johrstrom.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class PrometheusSaveConfig {
	
	private static final Logger log = LoggingManager.getLoggerForClass();

    public static final List<String> SAVE_CONFIG_NAMES = Collections.unmodifiableList(Arrays.asList(new String[]{
            "Label",
            "Code", // Response Code
            "Success",
        }));
    
    private boolean label, code, success;
    
    public PrometheusSaveConfig(){
    	this(true);
    }
   
    public PrometheusSaveConfig(boolean save){
    	this.setSaveLabel(save);
    	this.setSaveCode(save);
    	this.setSaveSuccess(save);
    }
    
    public boolean saveLabel(){
    	return this.label;
    }
    
    public void setSaveLabel(boolean save){
    	log.debug("Setting save label to " + save);
    	this.label = save;
    }
    
    public boolean saveCode(){
    	return this.code;
    }
    
    public void setSaveCode(boolean save){
    	log.debug("Setting save code to " + save);
    	this.code = save;
    }
    
    public boolean saveSuccess(){
    	return this.success;
    }
    
    public void setSaveSuccess(boolean save){
    	log.debug("Setting save success to " + save);
    	this.success = save;
    }
}
