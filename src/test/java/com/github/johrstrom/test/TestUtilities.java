package com.github.johrstrom.test;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.BaseCollectorConfig.JMeterCollectorType;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.listener.ListenerCollectorConfig.Measurable;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.tree.JMeterTreeListener;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.*;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestUtilities {
	
	public static final String TEST_VAR_NAME = "arbitrary_var";
	public static final String TEST_SAMPLER_NAME = "super_cool_sampler";
	public static final String TEST_ASSERTION_NAME = "super_cool_assertion";
	public static final String TEST_ASSERTION_NAME_ALT = "other_super_cool_assertion";
	public static final String TEST_SAMPLER_CODE = "909";
	public static final String TEST_VAR_VALUE = "bar_value";
	public static final String TEST_THREADNAME = "super_cool_name";

	public static final String[] TEST_LABELS = new String[] {TEST_VAR_NAME,"label","threadname","code"};
	public static final String[] EXPECTED_LABELS = new String[] {TEST_VAR_VALUE, TEST_SAMPLER_NAME, "super_cool_name", TEST_SAMPLER_CODE};
	
	public static final String[] TEST_ASSERTION_LABELS = new String[] {TEST_VAR_NAME,"label"};
	public static final String[] EXPECTED_ASSERTION_LABELS = new String[] {TEST_VAR_VALUE, TEST_ASSERTION_NAME};
	public static final String[] EXPECTED_ASSERTION_LABELS_ALT = new String[] {TEST_VAR_VALUE, TEST_ASSERTION_NAME_ALT};
	
	public static BaseCollectorConfig simpleCounterCfg() {
		BaseCollectorConfig cfg = new BaseCollectorConfig();
		cfg.setMetricName("simple_counter");
		cfg.setType(JMeterCollectorType.COUNTER.toString());
		cfg.setHelp("some helpe message");
		
		return cfg;
	}
	
	public static ListenerCollectorConfig listenerCounterCfg(String name, Measurable measurable, String listenTo) {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(TestUtilities.TEST_ASSERTION_LABELS);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName(name);
		cfg.setMeasuring(measurable.toString());
		cfg.setListenTo(listenTo);
		
		return cfg;
	}
	
	public static ListenerCollectorConfig listenerSuccessRatioCfg(String name, String listenTo) {
		ListenerCollectorConfig cfg = new ListenerCollectorConfig();
		cfg.setLabels(TestUtilities.TEST_ASSERTION_LABELS);
		cfg.setMetricName(name);
		cfg.setType(JMeterCollectorType.SUCCESS_RATIO.toString());
		cfg.setHelp("some helpe message");
		cfg.setListenTo(listenTo);
		
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
        JMeterUtils.setLocale(Locale.ENGLISH);
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
    	list.add(simpleSuccessRatioCfg());
    	
    	return list;
    }
    
    public static List<ListenerCollectorConfig> simpleListListener() {
    	List<ListenerCollectorConfig> list = new ArrayList<ListenerCollectorConfig>(); 
    	
    	list.add(new ListenerCollectorConfig(simpleGaugeCfg()));
    	list.add(new ListenerCollectorConfig(simpleHistogramCfg()));
    	list.add(new ListenerCollectorConfig(simpleSummaryCfg()));
    	list.add(new ListenerCollectorConfig(simpleCounterCfg()));
    	list.add(new ListenerCollectorConfig(simpleSuccessRatioCfg()));
    	
    	return list;
    }
    
    public static List<ListenerCollectorConfig> fullListListener(){
    	List<ListenerCollectorConfig> list = new ArrayList<ListenerCollectorConfig>();
    	
    	// ---------- counters and success ratio
    	ListenerCollectorConfig cfg = new ListenerCollectorConfig(simpleCounterCfg());
    	cfg = redoNameAndMeasuring(cfg, "test_count_total", Measurable.CountTotal);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_failure_total", Measurable.FailureTotal);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_success_total", Measurable.SuccessTotal);
    	list.add(cfg);
    	
    	cfg = new ListenerCollectorConfig(simpleSuccessRatioCfg());
    	cfg = redoNameAndMeasuring(cfg, "test_ratio", Measurable.SuccessRatio);
    	list.add(cfg);
    	
    	// ------- histograms
    	cfg = new ListenerCollectorConfig(simpleHistogramCfg());
    	cfg = redoNameAndMeasuring(cfg, "test_hist_rtime", Measurable.ResponseTime);
    	list.add(cfg);
    
    	cfg = redoNameAndMeasuring(cfg, "test_hist_rsize", Measurable.ResponseSize);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_hist_latency", Measurable.Latency);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_hist_idle_time", Measurable.IdleTime);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_hist_connect_time", Measurable.ConnectTime);
    	list.add(cfg);
    
    	// -------- summaries
    	cfg = new ListenerCollectorConfig(simpleSummaryCfg());
    	cfg = redoNameAndMeasuring(cfg, "test_summary_rtime", Measurable.ResponseTime);
    	list.add(cfg);
    
    	cfg = redoNameAndMeasuring(cfg, "test_summary_rsize", Measurable.ResponseSize);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_summary_latency", Measurable.Latency);
    	list.add(cfg);    	
    	
    	cfg = redoNameAndMeasuring(cfg, "test_summary_idle_time", Measurable.IdleTime);
    	list.add(cfg);
    	
    	cfg = redoNameAndMeasuring(cfg, "test_summary_connect_time", Measurable.ConnectTime);
    	list.add(cfg);
    	
    	return list;
    }
    
    public static ListenerCollectorConfig redoNameAndMeasuring(ListenerCollectorConfig cfg, String name, Measurable m) {
    	ListenerCollectorConfig clone = (ListenerCollectorConfig) cfg.clone();
    	
    	clone.setMetricName(name);
    	clone.setMeasuring(m.toString());
    	   	
    	return clone;
    }
    
    public static ResultAndVariables resultWithLabels() {
    	SampleResult result = new SampleResult();
    	
    	result.setSampleLabel(TEST_SAMPLER_NAME);
    	result.setResponseCode(TEST_SAMPLER_CODE);
    	
    	JMeterVariables vars = new JMeterVariables();
		vars.put(TEST_VAR_NAME, TEST_VAR_VALUE);
		JMeterContextService.getContext().setVariables(vars);
		
		return new ResultAndVariables(result, vars);
    }
    
    public static class ResultAndVariables {
    	public SampleResult result;
    	public JMeterVariables vars;
    	
    	public ResultAndVariables(SampleResult result, JMeterVariables vars) {
    		this.result = result;
    		this.vars = vars;
    	}
    }
    
    
}
