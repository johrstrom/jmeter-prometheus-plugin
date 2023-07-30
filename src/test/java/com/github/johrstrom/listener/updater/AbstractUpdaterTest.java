package com.github.johrstrom.listener.updater;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.test.TestUtilities;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Test;

public class AbstractUpdaterTest {

	public static class TestUpdater extends  AbstractUpdater {
		

		public TestUpdater(ListenerCollectorConfig cfg) {
			super(cfg);
		}

		@Override
		public void update(SampleEvent e) {
			// do nothing
		}
	}

	
	@Test
	public void testKeywords() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"label","code", "thread_group"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		TestUpdater u = new TestUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("test_label");
		res.setResponseCode("204");
		SampleEvent event = new SampleEvent(res,"test_tg", new JMeterVariables());
		
		String[] labels = u.labelValues(event);
		

		Assert.assertTrue(labels.length == 3);
		Assert.assertArrayEquals(new String[] {"test_label", "204", "test_tg"}, labels);
	}
	
	@Test
	public void testVariables() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"foo", "bar"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		TestUpdater u = new TestUpdater(cfg);

		JMeterVariables vars = new JMeterVariables();
		vars.put("foo", "funny");
		vars.put("bar", "banal");		
		JMeterContextService.getContext().setVariables(vars);
		
		SampleEvent event = new SampleEvent(new SampleResult(),"tg1", vars);
		
		
		String[] labels = u.labelValues(event);
		

		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"funny", "banal"}, labels);
	}
	
	
	@Test
	public void testCombo() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"foo", "code", "bar", "label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		TestUpdater u = new TestUpdater(cfg);

		JMeterVariables vars = new JMeterVariables();
		vars.put("foo", "funnier");
		vars.put("bar", "more banal");		
		JMeterContextService.getContext().setVariables(vars);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("one after the");
		res.setResponseCode("909");
		SampleEvent event = new SampleEvent(res,"tg1", vars);
		
		String[] labels = u.labelValues(event);

		Assert.assertTrue(labels.length == 4);
		Assert.assertArrayEquals(new String[] {"funnier", "909", "more banal", "one after the"}, labels);
	}
	
	@Test
	public void testNulls() {
		BaseCollectorConfig base = TestUtilities.simpleCounterCfg();
		base.setLabels(new String[] {"be_null_one", "be_null_two", "code"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		
		TestUpdater u = new TestUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setResponseCode("304");
		SampleEvent event = new SampleEvent(res ,"tg1", new JMeterVariables());
		
		String[] labels = u.labelValues(event);
		
		Assert.assertTrue(labels.length == 3);
		Assert.assertArrayEquals(new String[] {"null", "null", "304"}, labels);
	}

}
