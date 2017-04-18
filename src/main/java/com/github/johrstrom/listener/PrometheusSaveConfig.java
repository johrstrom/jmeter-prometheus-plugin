package com.github.johrstrom.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrometheusSaveConfig {

    public static final List<String> SAVE_CONFIG_NAMES = Collections.unmodifiableList(Arrays.asList(new String[]{
            "Label",
            "Code", // Response Code
            "Success",
//            "Bytes",
//            "Url",
//            "FileName",
//            "Latency",
//            "ConnectTime",	//TODO?
//            "IdleTime",		//TODO?
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
    	this.label = save;
    }
    
    public boolean saveCode(){
    	return this.code;
    }
    
    public void setSaveCode(boolean save){
    	this.code = save;
    }
    
    public boolean saveSuccess(){
    	return this.success;
    }
    
    public void setSaveSuccess(boolean save){
    	this.success = save;
    }
}
