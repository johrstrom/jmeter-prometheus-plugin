package com.github.johrstrom.config;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.johrstrom.config.CollectorConfig.QuantileDefinition;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.Type;


public class PrometheusMetricConfigTest {

	PrometheusMetricsConfig cfg;
	
	@Before
	public void setup() {
		cfg = new PrometheusMetricsConfig();
	}
	
	@After
	public void tearDown() {
		cfg = null;
	}
	
	@Test
	public void emptyLabelsOK() {
		CollectorConfig def = simpleCounterDef();
		
		// first try with a brand new String array 
		def.setLabels(new String[]{});  
		Collector collector = cfg.fromDefinition(def);
		Assert.assertTrue(collector != null);
		
		
		// Now try with a String array with an empty string in it
		def.setLabels(new String[]{""});
		collector = cfg.fromDefinition(def);
		Assert.assertTrue(collector != null);
		
		// Now just for kicks, try with several empty strings in it
		def.setLabels(new String[]{"a", "", "b", ""});
		collector = cfg.fromDefinition(def);
		Assert.assertTrue(collector != null);
		
	}
	
	@Test
	public void parseSingleQuantilesCorrectly() {
		CollectorConfig def = this.simpleSummaryDef();
		def.setQuantileOrBucket("0.95,0.1");
		
		
		QuantileDefinition[] quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 1);
		Assert.assertEquals(0.95, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
	}
	
	@Test
	public void parseMultipleQuantilesCorrectly() {
		CollectorConfig def = this.simpleSummaryDef();
		def.setQuantileOrBucket("0.95,0.1|0.99,0.1|0.999,0.1");
		
		
		QuantileDefinition[] quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertEquals(0.95, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		Assert.assertEquals(0.99, quantiles[1].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[1].error,0.001);
		Assert.assertEquals(0.999, quantiles[2].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[2].error,0.001);
	}
	
	@Test
	public void parseQauntileFailsGivesDefaults() {
		CollectorConfig def = this.simpleSummaryDef();
		def.setQuantileOrBucket("skdnfsdfunsf");
		
		QuantileDefinition[] quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertArrayEquals(CollectorConfig.DEFAULT_QUANTILES, quantiles);
		

	}
	
	
	private CollectorConfig simpleCounterDef() {
		CollectorConfig def = new CollectorConfig();
		def.setMetricName("simple_counter");
		def.setType(Type.COUNTER.name());
		def.setHelp("some helpe message");
		
		return def;
	}
	
	private CollectorConfig simpleSummaryDef() {
		CollectorConfig def = new CollectorConfig();
		def.setMetricName("simple_summary");
		def.setType(Type.SUMMARY.name());
		def.setHelp("some helpe message");
		
		return def;
	}
	
	private CollectorConfig simpleHistogramDef() {
		CollectorConfig def = new CollectorConfig();
		def.setMetricName("simple_histogram");
		def.setType(Type.HISTOGRAM.name());
		def.setHelp("some helpe message");
		
		return def;
	}
	
	private CollectorConfig simpleGaugeDef() {
		CollectorConfig def = new CollectorConfig();
		def.setMetricName("simple_gauge");
		def.setType(Type.GAUGE.name());
		def.setHelp("some helpe message");
		
		return def;
	}

}
