package com.github.johrstrom.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.BaseCollectorConfig.JMeterCollectorType;
import com.github.johrstrom.listener.ListenerCollectorConfig;

public class TestUtilities {
	
	public static BaseCollectorConfig simpleCounterCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_counter");
		cfg.setType(JMeterCollectorType.COUNTER.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleSummaryCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_summary");
		cfg.setType(JMeterCollectorType.SUMMARY.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleHistogramCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_histogram");
		cfg.setType(JMeterCollectorType.HISTOGRAM.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleGaugeCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_gauge");
		cfg.setType(JMeterCollectorType.GAUGE.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static BaseCollectorConfig simpleSuccessRatioCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_ratio");
		cfg.setType(JMeterCollectorType.SUCCESS_RATIO.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}

    public static void createJmeterEnv() {
    	
        JMeterUtils.setJMeterHome("src/test/resources");
        JMeterUtils.setLocale(new Locale("ignoreResources"));
        JMeterUtils.loadJMeterProperties("src/test/resources/bin/jmeter.properties");
        try {
			SaveService.loadProperties();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
    
    public static List<BaseCollectorConfig> simpleListConfig() {
    	List<BaseCollectorConfig> list = new ArrayList<BaseCollectorConfig>(); 
    	
    	list.add(simpleGaugeCfg());
    	list.add(simpleHistogramCfg());
    	list.add(simpleSummaryCfg());
    	list.add(simpleCounterCfg());
    	
    	return list;
    }
    
    public static List<ListenerCollectorConfig> simpleListListener() {
    	List<ListenerCollectorConfig> list = new ArrayList<ListenerCollectorConfig>(); 
    	
    	list.add(new ListenerCollectorConfig(simpleGaugeCfg()));
    	list.add(new ListenerCollectorConfig(simpleHistogramCfg()));
    	list.add(new ListenerCollectorConfig(simpleSummaryCfg()));
    	list.add(new ListenerCollectorConfig(simpleCounterCfg()));
    	
    	return list;
    }
}
