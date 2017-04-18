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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.report.core.Sample;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.SampleSaveConfiguration;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.ObjectProperty;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.MetricsServlet;

/**
 * The main test element listener class of this library.  Jmeter updates this class through the 
 * SampleListener interface and it in turn updates the CollectorRegistry.  This class is also
 * a TestStateListener to control when it starts up or shuts down the server that ultimately
 * serves Prometheus the results through an http api. 
 * 
 * 
 * @author Jeff Ohrstrom
 *
 */
public class PrometheusListener extends AbstractListenerElement 
	implements SampleListener, Serializable, TestStateListener, Remoteable, NoThreadClone {

	
	public static final String SAVE_CONFIG = "johrstrom.save_config";
	private static final long serialVersionUID = -4833646252357876746L;

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private Server server;
	private Summary transactions;
	private String[] labels;
	private Method[] sampleGetterMethods;

	/**
	 * Constructor. 
	 */
	public PrometheusListener(){
		this(new PrometheusSaveConfig());
	}
	
	public PrometheusListener(PrometheusSaveConfig config){
		super();
		this.setSaveConfig(config);
		log.debug("Creating new prometheus listener.");
	}
	
//	public 

	/* (non-Javadoc)
	 * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleOccurred(SampleEvent event) {		
		try {
			transactions.labels(this.labelValues(event)).observe(event.getResult().getTime());
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.error("Didn't updat metric because of exception. Message was: " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.samplers.SampleListener#sampleStarted(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleStarted(SampleEvent arg0) {}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.samplers.SampleListener#sampleStopped(org.apache.jmeter.samplers.SampleEvent)
	 */
	public void sampleStopped(SampleEvent arg0) {}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.testelement.TestStateListener#testEnded()
	 */
	public void testEnded() {
		try {
			this.server.stop();
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.testelement.TestStateListener#testEnded(java.lang.String)
	 */
	public void testEnded(String arg0) {
		this.testEnded();
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.testelement.TestStateListener#testStarted()
	 */
	public void testStarted() {
		this.server = new Server(8080);
		
	     ServletContextHandler context = new ServletContextHandler();
	     context.setContextPath("/");
	     server.setHandler(context);
	     context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
	     
	     try {
			server.start();
	     } catch (Exception e) {
			
			e.printStackTrace();
	     }
	     
	}
	
	/**
	 * Set a new Save configuration.  Note that this function reconfigures 
	 * this object and one should not set the save config directly through {@link #setProperty(org.apache.jmeter.testelement.property.JMeterProperty)}
	 * functions. 
	 * 
	 * @param config - the configuration object
	 */
	public void setSaveConfig(PrometheusSaveConfig config){
		this.setProperty(new ObjectProperty(SAVE_CONFIG, config));
		this.reconfigure();
	}
	
	
	public PrometheusSaveConfig getSaveConfig(){
		return (PrometheusSaveConfig) this.getProperty(SAVE_CONFIG).getObjectValue();
	}
	
	protected String[] labelValues(SampleEvent event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		String[] values = new String[labels.length];
		
		for (int i =0; i < labels.length; i++){
			Method m = this.sampleGetterMethods[i];
			String res = m.invoke(event.getResult()).toString();
			values[i] = res;
		}
		
		return values;
	}
	
	
	/**
	 * Helper function to modify private member variables {@link #labels} and {@link #sampleGetterMethods}.  These 2 
	 * arrays are used to translate JMeter SampleEvents to an array of Strings.    
	 */
	protected void reconfigure(){
		List<String> tmpLabels = new ArrayList<String>();
		List<Method> tmpMethods = new ArrayList<Method>();
		
		PrometheusSaveConfig config = this.getSaveConfig();

		try {
			
			if(config.saveLabel()){
				tmpLabels.add("label");
				tmpMethods.add(SampleResult.class.getMethod("getSampleLabel"));
			}
				
			if(config.saveCode()){
				tmpLabels.add("code");
				tmpMethods.add(SampleResult.class.getMethod("getResponseCode"));
			}
			
			if(config.saveSuccess()){
				tmpLabels.add("success");
				tmpMethods.add(SampleResult.class.getMethod("isSuccessful"));
			}
			
		
		} catch (NoSuchMethodException | SecurityException e) {
			log.error("Didn't reconfigure correctly. Keeping old configs. Message was: " + e.getMessage());
			return;
		}
		

		
		this.labels = tmpLabels.toArray(new String[tmpLabels.size()]);
		this.sampleGetterMethods = tmpMethods.toArray(new Method[tmpMethods.size()]);
		this.createMetrics();
	}

	/* (non-Javadoc)
	 * @see org.apache.jmeter.testelement.TestStateListener#testStarted(java.lang.String)
	 */
	public void testStarted(String arg0) {
		this.testStarted();		
	}
	
	
	protected void createMetrics(){
		
		CollectorRegistry.defaultRegistry.clear();		
		
		this.transactions = Summary.build()
				.name("synthetic_transaction")
				.help("Counter for all synthetic transactions")
				.labelNames(this.labels)
				.quantile(0.5, 0.1)
				.quantile(0.99, 0.1)
				.create()
				.register(CollectorRegistry.defaultRegistry);
		
		
	}

}
