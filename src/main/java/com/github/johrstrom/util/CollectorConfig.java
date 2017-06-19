package com.github.johrstrom.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.jmeter.assertions.AssertionResult;
import org.apache.jmeter.samplers.SampleResult;

public class CollectorConfig {
	
	private List<String> labels = new ArrayList<String>();
	private List<Method> getterMethods = new ArrayList<Method>();
	
	public static final String SAMPLER_NAME_LABEL = "sampler_name";
	public static final String ASSERTION_NAME_LABEL = "assertion_name";
	public static final String SUCCESS_LABEL = "success";
	public static final String FAILURE_LABEL = "failure";
	public static final String CODE_LABEL = "code";
	
	public String[] getLabels() {
		return Arrays.copyOf(this.labels.toArray(new String[]{}), this.labels.size());
	}
	
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public Method[] getGetterMethods() {
		return Arrays.copyOf(this.getterMethods.toArray(new Method[]{}), this.labels.size());
	}

	public void setGetterMethods(List<Method> getterMethods) {
		this.getterMethods = getterMethods;
	}
	
	public void addLabel(String label){
		this.labels.add(label);
	}
	
	public void addGetterMethod(Method m){
		this.getterMethods.add(m);
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamplerLabel() throws NoSuchMethodException, SecurityException {
		this.addLabel(SAMPLER_NAME_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("getSampleLabel"));
	}

	/**
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamplerSuccess() throws NoSuchMethodException, SecurityException {
		this.addLabel(SUCCESS_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("isSuccessful"));
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveSamlerCode() throws NoSuchMethodException, SecurityException {
		this.addLabel(CODE_LABEL);
		this.addGetterMethod(SampleResult.class.getMethod("getResponseCode"));
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveAssertionFailure() throws NoSuchMethodException, SecurityException {
		this.addLabel(FAILURE_LABEL);
		this.addGetterMethod(AssertionResult.class.getMethod("isFailure"));
	}
	
	/**
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public void saveAssertionName() throws NoSuchMethodException, SecurityException {
		this.addLabel(ASSERTION_NAME_LABEL);
		this.addGetterMethod(AssertionResult.class.getMethod("getName"));
	}
	
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		
		//print all the labels
		if(!this.labels.isEmpty()){
			sb.append("labels: [");
			
			for(String label : this.labels){
				sb.append(String.format("%s,", label));
			}
			
			sb.append("],");
		}
		
		//print all the methods
		
		if(!this.getterMethods.isEmpty()){
			sb.append("methods: [");
			
			for(Method method : this.getterMethods){
				sb.append(String.format("%s,", method.toString()));
			}
			
			sb.append("],");
		}
		
		sb.append("}");
		return sb.toString();
	}
	
}
