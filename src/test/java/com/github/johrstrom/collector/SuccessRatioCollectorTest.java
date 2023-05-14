package com.github.johrstrom.collector;

import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.test.TestUtilities;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SuccessRatioCollectorTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	private final String[] labelNames = new String[] {"foo_label","label"};
	private final String[] labelValues = new String[] {"bar_value", "myLabelz"};
	
	@Test
	public void testSuccess() {

		BaseCollectorConfig base = TestUtilities.simpleSuccessRatioCfg();
		
		base.setLabels(labelNames);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		String baseName = "something_ratio";
		cfg.setMetricName(baseName);
		
		SuccessRatioCollector c = (SuccessRatioCollector) reg.getOrCreateAndRegister(cfg);
		
		
		c.incrementSuccess(labelValues);
		
		List<MetricFamilySamples> families = c.collect();
		boolean foundSuccess, foundFailure, foundTotal;
		foundSuccess = foundFailure = foundTotal = false;
		
		for(MetricFamilySamples family : families) {
			switch (family.name) {
				case "something_ratio_success":
					assertOnSingleFamily(family, 1);
					foundSuccess = true;

					break;
				case "something_ratio_failure":
					assertOnSingleFamily(family, 0);
					foundFailure = true;

					break;
				case "something_ratio":
					assertOnSingleFamily(family, 1);
					foundTotal = true;

					break;
				default:
					Assert.fail(family.name + " is not an expected metric family name");
					break;
			}
		}
		
		Assert.assertTrue(foundSuccess && foundFailure && foundTotal);
	}
	
	@Test
	public void testFailure() {

		BaseCollectorConfig base = TestUtilities.simpleSuccessRatioCfg();
		
		base.setLabels(labelNames);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		String baseName = "otherthing_ratio";
		cfg.setMetricName(baseName);
		
		SuccessRatioCollector c = (SuccessRatioCollector) reg.getOrCreateAndRegister(cfg);
		
		
		c.incrementFailure(labelValues);	//only real diff with test above
		
		List<MetricFamilySamples> families = c.collect();
		boolean foundSuccess, foundFailure, foundTotal;
		foundSuccess = foundFailure = foundTotal = false;
		
		for(MetricFamilySamples family : families) {
			switch (family.name) {
				case "otherthing_ratio_success":
					assertOnSingleFamily(family, 0);
					foundSuccess = true;

					break;
				case "otherthing_ratio_failure":
					assertOnSingleFamily(family, 1);
					foundFailure = true;

					break;
				case "otherthing_ratio":
					assertOnSingleFamily(family, 1);
					foundTotal = true;

					break;
				default:
					Assert.fail(family.name + " is not an expected metric family name");
					break;
			}
		}
		
		Assert.assertTrue(foundSuccess && foundFailure && foundTotal);
	}
	
	private void assertOnSingleFamily(MetricFamilySamples family, double expectedValue) {
		Assert.assertEquals(2, family.samples.size());
		Sample sample = family.samples.get(0);
		
		Assert.assertArrayEquals(labelValues, sample.labelValues.toArray());
		Assert.assertArrayEquals(labelNames, sample.labelNames.toArray());
		Assert.assertEquals(expectedValue, sample.value, 0.1);
	}

}
