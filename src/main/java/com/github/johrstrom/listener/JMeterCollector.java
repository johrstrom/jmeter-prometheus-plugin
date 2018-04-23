package com.github.johrstrom.listener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Collector.Type;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.Summary.Builder;

public class JMeterCollector  {

	public static final String SAMPLER_NAME_LABEL = "sampler_name";
	public static final String ASSERTION_NAME_LABEL = "assertion_name";
	public static final String SUCCESS_LABEL = "success";
	public static final String FAILURE_LABEL = "failure";
	public static final String CODE_LABEL = "code";
	
	private String[] labels = new String[]{};
	private transient Method[] methods = new Method[]{};
	
	private transient Collector collector;
	private Type collectorType;
	private static final Logger log = LoggerFactory.getLogger(JMeterCollector.class);
	private String name;
	private AtomicBoolean isRegistered = new AtomicBoolean(false);
	
	public JMeterCollector(PrometheusSaveConfig config, Type type, String name) {
		this.setType(type);
		this.modifyCollectorLabels(config);
		this.setName(name);
		
		if(config.isCounter()) {
			this.createAsCounter();
		}
		
		if(config.isHistogram()) {
			this.createAsHistogram(config.getBucketsAsDoubles());
		}
		
		if(config.isSummary()) {
			this.createAsSummary(config.getQuantilesAsDoubles());
		}
	}
	
	
	public void update(SampleEvent event) {
		if(this.collector instanceof Counter) {
			updateCounter((Counter) this.collector, event);
		}else if(this.collector instanceof Summary) {
			updateSummary((Summary) this.collector, event);
		}else if(this.collector instanceof Histogram) {
			updateHistogram((Histogram) this.collector, event);
		}		
	}
	
	protected void updateCounter(Counter counter, SampleEvent event) {
		try {
			counter.labels(this.labelValues(event)).inc();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			log.warn(String.format("Didn't update counter %s because of %s:%s",
					this.getName(),e.getClass().toString(), e.getMessage()));
		}
	}
	
	protected void updateHistogram(Histogram histogram, SampleEvent event) {
		
	}
	
	protected void updateSummary(Summary sumamry, SampleEvent event) {
		
	}
	
	public Type getType() {
		return collectorType;
	}
	
