package com.github.johrstrom.listener.gui;

import org.apache.jmeter.util.JMeterUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;


public class ListenerGuiTest {

	@Test
	public void simpleTest() {
		JMeterUtils.setLocale(Locale.ENGLISH);
		
		PrometheusListenerGui gui = new PrometheusListenerGui();
		
		String comment = "this should be the comment";
		String name = "simple listener name";
		
		gui.setName(name);
		gui.setComment(comment);
		
		Assert.assertEquals(name, gui.getName());
		Assert.assertEquals(comment, gui.getComment());
	}

}
