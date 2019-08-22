package com.github.johrstrom.listener;

import org.junit.Assert;
import org.junit.Test;


public class PrometheusServerTest {
	
	@Test
	public void ensureCleanStartStop() throws Exception {
		PrometheusServer server = PrometheusServer.getInstance();
		Assert.assertNotNull(server);
		
		server.start();
		Thread.currentThread();
		Thread.sleep(1000);
		server.stop();
		
		server.start();
		Thread.currentThread();
		Thread.sleep(1000);
		server.stop();
	}

}
