package com.github.johrstrom.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class PrometheusSaveConfig {

	private static final Logger log = LoggerFactory.getLogger(PrometheusSaveConfig.class);

	public static final List<String> SAVE_CONFIG_NAMES = Collections
			.unmodifiableList(Arrays.asList(new String[] { "Label", "Code", // Response
																			// Code
					"Success", "Assertions", "Port", }));

	private boolean label, code, success, assertions;
	private int port;

	public PrometheusSaveConfig() {
		this(true);
	}

	public PrometheusSaveConfig(boolean save) {
		this.setSaveLabel(save);
		this.setSaveCode(save);
		this.setSaveSuccess(save);
		this.setSaveAssertions(save);
		this.setPort(8080);
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

	public int getPort() {
		return this.port;
	}

	public void setPort(int port_value) {
		log.debug("Setting port to " + port_value);
		this.port = port_value;
	}
}
