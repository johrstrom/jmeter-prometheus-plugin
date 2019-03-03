package com.github.johrstrom.listener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.test.TestUtilities;

public class PrometheusListenerTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	static {
		TestUtilities.createJmeterEnv();
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
	
	@Test
	public void canReadJMX() throws IOException {
		File jmx = new File("src/test/resources/simple_prometheus_example.jmx");
		HashTree tree = SaveService.loadTree(jmx);
		
		Assert.assertTrue(tree != null);
	}
	
}
