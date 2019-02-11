package com.github.johrstrom.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.test.TestUtilities;

public class PrometheusListenerTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	static {
		JMeterUtils.loadJMeterProperties("src/test/resources/user.properties");
	}
	
	@Before
	public void before() {
		reg.clear();
	}
	
	@After
	public void after() {
		reg.clear();
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
	
	@Test
	public void canHaveDuplicateMetrics() {
		PrometheusListener first = new PrometheusListener();
		PrometheusListener second = new PrometheusListener();
		
		first.setCollectorConfigs(TestUtilities.simpleListListener());
		second.setCollectorConfigs(TestUtilities.simpleListListener());
		
		first = (PrometheusListener) first.clone();
		second = (PrometheusListener) second.clone();
		
		first.testStarted();
		second.testStarted();
		
		first.testEnded();
		second.testEnded();
		
	}
	
}
