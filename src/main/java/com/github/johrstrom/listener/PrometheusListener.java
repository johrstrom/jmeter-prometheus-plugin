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
import io.prometheus.client.CollectorRegistry;


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

//		try {
//
//			// build the label values from the event and observe the sampler
//			// metrics
//			String[] samplerLabelValues = this.labelValues(event);
//			samplerCollector.labels(samplerLabelValues).observe(event.getResult().getTime());
//
//			// if there are any assertions to
//			if (collectAssertions) {
//				if (event.getResult().getAssertionResults().length > 0) {
//					for (AssertionResult assertionResult : event.getResult().getAssertionResults()) {
//						String[] assertionsLabelValues = this.labelValues(event, assertionResult);
//						
//						if(assertionsCollector instanceof Summary)
//							((Summary) assertionsCollector).labels(assertionsLabelValues).observe(event.getResult().getTime());
//						else if (assertionsCollector instanceof Counter)
//							((Counter) assertionsCollector).labels(assertionsLabelValues).inc();
//					}
//				}
//			}
//
//		} catch (Exception e) {
//			log.error("Didn't update metric because of exception. Message was: {}", e.getMessage());
//		}
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
	 * For a given SampleEvent, get all the label values as determined by the
	 * configuration. Can return reflection related errors because this invokes
	 * SampleEvent accessor methods like getResponseCode or getSuccess.
	 * 
	 * @param event
	 *            - the event that occurred
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
//	protected String[] labelValues(SampleEvent event)
//			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//		
//		String[] sampleVarArr = this.sampleVariableValues(event);
//		int configLabelLength = this.samplerConfig.getLabels().length;
//		int totalLength = configLabelLength + sampleVarArr.length;
//		
//		String[] values = new String[totalLength];
//		int valuesIndex = -1;	//start at -1 so you can ++ when referencing it
//
//		for (int i = 0; i < configLabelLength; i++) {
//			Method m = this.samplerConfig.getMethods()[i];
//			values[++valuesIndex] = m.invoke(event.getResult()).toString();
//		}
//		
//		System.arraycopy(sampleVarArr, 0, values, configLabelLength, sampleVarArr.length);
//
//		return values;
//
//	}

	/**
	 * For a given SampleEvent and AssertionResult, get all the label values as
	 * determined by the configuration. Can return reflection related errors
	 * because this invokes SampleEvent accessor methods like getResponseCode or
	 * getSuccess.
	 * 
	 * @param event
	 *            - the event that occurred
	 * @param assertionResult
	 *            - the assertion results associated to the event
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
//	protected String[] labelValues(SampleEvent event, AssertionResult assertionResult)
//			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//
//		String[] sampleVarArr = this.sampleVariableValues(event);
//		int assertionLabelLength = this.assertionConfig.getLabels().length;
//		int sampleVariableLength = sampleVarArr.length;
//		int combinedLength = assertionLabelLength + sampleVariableLength;
//		
//		String[] values = new String[combinedLength];
//
//		for (int i = 0; i < assertionLabelLength; i++) {
//			Method m = this.assertionConfig.getMethods()[i];
//			if (m.getDeclaringClass().equals(AssertionResult.class))
//				values[i] = m.invoke(assertionResult).toString();
//			else
//				values[i] = m.invoke(event.getResult()).toString();
//		}
//		
//		System.arraycopy(sampleVarArr, 0, values, assertionLabelLength, sampleVariableLength);
//
//		log.info("assertion values: {}", (Object) values);
//		
//		return values;
//
//	}
	
//	private String[] sampleVariableValues(SampleEvent event) {
//		int sampleVariableLength = SampleEvent.getVarCount();
//		String[] values = new String[sampleVariableLength];
//		
//		for(int i = 0; i < sampleVariableLength; i++) {
//			String varValue =  event.getVarValue(i);
//			values[i] = (varValue == null) ?  "" : varValue;
//		}
//		
//		return values;
//	}

	/**
	 * Helper function to modify private member collectors and collector
	 * configurations. Any invocation of this method will modify them, even if
	 * configuration fails due to reflection errors, default configurations are
	 * applied and new collectors created.
	 */
	protected synchronized void reconfigure() {

		this.deleteCollectors();
		this.createSamplerCollectors();
		
		//tmpSamplerCollectors = new JMeterCollector();

//		CollectorConfig tmpAssertConfig = new CollectorConfig();
//		CollectorConfig tmpSamplerConfig = new CollectorConfig();
//
//		// activate collections
//		collectAssertions = this.getSaveConfig().saveAssertions();
//
//		try {
//			// try to build new config objects
//			tmpAssertConfig = this.newAssertionCollectorConfig();
//			tmpSamplerConfig = this.newSamplerCollectorConfig();
//
//		} catch (NoSuchMethodException | SecurityException e) {
//			log.error("Only partial reconfigure due to exception.", e);
//		}
//
//		// remove old collectors and reassign member variables
//		CollectorRegistry.defaultRegistry.clear();
//		this.assertionConfig = tmpAssertConfig;
//		this.samplerConfig = tmpSamplerConfig;
//
//		// register new collectors
//		this.createSamplerCollector();
//		this.createAssertionCollector();
//
//		log.info("Reconfigure complete.");
//
//		if (log.isDebugEnabled()) {
//			log.debug("Assertion Configuration: " + this.assertionConfig.toString());
//			log.debug("Sampler Configuration: " + this.samplerConfig.toString());
//		}

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
	

	/**
	 * Create a new CollectorConfig for Samplers. Due to reflection this throws
	 * errors based on security and absence of method definitions.
	 * 
	 * @return the new CollectorConfig
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
//	protected CollectorConfig newSamplerCollectorConfig() throws NoSuchMethodException, SecurityException {
//		PrometheusSaveConfig saveConfig = this.getSamplerSaveConfig();
//		CollectorConfig collectorConfig = new CollectorConfig();
//
//		if (saveConfig.saveLabel()) {
//			collectorConfig.saveSamplerLabel();
//		}
//
//		if (saveConfig.saveCode()) {
//			collectorConfig.saveSamlerCode();
//		}
//
//		if (saveConfig.saveSuccess()) {
//			collectorConfig.saveSamplerSuccess();
//		}
//
//		return collectorConfig;
//	}

	/**
	 * Create a new CollectorConfig for Assertions. Due to reflection this
	 * throws errors based on security and absence of method definitions.
	 * 
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
//	protected CollectorConfig newAssertionCollectorConfig() throws NoSuchMethodException, SecurityException {
//		PrometheusSaveConfig saveConfig = this.getSamplerSaveConfig();
//		CollectorConfig collectorConfig = new CollectorConfig();
//
//		if (saveConfig.saveAssertions()) {
//			// TODO configure assertions more granularly
//			collectorConfig.saveSamplerLabel();
//			collectorConfig.saveAssertionFailure();
//			collectorConfig.saveAssertionName();
//		}
//
//		return collectorConfig;
//	}
	
	
//	protected void createAssertionCollector(){
//		if (!collectAssertions){
//			return;
//		}
//		
//		String[] labelNames = new String[]{};
//		
//		if (SampleEvent.getVarCount() > 0) {
//			labelNames = this.combineAssertionLabelsWithSampleVars();
//		}else {
//			labelNames = this.assertionConfig.getLabels();
//		}
//		
//		if(this.getSaveConfig().getAssertionClass().equals(Summary.class))
//			this.assertionsCollector = Summary.build().name("jmeter_assertions_total").help("Counter for assertions")
//				.labelNames(labelNames).quantile(0.5, 0.1).quantile(0.99, 0.1)
//				.create().register(CollectorRegistry.defaultRegistry);
//		
//		else if(this.getSaveConfig().getAssertionClass().equals(Counter.class))
//			this.assertionsCollector = Counter.build().name("jmeter_assertions_total").help("Counter for assertions")
//			.labelNames(labelNames).create().register(CollectorRegistry.defaultRegistry);
//			
//	}

	
//	protected void createSamplerCollector(){
//		
//		String[] labelNames = new String[]{};
//		
//		if (SampleEvent.getVarCount() > 0) {
//			labelNames = this.combineConfigLabelsWithSampleVars();
//		}else {
//			labelNames = this.samplerConfig.getLabels();
//		}
//		
//		this.samplerCollector = Summary.build()
//				.name("jmeter_samples_latency")
//				.help("Summary for Sample Latency")
//				.labelNames(labelNames)
//				.quantile(0.5, 0.1)
//				.quantile(0.99, 0.1)
//				.create()
//				.register(CollectorRegistry.defaultRegistry);
//		
//	}
	
//	private String[] combineAssertionLabelsWithSampleVars() {
//		int assertionLabelLength = this.assertionConfig.getLabels().length;
//		int sampleVariableLength = SampleEvent.getVarCount();
//		int combinedLength = assertionLabelLength + sampleVariableLength;
//		
//		String[] returnArray = new String[combinedLength];
//		int returnArrayIndex = -1;	//start at -1 so you can ++ when referencing it
//		
//		//add config first
//		String[] configuredLabels = this.assertionConfig.getLabels();
//		for (int i = 0; i < assertionLabelLength; i++) {
//			returnArray[++returnArrayIndex] = configuredLabels[i];
//		}
//		
//		//now add sample variables
//		for (int i = 0; i < sampleVariableLength; i++) {
//			returnArray[++returnArrayIndex] = SampleEvent.getVarName(i);
//		}
//		
//		return returnArray;
//	}
	
//	private String[] combineConfigLabelsWithSampleVars() {
//		int configLabelLength = this.samplerConfig.getLabels().length;
//		int sampleVariableLength = SampleEvent.getVarCount();
//		int combinedLength = configLabelLength + sampleVariableLength;
//		
//		String[] returnArray = new String[combinedLength];
//		int returnArrayIndex = -1;	//start at -1 so you can ++ when referencing it
//		
//		//add config first
//		String[] configuredLabels = this.samplerConfig.getLabels();
//		for (int i = 0; i < configLabelLength; i++) {
//			returnArray[++returnArrayIndex] = configuredLabels[i];
//		}
//		
//		//now add sample variables
//		for (int i = 0; i < sampleVariableLength; i++) {
//			returnArray[++returnArrayIndex] = SampleEvent.getVarName(i);
//		}
//		
//		return returnArray;
//	}
	
}
