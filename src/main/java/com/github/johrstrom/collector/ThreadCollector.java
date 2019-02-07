package com.github.johrstrom.collector;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.util.JMeterUtils;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;

public class ThreadCollector extends Collector {
	
	public static final String COLLECT_THREADS_NAME = "prometheus.save.threads.name";
	public static final String COLLECT_THREADS_NAME_DEFAULT = "jmeter_threads";
	
	public static final String COLLECT_THREADS = "prometheus.save.threads";
	public static final boolean COLLECT_THREADS_DEFAULT = true;
	
	private static final boolean saveThreads = JMeterUtils.getPropDefault(COLLECT_THREADS, COLLECT_THREADS_DEFAULT);
	
	private static final Gauge threadCollector = Gauge.build()
			.name(threadMetricName())
			.help("Guage for jmeter threads")
			.labelNames("state")
			.create();

	
	private static ThreadCollector instance = null;
	private static final AtomicBoolean registered = new AtomicBoolean(false);
	
	public static ThreadCollector getInstance() {
		if (instance == null) {
			instance = new ThreadCollector();
		}
		
		return instance;
	}
	
	private ThreadCollector() {
		if(saveThreads && !registered.get()) {
			this.register(CollectorRegistry.defaultRegistry);
			registered.set(true);
		}
	}
	
	
	public static String threadMetricName() {
		return JMeterUtils.getPropDefault(COLLECT_THREADS_NAME, COLLECT_THREADS_NAME_DEFAULT);
	}

	@Override
	public List<MetricFamilySamples> collect() {	
		ThreadCounts tc = JMeterContextService.getThreadCounts();
		
		threadCollector.labels("active").set(tc.activeThreads);
		threadCollector.labels("finished").set(tc.finishedThreads);
		threadCollector.labels("started").set(tc.startedThreads);
		
		return threadCollector.collect();
	}

}
