package com.github.johrstrom.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;

/**
 * A simple Pojo for mapping configurations from Jmeter to the Prometheus
 * collectors. It holds an array of labels for a given collector 
 * and the methods to access SamplerResult and AssertionResult.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class CollectorConfig implements Serializable {
	
	private static final long serialVersionUID = 6317013594387218799L;
	
	private String[] labels = new String[]{};
	private transient Method[] methods = new Method[]{};
	
	public static final String SAMPLER_NAME_LABEL = "sampler_name";
	public static final String ASSERTION_NAME_LABEL = "assertion_name";
	public static final String SUCCESS_LABEL = "success";
	public static final String FAILURE_LABEL = "failure";
	public static final String CODE_LABEL = "code";
	
	/**
	 * Get all the labels for this Collector.
	 * 
	 * @return - an ordered array of labels.
	 */
	public String[] getLabels() {
		return this.labels;
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
	
}
