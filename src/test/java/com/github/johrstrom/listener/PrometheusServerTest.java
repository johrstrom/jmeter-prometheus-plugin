package com.github.johrstrom.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

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

		pingAPI();  // also be sure that you can hit the api
		server.stop();

		server.start();
		Thread.currentThread();
		Thread.sleep(1000);

		pingAPI();	// you can still hit it after stopping
		server.stop();
	}

	private String pingAPI() throws IOException {
		URL url = new URL("http://localhost:9270/metrics");
		URLConnection conn = url.openConnection();
		conn.setReadTimeout(3000);
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		StringBuilder sb = new StringBuilder();
		String line = "";

		while ((line = in.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}

}
