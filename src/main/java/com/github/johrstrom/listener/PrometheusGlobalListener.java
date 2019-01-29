package com.github.johrstrom.listener;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.util.JMeterUtils;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

public class PrometheusGlobalListener {
	
	public static final String COLLECT_THREADS = "prometheus.save.threads";
	public static final boolean COLLECT_THREADS_DEFAULT = true;
	
	public static final String COLLECT_THREADS_NAME = "prometheus.save.threads.name";
	public static final String COLLECT_THREADS_NAME_DEFAULT = "jmeter_threads";
	
	private static PrometheusGlobalListener listener;
	private static AtomicInteger metricPrefixNumber = new AtomicInteger(0);

	private transient Gauge threadCollector;
	
	private PrometheusGlobalListener() {
		if (collectThreads()) {
			this.threadCollector = Gauge.build()
					.name(this.threadMetricName())
					.help("Guage for jmeter threads")
					.labelNames("state")
					.create()
					.register(CollectorRegistry.defaultRegistry);
		}		
	}
	
	public static PrometheusGlobalListener getInstance() {
		if(listener == null) {
			listener = new PrometheusGlobalListener();
		}
		
		return listener;
	}
	

	public void update() {
		if (collectThreads()) {
			
			ThreadCounts tc = JMeterContextService.getThreadCounts();
			
			threadCollector.labels("active").set(tc.activeThreads);
			threadCollector.labels("finished").set(tc.finishedThreads);
			threadCollector.labels("started").set(tc.startedThreads);
			
		}
		
	}
	
	public int getMetricPrefixNumber() {
		return metricPrefixNumber.get();
	}
	
	public int incrementAndGetMetricPrefixNumber() {
		return metricPrefixNumber.incrementAndGet();
	}
	
	public void decrementMetricPrefixNumber() {
		metricPrefixNumber.decrementAndGet();
	}
	
	public boolean collectThreads() {
		return JMeterUtils.getPropDefault(COLLECT_THREADS, COLLECT_THREADS_DEFAULT);
	}
	
	public String threadMetricName() {
		return JMeterUtils.getPropDefault(COLLECT_THREADS_NAME, COLLECT_THREADS_NAME_DEFAULT);
	}

}
