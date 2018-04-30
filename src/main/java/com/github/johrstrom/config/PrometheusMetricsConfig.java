package com.github.johrstrom.config; //com.github.johrstrom.config.PrometheusMetricsConfig


import java.util.Map.Entry;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.testelement.TestStateListener;
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

}
