package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;

public class SuccessTotalUpdater extends AbstractUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(SuccessTotalUpdater.class);

	public SuccessTotalUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}

	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			Counter c = (Counter) collector;
			String[] labels = this.labelValues(event);
			if(event.getResult().isSuccessful()) {
				c.labels(labels).inc();
			}
			
		} catch (Exception e) {
			log.error("Did not update {} because of error: {}", this.config.getMetricName(), e.getMessage());
			log.debug(e.getMessage(), e);
		}
	}

}
