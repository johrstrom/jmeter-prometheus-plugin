package com.github.johrstrom.config;

import java.util.Map.Entry;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.CollectorElement;

import io.prometheus.client.Collector;

public class PrometheusMetricsConfig extends CollectorElement<BaseCollectorConfig>
	implements NoThreadClone, TestStateListener {

	private static final long serialVersionUID = 7602510312126862226L;

	private Logger log = LoggerFactory.getLogger(PrometheusMetricsConfig.class);
	
	@Override
	public void testEnded() {
		this.setRunningVersion(false);
		JMeterVariables variables = getThreadContext().getVariables();
		
		for (Entry<String, Collector> entry : this.collectors.entrySet()) {
			variables.remove(entry.getKey());
		}
		
		this.unRegisterAllCollectors();
		
	}

	@Override
	public void testEnded(String arg0) {
		this.testEnded();
	}

	@Override
	public void testStarted() {
		this.setRunningVersion(true);
		JMeterVariables variables = getThreadContext().getVariables();
		this.registerAllCollectors();
		
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
	public PrometheusMetricsConfig clone() {
		PrometheusMetricsConfig clone = new PrometheusMetricsConfig();
		clone.setCollectorConfigs(this.getCollectorConfigs());
				
		return clone;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PrometheusMetricsConfig) {
			PrometheusMetricsConfig other = (PrometheusMetricsConfig) o;
			
			CollectionProperty thisConfig = this.getCollectorConfigs();
			CollectionProperty otherConfig = other.getCollectorConfigs();
			boolean sameSize = thisConfig.size() == otherConfig.size();
			
			for (int i = 0; i < thisConfig.size(); i++) {
				JMeterProperty left = thisConfig.get(i);
				JMeterProperty right = otherConfig.get(i);
				
				if(!left.equals(right)) {
					return false;
				}
			}
			
			return true && sameSize;
		}
		
		return false;
	}

}
