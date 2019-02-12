package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

public class ResponseSizeUpdater extends AbstractUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(ResponseSizeUpdater.class);
	
	public ResponseSizeUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}
	
	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			long size = event.getResult().getBodySizeAsLong();
			String[] labels = this.labelValues(event);
			
			if(collector instanceof Histogram) {
				Histogram hist = (Histogram) collector;
				hist.labels(labels).observe(size);
				
			}else if(collector instanceof Summary) {
				Summary sum = (Summary) collector;
				sum.labels(labels).observe(size);
			}
			
		} catch (Exception e) {
			log.error("Did not update {} because of error: {}", this.config.getMetricName(), e.getMessage());
			log.debug(e.getMessage(), e);
		}
	}

}
