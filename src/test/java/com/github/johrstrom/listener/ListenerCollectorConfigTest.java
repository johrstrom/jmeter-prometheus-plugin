package com.github.johrstrom.listener;

import org.junit.Assert;
import org.junit.Test;

import com.github.johrstrom.test.TestUtilities;

public class ListenerCollectorConfigTest {

	@Test
	public void setOfElementsTest() {
		ListenerCollectorConfig left = new ListenerCollectorConfig(TestUtilities.simpleCounterCfg());
		ListenerCollectorConfig right = new ListenerCollectorConfig(TestUtilities.simpleCounterCfg());
		
		Assert.assertTrue(left != right);
		Assert.assertTrue(left.equals(right));
		
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();
		
		Assert.assertTrue(leftHash == rightHash);
	}
	
}
