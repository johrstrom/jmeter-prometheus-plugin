package org.ohrstrom.listener;

import java.io.Serializable;

import org.apache.jmeter.engine.util.NoThreadClone;
import org.apache.jmeter.reporters.AbstractListenerElement;
import org.apache.jmeter.samplers.Remoteable;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.prometheus.client.exporter.MetricsServlet;

public class PrometheusListener extends AbstractListenerElement 
	implements SampleListener, Serializable, TestStateListener, Remoteable, NoThreadClone {

	private static final long serialVersionUID = -4833646252357876746L;

	private static final Logger log = LoggingManager.getLoggerForClass();
	
	private Server server;
	private Summary transactions;

	public PrometheusListener(){
		this("Prometheus Listener");
	}
	
	public PrometheusListener(String name){
		super();
		this.createMetrics();
		log.info("Creating new prometheus listener.");
	}

	public void sampleOccurred(SampleEvent arg0) {
		SampleResult res = arg0.getResult();
		
		long latency = res.getLatency();
		String status = res.getResponseCode();
		String name = res.getSampleLabel();
		
//		this.server.getThreadPool().dis
		
		transactions.labels(name, status).observe(latency);
		
	}

	public void sampleStarted(SampleEvent arg0) {}

	public void sampleStopped(SampleEvent arg0) {}

	public void testEnded() {
		try {
			this.server.stop();
		} catch (Exception e) {
			
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testEnded(String arg0) {
		this.testEnded();
	}

	public void testStarted() {
		this.server = new Server(8080);
		
	     ServletContextHandler context = new ServletContextHandler();
	     context.setContextPath("/");
	     server.setHandler(context);
	     context.addServlet(new ServletHolder(new MetricsServlet()), "/metrics");
	     
	     try {
			server.start();
	     } catch (Exception e) {
			
			e.printStackTrace();
	     }
	     
	     	
		
	}

	public void testStarted(String arg0) {
		this.testStarted();		
	}
	
	
	private void createMetrics(){
		
		CollectorRegistry.defaultRegistry.clear();		
		
		this.transactions = Summary.build()
				.name("synthetic_transaction")
				.help("Counter for all synthetic transactions")
				.labelNames("name", "status")
				.quantile(0.5, 0.1)
				.quantile(0.99, 0.1)
				.create()
				.register(CollectorRegistry.defaultRegistry);
		
		
	}

}
