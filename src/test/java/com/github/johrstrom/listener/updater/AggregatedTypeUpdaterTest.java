package com.github.johrstrom.listener.updater;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.listener.ListenerCollectorConfig;
import com.github.johrstrom.listener.ListenerCollectorConfig.Measurable;
import com.github.johrstrom.test.TestUtilities;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AggregatedTypeUpdaterTest {

	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	private static final String[] labels = new String[] {"foo_label","label","code"};
	private static final String name = "super_cool_sampler";
	private static final String code = "super_cool_sampler";
	private static final String var_value = "bar_value";
	private static final String[] expectedLabels = new String[] {var_value,name,code};

	@Test
	public void testHistogramResponseTime() {
		BaseCollectorConfig base = TestUtilities.simpleHistogramCfg();
		base.setLabels(labels);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMeasuring(Measurable.ResponseTime.toString());
		cfg.setMetricName("ct_updater_test_hist_rt");

		Histogram collector = (Histogram) reg.getOrCreateAndRegister(cfg);
		AggregatedTypeUpdater u = new AggregatedTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel(name);
		int responseTime = 650;
		res.setStampAndTime(System.currentTimeMillis(), 650);
		res.setResponseCode(code);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", var_value);
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		Assert.assertArrayEquals(expectedLabels, actualLabels);
		
		u.update(e);

		List<MetricFamilySamples> metrics = collector.collect();
		assertEquals(1, metrics.size());
		MetricFamilySamples family = metrics.get(0);
		assertEquals(8, family.samples.size());     // 4 buckets + Inf + count + sum


		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			//correct labels without 'le' (bin size)
			boolean correctLabels = names.get(0).equals(labels[0]) &&
					names.get(1).equals(labels[1]) &&
					names.get(2).equals(labels[2]) &&
					values.get(0).equals(expectedLabels[0]) && 
					values.get(1).equals(expectedLabels[1]) &&
					values.get(2).equals(expectedLabels[2]);
			
			assertTrue(correctLabels);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum") || sample.name.endsWith("created")) {
				assertTrue(values.size() == 3 && names.size() == 3);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				} else if (sample.name.endsWith("created")) {
					Assert.assertEquals(System.currentTimeMillis() / 1000.0, sample.value, 0.1);
				} else {
					Assert.assertEquals(responseTime, sample.value, 0.1);
				}
				
			}else {
				assertTrue(values.size() == 4 && names.size() == 4);
				
				String leString = values.get(3);
				
				double le = (!leString.isEmpty() && !leString.equals("+Inf")) ? Double.parseDouble(leString) : Double.MAX_VALUE;
				
				if(le == Double.MAX_VALUE) {
					Assert.assertEquals(1, sample.value, 0.1);
				} else if(le < responseTime) {
					Assert.assertEquals(0, sample.value, 0.1);
				}else if(le > responseTime) {
					Assert.assertEquals(1, sample.value, 0.1);
				}
				
			}
		}
		
		
	}

	
	@Test
	public void testSummaryResponseTime() {
		BaseCollectorConfig base = TestUtilities.simpleSummaryCfg();
		base.setLabels(labels);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMeasuring(Measurable.ResponseTime.toString());
		cfg.setMetricName("ct_updater_test_summary_rt");

		Summary collector = (Summary) reg.getOrCreateAndRegister(cfg);
		AggregatedTypeUpdater u = new AggregatedTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel(name);
		int responseTime = 650;
		res.setStampAndTime(System.currentTimeMillis(), 650);
		res.setResponseCode(code);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", var_value);
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		
		Assert.assertArrayEquals(expectedLabels, actualLabels);
		
		u.update(e);
		
		List<MetricFamilySamples> metrics = collector.collect();
		assertEquals(1, metrics.size());
		MetricFamilySamples family = metrics.get(0);
		assertEquals(6, family.samples.size());     // 3 quantiles + count + sum

		
		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			//correct labels without quantile 
			boolean correctLabels = names.get(0).equals(labels[0]) && 
					names.get(1).equals(labels[1]) &&
					names.get(2).equals(labels[2]) &&
					values.get(0).equals(expectedLabels[0]) && 
					values.get(1).equals(expectedLabels[1]) &&
					values.get(2).equals(expectedLabels[2]);
			
			assertTrue(correctLabels);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum") || sample.name.endsWith("created")) {
				assertTrue(values.size() == 3 && names.size() == 3);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				}else if (sample.name.endsWith("created")) {
					Assert.assertEquals(System.currentTimeMillis() / 1000.0, sample.value, 0.1);
				} else {
					Assert.assertEquals(responseTime, sample.value, 0.1);
				}
				
			}else {
				assertTrue(values.size() == 4 && names.size() == 4);
				Assert.assertEquals(responseTime, sample.value, 0.1);
			}
		}
			
	}

	
	@Test
	public void testHistogramResponseSize() {
		BaseCollectorConfig base = TestUtilities.simpleHistogramCfg();
		base.setLabels(labels);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("ct_updater_test_histogram_rsize");
		cfg.setMeasuring(Measurable.ResponseSize.toString());

		Histogram collector = (Histogram) reg.getOrCreateAndRegister(cfg);
		AggregatedTypeUpdater u = new AggregatedTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel(name);
		int responseSize = 650;
		res.setResponseData(new byte[responseSize]);
		res.setResponseCode(code);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", var_value);
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		Assert.assertArrayEquals(expectedLabels, actualLabels);
		
		
		u.update(e);
		
		
		List<MetricFamilySamples> metrics = collector.collect();
		Assert.assertEquals(1, metrics.size());
		MetricFamilySamples family = metrics.get(0);
		Assert.assertEquals(8, family.samples.size());     // 4 buckets + Inf + count + sum


		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			this.correctLabels(names, values);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum") || sample.name.endsWith("created")) {
				assertTrue(values.size() == 3 && names.size() == 3);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				} else if (sample.name.endsWith("created")) {
					Assert.assertEquals(System.currentTimeMillis() / 1000.0, sample.value, 0.1);
				} else {
					Assert.assertEquals(responseSize, sample.value, 0.1);
				}
				
			}else {
				assertTrue(values.size() == 4 && names.size() == 4);
				
				String leString = values.get(3);
				
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
	public void testSummaryResponseSize() {
		BaseCollectorConfig base = TestUtilities.simpleSummaryCfg();
		base.setLabels(labels);
		ListenerCollectorConfig cfg = new ListenerCollectorConfig(base);
		cfg.setMetricName("ct_updater_test_summary_rsize");
		cfg.setMeasuring(Measurable.ResponseSize.toString());

		Summary collector = (Summary) reg.getOrCreateAndRegister(cfg);
		AggregatedTypeUpdater u = new AggregatedTypeUpdater(cfg);
		
		SampleResult res = new SampleResult();
		res.setSampleLabel(name);
		int responseSize = 650;
		res.setResponseData(new byte[responseSize]);
		res.setResponseCode(code);
		
		JMeterVariables vars = new JMeterVariables();
		vars.put("foo_label", var_value);
		JMeterContextService.getContext().setVariables(vars);
		SampleEvent e = new SampleEvent(res,"tg1", vars);
		
		
		String[] actualLabels = u.labelValues(e);
		Assert.assertArrayEquals(expectedLabels, actualLabels);
		
		u.update(e);
		
		List<MetricFamilySamples> metrics = collector.collect();
		Assert.assertEquals(1, metrics.size());
		MetricFamilySamples family = metrics.get(0);
		Assert.assertEquals(6, family.samples.size());     // 3 quantiles + count + sum


		for(Sample sample : family.samples) {
			List<String> values = sample.labelValues;
			List<String> names = sample.labelNames;
			
			this.correctLabels(names, values);
			
			// _sum and _count don't have an 'le' label
			if(sample.name.endsWith("count") || sample.name.endsWith("sum") || sample.name.endsWith("created")) {
				assertTrue(values.size() == 3 && names.size() == 3);
				
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(1, sample.value, 0.1);
				} else if (sample.name.endsWith("created")) {
					Assert.assertEquals(System.currentTimeMillis() / 1000.0, sample.value, 0.1);
				} else {
					Assert.assertEquals(responseSize, sample.value, 0.1);
				}
				
			}else {
				assertTrue(values.size() == 4 && names.size() == 4);
				Assert.assertEquals(responseSize, sample.value, 0.1);
			}
		}
	}
	
	private void correctLabels(List<String> names, List<String> values) {
		boolean correctLabels = names.get(0).equals(labels[0]) && 
				names.get(1).equals(labels[1]) &&
				names.get(2).equals(labels[2]) &&
				values.get(0).equals(expectedLabels[0]) && 
				values.get(1).equals(expectedLabels[1]) &&
				values.get(2).equals(expectedLabels[2]);
		
		assertTrue(correctLabels);
	}
}
