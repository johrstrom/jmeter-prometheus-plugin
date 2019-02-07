package com.github.johrstrom.listener;

import com.github.johrstrom.collector.BaseCollectorConfig;

public class ListenerCollectorConfig extends BaseCollectorConfig {

	private static final long serialVersionUID = -8968099072667146399L;

	public static String LISTEN_TO = "listener.collector.listen_to";
	public static String MEASURING = "listener.collector.measuring";
	
	public ListenerCollectorConfig() {
		this(new BaseCollectorConfig());
	}
	
	public ListenerCollectorConfig(BaseCollectorConfig base) {
		this.setMetricName(base.getMetricName());
		this.setType(base.getType());
		this.setHelp(base.getHelp());
		this.setLabels(base.getLabels());
	}
	
	public enum Measurable {
		ResponseTime,
		ResponseSize,
		SuccessTotal,
		FailureTotal,
		CountTotal;
	}
	
	public void setListenTo(String listenTo) {
		this.setProperty(LISTEN_TO, listenTo);
	}
	
	public String getListenTo() {
		return this.getPropertyAsString(LISTEN_TO, "samples");
	}
	
	public void setMeasuring(String measuring) {
		this.setProperty(MEASURING, measuring);
	}
	
	public String getMeasuring() {
		return this.getPropertyAsString(MEASURING, Measurable.ResponseTime.toString());
	}
	
	public Measurable getMeasuringAsEnum() {		
		return Measurable.valueOf(this.getMeasuring());
	}
	
}
