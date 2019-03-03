package com.github.johrstrom.listener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PrometheusServerTest {
	
	@Test
	public void ensureCleanStartStop() throws Exception {
		PrometheusServer server = PrometheusServer.getInstance();
		Assert.assertTrue(server != null);
		
		server.start();
		Thread.currentThread();
		Thread.sleep(1000);
		server.stop();
		
		server.start();
		Thread.currentThread();
		Thread.sleep(1000);
		server.stop();
	}
	

//	@Test
//	public void ensurePropertiesLoaded() throws Exception {
//		PrometheusServer server = PrometheusServer.getInstance();
//		server.start();
//		
//		ThreadPool tp = server.getThreadPool();
//		
//		Assert.assertTrue("expected 5, got " + tp.getThreads(), tp.getThreads() == 10);
//		Assert.assertTrue("expected 5, got " + tp.getIdleThreads(), tp.getIdleThreads() >= 5);
//		
//		server.stop();
//	}

}
