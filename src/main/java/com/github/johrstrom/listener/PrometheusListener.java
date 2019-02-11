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
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.CollectorElement;
import com.github.johrstrom.listener.updater.*;

import io.prometheus.client.Collector;


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
public class PrometheusListener extends CollectorElement<ListenerCollectorConfig>
		implements SampleListener, Serializable, TestStateListener, NoThreadClone {

	private static final long serialVersionUID = -4833646252357876746L;

	private static final Logger log = LoggerFactory.getLogger(PrometheusListener.class);

	private transient PrometheusServer server = PrometheusServer.getInstance();
	
	private List<AbstractUpdater> updaters;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.samplers.SampleListener#sampleOccurred(org.apache.
	 * jmeter.samplers.SampleEvent)
	 */
	public void sampleOccurred(SampleEvent event) {

		for(AbstractUpdater updater : this.updaters) {
			updater.update(event);
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
		this.clearCollectors();
		
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
		this.makeNewCollectors();
		//this.registerAllCollectors();
		
		try {
			server.start();
		} catch (Exception e) {
			log.error("Couldn't start http server", e);
		}

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

	@Override
	protected void makeNewCollectors() {
		//this.clearCollectors();
		this.updaters = new ArrayList<AbstractUpdater>();
		
		CollectionProperty collectorDefs = this.getCollectorConfigs();
		PropertyIterator iter = collectorDefs.iterator();
		
		while(iter.hasNext()) {
			
			try {
				ListenerCollectorConfig config = (ListenerCollectorConfig) iter.next().getObjectValue();
				Collector collector = this.registry.getOrCreateAndRegister(config);
				AbstractUpdater updater = null;
				
				switch (config.getMeasuringAsEnum()) {
				case CountTotal:
					updater = new CountTotalUpdater(config);
					break;
				case FailureTotal:
					updater = new FailureTotalUpdater(config);
					break;
				case ResponseSize:
					break;
				case ResponseTime:
					updater = new ResponseTimeUpdater(config);
					break;
				case SuccessTotal:
					updater = new SuccessTotalUpdater(config);
					break;
				default:
					// improbable because you get a sort of class cast exception on Enum
					// because you're casting null*
					// *unless we've missed a case, i.e., more enums defined than switch cases coded here
					break;		
				}
				
				if(updater != null) {
					this.collectors.put(config, collector);
					this.updaters.add(updater);
					log.debug("added " + config.getMetricName() + " to list of collectors");
					
				} else {
					log.warn("didn't find an updatetr for {}", config);
				}
				
				
			}catch(Exception e) {
				log.error("Didn't create new collector because of error, ",e);
			}
			
		}
		
	}



		
}
