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
package com.github.johrstrom.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector.Type;


/**
 * The main test element listener class of this library. Jmeter updates this
 * class through the SampleListener interface and it in turn updates the
 * CollectorRegistry. This class is also a TestStateListener to control when it
 * starts up or shuts down the server that ultimately serves Prometheus the
 * results through an http api.
 * 
 * 
 * @author Jeff Ohrstrom
 *
 */
public class PrometheusListener extends AbstractListenerElement
		implements SampleListener, Serializable, TestStateListener, NoThreadClone {

	public static final String SAMPLER_SAVE_CONFIG = "johrstrom.prometheus.sampler_save_config";
	public static final String ASSERTION_SAVE_CONFIG = "johrstrom.prometheus.assertion_save_config";

	private static final long serialVersionUID = -4833646252357876746L;

	private static final Logger log = LoggerFactory.getLogger(PrometheusListener.class);

	private transient PrometheusServer server = PrometheusServer.getInstance();

	private transient List<JMeterCollector> sampleCollectors = new ArrayList<JMeterCollector>();
	private transient List<JMeterCollector> assertionCollectors = new ArrayList<JMeterCollector>();

	/**
	 * Default Constructor.
	 */
	public PrometheusListener() {
		this(new PrometheusSaveConfig(PrometheusSaveConfig.DEFAULT_SAMPLE_PREFIX), 
				new PrometheusSaveConfig(PrometheusSaveConfig.DEFAULT_ASSERTION_PREFIX));
	}

	/**
	 * Constructor with a configuration argument.
	 * 
	 * @param config
	 *            - the configuration to use.
	 */
	public PrometheusListener(PrometheusSaveConfig sampleSave, PrometheusSaveConfig assertionSave) {
		super();
		log.debug("Creating new prometheus listener " + this.toString());
		
		this.setSamplerSaveConfig(sampleSave);
		// TODO save assertion config
		
	}
	
	

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return super.clone();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.
	 * jmeter.samplers.SampleEvent)
	 */
	public void sampleOccurred(SampleEvent event) {
		
		for(JMeterCollector collector : this.sampleCollectors) {
			collector.update(event);
		}
		
		for(JMeterCollector collector : this.assertionCollectors) {
			collector.update(event);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jmeter.samplers.SampleListener#sampleStarted(org.apache.jmeter
	 * .samplers.SampleEvent)
	 */
	public void sampleStarted(SampleEvent arg0) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jmeter.samplers.SampleListener#sampleStopped(org.apache.jmeter
	 * .samplers.SampleEvent)
	 */
	public void sampleStopped(SampleEvent arg0) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestStateListener#testEnded()
	 */
	public void testEnded() {
		try {
			this.server.stop();
		} catch (Exception e) {
			log.error("Couldn't stop http server", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestStateListener#testEnded(java.lang.
	 * String)
	 */
	public void testEnded(String arg0) {
		this.testEnded();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestStateListener#testStarted()
	 */
	public void testStarted() {
		// update the configuration
		this.reconfigure();
		this.registerAllCollectors();
		
		try {
			server.start();
		} catch (Exception e) {
			log.error("Couldn't start http server", e);
		}

	}

	/**
	 * Set a new Save configuration. Note that this function reconfigures this
	 * object and one should not set the save config directly through
	 * {@link #setProperty(org.apache.jmeter.testelement.property.JMeterProperty)}
	 * functions.
	 * 
	 * @param config
	 *            - the configuration object
	 */
	public void setSamplerSaveConfig(PrometheusSaveConfig config) {
		this.setProperty(new ObjectProperty(SAMPLER_SAVE_CONFIG, config));
		this.reconfigure();
	}

	/**
	 * Get the current Save configuration
	 * 
	 * @return
	 */
	public PrometheusSaveConfig getSamplerSaveConfig() {
		return (PrometheusSaveConfig) this.getProperty(SAMPLER_SAVE_CONFIG).getObjectValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.jmeter.testelement.TestStateListener#testStarted(java.lang.
	 * String)
	 */
	public void testStarted(String arg0) {
		this.testStarted();
	}


	/**
	 * Helper function to modify private member collectors and collector
	 * configurations. Any invocation of this method will modify them, even if
	 * configuration fails due to reflection errors, default configurations are
	 * applied and new collectors created.
	 */
	protected synchronized void reconfigure() {
		this.deleteCollectors();
		this.createSamplerCollectors();
	}
	
	protected synchronized void deleteCollectors() {

		for (JMeterCollector collector : this.sampleCollectors) {
			collector.unregister();
		}
		
		for (JMeterCollector collector : this.assertionCollectors) {
			collector.unregister();
		}
		
		this.sampleCollectors.clear();
		this.assertionCollectors.clear();
		
	}
	
	protected synchronized void registerAllCollectors() {
		for (JMeterCollector collector : this.sampleCollectors) {
			collector.register();
		}
		
		for (JMeterCollector collector : this.assertionCollectors) {
			collector.register();
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.deleteCollectors();
	}

	protected void createSamplerCollectors() {
		PrometheusSaveConfig config = this.getSamplerSaveConfig();
		
		if(config.isCounter()) {
			JMeterCollector collector = 
					new JMeterCollector(this.getSamplerSaveConfig(), Type.COUNTER, config.getMetricPrefix() + "_total");
			this.sampleCollectors.add(collector);
		}
		
		if(config.isFailureCounter()) {
			JMeterCollector collector = 
					new JMeterCollector(this.getSamplerSaveConfig(), Type.COUNTER, config.getMetricPrefix() + "_failures_total");
			this.sampleCollectors.add(collector);
		}
		
		if(config.isSummary()) {
			JMeterCollector collector = 
					new JMeterCollector(this.getSamplerSaveConfig(), Type.SUMMARY, config.getMetricPrefix() + "_summary");
			this.sampleCollectors.add(collector);
		}
		
		if(config.isHistogram()) {
			JMeterCollector collector = 
					new JMeterCollector(this.getSamplerSaveConfig(), Type.HISTOGRAM, config.getMetricPrefix() + "_histogram");
			this.sampleCollectors.add(collector);
		}
		
	}
		
}
