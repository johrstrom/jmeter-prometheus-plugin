package com.github.johrstrom.collector;

public class SampleCollectorConfig extends BaseCollectorConfig {

	private static final long serialVersionUID = -8968099072667146399L;

	public static String LISTEN_TO = "collector.listen_to";
	
	public void setListenTo(String listenTo) {
		this.getPropertyAsString(LISTEN_TO, listenTo);
	}
	
	public void getListenTo() {
		this.setProperty(LISTEN_TO, "samples");
	}
	
	
}
