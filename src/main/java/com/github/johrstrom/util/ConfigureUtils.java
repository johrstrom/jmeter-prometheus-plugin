package com.github.johrstrom.util;

import java.util.ArrayList;
import java.util.List;

import com.github.johrstrom.listener.JMeterCollector;
import com.github.johrstrom.listener.PrometheusSaveConfig;

import io.prometheus.client.Collector.Type;

public class ConfigureUtils {
	
	
	public static List<JMeterCollector> saveConfigToCollector(PrometheusSaveConfig config) {
		List<JMeterCollector> collectors = new ArrayList<>();
		
		String prefix = config.getMetricPrefix();
		
		if(config.isFailureCounter()) {
			collectors.add(new JMeterCollector(config, Type.COUNTER, prefix + "_failures_total"));
		}
		
		if(config.isCounter()) {
			collectors.add(new JMeterCollector(config, Type.COUNTER, prefix + "_count")); //TODO find docs on best practices
		}

		if(config.isHistogram()) {
			collectors.add(new JMeterCollector(config, Type.HISTOGRAM, prefix + "_histogram"));
		}
		
		if(config.isSummary()) {
			collectors.add(new JMeterCollector(config, Type.SUMMARY, prefix + "_summary"));
		}
		
		return collectors;
	}
	

	

	
	

}
