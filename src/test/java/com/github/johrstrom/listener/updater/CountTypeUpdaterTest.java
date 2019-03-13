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
import com.github.johrstrom.listener.ListenerCollectorConfig.Measurable;
import com.github.johrstrom.test.TestUtilities;

import io.prometheus.client.Counter;


public class CountTypeUpdaterTest {

	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	private final String[] labelNames = new String[] {"foo_label","label", "code"};
	private final String[] expectedLabels = new String[] {"bar_value", "myLabelz", "909"};
	
	@Test
	public void successCountOnSamplesTest() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(labelNames);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMeasuring(Measurable.SuccessTotal.toString());
		cfg.setMetricName("ct_updater_test_success_only");
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		CountTypeUpdater u = new CountTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(true);	// #1
		res.setResponseCode("909");
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e);
		
		Assert.assertArrayEquals(expectedLabels, labels);
		
		double shouldBeOne = c.labels(expectedLabels).get();
		Assert.assertEquals(1, shouldBeOne, 0.1);
		
		u.update(e);	// #2
		
		res.setSuccessful(false);
		e = new SampleEvent(res,"tg1", vars);
		
		u.update(e);	// could be #3, but shouldn't update
		
		double shouldBeTwo = c.labels(expectedLabels).get();
		
		Assert.assertEquals(2, shouldBeTwo, 0.1);
	}
	
	@Test
	public void failureCountOnSamplesTest() {		
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(labelNames);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("ct_updater_test_failure_only");
		cfg.setMeasuring(Measurable.FailureTotal.toString());
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		CountTypeUpdater u = new CountTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(false); //	#1
		res.setResponseCode("909");
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e);
		
		Assert.assertArrayEquals(expectedLabels, labels);
		
		double shouldBeOne = c.labels(expectedLabels).get();		
		
		Assert.assertEquals(1, shouldBeOne, 0.1);
		
		u.update(e);	// #2
		
		res.setSuccessful(true);
		e = new SampleEvent(res,"tg1", vars);
		
		u.update(e);	// could be #3, but shouldn't update
		
		double shouldBeTwo = c.labels(expectedLabels).get();
		
		Assert.assertEquals(2, shouldBeTwo, 0.1);
	}

	@Test
	public void countSamplesTotalTest() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(this.labelNames);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("count_sample_total_test");
		cfg.setMeasuring(Measurable.CountTotal.toString());
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		CountTypeUpdater u = new CountTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setResponseCode("909");
		
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e); // #1
		
		
		Assert.assertArrayEquals(this.expectedLabels, labels);
		double shouldBeOne = c.labels(this.expectedLabels).get();
		
		Assert.assertEquals(1, shouldBeOne, 0.1);
		
		u.update(e);  // #2
		
		res.setSuccessful(false); 				// should be 3 no matter what, even if last
		e = new SampleEvent(res,"tg1", vars);	// 2 where success
		u.update(e);
		
		double shouldBeThree = c.labels(this.expectedLabels).get();
		Assert.assertEquals(3, shouldBeThree, 0.1);
	}
	
	@Test
	public void testSROnSamples() {
		BaseCollectorConfig base = TestUtilities.simpleSuccessRatioCfg();
		base.setLabels(labelNames);
		String baseName = "ct_updater_test_success_ratio_samples";
		base.setMetricName(baseName);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		SuccessRatioCollector c = (SuccessRatioCollector) reg.getOrCreateAndRegister(cfg);
		CountTypeUpdater u = new CountTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(true);
		res.setResponseCode("909");
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		u.update(e);	// first success
		
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
