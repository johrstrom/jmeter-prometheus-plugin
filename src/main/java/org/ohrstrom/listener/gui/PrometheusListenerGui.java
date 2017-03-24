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
package org.ohrstrom.listener.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.ohrstrom.listener.PrometheusListener;

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
		
		listener.setProperty(TestElement.GUI_CLASS, org.ohrstrom.listener.gui.PrometheusListenerGui.class.getName());
		listener.setProperty(TestElement.TEST_CLASS, org.ohrstrom.listener.PrometheusListener.class.getName());
		
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
	public void modifyTestElement(TestElement arg0) {
		super.configureTestElement(arg0);
		
		configureTestElement(arg0);
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
	}

	
	/**
	 * Private helper function to initialize all the Swing 
	 * components. 
	 */
	private void init(){
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		

	}

}
