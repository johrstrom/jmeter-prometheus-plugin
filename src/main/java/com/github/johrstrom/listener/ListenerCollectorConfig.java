package com.github.johrstrom.listener;

import com.github.johrstrom.collector.BaseCollectorConfig;

public class ListenerCollectorConfig extends BaseCollectorConfig {

	private static final long serialVersionUID = -8968099072667146399L;

	public static String LISTEN_TO = "listener.collector.listen_to";
	public static String MEASURING = "listener.collector.measuring";
	
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
		return this.getPropertyAsString(MEASURING, "response time");
	}
	
	
}
