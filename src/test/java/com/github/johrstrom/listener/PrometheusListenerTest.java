package com.github.johrstrom.listener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.save.SaveService;
import org.apache.jorphan.collections.HashTree;
import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.JMeterCollectorRegistry;
import com.github.johrstrom.collector.SuccessRatioCollector;
import com.github.johrstrom.test.TestUtilities;
import com.github.johrstrom.test.TestUtilities.ResultAndVariables;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;

public class PrometheusListenerTest {
	
	private static final JMeterCollectorRegistry reg = JMeterCollectorRegistry.getInstance();
	
	static {
		TestUtilities.createJmeterEnv();
	}
	
	@Test
	public void listenerIsSerializable() throws IOException {
		ByteArrayOutputStream objectBuffer = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(objectBuffer);
		
		PrometheusListener listener = new PrometheusListener();
		out.writeObject(listener);
		
		Assert.assertTrue(listener != null);
		Assert.assertTrue(objectBuffer.size() > 0);
		
	}
	
	@Test
	public void canHaveDuplicateMetrics() {
		PrometheusListener first = new PrometheusListener();
		PrometheusListener second = new PrometheusListener();
		
		first.setCollectorConfigs(TestUtilities.simpleListListener());
		second.setCollectorConfigs(TestUtilities.simpleListListener());
		
		first = (PrometheusListener) first.clone();
		second = (PrometheusListener) second.clone();
		
		first.testStarted();
		second.testStarted();
		
		first.testEnded();
		second.testEnded();
		
	}
	
	@Test
	public void canReadJMX() throws IOException {
		File jmx = new File("src/test/resources/simple_prometheus_example.jmx");
		HashTree tree = SaveService.loadTree(jmx);
		
		
		
		Assert.assertTrue(tree != null);
	}
	
	@Test
	public void updateAllTypes() {
		PrometheusListener listener = new PrometheusListener();
		List<ListenerCollectorConfig> configs = TestUtilities.fullListListener();
		
		for(ListenerCollectorConfig cfg : configs) {
			cfg.setLabels(TestUtilities.TEST_LABELS);
		}
		
		listener.setCollectorConfigs(configs);
		listener.testStarted();
		
		long connectTime = 3123;
		long idleTime = 1233;
		long elapsedTime = 2213;
		long latency = 1532;
		int responseSize = 1342;
		int samplesOccured = 0;
		
		ResultAndVariables res = TestUtilities.resultWithLabels();
		res.result.setConnectTime(connectTime);
		res.result.setResponseData(new byte[responseSize]);
		res.result.setLatency(latency);
		res.result.setIdleTime(idleTime);
		res.result.setStampAndTime(System.currentTimeMillis(), elapsedTime);
		res.result.setSuccessful(true);
		SampleEvent event = new SampleEvent(res.result, "tg1", res.vars);
		listener.sampleOccurred(event); // 1st event, successful
		samplesOccured++;
		
		res.result.setSuccessful(false);	// 2nd event, failure
		event = new SampleEvent(res.result, "tg1", res.vars);
		listener.sampleOccurred(event);
		samplesOccured++;
		
		for(ListenerCollectorConfig cfg : configs) {
			String name = cfg.getMetricName();
			switch(name) {
			
			case "test_count_total":
				Counter counter = (Counter) reg.getOrCreateAndRegister(cfg);
				double shouldBeTwo = counter.labels(TestUtilities.EXPECTED_LABELS).get();
				Assert.assertEquals(2.0, shouldBeTwo, 0.1);
				break;
	    	case "test_failure_total":
	    		counter = (Counter) reg.getOrCreateAndRegister(cfg);
				double shouldBeOne = counter.labels(TestUtilities.EXPECTED_LABELS).get();
				Assert.assertEquals(1.0, shouldBeOne, 0.1);
	    		break;
	    	case "test_success_total":
	    		counter = (Counter) reg.getOrCreateAndRegister(cfg);
				shouldBeOne = counter.labels(TestUtilities.EXPECTED_LABELS).get();
				Assert.assertEquals(1.0, shouldBeOne, 0.1);
	    		break;
	    	case "test_ratio":
	    		assertOnRatio(cfg);	    		
	    		break;
	    		
	    	// histograms
	    	case "test_hist_rtime":			
	    		assertOnHistogram(reg.getOrCreateAndRegister(cfg), elapsedTime*samplesOccured, samplesOccured, elapsedTime);
	    		break;    		    
	    	case "test_hist_rsize":
	    		assertOnHistogram(reg.getOrCreateAndRegister(cfg), responseSize*samplesOccured, samplesOccured, responseSize);
	    		break;
	    	case "test_hist_latency":
	    		assertOnHistogram(reg.getOrCreateAndRegister(cfg), latency*samplesOccured, samplesOccured, latency);
	    		break;
	    	case "test_hist_idle_time":
	    		assertOnHistogram(reg.getOrCreateAndRegister(cfg), idleTime*samplesOccured, samplesOccured, idleTime);
	    		break;
	    	case "test_hist_connect_time":
	    		assertOnHistogram(reg.getOrCreateAndRegister(cfg), connectTime*samplesOccured, samplesOccured, connectTime);
	    		break;
	    		
	    	// summaries
	    	case "test_summary_rtime":
	    		assertOnSummary(reg.getOrCreateAndRegister(cfg), elapsedTime*samplesOccured, samplesOccured, elapsedTime);
	    		break;	    
	    	case "test_summary_rsize":
	    		assertOnSummary(reg.getOrCreateAndRegister(cfg), responseSize*samplesOccured, samplesOccured, responseSize);
	    		break;
	    	case "test_summary_latency":
	    		assertOnSummary(reg.getOrCreateAndRegister(cfg), latency*samplesOccured, samplesOccured, latency);
	    		break;
	    	case "test_summary_idle_time":
	    		assertOnSummary(reg.getOrCreateAndRegister(cfg), idleTime*samplesOccured, samplesOccured, idleTime);
	    		break;
	    	case "test_summary_connect_time":
	    		assertOnSummary(reg.getOrCreateAndRegister(cfg), connectTime*samplesOccured, samplesOccured, connectTime);
	    		break;
	    		
			default:
				Assert.assertTrue( name + " triggered untested switch case", false);
				break;
			}
		}
		
		listener.testEnded();
	}
	
