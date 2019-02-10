package com.github.johrstrom.collector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.NullProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;

public abstract class CollectorElement<C extends BaseCollectorConfig> extends AbstractTestElement {

	public static final String COLLECTOR_DEF = "prometheus.collector_definitions";
	
	protected Map<C,Collector> collectors = new HashMap<C,Collector>();
	protected transient JMeterCollectorRegistry registry = JMeterCollectorRegistry.getInstance();
	
	
	private static Logger log = LoggerFactory.getLogger(CollectorElement.class);
	private static final long serialVersionUID = 963612021269632269L;
	
	public CollectorElement() {
		log.debug("making a new config element: " + this.toString());
		this.setCollectorConfigs(new ArrayList<C>());
	}
	
	public CollectionProperty getCollectorConfigs() {
		JMeterProperty collectorDefinitions = this.getProperty(COLLECTOR_DEF);
		
		if(collectorDefinitions == null || collectorDefinitions instanceof NullProperty) {
			collectorDefinitions = new CollectionProperty(COLLECTOR_DEF, new ArrayList<C>());
			collectorDefinitions.setName(COLLECTOR_DEF);
		}
		
		return (CollectionProperty) collectorDefinitions;
		
	}
	
	public void setCollectorConfigs(List<C> collectors) {
		log.debug("setting new collectors. size is: " + collectors.size());
		this.setCollectorConfigs(new CollectionProperty(COLLECTOR_DEF, collectors));
	}
	
	public void setCollectorConfigs(CollectionProperty collectors) {
		this.setProperty(collectors);
	}
	
	protected void  clearCollectors() {
		Iterator<Entry<C, Collector>> iter = this.collectors.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<C, Collector> entry = iter.next();
			this.registry.unregister(entry.getKey());
			iter.remove();
		}
	}
	
	protected void makeNewCollectors() {
		this.clearCollectors();
		
		CollectionProperty collectorDefs = this.getCollectorConfigs();
		PropertyIterator iter = collectorDefs.iterator();
		
		while(iter.hasNext()) {
			
			try {
				@SuppressWarnings("unchecked")
				C config = (C) iter.next().getObjectValue();
				Collector collector = registry.getOrCreateAndRegister(config);
				
				this.collectors.put(config, collector);
				log.debug("added " + config.getMetricName() + " to list of collectors");
			}catch(Exception e) {
				log.error("Didn't create new collector because of error, ",e);
			}
			
		}
		
	}
	
	
}
