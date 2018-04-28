package com.github.johrstrom.collector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.johrstrom.collector.CollectorConfig;
import com.github.johrstrom.collector.CollectorConfig.QuantileDefinition;
import com.github.johrstrom.config.PrometheusMetricsConfig;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.Type;


public class CollectorConfigTest {
	
	@Test
	public void emptyLabelsOK() {
		CollectorConfig def = simpleCounterDef();
		
		// first try with a brand new String array 
		def.setLabels(new String[]{});  
		Collector collector = CollectorConfig.fromDefinition(def);
		Assert.assertTrue(collector != null);
		
		
		// Now try with a String array with an empty string in it
		def.setLabels(new String[]{""});
		collector = CollectorConfig.fromDefinition(def);
		Assert.assertTrue(collector != null);
		
		// Now just for kicks, try with several empty strings in it
		def.setLabels(new String[]{"a", "", "b", ""});
		collector = CollectorConfig.fromDefinition(def);
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
	public void parseQauntileFailsAndGivesDefaults() {
		CollectorConfig def = this.simpleSummaryDef();
		def.setQuantileOrBucket("skdn fsdfu|,nsf");
		
		QuantileDefinition[] quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertArrayEquals(CollectorConfig.DEFAULT_QUANTILES, quantiles);
		
		def.setQuantileOrBucket(";sdfg|other_str	ing|asl dfuy");
		quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertArrayEquals(CollectorConfig.DEFAULT_QUANTILES, quantiles);
	}
	
	@Test
	public void parseReturnsPartialForQuantiles() {
		CollectorConfig def = this.simpleSummaryDef();
		def.setQuantileOrBucket("skdnfs dfuns	f|0.5|0.75,0.1"); //only 1 good at the end
		
		QuantileDefinition[] quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 1);
		Assert.assertEquals(0.75, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		
		def.setQuantileOrBucket("skdnfsdfunsf|0.5,0.1|0.75,0.1|0.99"); //2 in middle are good
		quantiles = def.getQuantiles();
		Assert.assertTrue(quantiles.length == 2);
		Assert.assertEquals(0.5, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		Assert.assertEquals(0.75, quantiles[1].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[1].error,0.001);
		
	}
	
	@Test
	public void parseBucketsCorrectly() {
		CollectorConfig def = this.simpleHistogramDef();
		def.setQuantileOrBucket("100,500,1000,2500,5000");
		double[] expected = new double[] {100, 500, 1000, 2500, 5000};
		
		double[] buckets = def.getBuckets();
		Assert.assertTrue(buckets.length == 5);
		Assert.assertArrayEquals(expected, buckets,0.01);
	}
	
	@Test
	public void parseBucketFailureReturnsDefaults() {
		CollectorConfig def = this.simpleHistogramDef();
		def.setQuantileOrBucket("akldjand| sfpoa	sdnf");
		
		double[] buckets = def.getBuckets();
		Assert.assertTrue(buckets.length == 4);
		Assert.assertArrayEquals(CollectorConfig.DEFAULT_BUCKET_SIZES, buckets,0.01);
		
		def.setQuantileOrBucket("fail,otherFi al,123cantPa	rse,not123 ThisEither,a3");
		buckets = def.getBuckets();
		Assert.assertTrue(buckets.length == 4);
		Assert.assertArrayEquals(CollectorConfig.DEFAULT_BUCKET_SIZES, buckets,0.01);
		
	}
	
	@Test
	public void parseBucketsWithPartialSuccess() {
		CollectorConfig def = this.simpleHistogramDef();
		def.setQuantileOrBucket("akldjand,17,lakj	asdf,123no,48");
		
		double[] buckets = def.getBuckets();
		Assert.assertTrue(buckets.length == 2);
		Assert.assertEquals(17, buckets[0], 0.001);
		Assert.assertEquals(48, buckets[1], 0.001);
		
		
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
