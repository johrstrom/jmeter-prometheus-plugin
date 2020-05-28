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


import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.CollectorElement;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.listener.PrometheusListener;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * The GUI class for the Prometheus Listener.
 * <p>
 * Currently, all configurations are done through properties files so this class
 * shows nothing visually other than comments.
 *
 * @author Jeff Ohrstrom
 */
public class PrometheusListenerGui extends AbstractListenerGui {


	private static final long serialVersionUID = 4984653136457108054L;

	private ListenerCollectorTable table = new ListenerCollectorTable();
	private Logger log = LoggerFactory.getLogger(PrometheusListenerGui.class);

	public PrometheusListenerGui() {
		super();
		log.debug("making a new listener gui: {}", this.toString());
		init();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#getLabelResource()
	 */
	@Override
	public String getLabelResource() {
		return getClass().getCanonicalName();
	}


	@Override
	protected PrometheusListenerGui clone() throws CloneNotSupportedException {
		return new PrometheusListenerGui();
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
		return super.getName() == null ? this.getStaticLabel() : super.getName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(TestElement ele) {
		super.configure(ele);
		if (ele instanceof CollectorElement<?>) {
			try {
				this.table.populateTable((CollectorElement<ListenerCollectorConfig>) ele);
			} catch (Exception e) {
				log.error("didn't modify test element because {}. {}", e.getClass(), e.getMessage());
			}
		}

		//ele.getName() == null ? this.setName(ele.getName()) : this.setName(getStaticLabel());;
		this.setName(ele.getName() == null ? getStaticLabel() : ele.getName());
		this.setComment(ele.getComment() == null ? "" : ele.getComment());
	}

	@Override
	public TestElement createTestElement() {
		PrometheusListener listener = new PrometheusListener();

		listener.setProperty(TestElement.GUI_CLASS, PrometheusListenerGui.class.getName());
		listener.setProperty(TestElement.TEST_CLASS, PrometheusListener.class.getName());
		this.modifyTestElement(listener);
		listener.setCollectorConfigs(defaultCollectors());
		return listener;
	}

	private void init() {
		this.setLayout(new BorderLayout(0, 5));
		this.add(makeTitlePanel(), BorderLayout.NORTH);
		this.add(this.table, BorderLayout.CENTER);
	}

	@Override
	public void modifyTestElement(TestElement ele) {
		if (!(ele instanceof CollectorElement)) {
			return;
		}

		@SuppressWarnings("unchecked")
		CollectorElement<ListenerCollectorConfig> config = (CollectorElement<ListenerCollectorConfig>) ele;
		List<ListenerCollectorConfig> collectors = this.table.getRowsAsCollectors();
		config.setCollectorConfigs(collectors);

		config.setName(this.getName());
		config.setComment(this.getComment());
	}

	@Override
	public void clearGui() {
		super.clearGui();
		this.table.clearModelData();
	}


	private List<ListenerCollectorConfig> defaultCollectors() {
		List<ListenerCollectorConfig> collectors = new ArrayList<>();
		collectors.add(buildResponseTimeCollector());
		collectors.add(buildSuccessRatioCollector());
		return collectors;
	}

	private ListenerCollectorConfig buildSuccessRatioCollector() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("Ratio");
		cfg.setHelp("Success and failure ratio");
		cfg.setLabels("label,code");
		cfg.setType(BaseCollectorConfig.JMeterCollectorType.SUCCESS_RATIO.toString());
		ListenerCollectorConfig listenerCfg = new ListenerCollectorConfig(cfg);
		listenerCfg.setListenTo(ListenerCollectorConfig.SAMPLES.toString());
		listenerCfg.setMeasuring(ListenerCollectorConfig.Measurable.SuccessRatio.toString());
		return listenerCfg;
	}

	private ListenerCollectorConfig buildResponseTimeCollector() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("ResponseTime");
		cfg.setHelp("Sampler Response Time");
		cfg.setLabels("label,code");
		cfg.setType(BaseCollectorConfig.JMeterCollectorType.SUMMARY.toString());
		cfg.setQuantileOrBucket("0.75,0.5|0.95,0.1|0.99,0.01;60");
		ListenerCollectorConfig listenerCfg = new ListenerCollectorConfig(cfg);
		listenerCfg.setListenTo(ListenerCollectorConfig.SAMPLES.toString());
		listenerCfg.setMeasuring(ListenerCollectorConfig.Measurable.ResponseTime.toString());
		return listenerCfg;
	}

}
