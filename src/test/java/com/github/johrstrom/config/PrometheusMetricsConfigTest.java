package com.github.johrstrom.config;

import com.github.johrstrom.test.TestUtilities;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;


public class PrometheusMetricsConfigTest {
	
	@Test
	public void listenerIsSerializable() throws IOException {
		ByteArrayOutputStream objectBuffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(objectBuffer);
		
		PrometheusMetricsConfig cfg = new PrometheusMetricsConfig();
		cfg.setCollectorConfigs(TestUtilities.simpleListConfig());
		
		out.writeObject(cfg);

		Assert.assertNotNull(cfg);
		Assert.assertTrue(objectBuffer.size() > 0);
		
	}

}
