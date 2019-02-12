package com.github.johrstrom.listener.updater;

import java.util.List;

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

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;

public class SizeUpdaterTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	@Test
	public void testHistogram() throws Exception {		
		BaseCollectorConfig base = TestUtilities.simpleHistogramCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("size_updater_test_hist");

		Histogram collector = (Histogram) reg.getOrCreateAndRegister(cfg);
		ResponseSizeUpdater u = new ResponseSizeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		res.setStampAndTime(System.currentTimeMillis(), 10000);
		int responseSize = 650;
		res.setResponseData(new byte[responseSize]);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		u.update(e);
		
		
		List<MetricFamilySamples> metrics = collector.collect();
		Assert.assertTrue(metrics.size() == 1);
		MetricFamilySamples family = metrics.get(0);
		Assert.assertTrue(family.samples.size() == 7); 	// 4 buckets + Inf + count + sum
		
		
		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			//correct labels without 'le' (bin size)
			boolean correctLabels = names.get(0).equals("foo_label") && 
					names.get(1).equals("label") &&
					values.get(0).equals("bar_value") && 
					values.get(1).equals("myLabelz");
			
			Assert.assertTrue(correctLabels);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum")) {
				Assert.assertTrue(values.size() == 2 && names.size() == 2);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				}else {
					Assert.assertEquals(responseSize, sample.value, 0.1);
				}
				
			}else {
				Assert.assertTrue(values.size() == 3 && names.size() == 3);
				
				String leString = values.get(2);
				
				double le = (!leString.isEmpty() && !leString.equals("+Inf")) ? Double.parseDouble(leString) : Double.MAX_VALUE;
				
				if(le == Double.MAX_VALUE) {
					Assert.assertEquals(1, sample.value, 0.1);
				} else if(le < responseSize) {
					Assert.assertEquals(0, sample.value, 0.1);
				}else if(le > responseSize) {
					Assert.assertEquals(1, sample.value, 0.1);
				}
				
			}		
			
		}
		
	}
	
	@Test
	public void testSummary() throws Exception {	
		BaseCollectorConfig base = TestUtilities.simpleSummaryCfg();
		base.setLabels(new String[] {"foo_label","label"});
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("size_updater_test_summ");

		Summary collector = (Summary) reg.getOrCreateAndRegister(cfg);
		ResponseSizeUpdater u = new ResponseSizeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel("myLabelz");
		int responseSize = 650;
		res.setStampAndTime(System.currentTimeMillis(), 1000);
		res.setResponseData(new byte[responseSize]);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", "bar_value");
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] labels = u.labelValues(e);
		Assert.assertTrue(labels.length == 2);
		Assert.assertArrayEquals(new String[] {"bar_value", "myLabelz"}, labels);
		
		u.update(e);
		
		List<MetricFamilySamples> metrics = collector.collect();
		Assert.assertTrue(metrics.size() == 1);
		MetricFamilySamples family = metrics.get(0);
		Assert.assertTrue(family.samples.size() == 5); 	// 3 quantiles + count + sum
		
		
		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			//correct labels without 'le' (bin size)
			boolean correctLabels = names.get(0).equals("foo_label") && 
					names.get(1).equals("label") &&
					values.get(0).equals("bar_value") && 
					values.get(1).equals("myLabelz");
			
			Assert.assertTrue(correctLabels);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum")) {
				Assert.assertTrue(values.size() == 2 && names.size() == 2);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				}else {
					Assert.assertEquals(responseSize, sample.value, 0.1);
				}
				
			}else {
				Assert.assertTrue(values.size() == 3 && names.size() == 3);
				
				//double quantile =  Double.parseDouble(values.get(2));
				
				
				Assert.assertEquals(responseSize, sample.value, 0.1);
				
		
			}
		}
	}
	
}
