package com.github.johrstrom.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class PrometheusSaveConfig implements Serializable {

	public static final String DEFAULT_QUANTILES = "0.5,0.1|0.9,0.1|0.99,0.1";
	public static final String DEFAULT_HISTO_BUCKETS = "100,200,500,1000,3000";
	public static final String DEFAULT_SAMPLE_PREFIX = "jmeter_samples";
	public static final String DEFAULT_ASSERTION_PREFIX = "jmeter_assertions";
	
	
	private static final long serialVersionUID = 3374323089879858706L;

	private static final Logger log = LoggerFactory.getLogger(PrometheusSaveConfig.class);

	private boolean label, code, success, assertions, counter, summary, histogram, failureCounter;
	private String quantiles, buckets, metricPrefix;

	public PrometheusSaveConfig() {
		this(false, DEFAULT_SAMPLE_PREFIX);
	}
	
	public PrometheusSaveConfig(String prefix) {
		this(false, prefix);
	}

	public PrometheusSaveConfig(boolean save, String prefix) {
		this.setSaveLabel(save);
		this.setSaveCode(save);
		this.setSaveSuccess(save);
		this.setSaveAssertions(save);
		this.setCounter(save);
		this.setSummary(save);
		this.setHistogram(save);
		this.setFailureCounter(save);
		this.setMetricPrefix(prefix);
	}

	public boolean saveLabel() {
		return this.label;
	}

	public void setSaveLabel(boolean save) {
		log.debug("Setting save label to " + save);
		this.label = save;
	}

	public boolean saveCode() {
		return this.code;
	}

	public void setSaveCode(boolean save) {
		log.debug("Setting save code to " + save);
		this.code = save;
	}

	public boolean saveSuccess() {
		return this.success;
	}

	public void setSaveSuccess(boolean save) {
		log.debug("Setting save success to " + save);
		this.success = save;
	}

	public boolean saveAssertions() {
		return this.assertions;
	}

	public void setSaveAssertions(boolean save) {
		log.debug("Setting save assertions to " + save);
		this.assertions = save;
	}

	public boolean isCounter() {
		return counter;
	}

	public void setCounter(boolean counter) {
		this.counter = counter;
	}

	public boolean isHistogram() {
		return histogram;
	}

	public void setHistogram(boolean histogram) {
		this.histogram = histogram;
	}

	public boolean isFailureCounter() {
		return failureCounter;
	}

	public void setFailureCounter(boolean failureCounter) {
		this.failureCounter = failureCounter;
	}

	public String getBuckets() {
		return buckets == null ? DEFAULT_HISTO_BUCKETS : buckets;
	}
	
	public List<Double> getBucketsAsDoubles() {
		String bucketString = this.getBuckets();
		List<Double> bucketList = new ArrayList<>();
		
		for(String bucket : bucketString.split(",")) {
			try {
				double b = Double.parseDouble(bucket);
				bucketList.add(b);
			} catch (NumberFormatException e) {
				log.warn(String.format("Didn't parse bucket %s because of %s: %s", 
						bucket, e.getClass().toString(), e.getMessage()));
			}
		}
		
		return bucketList;
	}

	public void setBuckets(String buckets) {
		this.buckets = buckets;
	}

	public String getQuantiles() {
		return quantiles == null ? DEFAULT_QUANTILES : quantiles;
	}
	
	public List<Double> getQuantilesAsDoubles() {
		String quantString = this.getQuantiles();
		List<Double> quantileDoubles = new ArrayList<>();
		
		for(String subQuantiles : quantString.split("|")) {
			String[] singleQuantile = subQuantiles.split(",");
			if(singleQuantile.length == 2) {
				
				try {
					double q = Double.parseDouble(singleQuantile[0]);
					double e = Double.parseDouble(singleQuantile[1]);
					
					quantileDoubles.add(q);
					quantileDoubles.add(e);
				} catch (NumberFormatException e) {
					log.warn(String.format("Didn't parse quantile %s/%s because of %s: %s", 
							singleQuantile[0], singleQuantile[1], e.getClass().toString(), e.getMessage()));
				}

			}
		}
		
		return quantileDoubles;
	}

	public void setQuantiles(String quantiles) {
		this.quantiles = quantiles;
	}

	public boolean isSummary() {
		return summary;
	}

	public void setSummary(boolean summary) {
		this.summary = summary;
	}

	public String getMetricPrefix() {
		return metricPrefix;
	}

	public void setMetricPrefix(String metricPrefix) {
		this.metricPrefix = metricPrefix;
	}
	
	

}
