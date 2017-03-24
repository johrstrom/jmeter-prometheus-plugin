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

public class PrometheusListenerGui extends AbstractListenerGui {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4984653136457108054L;

	public PrometheusListenerGui(){
		super();
		init();
	}
	
	public TestElement createTestElement() {		
		PrometheusListener list = new PrometheusListener(getStaticLabel());
		modifyTestElement(list);
		
		list.setProperty(TestElement.GUI_CLASS, org.ohrstrom.listener.gui.PrometheusListenerGui.class.getName());
		list.setProperty(TestElement.TEST_CLASS, org.ohrstrom.listener.PrometheusListener.class.getName());
		
		return list;
	}

	public String getLabelResource() {
		return getClass().getCanonicalName();
	}	

	@Override
	public String getStaticLabel() {
		return "Prometheus Listener";
	}

	public void modifyTestElement(TestElement arg0) {
		super.configureTestElement(arg0);
		
		configureTestElement(arg0);
	}
	
	
	
	
	@Override
	public String getName() {
		if (super.getName() == null) {
			return this.getStaticLabel();
		}else{
			return super.getName();
		}
	}


	@Override
	public void configure(TestElement element) {
		super.configure(element);
	}

	private void init(){
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		

	}

}