	public void setType(Type type) {
		this.collectorType = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String[] getLabels() {
		return this.labels;
	}
	
	/**
	 * Get all the labels for this Collector.
	 * 
	 * @return - an ordered array of labels.
	 */
	public String[] getLabelsWithSampleVariables() {
		int configLabelLength = this.labels.length;
		int sampleVariableLength = SampleEvent.getVarCount();
		int combinedLength = configLabelLength + sampleVariableLength;
		
		String[] returnArray = new String[combinedLength];
		int returnArrayIndex = -1;	//start at -1 so you can ++ when referencing it
		
		//add config first
		for (int i = 0; i < configLabelLength; i++) {
			returnArray[++returnArrayIndex] = this.labels[i];
		}
		
		//now add sample variables
		for (int i = 0; i < sampleVariableLength; i++) {
			returnArray[++returnArrayIndex] = SampleEvent.getVarName(i);
		}
		
		return returnArray;
	}
	
	/**
	 * Set the labels for this Collector.
	 *  
	 * @param labels - the orderd list to set to.
	 */
	protected void setLabels(String[] labels) {
		this.labels = labels;
	}

	/**
	 * Get an array of all the Methods to use when updating
	 * the Collector. 
	 * 
	 * @return - an ordered array of methods. 
	 */
	public Method[] getMethods() {
		return this.methods;
	}

	/**
	 * Modify the list of getter methods.
	 * 
	 * @param getterMethods - the ordered list to set to.
	 */
	protected void setMethods(Method[] methods) {
		this.methods = methods;
	}
	
	/**
	 * Add a label to the list of labels.
	 * 
	 * @param label - the label to add
	 */
	public void addLabel(String label){
		int len = this.getLabels().length; 
		String[] newArr = Arrays.copyOf(this.getLabels(), len+1);
		newArr[len] = label;
		this.setLabels(newArr);
	}
	
	/**
	 * Add a method to the list of methods.
	 * 
	 * @param m - the method to add
	 */
	public void addGetterMethod(Method m){
		int len = this.getMethods().length;
		Method[] newArr = Arrays.copyOf(this.getMethods(), len+1);
		newArr[len] = m;
		this.setMethods(newArr);
	}
	
	/**
	 * Convenience method for exposing sampler labels.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamplerLabel() throws NoSuchMethodException, SecurityException {
		this.addLabel(SAMPLER_NAME_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("getSampleLabel"));
	}

	/**
	 * Convenience method for exposing sampler success.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamplerSuccess() throws NoSuchMethodException, SecurityException {
		this.addLabel(SUCCESS_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("isSuccessful"));
	}
	
	/**
	 * Convenience method for exposing sampler response code.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamlerCode() throws NoSuchMethodException, SecurityException {
		this.addLabel(CODE_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("getResponseCode"));
	}
	
	/**
	 * Convenience method for exposing assertion failures.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveAssertionFailure() throws NoSuchMethodException, SecurityException {
		this.addLabel(FAILURE_LABEL);
		this.addGetterMethod(AssertionResult.class.getMethod("isFailure"));
	}
	
	/**
	 * Convenience method for exposing assertion names.
	 * 
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveAssertionName() throws NoSuchMethodException, SecurityException {
		this.addLabel(ASSERTION_NAME_LABEL);
		this.addGetterMethod(AssertionResult.class.getMethod("getName"));
	}
	
	public void register() {
		if(!this.isRegistered())
			this.collector.register(CollectorRegistry.defaultRegistry);
		
		this.isRegistered.set(true);
	}
	
	public void unregister() {
		if(this.isRegistered())
			CollectorRegistry.defaultRegistry.unregister(this.collector);
		
		this.isRegistered.set(false);
	}
	
	public boolean isRegistered() {
		return this.isRegistered.get();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		//print all the labels
		if(this.getLabels().length != 0){
			sb.append("labels: [");
			
			for(String label : this.getLabels()){
				sb.append(String.format("%s,", label));
			}
			
			sb.append("],");
		}
		
		//print all the methods
		
		if(this.getMethods().length != 0){
			sb.append("methods: [");
			
			for(Method method : this.getMethods()){
				sb.append(String.format("%s,", method.toString()));
			}
			
			sb.append("],");
		}
		
		sb.append("}");
		return sb.toString();
	}
	
	
	private void modifyCollectorLabels(PrometheusSaveConfig config) {
		try {

			if (config.saveLabel()) {
				this.saveSamplerLabel();
			}

			if (config.saveCode()) {
				this.saveSamlerCode();
			}

			if (config.saveSuccess()) {
				this.saveSamplerSuccess();
			}
			
			
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}
	
	private void createAsCounter() {
		
		this.collector = Counter.build(this.getName(), "JMeter events in a counter")
				.labelNames(this.getLabelsWithSampleVariables())
				.create();
	}
	
	private void createAsHistogram(List<Double> buckets) {
				
		this.collector = Histogram.build(this.getName(), "JMeter latencies in a histogram")
				.labelNames(this.getLabelsWithSampleVariables())
				.buckets(buckets.stream().mapToDouble(Double::doubleValue).toArray())
				.create();
		
	}
	
	private void createAsSummary(List<Double> quantiles) {
		Builder summaryBuilder = Summary.build(name, "JMeter latencies in a summary")
				.labelNames(this.getLabelsWithSampleVariables());
				
		
		for(int i = 0; i < quantiles.size(); i+=2) {
			summaryBuilder.quantile(quantiles.get(i), quantiles.get(i+1));
		}
		
		this.collector = summaryBuilder.create();
	}
	
	/**
	 * For a given SampleEvent, get all the label values as determined by the
	 * configuration. Can return reflection related errors because this invokes
	 * SampleEvent accessor methods like getResponseCode or getSuccess.
	 * 
	 * @param event
	 *            - the event that occurred
	 * @return
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	protected String[] labelValues(SampleEvent event)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String[] sampleVarArr = this.sampleVariableValues(event);
		int configLabelLength = this.getLabels().length;
		int totalLength = configLabelLength + sampleVarArr.length;
		
		String[] values = new String[totalLength];
		int valuesIndex = -1;	//start at -1 so you can ++ when referencing it

		for (int i = 0; i < configLabelLength; i++) {
			Method m = this.getMethods()[i];
			values[++valuesIndex] = m.invoke(event.getResult()).toString();
		}
		
		System.arraycopy(sampleVarArr, 0, values, configLabelLength, sampleVarArr.length);

		return values;

	}
	
	private String[] sampleVariableValues(SampleEvent event) {
		int sampleVariableLength = SampleEvent.getVarCount();
		String[] values = new String[sampleVariableLength];
		
		for(int i = 0; i < sampleVariableLength; i++) {
			String varValue =  event.getVarValue(i);
			values[i] = (varValue == null) ?  "" : varValue;
		}
		
		return values;
	}
	
}
