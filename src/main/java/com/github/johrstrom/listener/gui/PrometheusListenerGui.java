/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.johrstrom.listener.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.PrometheusListener;
import com.github.johrstrom.listener.PrometheusSaveConfig;

/**
 * The GUI class for the Prometheus Listener.
 * 
 * Currently, all configurations are done through properties files so this class
 * shows nothing visually other than comments.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class PrometheusListenerGui extends AbstractListenerGui {

	private static final long serialVersionUID = 4984653136457108054L;
	public static final String SAVE_CONFIG = "johrstrom.prometheus.save_config";
	private static final Logger log = LoggerFactory.getLogger(PrometheusListenerGui.class);
	
	//Server related configs
	private JTextField portTextField;

	/**
	 * Default constructor
	 */
	public PrometheusListenerGui() {
		super();
		createGUI();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		PrometheusListener listener = new PrometheusListener();
		modifyTestElement(listener);
		listener.setProperty(TestElement.GUI_CLASS,
				com.github.johrstrom.listener.gui.PrometheusListenerGui.class.getName());
		listener.setProperty(TestElement.TEST_CLASS, com.github.johrstrom.listener.PrometheusListener.class.getName());

		return listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	public String getLabelResource() {
		return getClass().getCanonicalName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getStaticLabel()
	 */
	@Override
	public String getStaticLabel() {
		return "Prometheus Listener";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.
	 * jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);

		if (element instanceof PrometheusListener) {
			PrometheusListener listener = (PrometheusListener) element;
			
			PrometheusSaveConfig config = new PrometheusSaveConfig();
			this.setServerConfigs(config);			
			
			listener.setSaveConfig(config);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getName()
	 */
	@Override
	public String getName() {
		if (super.getName() == null) {
			return this.getStaticLabel();
		} else {
			return super.getName();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jmeter.gui.AbstractJMeterGuiComponent#configure(org.apache.
	 * jmeter.testelement.TestElement)
	 */
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		if (element instanceof PrometheusListener) {
			PrometheusSaveConfig config = ((PrometheusListener) element).getSaveConfig();
			
			this.portTextField.setText(Integer.toString(config.getPort()));
		}
	}

	/**
	 * Private helper function to initialize all the Swing components.
	 */
	protected void createGUI() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout(0, 5));
		mainPanel.add(makeTitlePanel(), BorderLayout.NORTH);

		add(mainPanel, BorderLayout.NORTH);
		add(createTopMostPanel(), BorderLayout.CENTER);
	}
	
	
	/**
	 * Create the panel that holds all the other panels (except for the title panel)
	 * 
	 * @return - the top most JPanel
	 */
	protected JPanel createTopMostPanel(){
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		
		panel.add(createServerPanel(), BorderLayout.NORTH);
		
		return panel;
	}
	
	/**
	 * Create the panel that holds all the server configuration (ports, config files etc.) 
	 * 
	 * @return - the server configuration panel
	 */
	protected JPanel createServerPanel(){
		HorizontalPanel panel = new HorizontalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Server Configurations"));
		
		panel.add(this.createPortPanel());
		
		return panel;
	}
	
	
	/**
	 * Create the panel that holds the {@link #portTextField} for configuring the servers port.
	 * 
	 * @return
	 */
	protected JPanel createPortPanel(){
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel("Port");
        
		panel.add(label, BorderLayout.WEST);
        this.portTextField = new JTextField();
        panel.add(portTextField, BorderLayout.CENTER);
        
        return panel;
	}
	
	
	/**
	 * Set the input save configuration such that it reflects what's in the GUI.
	 * I.e., what's stored in checkboxes and text fields.
	 * 
	 * @param config - the save config to modify
	 */
	protected void setServerConfigs(PrometheusSaveConfig config){
		int port = config.getPort();
		try {
			port = Integer.parseInt(this.portTextField.getText());
		} catch (NumberFormatException e){
			log.error("Caught {} while trying to parse {} to string. Using {} port.", 
					e.getClass(), this.portTextField.getText(), port);
		}
		
		config.setPort(port);
		
	}

}
