package com.github.johrstrom.config.gui;

import java.util.Locale;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.collector.BaseCollectorConfig;



public class ConfigGuiTest {

	@Test
	public void simpleTest() {
		JMeterUtils.setLocale(Locale.ENGLISH);
		
		PrometheusMetricsConfigGui<BaseCollectorConfig> gui = new PrometheusMetricsConfigGui<>();
		
		String comment = "this should be the comment";
		String name = "simple cfg name";
		
		gui.setName(name);
		gui.setComment(comment);
		
		Assert.assertEquals(name, gui.getName());
		Assert.assertEquals(comment, gui.getComment());
	}

}
