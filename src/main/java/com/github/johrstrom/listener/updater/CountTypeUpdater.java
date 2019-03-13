package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.collector.SuccessRatioCollector;
import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;

/**
 * This is the AbstractUpdater sub-type that can handle updating any kind of Counter metrics 
 * along with {@link SuccessRatioCollector} type.
 * 
 * @author Jeff ohrstrom
 *
 */
public class CountTypeUpdater extends AbstractUpdater {
	
	private static final Logger log = LoggerFactory.getLogger(CountTotalUpdater.class);

	public CountTypeUpdater(ListenerCollectorConfig cfg) {
		super(cfg);
	}

	@Override
	public void update(SampleEvent event) {
		try {
			Collector collector = registry.getOrCreateAndRegister(this.config);
			
			String[] labels = this.labelValues(event);
			boolean successful = event.getResult().isSuccessful();
			
			if(collector instanceof Counter) {
				Counter c = (Counter) collector;
				
				switch (config.getMeasuringAsEnum()) {
				case CountTotal: 
					c.labels(labels).inc();
					break;
				case FailureTotal:
					if(!successful) {
						c.labels(labels).inc();
					}
					break;
				case SuccessTotal:
					if(successful) {
						c.labels(labels).inc();
					}
					break;
				default:
					break;
				}
				
			} else if(collector instanceof SuccessRatioCollector) {
				SuccessRatioCollector c = (SuccessRatioCollector) collector;
				if(successful) {
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
