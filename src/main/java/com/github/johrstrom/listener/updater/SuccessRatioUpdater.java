package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.SuccessRatioCollector;
import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;

public class SuccessRatioUpdater extends AbstractUpdater {

	private static final Logger log = LoggerFactory.getLogger(SuccessRatioUpdater.class);

	public SuccessRatioUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}
	
	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			if(collector instanceof SuccessRatioCollector) {
				SuccessRatioCollector c = (SuccessRatioCollector) collector;
				String[] labels = this.labelValues(event);
				 
				if(event.getResult().isSuccessful()) {
					c.incrementSuccess(labels);
				} else {
					c.incrementFailure(labels);
				}
				
			}
			
		} catch (Exception e) {
			log.error("Did not update {} because of error: {}", this.config.getMetricName(), e.getMessage());
			log.debug(e.getMessage(), e);
		}
		
	}

}
