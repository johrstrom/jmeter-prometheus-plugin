package com.github.johrstrom.collector;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterContextService.ThreadCounts;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Gauge;
import io.prometheus.client.hotspot.*;

public class JMeterCollectorRegistry extends CollectorRegistry {

	private static JMeterCollectorRegistry instance = null;
	private static Logger log = LoggerFactory.getLogger(JMeterCollectorRegistry.class);
	private ConcurrentHashMap<BaseCollectorConfig,Collector> registered = new ConcurrentHashMap<>();
	
	private static final boolean saveThreads = 
			JMeterUtils.getPropDefault(ThreadCollector.COLLECT_THREADS, ThreadCollector.COLLECT_THREADS_DEFAULT);
	
	public static final String COLLECT_JVM  = "prometheus.save.jvm";
	public static final boolean COLLECT_JVM_DEFAULT  = true;
	private static final boolean saveJVM = JMeterUtils.getPropDefault(COLLECT_JVM, COLLECT_JVM_DEFAULT);
	
			
	public synchronized static JMeterCollectorRegistry getInstance() {
		if (instance == null) {
			log.debug("Creating prometheus collector registry");
			instance = new JMeterCollectorRegistry();
		}
		return instance;
	}
	
	private JMeterCollectorRegistry() {
		super(true);
		this.initDefaultExports();
		this.createJMeterExports();
	}
	
	private void initDefaultExports() {
		if(saveJVM) {
		    new StandardExports().register(this);
		    new MemoryPoolsExports().register(this);
		    new MemoryAllocationExports().register(this);
		    new BufferPoolsExports().register(this);
		    new GarbageCollectorExports().register(this);
		    new ThreadExports().register(this);
		    new ClassLoadingExports().register(this);
		    new VersionInfoExports().register(this);	
		}
	}
	
	private void createJMeterExports() {
		if(saveThreads) {
			ThreadCollector tc = new ThreadCollector();
			this.register(tc);
			this.registered.put(ThreadCollector.getConfig(), tc);
		}
	}

	
	public synchronized void unregister(BaseCollectorConfig cfg) {
		log.debug("unregistering {}", cfg.getMetricName());
		if(registered.containsKey(cfg)) {
			Collector collector = registered.get(cfg);
			
			try {
				this.unregister(collector);
				this.registered.remove(cfg);
			} catch(Exception e) {
				log.error("can't unregister collector because error: ", e);
			}
		}
	}
	
	
	public synchronized Collector getOrCreateAndRegister(BaseCollectorConfig cfg) { 
		if(registered.containsKey(cfg)) {
			log.trace("{} found already registered.", cfg.getMetricName());
			return registered.get(cfg);
		}else {
			Collector c = BaseCollectorConfig.fromConfig(cfg);
			this.register(c);	//throws exception here if it fails to register
			
			this.registered.put(cfg, c);
			log.debug("created and registered {}", cfg);
			return c;
		}
	}
	
	@Override
	public synchronized void clear() {
		super.clear();
		this.registered.clear();
	}
	
	private static class ThreadCollector extends Collector {
		
		public static final String COLLECT_THREADS_NAME = "prometheus.save.threads.name";
		public static final String COLLECT_THREADS_NAME_DEFAULT = "jmeter_threads";
		
		public static final String COLLECT_THREADS = "prometheus.save.threads";
		public static final boolean COLLECT_THREADS_DEFAULT = true;
		
		
		private final Gauge innerCollector;
	 
		protected ThreadCollector() {
			BaseCollectorConfig cfg = getConfig();
			
			innerCollector = Gauge.build()
					.name(cfg.getMetricName())
					.labelNames(cfg.getLabels())
					.help(cfg.getHelp())
					.create();
		}
		
		protected static BaseCollectorConfig getConfig() {
			BaseCollectorConfig cfg = new BaseCollectorConfig();

			cfg.setHelp("Gauge for jmeter threads");
			cfg.setMetricName(threadMetricName());
			cfg.setLabels(new String[] {"state"});
			cfg.setType(Type.GAUGE.name());
			
			return cfg;
		}
		
		
		public static String threadMetricName() {
			return JMeterUtils.getPropDefault(COLLECT_THREADS_NAME, COLLECT_THREADS_NAME_DEFAULT);
		}

		@Override
		public List<MetricFamilySamples> collect() {	
			ThreadCounts tc = JMeterContextService.getThreadCounts();
			
			innerCollector.labels("active").set(tc.activeThreads);
			innerCollector.labels("finished").set(tc.finishedThreads);
			innerCollector.labels("started").set(tc.startedThreads);
			
			return innerCollector.collect();
		}

	}
	
}
