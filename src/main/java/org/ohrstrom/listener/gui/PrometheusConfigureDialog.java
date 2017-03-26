package org.ohrstrom.listener.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.ohrstrom.listener.PrometheusSaveConfig;

public class PrometheusConfigureDialog extends JDialog implements ActionListener{

	private static final long serialVersionUID = 7132092878660788111L;

	PrometheusConfigureDialog(Frame owner, String title, boolean modal, PrometheusSaveConfig config){
		super(owner, title, modal);
		
		init();
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void init(){
		this.getContentPane().setLayout(new BorderLayout());
		final int configCount = (PrometheusSaveConfig.SAVE_CONFIG_NAMES.size() / 3) + 1;
		 
		JPanel checkPanel = new JPanel(new GridLayout(configCount, 3));
		for(String name : PrometheusSaveConfig.SAVE_CONFIG_NAMES){
			
			
		}
		
		
	}

}
