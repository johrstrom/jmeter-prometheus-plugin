package com.github.johrstrom.listener;

import org.apache.jmeter.util.JMeterUtils;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PrometheusServerTest {
	
	static {
		JMeterUtils.loadJMeterProperties("src/test/resources/user.properties");
	}
	
	@Before
	public void setup() {
		//
	}
	
	@Test
	public void ensureCleanStartStop() throws Exception {
		PrometheusServer server = PrometheusServer.getInstance();
		server.start();
		Assert.assertTrue(server.isRunning());
		
		server.stop();
		Assert.assertTrue(server.isStopped());
	}
	

	@Test
	public void ensurePropertiesLoaded() throws Exception {
		PrometheusServer server = PrometheusServer.getInstance();
		server.start();
		
		ThreadPool tp = server.getThreadPool();
		
		Assert.assertTrue("expected 5, got " + tp.getThreads(), tp.getThreads() == 10);
		Assert.assertTrue("expected 5, got " + tp.getIdleThreads(), tp.getIdleThreads() >= 5);
		
		server.stop();
	}

}
