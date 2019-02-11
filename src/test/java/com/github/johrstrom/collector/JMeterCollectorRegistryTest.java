package com.github.johrstrom.collector;

import org.junit.Test;

import com.github.johrstrom.test.TestUtilities;

import io.prometheus.client.Collector;

import org.junit.Assert;

public class JMeterCollectorRegistryTest {
	
	JMeterCollectorRegistry registry = JMeterCollectorRegistry.getInstance();

	@Test
	public void safelyGetOrCreate() {
		BaseCollectorConfig cfg = TestUtilities.simpleHistogramCfg();
		cfg.setMetricName("register_tester");
		
		Collector c1 = registry.getOrCreateAndRegister(cfg);
		Collector c2 = registry.getOrCreateAndRegister(cfg);
		
		Assert.assertTrue(c1 == c2);
		Assert.assertTrue(c1.equals(c2));
		
		registry.unregister(cfg);
		registry.unregister(cfg);
		registry.unregister(cfg);
		
		Collector c3 = registry.getOrCreateAndRegister(cfg);
		
		Assert.assertTrue(c3 != c1 && c3 != c2);
		Assert.assertTrue(!c3.equals(c1) && !c3.equals(c2));
		
		registry.unregister(cfg);
		registry.unregister(cfg);
	}
}