	private void assertOnRatio(ListenerCollectorConfig cfg) {
		SuccessRatioCollector ratio = (SuccessRatioCollector) reg.getOrCreateAndRegister(cfg);
		double shouldBeOne = ratio.getSuccess(TestUtilities.EXPECTED_LABELS);
		Assert.assertEquals(1.0, shouldBeOne, 0.1);
		
		shouldBeOne = ratio.getFailure(TestUtilities.EXPECTED_LABELS);
		Assert.assertEquals(1.0, shouldBeOne, 0.1);
		
		double shouldBeTwo = ratio.getTotal(TestUtilities.EXPECTED_LABELS);
		Assert.assertEquals(2.0, shouldBeTwo, 0.1);
	}
	
	  protected static void assertOnHistogram(Collector collector, 
	    		double expectedSum,
	    		double expectedCount,
	    		double boundary
	    		) {
			List<MetricFamilySamples> metrics = collector.collect();
			Assert.assertTrue(metrics.size() == 1);
			MetricFamilySamples family = metrics.get(0);
			
			// labels + Inf + count + sum
			Assert.assertEquals(TestUtilities.EXPECTED_LABELS.length +4, family.samples.size());	
			
			for(Sample sample : family.samples) {
				List<String> values = sample.labelValues;
				List<String> names = sample.labelNames;
				
				//correct labels without 'le' (bin size)
				for(int i = 0; i< TestUtilities.TEST_LABELS.length; i++) {
					Assert.assertEquals(TestUtilities.TEST_LABELS[i], names.get(i));
					Assert.assertEquals(TestUtilities.EXPECTED_LABELS[i], values.get(i));
				}				
				
				int labelSize = TestUtilities.EXPECTED_LABELS.length;
				
				// _sum and _count don't have an 'le' label
				if(sample.name.endsWith("count")) {
					Assert.assertTrue(values.size() == labelSize && names.size() == labelSize);
					Assert.assertEquals(expectedCount, sample.value, 0.1);
					
				} else if(sample.name.endsWith("sum")) {
					Assert.assertTrue(values.size() == labelSize && names.size() == labelSize);
					Assert.assertEquals(expectedSum, sample.value, 0.1);
					
				}else {
					Assert.assertEquals(values.size(), labelSize+1);
					Assert.assertEquals(names.size(), labelSize+1);
					
					String leString = values.get(labelSize);
					
					double le = (!leString.isEmpty() && !leString.equals("+Inf")) ? Double.parseDouble(leString) : Double.MAX_VALUE;
					
					if(le == Double.MAX_VALUE) {
						Assert.assertEquals(expectedCount, sample.value, 0.1);
					} else if(le < boundary) {
						Assert.assertEquals(0, sample.value, 0.1);
					}else if(le > boundary) {
						Assert.assertEquals(expectedCount, sample.value, 0.1);
					}
				}
			}
	    }
	  
	  
		protected void assertOnSummary(
				Collector collector, 
				double expectedSum,
				double expectedCount, 
				double expectedValue) {	
			
			List<MetricFamilySamples> metrics = collector.collect();
			Assert.assertTrue(metrics.size() == 1);
			MetricFamilySamples family = metrics.get(0);
			Assert.assertTrue(family.samples.size() == 5); 	// 3 quantiles + count + sum
			
			
			for(Sample sample : family.samples) {
				List<String> values = sample.labelValues;
				List<String> names = sample.labelNames;
				
				for(int i = 0; i< TestUtilities.TEST_LABELS.length; i++) {
					Assert.assertEquals(TestUtilities.TEST_LABELS[i], names.get(i));
					Assert.assertEquals(TestUtilities.EXPECTED_LABELS[i], values.get(i));
				}	
				
				// _sum and _count don't have an 'le' label
				if(sample.name.endsWith("count")) {
					Assert.assertEquals(values.size(), TestUtilities.EXPECTED_LABELS.length);
					Assert.assertEquals(names.size(), TestUtilities.TEST_LABELS.length);
					
					Assert.assertEquals(expectedCount, sample.value, 0.1);
					
				} else if(sample.name.endsWith("sum")){
					Assert.assertEquals(expectedSum, sample.value, 0.1);
				}else {
					Assert.assertEquals(values.size(), TestUtilities.EXPECTED_LABELS.length+1);
					Assert.assertEquals(values.size(), TestUtilities.EXPECTED_LABELS.length+1);
					
					Assert.assertEquals(expectedValue, sample.value, 0.1);
				}
			}
				
		}
}
