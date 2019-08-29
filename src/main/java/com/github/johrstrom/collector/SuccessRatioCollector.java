package com.github.johrstrom.collector;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;

import java.util.ArrayList;
import java.util.List;

public class SuccessRatioCollector extends Collector {

	private final Counter success, failure, total;
	
	public SuccessRatioCollector(BaseCollectorConfig config) {
		this.success = new Counter.Builder()
				.help(config.getHelp())
				.name(extendedName(config.getMetricName(), "success"))
				.labelNames(config.getLabels())
				.create();
		
		this.failure = new Counter.Builder()
				.help(config.getHelp())
				.name(extendedName(config.getMetricName(), "failure"))
				.labelNames(config.getLabels())
				.create();
		
		this.total = new Counter.Builder()
				.help(config.getHelp())
				.name(extendedName(config.getMetricName(), "total"))
				.labelNames(config.getLabels())
				.create();
		
	}
	
	public void incrementSuccess(String[] labels) {
		this.success.labels(labels).inc();
		this.total.labels(labels).inc();
		
		// this ensures that we emit 0 when this set of labels has 
		// never failed. i.e, total = success when failure = 0. 
		if(this.getFailure(labels) < 1 ) {
			this.failure.labels(labels).inc(0);
		}
	}
	
	public void incrementFailure(String[] labels) {
		this.failure.labels(labels).inc();
		this.total.labels(labels).inc();
		
		// this ensures that we emit 0 when this set of labels has 
		// never succeeded. i.e, total = failures when success = 0.
		if(this.getSuccess(labels) < 1) {
			this.success.labels(labels).inc(0);
		}
	}
	
	public double getSuccess(String[] labels) {
		return this.success.labels(labels).get();
	}
	
	public double getFailure(String[] labels) {
		return this.failure.labels(labels).get();
	}
	
	public double getTotal(String[] labels) {
		return this.total.labels(labels).get();
	}

	@Override
	public List<MetricFamilySamples> collect() {
		ArrayList<MetricFamilySamples> metrics = new ArrayList<MetricFamilySamples>();

		metrics.addAll(this.success.collect());
		metrics.addAll(this.failure.collect());
		metrics.addAll(this.total.collect());
		
		return metrics;
	}
	
	private static String extendedName(String orig, String append) {
		StringBuilder sb = new StringBuilder(32);
		
		sb.append(orig);
		if(!orig.endsWith("_")) {
			sb.append("_");
		}
		sb.append(append);
		
		return sb.toString();
	}

}
