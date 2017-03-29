package org.ohrstrom.listener.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.ohrstrom.listener.PrometheusSaveConfig;

public class PrometheusConfigureDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 7132092878660788111L;
	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private PrometheusSaveConfig config;
	Map<String, Method> accessors;
	Map<String, Method> mutators;

	PrometheusConfigureDialog(Frame owner, String title, boolean modal, PrometheusSaveConfig config){
		super(owner, title, modal);
		this.config = config;
		init();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void init(){
		this.initConfigFunctions();
		
		this.getContentPane().setLayout(new BorderLayout());
		final int configCount = (PrometheusSaveConfig.SAVE_CONFIG_NAMES.size() / 3) + 1;
		 
		JPanel checkPanel = new JPanel(new GridLayout(configCount, 3));
		for(String name : PrometheusSaveConfig.SAVE_CONFIG_NAMES){
			
			Method m = this.accessors.get(name);
			Boolean b = true;
			try {
				b = (Boolean) m.invoke(this.config);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Cannot invoke method " + m.toString()  + "");
			}
            JCheckBox check = new JCheckBox(name,b);
            
            check.addActionListener(this);
            check.setActionCommand(name);
            checkPanel.add(check);
            
		}
		
		
	}
	
	
	private void initConfigFunctions(){
		String getPrefix = "save";
		String setPrefix = "setSave";
		this.accessors = new HashMap<String, Method>();
		this.mutators = new HashMap<String, Method>();
		
		
		for(String name : PrometheusSaveConfig.SAVE_CONFIG_NAMES){
			String setFuncName = setPrefix + name;
			String getFuncName = getPrefix + name;
			
			try {
				Method mm = PrometheusSaveConfig.class.getMethod(setFuncName, boolean.class);
				Method am = PrometheusSaveConfig.class.getMethod(getFuncName);
				
				this.mutators.put(name, mm);
				this.accessors.put(name, am);
				
			} catch (NoSuchMethodException | SecurityException e) {
				log.warn("Couldn't load method for PrometheusSaveConfig: " + e.getMessage());
			}
			
			
		}
		
		
	}

}
