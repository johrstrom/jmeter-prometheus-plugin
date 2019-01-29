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
	
}
