package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

public class AggregatedTypeUpdater extends AbstractUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(AggregatedTypeUpdater.class);

	public AggregatedTypeUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}

	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			String[] labels = this.labelValues(event);
			long measurement = this.measure(event);
			
			if(collector instanceof Histogram) {
				Histogram hist = (Histogram) collector;
				hist.labels(labels).observe(measurement);
				
			}else if(collector instanceof Summary) {
				Summary sum = (Summary) collector;
				sum.labels(labels).observe(measurement);
			}
			
		} catch(Exception e) {
			log.error("Did not update {} because of error: {}", this.config.getMetricName(), e.getMessage());
			log.debug(e.getMessage(), e);
		}

	}
	
	
	protected long measure(SampleEvent event) {
		SampleResult result = event.getResult();
		switch(this.config.getMeasuringAsEnum()) {
		case ResponseSize:
			return result.getBodySizeAsLong();
		case ResponseTime:
			return result.getTime();
		case Latency:
			return result.getLatency();
		case IdleTime:
			return result.getIdleTime();
		default:
			return 0;
		}
	}

}
