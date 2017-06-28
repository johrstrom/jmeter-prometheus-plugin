package com.github.johrstrom.listener;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import io.prometheus.client.Collector;
import io.prometheus.client.Counter;

import org.slf4j.Logger;

public class PrometheusSaveConfig implements Serializable {

	private static final long serialVersionUID = 3374323089879858706L;

	private static final Logger log = LoggerFactory.getLogger(PrometheusSaveConfig.class);

	public static final List<String> SAVE_CONFIG_NAMES = Collections
			.unmodifiableList(Arrays.asList(new String[] { "Label", "Code", // Response
																			// Code
					"Success", "Threads", "Assertions", "Port", }));

	private boolean label, code, success, assertions, threads;
	private int port;
	private Class<? extends Collector> assertionClass;

	public PrometheusSaveConfig() {
		this(true);
	}

	public PrometheusSaveConfig(boolean save) {
		this.setSaveLabel(save);
		this.setSaveCode(save);
		this.setSaveThreads(save);
		this.setSaveSuccess(save);
		this.setSaveAssertions(save);
		this.setAssertionClass(Counter.class);
		this.setPort(9270);
	}

	public boolean saveLabel() {
		return this.label;
	}

	public void setSaveLabel(boolean save) {
		log.debug("Setting save label to " + save);
		this.label = save;
	}

	public boolean saveCode() {
		return this.code;
	}

	public void setSaveCode(boolean save) {
		log.debug("Setting save code to " + save);
		this.code = save;
	}

	public boolean saveSuccess() {
		return this.success;
	}

	public void setSaveSuccess(boolean save) {
		log.debug("Setting save success to " + save);
		this.success = save;
	}

	public boolean saveAssertions() {
		return this.assertions;
	}

	public void setSaveAssertions(boolean save) {
		log.debug("Setting save assertions to " + save);
		this.assertions = save;
	}
	
	public boolean saveThreads() {
		return this.threads;
	}

	public void setSaveThreads(boolean save) {
		log.debug("Setting save threads to " + save);
		this.threads = save;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port_value) {
		log.debug("Setting port to " + port_value);
		this.port = port_value;
	}

	public Class<? extends Collector> getAssertionClass() {
		return assertionClass;
	}

	public void setAssertionClass(Class<? extends Collector> assertionClass) {
		this.assertionClass = assertionClass;
	}
}
