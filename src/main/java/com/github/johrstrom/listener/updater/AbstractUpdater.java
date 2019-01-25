package com.github.johrstrom.listener.updater;

import java.util.HashMap;
import java.util.Map;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.johrstrom.listener.ListenerCollectorConfig;

import io.prometheus.client.Collector;

public abstract class AbstractUpdater {
	
	public static String NULL = "null";
	
	protected Collector collector;
	protected ListenerCollectorConfig config;
	
	private Map<String,Integer> varIndexLookup;
	private static final Logger log = LoggerFactory.getLogger(AbstractUpdater.class);

	public AbstractUpdater(Collector c, ListenerCollectorConfig cfg) {
		this.collector = c;
		this.config = cfg;
		this.buildVarLookup();
	}

	public abstract void update(SampleEvent e);
	
	
	protected String[] labelValues(SampleEvent event) {
		String[] labels = config.getLabels();
		String[] values =  new String[labels.length];
		
		
		for(int i = 0; i < labels.length; i++) {
			String name = labels[i];
			String value = null;
			
			
			if (this.varIndexLookup.get(name) == null) {
				log.debug("no variable index found for {}, must not be in sample_variables", name);
				
				JMeterVariables vars = JMeterContextService.getContext().getVariables();
				value = vars.get(name);
				
			}else {
				int idx = this.varIndexLookup.get(name);
				value = event.getVarValue(idx);
			}
			values[i] = (value == null || value.isEmpty()) ? NULL : value;
		}
		
		
		
		return values;
	}
	

	
	private void buildVarLookup() {
		this.varIndexLookup = new HashMap<String,Integer>();
				
		for(int i = 0; i < SampleEvent.getVarCount(); i++) {
			String name = SampleEvent.getVarName(i);
			if(inLabels(name)) {
				this.varIndexLookup.put(name, i);
			}
		}
		
	}
	
	private boolean inLabels(String searchFor) {
		String[] labels = config.getLabels();
		for(int i = 0; i < labels.length; i++) {
			if(labels[i].equalsIgnoreCase(searchFor)) {
				return true;
			}
		}
		
		return false;
	}

	
}
