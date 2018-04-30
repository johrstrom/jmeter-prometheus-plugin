package com.github.johrstrom.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Test;

public class PrometheusListenerTest {
	
	static {
		JMeterUtils.loadJMeterProperties("src/test/resources/user.properties");
	}
	
	@Test
	public void listenerIsSerializable() throws IOException {
		ByteArrayOutputStream objectBuffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(objectBuffer);
		
		PrometheusListener listener = new PrometheusListener();
		out.writeObject(listener);
		
		Assert.assertTrue(listener != null);
		Assert.assertTrue(objectBuffer.size() > 0);
		
	}
	
	
//	@Test
//	public void multipleDefaultInstancesOK() {
//		PrometheusListener listener1 = new PrometheusListener();
//		
//		PrometheusSaveConfig sampleConfig = new PrometheusSaveConfig(false, "second_instance");
//		PrometheusSaveConfig assertionConfig = new PrometheusSaveConfig(false, "second_instance");
//		
//		PrometheusListener listener2 = new PrometheusListener(sampleConfig, assertionConfig);
//		
//		Assert.assertTrue(listener1 != listener2);
//		
//		listener1.deleteCollectors();
//		listener2.deleteCollectors();
//	}
//
//	
//	@Test
//	public void multipleWithSaveInstancesOK() {
//		PrometheusListener listener1 = new PrometheusListener();
//		
//		PrometheusSaveConfig sampleConfig = new PrometheusSaveConfig(true, "second_instance");
//		PrometheusSaveConfig assertionConfig = new PrometheusSaveConfig(true, "second_instance");
//		
//		PrometheusListener listener2 = new PrometheusListener(sampleConfig, assertionConfig);
//		
//		Assert.assertTrue(listener1 != listener2);
//		
//		listener1.deleteCollectors();
//		listener2.deleteCollectors();
//	}
	
}
