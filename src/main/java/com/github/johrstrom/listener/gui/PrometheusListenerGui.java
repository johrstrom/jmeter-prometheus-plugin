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

import java.util.Arrays;
import java.util.Collection;

import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;

import org.apache.jmeter.gui.GUIFactory;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;

import com.github.johrstrom.collector.SampleCollectorConfig;
import com.github.johrstrom.collector.gui.AbstractCollectorGui;
import com.github.johrstrom.collector.gui.Flatten;
import com.github.johrstrom.collector.gui.SampleCollectorGuiHelper;
import com.github.johrstrom.listener.PrometheusListener;

/**
 * The GUI class for the Prometheus Listener.
 * 
 * Currently, all configurations are done through properties files so this class
 * shows nothing visually other than comments.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class PrometheusListenerGui extends AbstractCollectorGui<SampleCollectorConfig>  {


	private static final long serialVersionUID = 4984653136457108054L;
	
	static {
		GUIFactory.registerIcon(PrometheusListenerGui.class.getName(),GUIFactory.getIcon(AbstractListenerGui.class));
	}
	
	
	public PrometheusListenerGui() {
		super(SampleCollectorConfig.class);
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

	@Override
	public JPopupMenu createPopupMenu() {
		return MenuFactory.getDefaultVisualizerMenu();
	}

	@Override
	public TestElement createTestElement() {
		if(this.collector == null) {
			this.collector = new PrometheusListener();
		}
		
		this.collector.setProperty(TestElement.GUI_CLASS, PrometheusListenerGui.class.getName());
		this.collector.setProperty(TestElement.TEST_CLASS, PrometheusListener.class.getName());
		this.modifyTestElement(collector);
		
		return (TestElement) collector.clone();
	}

	@Override
	public Collection<String> getMenuCategories() {
		 return Arrays.asList(MenuFactory.LISTENERS);
	}

	@Override
	public Flatten getGuiHelper() {
		return new SampleCollectorGuiHelper();
	}

	@Override
	public void modifyColumns() {
		TableColumn column = table.getColumnModel().getColumn(SampleCollectorGuiHelper.TYPE_INDEX);
		column.setCellEditor(new DefaultCellEditor(SampleCollectorGuiHelper.typeComboBox));
		
		column = table.getColumnModel().getColumn(SampleCollectorGuiHelper.LISTEN_TO_INDEX);
		column.setCellEditor(new DefaultCellEditor(SampleCollectorGuiHelper.listenToComboBox));
	}


	

}
