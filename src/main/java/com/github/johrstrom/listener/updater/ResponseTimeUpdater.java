package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

/**
 * The Updater class that records Response Time.  Only Supports Summary and Histogram type Collectors.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class ResponseTimeUpdater extends AbstractUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(ResponseTimeUpdater.class);

	public ResponseTimeUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}

	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			long rt = event.getResult().getTime();
			String[] labels = this.labelValues(event);
			
			if(collector instanceof Histogram) {
				Histogram hist = (Histogram) collector;
				hist.labels(labels).observe(rt);
				
			}else if(collector instanceof Summary) {
				Summary sum = (Summary) collector;
				sum.labels(labels).observe(rt);
			}
			
		} catch (Exception e) {
			log.error("Did not update {} because of error: {}", this.config.getMetricName(), e.getMessage());
			log.debug(e.getMessage(), e);
		}
	}

}
