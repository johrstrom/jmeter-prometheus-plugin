package com.github.johrstrom.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector.Type;

public class CollectorConfig extends AbstractTestElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1520731432941268549L;
	
	public static String HELP = "collector.help";
	public static String NAME = "collector.metric_name";
	public static String TYPE = "collector.type";
	public static String LABELS = "collector.labels";
	public static String QUANTILES_OR_BUCKETS = "collector.quantiles_or_buckets";
	
	public static double[] DEFAULT_BUCKET_SIZES = {100,200,500,1000};
	public static QuantileDefinition[] DEFAULT_QUANTILES = defaultQuantiles();
	
	private Logger log = LoggerFactory.getLogger(CollectorConfig.class);

	public String getHelp() {
		return this.getPropertyAsString(HELP, "");
	}

	public void setHelp(String help) {
		this.setProperty(HELP, help);
	}

	public String getType() {
		return this.getPropertyAsString(TYPE, "");
	}
	
	public Type getPrometheusType() {
		return Type.valueOf(this.getType());
	}

	public void setType(String type) {
		this.setProperty(TYPE, type);
	}

	public String getQuantileOrBucket() {
		return this.getPropertyAsString(QUANTILES_OR_BUCKETS,"");
	}

	public void setQuantileOrBucket(String quantileOrBucket) {
		this.setProperty(QUANTILES_OR_BUCKETS, quantileOrBucket);
	}
	
	public double[] getBuckets() {
		String buckets = getQuantileOrBucket();
		
		if(buckets == null || buckets.isEmpty()) {
			return DEFAULT_BUCKET_SIZES;
		}else {
			return this.parseBucketsFromString(buckets);
		}
	}
	
	public QuantileDefinition[] getQuantiles() {
		String quantiles = getQuantileOrBucket();
		
		if(quantiles == null || quantiles.isEmpty()) {
			return DEFAULT_QUANTILES;
		}else {
			return this.parseQuantilesFromString(quantiles);
		}
	}

	public String getMetricName() {
		return this.getPropertyAsString(NAME, "");
	}

	public void setMetricName(String name) {
		this.setProperty(NAME, name);
	}
	
	public void setLabels(String[] labels) {
		List<String> list = new ArrayList<String>(Arrays.asList(labels));
		
		Iterator<String> it = list.iterator();
		while(it.hasNext()) { // can't have empty strings from Gui
			String item = it.next();
			if(item == null || item.isEmpty()) {
				it.remove();
			}
		}
		
		this.setProperty(new CollectionProperty(LABELS, list));
	}
	
	public String[] getLabels() {
		CollectionProperty prop = (CollectionProperty) this.getProperty(LABELS);
		String[] retArray = new String[prop.size()];
		PropertyIterator it = prop.iterator();
		
		int i=0;
		while(it.hasNext()) {
			String next = it.next().getStringValue();
			retArray[i] = next;
			i++;
		}
		
		return retArray;		
	}
	
	public String getLabelsAsString() {
		CollectionProperty prop = (CollectionProperty) this.getProperty(LABELS);
		StringBuilder sb = new StringBuilder();
		PropertyIterator it = prop.iterator();
		
		while(it.hasNext()) {
			String next = it.next().getStringValue();
			
			sb.append(next);
			if(it.hasNext())
				sb.append(",");
			
		}
		
		return sb.toString();
	}
	
	@Override
	public String toString() {
		PropertyIterator it = this.propertyIterator();
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		while(it.hasNext()) {
			JMeterProperty prop = it.next();
			sb.append(prop.getName()).append(": ").append(prop.getStringValue()).append(", ");
		}
		
		sb.append("]");
		return sb.toString();
	}
	
	protected double[] parseBucketsFromString(String fullBucketString) {
		String[] bucketStrings = fullBucketString.split(",");
		List<Double> buckets = new ArrayList<Double>();
		
		for(String bucket : bucketStrings) {
			try {
				double d = Double.parseDouble(bucket);
				buckets.add(d);
			}catch(Exception e) {
				log.warn("couldn't parse {} because of error {}:{}. It wont be included in buckets for the metric {}",
						bucket, e.getClass().toString(), e.getMessage(), this.getMetricName());
			}
		}
		
		if(buckets.isEmpty()) {
			log.warn("Did not parse any buckets for metric {}. Returning defaults", this.getMetricName());
			return DEFAULT_BUCKET_SIZES;
		}else {
			return buckets.stream().mapToDouble(Double::doubleValue).toArray();
		}
	}
	
	protected QuantileDefinition[] parseQuantilesFromString(String fullQuantileString) {
		String[] quantileDefStrings = fullQuantileString.split("\\|");
		List<QuantileDefinition> quantiles = new ArrayList<QuantileDefinition>();
		
		for(String quantile : quantileDefStrings) {
			try {
				QuantileDefinition q = new QuantileDefinition(quantile.split(","));
				quantiles.add(q);
			}catch(Exception e) {
				log.warn("couldn't parse {} because of error {}:{}. It wont be included in quantiles for the metric {}",
						quantile, e.getClass().toString(), e.getMessage(), this.getMetricName());
			}
		}
		
		if(quantiles.isEmpty()) {
			log.warn("Did not parse any quantiles for metric {}. Returning defaults", this.getMetricName());
			return DEFAULT_QUANTILES;
		}else {
			return quantiles.toArray(new QuantileDefinition[quantiles.size()]);
		}
		
	}
	
	protected static QuantileDefinition[] defaultQuantiles() {
		QuantileDefinition[] def = new QuantileDefinition[3];
		
		def[0] = new QuantileDefinition(0.75,0.5);
		def[1] = new QuantileDefinition(0.95,0.1);
		def[2] = new QuantileDefinition(0.99,0.01);
		
		return def;
	}
	
	public static class QuantileDefinition {
		public double quantile;
		public double error;
		
		QuantileDefinition(double quantile, double error){
			this.quantile = quantile;
			this.error = error;
		}
		
		QuantileDefinition(String quantile, String error){
			this.quantile = Double.parseDouble(quantile);
			this.error = Double.parseDouble(error);
		}
		
		QuantileDefinition(String[] definition){
			if(definition.length != 2) {
				throw new IllegalArgumentException(String.format("Quantiles need exactly 2 parameters. %d given.", definition.length));
			}
			this.quantile = Double.parseDouble(definition[0]);
			this.error = Double.parseDouble(definition[1]);
		}
	}

}


