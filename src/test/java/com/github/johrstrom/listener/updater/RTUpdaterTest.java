package com.github.johrstrom.listener.updater;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.test.TestUtilities;

import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

/**
 * ResponseTimeUpdater test class.
 * 
 * @author Jeff Ohrstrom
 *
 */
public class RTUpdaterTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();

	@Test
	public void testHistogram() throws Exception {
		JMeterUtils.loadJMeterProperties("src/test/resources/user.properties");
		
		BaseCollectorConfig base = TestUtilities.simpleHistogramCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("rt_updater_test_hist");

		Histogram collector = (Histogram) reg.getOrCreateAndRegister(cfg);
		ResponseTimeUpdater u = new ResponseTimeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		u.update(e);
		io.prometheus.client.Histogram.Child.Value exists = collector.labels("bar_value","myLabelz").get();
		Assert.assertTrue(exists.sum > 1000);
		
		io.prometheus.client.Histogram.Child.Value nonExistant = collector.labels("asdfsfd","asdgfsdgs").get();
		Assert.assertEquals(0, nonExistant.sum, 0.1);
		
		
	}

	
	@Test
	public void testSummary() throws Exception {
		JMeterUtils.loadJMeterProperties("src/test/resources/user.properties");
		
		
		BaseCollectorConfig base = TestUtilities.simpleSummaryCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("rt_updater_test_summ");

		Summary collector = (Summary) reg.getOrCreateAndRegister(cfg);
		ResponseTimeUpdater u = new ResponseTimeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		u.update(e);
		io.prometheus.client.Summary.Child.Value exists = collector.labels("bar_value","myLabelz").get();
		Assert.assertTrue(exists.sum > 1000);
		
		io.prometheus.client.Summary.Child.Value nonExistant = collector.labels("asdfsfd","asdgfsdgs").get();
		Assert.assertEquals(0, nonExistant.sum, 0.1);
		
		
	}

}
