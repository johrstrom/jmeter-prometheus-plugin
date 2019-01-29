package com.github.johrstrom.test;

import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterThreadMonitor;

public class NOOPThreadMonitor implements JMeterThreadMonitor {

   @Override
   public void threadFinished(JMeterThread jmt) {
     // NOOP
   }

}