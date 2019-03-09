package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.collector.SuccessRatioCollector;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.test.TestUtilities;

public class SuccessRatioUpdaterTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	@Test
	public void testSamples() {
		BaseCollectorConfig base = TestUtilities.simpleSuccessRatioCfg();
		base.setLabels(new String[] {"foo_label","label"});
		String baseName = "something_ratio";
		base.setName(baseName);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		SuccessRatioCollector c = (SuccessRatioCollector) reg.getOrCreateAndRegister(cfg);
		SuccessRatioUpdater u = new SuccessRatioUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(true);		
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		u.update(e);	// first success
		
		Assert.assertTrue(actualLabels.length == 2);
		String[] expectedLabels = new String[] {"bar_value", "myLabelz"};
		Assert.assertArrayEquals(expectedLabels, actualLabels);
		
		double successShouldBeOne = c.getSuccess(expectedLabels);
		double failureShouldBeZero = c.getFailure(expectedLabels);
		double totalShouldBeOne = c.getTotal(expectedLabels);
		
		
		Assert.assertEquals(1, successShouldBeOne, 0.1);
		Assert.assertEquals(0, failureShouldBeZero, 0.1);
		Assert.assertEquals(1, totalShouldBeOne, 0.1);
		
		u.update(e);	// the 2nd success
		res.setSuccessful(false);
		e = new SampleEvent(res,"tg1", vars);
		
		u.update(e);	// now failure = 1, success = 2 and total = 3
		
		double successShouldBeTwo = c.getSuccess(expectedLabels);
		double failureShouldBeOne = c.getFailure(expectedLabels);
		double totalShouldBeThree = c.getTotal(expectedLabels);
		
		
		Assert.assertEquals(2, successShouldBeTwo, 0.1);
		Assert.assertEquals(1, failureShouldBeOne, 0.1);
		Assert.assertEquals(3, totalShouldBeThree, 0.1);
	}

}
