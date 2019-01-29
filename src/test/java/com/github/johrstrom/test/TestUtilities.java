package com.github.johrstrom.test;

import java.util.Locale;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import com.github.johrstrom.collector.BaseCollectorConfig;

import io.prometheus.client.Collector.Type;

public class TestUtilities {
	
	public static BaseCollectorConfig simpleCounterCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_counter");
		cfg.setType(Type.COUNTER.name());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleSummaryCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_summary");
		cfg.setType(Type.SUMMARY.name());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleHistogramCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_histogram");
		cfg.setType(Type.HISTOGRAM.name());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleGaugeCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_gauge");
		cfg.setType(Type.GAUGE.name());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}

    public static void createJmeterEnv() {
        JMeterUtils.setJMeterHome(System.getProperty("java.io.tmpdir"));
        JMeterUtils.setLocale(new Locale("ignoreResources"));

        JMeterTreeModel jMeterTreeModel = new JMeterTreeModel();
        JMeterTreeListener jMeterTreeListener = new JMeterTreeListener();
        jMeterTreeListener.setModel(jMeterTreeModel);
        
        JMeterContextService.getContext().setVariables(new JMeterVariables());
        StandardJMeterEngine engine = new StandardJMeterEngine();
        JMeterContextService.getContext().setEngine(engine);
        
        JMeterThreadMonitor monitor = new NOOPThreadMonitor();
        
        
        HashTree hashtree = new HashTree();
        hashtree.add(new LoopController());
        
        JMeterThread thread = new JMeterThread(hashtree, monitor, null);
        thread.setThreadName("test thread");
        JMeterContextService.getContext().setThread(thread);
        
        
        ThreadGroup tg1 = new ThreadGroup();
        tg1.setName("tg1");
        JMeterContextService.getContext().setThreadGroup(tg1);

        
    }
}
