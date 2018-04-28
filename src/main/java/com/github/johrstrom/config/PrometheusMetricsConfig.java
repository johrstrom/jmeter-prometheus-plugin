package com.github.johrstrom.config; //com.github.johrstrom.config.PrometheusMetricsConfig

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.CollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

public class PrometheusMetricsConfig extends AbstractTestElement 
	implements ConfigElement, NoThreadClone, TestStateListener {

	private static final long serialVersionUID = 7602510312126862226L;
	
	private static final String COLLECTOR_DEF = "johrstrom.config_collector_definitions";
	
	private Logger log = LoggerFactory.getLogger(PrometheusMetricsConfig.class);
	
	private Map<String,Collector> collectors = new HashMap<String,Collector>();
	
	
	public CollectionProperty getCollectorDefinitions() {
		JMeterProperty collectorDefinitions = this.getProperty(COLLECTOR_DEF);
		
		if(collectorDefinitions == null || collectorDefinitions instanceof NullProperty) {
			collectorDefinitions = new CollectionProperty(COLLECTOR_DEF, new ArrayList<CollectorConfig>());
			collectorDefinitions.setName(COLLECTOR_DEF);
		}
		
		return (CollectionProperty) collectorDefinitions;
		 
	}
	
	public void setCollectorDefinitions(List<CollectorConfig> collectors) {
		this.setProperty(new CollectionProperty(COLLECTOR_DEF, collectors));
		this.makeNewCollectors();
	}
	
	
	private void makeNewCollectors() {
		for (Entry<String, Collector> entry : this.collectors.entrySet()) {
			CollectorRegistry.defaultRegistry.unregister(entry.getValue());
		}
		
		this.collectors.clear();
		
		CollectionProperty collectorDefs = this.getCollectorDefinitions();
		PropertyIterator iter = collectorDefs.iterator();
		
		while(iter.hasNext()) {
			CollectorConfig definition = (CollectorConfig) iter.next().getObjectValue();
			Collector collector = CollectorConfig.fromDefinition(definition);
			try {
				collector.register(CollectorRegistry.defaultRegistry);
				this.collectors.put(definition.getMetricName(), collector);
			}catch(Exception e) {
				log.error("Didn't register collector because of error",e);
			}
			
		}
		
	}
	
	
	@Override
	public void testEnded() {
		this.setRunningVersion(false);
		JMeterVariables variables = getThreadContext().getVariables();
		
		for (Entry<String, Collector> entry : this.collectors.entrySet()) {
			variables.remove(entry.getKey());
		}
		
	}

	@Override
	public void testEnded(String arg0) {
		this.testEnded();
	}

	@Override
	public void testStarted() {
		this.setRunningVersion(true);
		JMeterVariables variables = getThreadContext().getVariables();
		this.makeNewCollectors();
		
		log.debug("Test started, adding {} collectors to variables", this.collectors.size());
		
		for (Entry<String, Collector> entry : this.collectors.entrySet()) {
			variables.putObject(entry.getKey(), entry.getValue());
			log.debug("Added ({},{}) to variables.", entry.getKey(), entry.getValue().toString());
		}
 	}

	@Override
	public void testStarted(String arg0) {
		this.testStarted();		
	}

	@Override
	public void addConfigElement(ConfigElement arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean expectsModification() {
		// TODO Auto-generated method stub
		return false;
	}
}
