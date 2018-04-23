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

import static com.github.johrstrom.listener.PrometheusSaveConfig.*;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
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
	
	
	//Label configs
	private JCheckBox sampleCodeCheckBox;
	private JCheckBox sampleLabelsCheckBox;
	private JCheckBox SampleSuccessCheckBox;
	private JCheckBox sampleCounterCheckbox;
	private JCheckBox sampleSummaryCheckbox;
	private JCheckBox sampleHistogramCheckbox;
	private JCheckBox sampleFailureCounter;
	
	private JTextField sampleSummaryQuantiles;
	private JTextField sampleHistogramBuckets;
	private JTextField sampleMetricPrefix;
	

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
		log.debug("creating new test element");
		
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

			this.modifySamplerConfig(config);
			
			listener.setSamplerSaveConfig(config);
		}
	}
	
	
	private void modifySamplerConfig(PrometheusSaveConfig config){
		//general section
		config.setMetricPrefix(this.sampleMetricPrefix.getText());
		config.setFailureCounter(this.sampleFailureCounter.isSelected());
		
		//labels
		config.setSaveCode(this.sampleCodeCheckBox.isSelected());
		config.setSaveLabel(this.sampleLabelsCheckBox.isSelected());
		config.setSaveSuccess(this.SampleSuccessCheckBox.isSelected());
		
		// types
		config.setCounter(this.sampleCounterCheckbox.isSelected());
		
		config.setHistogram(this.sampleHistogramCheckbox.isSelected());
		config.setBuckets(this.sampleHistogramBuckets.getText());
		
		config.setSummary(this.sampleSummaryCheckbox.isSelected());
		config.setQuantiles(this.sampleSummaryQuantiles.getText());
		
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
			PrometheusSaveConfig config = ((PrometheusListener) element).getSamplerSaveConfig();			
//			this.configureAssertionClass(config);
			this.configureSamplerSection(config);
		}
	}
	
	
	private void configureSamplerSection(PrometheusSaveConfig config){
		//general section
		this.sampleMetricPrefix.setText(config.getMetricPrefix());
		this.sampleFailureCounter.setSelected(config.isFailureCounter());
		
		//labels
		this.sampleCodeCheckBox.setSelected(config.saveCode());
		this.SampleSuccessCheckBox.setSelected(config.saveSuccess());
		this.sampleLabelsCheckBox.setSelected(config.saveLabel());
		
		// types
		this.sampleCounterCheckbox.setSelected(config.isCounter());
		
		this.sampleHistogramCheckbox.setSelected(config.isHistogram());
		this.sampleHistogramBuckets.setText(config.getBuckets());
		
		this.sampleSummaryCheckbox.setSelected(config.isSummary());
		this.sampleSummaryQuantiles.setText(config.getQuantiles());
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
		VerticalPanel panel = new VerticalPanel();
		
		panel.add(this.createSamplesPanel());
//		panel.add(this.createAssertionsPanel());
		
		return panel;
	}
	
	protected JPanel createSamplesPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Samples"));
		
		panel.add(createGeneralPanel());
		panel.add(createSampleTypesPanel());
		panel.add(createLabelsPanel());
		
		return panel;
	}
	
	protected JPanel createSampleTypesPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Metric Types"));
	
		panel.add(createSampleCounterPanel());
		panel.add(createSampleSummaryPanel());
		panel.add(createSampleHistogramPanel());
		
		return panel;
	}
	
	protected JPanel createGeneralPanel() {
		VerticalPanel panel = new VerticalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"General"));
		
		panel.add(createPrefixPanel());
		panel.add(createSampleFailureCounterPanel());
		
		return panel;
	}
	
	protected JPanel createSampleFailureCounterPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		
		this.sampleFailureCounter = new JCheckBox("Failure Counter");
		panel.add(this.sampleFailureCounter);
		
		return panel;
	}
	
	protected JPanel createPrefixPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 0));
		JLabel label = new JLabel("Sample Metric Prfix:");
		this.sampleMetricPrefix = new JTextField(DEFAULT_SAMPLE_PREFIX);
		
		panel.add(label, BorderLayout.WEST);
		panel.add(this.sampleMetricPrefix, BorderLayout.CENTER);
	
		return panel;	
	}
	
	protected JPanel createSampleCounterPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		
		this.sampleCounterCheckbox = new JCheckBox("Counter");
		panel.add(this.sampleCounterCheckbox);
		
		return panel;
	}
	
	
	protected JPanel createSampleSummaryPanel() {
		HorizontalPanel panel = new HorizontalPanel();
				
		this.sampleSummaryQuantiles = new JTextField(DEFAULT_QUANTILES);

		this.sampleSummaryCheckbox = new JCheckBox("Summary");
		this.sampleSummaryCheckbox.addActionListener(
				(ActionEvent event)  -> {
				if(event.getSource() == sampleSummaryCheckbox) {
					sampleSummaryQuantiles.setEnabled(sampleSummaryCheckbox.isSelected());
				}
			});
		this.sampleSummaryCheckbox.setSelected(false);
		this.sampleSummaryQuantiles.setEnabled(false);
		
		panel.add(this.sampleSummaryCheckbox);
		panel.add(this.sampleSummaryQuantiles);
		return panel;
	}
	
	protected JPanel createSampleHistogramPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		
		this.sampleHistogramBuckets = new JTextField(DEFAULT_HISTO_BUCKETS);

		this.sampleHistogramCheckbox = new JCheckBox("Histogram");
		this.sampleHistogramCheckbox.addActionListener(
				(ActionEvent event)  -> {
				if(event.getSource() == sampleHistogramCheckbox) {
					sampleHistogramBuckets.setEnabled(sampleHistogramCheckbox.isSelected());
				}
			});
		this.sampleHistogramCheckbox.setSelected(false);
		this.sampleHistogramBuckets.setEnabled(false);
		
		panel.add(this.sampleHistogramCheckbox);
		panel.add(sampleHistogramBuckets);
		return panel;
	}
	
	protected JPanel createLabelsPanel() {
		HorizontalPanel panel = new HorizontalPanel();
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Labels"));
		
		this.sampleCodeCheckBox = new JCheckBox("Code");
		panel.add(this.sampleCodeCheckBox);
		
		this.sampleLabelsCheckBox = new JCheckBox("Jmeter Labels");
		panel.add(this.sampleLabelsCheckBox);
		
		this.SampleSuccessCheckBox = new JCheckBox("Success");
		panel.add(this.SampleSuccessCheckBox);
		
		return panel;
	}
	

}
