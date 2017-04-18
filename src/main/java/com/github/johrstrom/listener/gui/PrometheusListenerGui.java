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

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.SavePropertyDialog;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.gui.ComponentUtil;
import com.github.johrstrom.listener.PrometheusListener;
import com.github.johrstrom.listener.PrometheusSaveConfig;

/**
 * The GUI class for the Prometheus Listener. 
 * 
 * Currently, all configurations are done through properties files so this
 * class shows nothing visually other than comments.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class PrometheusListenerGui extends AbstractListenerGui {

	private static final long serialVersionUID = 4984653136457108054L;
	public static final String SAVE_CONFIG = "johrstrom.prometheus.save_config";
	
	
	PrometheusSaveConfig config = new PrometheusSaveConfig();

	/**
	 * Default constructor 
	 */
	public PrometheusListenerGui(){
		super();
		init();
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {		
		PrometheusListener listener = new PrometheusListener();
		modifyTestElement(listener);
		
		listener.setProperty(TestElement.GUI_CLASS, com.github.johrstrom.listener.gui.PrometheusListenerGui.class.getName());
		listener.setProperty(TestElement.TEST_CLASS, com.github.johrstrom.listener.PrometheusListener.class.getName());
		
		return listener;
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	public String getLabelResource() {
		return getClass().getCanonicalName();
	}	

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getStaticLabel()
	 */
	@Override
	public String getStaticLabel() {
		return "Prometheus Listener";
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		super.configureTestElement(element);
		
		if(element instanceof PrometheusListener){
			PrometheusListener listener = (PrometheusListener) element;
			listener.setSaveConfig(this.config);			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#getName()
	 */
	@Override
	public String getName() {
		if (super.getName() == null) {
			return this.getStaticLabel();
		}else{
			return super.getName();
		}
	}


	/* (non-Javadoc)
	 * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#configure(org.apache.jmeter.testelement.TestElement)
	 */
	@Override
	public void configure(TestElement element) {
		super.configure(element);
		if(element instanceof PrometheusListener){
			this.config = ((PrometheusListener) element).getSaveConfig();
		}
	}

	
	/**
	 * Private helper function to initialize all the Swing 
	 * components. 
	 */
	private void init(){
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		JPanel configurePanel = new JPanel();
		configurePanel.setLayout(new BorderLayout(0,5));
		
		configurePanel.add(makeTitlePanel(), BorderLayout.NORTH);
		
		JButton saveConfigButton = new JButton(JMeterUtils.getResString("config_save_settings")); 
		saveConfigButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrometheusConfigureDialog d = new PrometheusConfigureDialog(
                        GuiPackage.getInstance().getMainFrame(),
                        JMeterUtils.getResString("sample_result_save_configuration"),
                        true, config);
                
                d.pack();
                ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), d);
                d.setVisible(true);
            }
        });

		configurePanel.add(saveConfigButton, BorderLayout.EAST);
		
		add(configurePanel, BorderLayout.NORTH);
		
	}

}
