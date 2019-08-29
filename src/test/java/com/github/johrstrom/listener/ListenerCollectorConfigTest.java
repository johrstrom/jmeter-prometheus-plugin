package com.github.johrstrom.listener;

import com.github.johrstrom.test.TestUtilities;
import org.junit.Assert;
import org.junit.Test;

public class ListenerCollectorConfigTest {

	@Test
	public void setOfElementsTest() {
		ListenerCollectorConfig left = new ListenerCollectorConfig(TestUtilities.simpleCounterCfg());
		ListenerCollectorConfig right = new ListenerCollectorConfig(TestUtilities.simpleCounterCfg());

		Assert.assertNotSame(left, right);
		Assert.assertEquals(left, right);
		
		int leftHash = left.hashCode();
		int rightHash = right.hashCode();

		Assert.assertEquals(leftHash, rightHash);
	}
	
}
