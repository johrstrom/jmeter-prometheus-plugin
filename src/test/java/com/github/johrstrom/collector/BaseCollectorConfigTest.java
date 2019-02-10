package com.github.johrstrom.collector;

import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.BaseCollectorConfig;
import com.github.johrstrom.collector.BaseCollectorConfig.QuantileDefinition;
import com.github.johrstrom.test.TestUtilities;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.Type;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;


public class BaseCollectorConfigTest {
	
	@Test
	public void emptyLabelsOK() {
		BaseCollectorConfig cfg = TestUtilities.simpleCounterCfg();
		
		// first try with a brand new String array 
		cfg.setLabels(new String[]{});  
		Collector collector = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(collector != null);
		
		
		// Now try with a String array with an empty string in it
		cfg.setLabels(new String[]{""});
		collector = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(collector != null);
		
		// Now just for kicks, try with several empty strings in it
		cfg.setLabels(new String[]{"a", "", "b", ""});
		collector = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(collector != null);
		
	}
	
	@Test
	public void parseSingleQuantilesCorrectly() {
		BaseCollectorConfig cfg = TestUtilities.simpleSummaryCfg();
		cfg.setQuantileOrBucket("0.95,0.1");
		
		
		QuantileDefinition[] quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 1);
		Assert.assertEquals(0.95, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
	}
	
	@Test
	public void parseMultipleQuantilesCorrectly() {
		BaseCollectorConfig cfg = TestUtilities.simpleSummaryCfg();
		cfg.setQuantileOrBucket("0.95,0.1|0.99,0.1|0.999,0.1");
		
		
		QuantileDefinition[] quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertEquals(0.95, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		Assert.assertEquals(0.99, quantiles[1].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[1].error,0.001);
		Assert.assertEquals(0.999, quantiles[2].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[2].error,0.001);
	}
	
	@Test
	public void parseQauntileFailsAndGivesDEFAULTs() {
		BaseCollectorConfig cfg = TestUtilities.simpleSummaryCfg();
		cfg.setQuantileOrBucket("skdn fsdfu|,nsf");
		
		QuantileDefinition[] quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertArrayEquals(BaseCollectorConfig.DEFAULT_QUANTILES, quantiles);
		
		cfg.setQuantileOrBucket(";sdfg|other_str	ing|asl dfuy");
		quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 3);
		Assert.assertArrayEquals(BaseCollectorConfig.DEFAULT_QUANTILES, quantiles);
	}
	
	@Test
	public void parseReturnsPartialForQuantiles() {
		BaseCollectorConfig cfg = TestUtilities.simpleSummaryCfg();
		cfg.setQuantileOrBucket("skdnfs dfuns	f|0.5|0.75,0.1"); //only 1 good at the end
		
		QuantileDefinition[] quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 1);
		Assert.assertEquals(0.75, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		
		cfg.setQuantileOrBucket("skdnfsdfunsf|0.5,0.1|0.75,0.1|0.99"); //2 in middle are good
		quantiles = cfg.getQuantiles();
		Assert.assertTrue(quantiles.length == 2);
		Assert.assertEquals(0.5, quantiles[0].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[0].error,0.001);
		Assert.assertEquals(0.75, quantiles[1].quantile,0.001);
		Assert.assertEquals(0.1, quantiles[1].error,0.001);
		
	}
	
	@Test
	public void parseBucketsCorrectly() {
		BaseCollectorConfig cfg = TestUtilities.simpleHistogramCfg();
		cfg.setQuantileOrBucket("100,500,1000,2500,5000");
		double[] expected = new double[] {100, 500, 1000, 2500, 5000};
		
		double[] buckets = cfg.getBuckets();
		Assert.assertTrue(buckets.length == 5);
		Assert.assertArrayEquals(expected, buckets,0.01);
	}
	
	@Test
	public void parseBucketFailureReturnsDefaults() {
		BaseCollectorConfig cfg = TestUtilities.simpleHistogramCfg();
		cfg.setQuantileOrBucket("akldjand| sfpoa	sdnf");
		
		double[] buckets = cfg.getBuckets();
		Assert.assertTrue(buckets.length == 4);
		Assert.assertArrayEquals(BaseCollectorConfig.DEFAULT_BUCKET_SIZES, buckets,0.01);
		
		cfg.setQuantileOrBucket("fail,otherFi al,123cantPa	rse,not123 ThisEither,a3");
		buckets = cfg.getBuckets();
		Assert.assertTrue(buckets.length == 4);
		Assert.assertArrayEquals(BaseCollectorConfig.DEFAULT_BUCKET_SIZES, buckets,0.01);
		
	}
	
	@Test
	public void parseBucketsWithPartialSuccess() {
		BaseCollectorConfig cfg = TestUtilities.simpleHistogramCfg();
		cfg.setQuantileOrBucket("akldjand,17,lakj	asdf,123no,48");
		
		double[] buckets = cfg.getBuckets();
		Assert.assertTrue(buckets.length == 2);
		Assert.assertEquals(17, buckets[0], 0.001);
		Assert.assertEquals(48, buckets[1], 0.001);
	}
	
	
	@Test
	public void initCorrectly() {
		BaseCollectorConfig init = new BaseCollectorConfig();
		
		Assert.assertEquals(init.getHelp(), BaseCollectorConfig.DEFAULT_HELP_STRING);
		Assert.assertArrayEquals(new String[0], init.getLabels());
		Assert.assertTrue(init.getLabelsAsString().isEmpty());
		Assert.assertEquals(init.getType(), Type.COUNTER.name());
		
		Assert.assertTrue(
			init.getMetricName() + " does not match the expected pattern.",
			Pattern.matches(BaseCollectorConfig.METRIC_NAME_BASE + "\\p{Alnum}{8}", 
			init.getMetricName()));
		
	}


	@Test
	public void createCorrectType() {
		BaseCollectorConfig cfg = TestUtilities.simpleCounterCfg();
		Collector c = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(c instanceof Counter);
		
		cfg = TestUtilities.simpleGaugeCfg();
		c = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(c instanceof Gauge);
		
		cfg = TestUtilities.simpleHistogramCfg();
		c = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(c instanceof Histogram);
		
		cfg = TestUtilities.simpleSummaryCfg();
		c = BaseCollectorConfig.fromConfig(cfg);
		Assert.assertTrue(c instanceof Summary);
	}
	
	@Test
	public void setOfElementsTest() {
		BaseCollectorConfig left = TestUtilities.simpleCounterCfg();
		BaseCollectorConfig right = TestUtilities.simpleCounterCfg();
		
		Assert.assertTrue(left != right);
		Assert.assertTrue(left.equals(right));
		
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();
		
		Assert.assertTrue(leftHash == rightHash);
	}

}
