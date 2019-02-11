package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.test.TestUtilities;

import io.prometheus.client.Counter;

public class CounterTests {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	@Test
	public void countTotalTest() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("count_test_total");
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		CountTotalUpdater u = new CountTotalUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e);
		
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		double shouldBeOne = c.labels("bar_value","myLabelz").get();
		double shouldBeZero = c.labels("asdfsfd","asdgfsdgs").get();
		
		
		Assert.assertEquals(1, shouldBeOne, 0.1);
		Assert.assertEquals(0, shouldBeZero, 0.1);
		
		u.update(e);
		u.update(e);
		
		double shouldBeThree = c.labels("bar_value","myLabelz").get();
		double stillZero = c.labels("asdfsfd","asdgfsdgs").get();
		
		Assert.assertEquals(3, shouldBeThree, 0.1);
		Assert.assertEquals(0, stillZero, 0.1);
	}
	
	@Test
	public void failureCountTotalTest() {		
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("count_test_failure_total");
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		FailureTotalUpdater u = new FailureTotalUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(false); //	#1
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e);
		
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		double shouldBeOne = c.labels("bar_value","myLabelz").get();		
		
		Assert.assertEquals(1, shouldBeOne, 0.1);
		
		u.update(e);	// #2
		
		res.setSuccessful(true);
		e = new SampleEvent(res,"tg1", vars);
		
		u.update(e);	// could be #3, but shouldn't update
		
		double shouldBeTwo = c.labels("bar_value","myLabelz").get();
		
		Assert.assertEquals(2, shouldBeTwo, 0.1);
	}

	@Test
	public void successTotalCount() {		
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("count_test_success_total");
		
		Counter c = (Counter) reg.getOrCreateAndRegister(cfg);
		SuccessTotalUpdater u = new SuccessTotalUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		res.setSuccessful(true);	// #1
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		u.update(e);
		
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		double shouldBeOne = c.labels("bar_value","myLabelz").get();
		double shouldBeZero = c.labels("asdfsfd","asdgfsdgs").get();
		
		
		Assert.assertEquals(1, shouldBeOne, 0.1);
		Assert.assertEquals(0, shouldBeZero, 0.1);
		
		u.update(e);	// #2
		
		res.setSuccessful(false);
		e = new SampleEvent(res,"tg1", vars);
		
		u.update(e);	// could be #3, but shouldn't update
		
		double shouldBeTwo = c.labels("bar_value","myLabelz").get();
		
		Assert.assertEquals(2, shouldBeTwo, 0.1);
	}
}
