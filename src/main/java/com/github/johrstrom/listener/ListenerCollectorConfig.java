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
		CountTotal,
		SuccessRatio;
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
	
//	@Override
//	public boolean equals(Object o) {
//		if(o instanceof ListenerCollectorConfig) {
//			boolean base = super.equals(o);
//			ListenerCollectorConfig other = (ListenerCollectorConfig) o;
//			
//			boolean measuring = this.getMeasuring().equals(other.getMeasuring());
//			boolean listenTo = this.getListenTo().equals(other.getListenTo());
//			
//			return base && measuring && listenTo;
//		} 
//		
//		return false;
//	}
//
//	@Override
//	public int hashCode() {
//		
//	    final int prime = 31;
//	    int result = 1;
//	    result = prime * result + this.getName().hashCode();
//	    result = prime * result + this.getHelp().hashCode();
//	    result = prime * result + this.getType().hashCode();
//	    result = prime * result + this.getQuantileOrBucket().hashCode();
//	    result = prime * result + this.getLabelsAsString().hashCode();
//	    result = prime * result + this.getListenTo().hashCode();
//	    result = prime * result + this.getMeasuring().hashCode();
//	   
//	    return result;
//	}
	
	
	
}
