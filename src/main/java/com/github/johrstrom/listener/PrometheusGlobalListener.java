package com.github.johrstrom.listener;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

public class PrometheusGlobalListener {
	
	public static final String COLLECT_THREADS = "prometheus.save.threads";
	public static final boolean COLLECT_THREADS_DEFAULT = true;
	
	private static PrometheusGlobalListener listener;
	private static AtomicInteger metricPrefixNumber = new AtomicInteger(0);
	
	// Thread counter
	private transient Gauge threadCollector;
	
	private PrometheusGlobalListener() {
		if (collectThreads())
			this.threadCollector = Gauge.build().name("jmeter_running_threads").help("Counter for running threds")
					.create().register(CollectorRegistry.defaultRegistry);
		
		
	}
	
	public static PrometheusGlobalListener getInstance() {
		if(listener == null) {
			listener = new PrometheusGlobalListener();
		}
		
		return listener;
	}
	

	public void update() {
		if (collectThreads())
			threadCollector.set(JMeterContextService.getContext().getThreadGroup().getNumberOfThreads());
		
		
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
	
	protected boolean collectThreads() {
		return JMeterUtils.getPropDefault(COLLECT_THREADS, COLLECT_THREADS_DEFAULT);
	}

}
