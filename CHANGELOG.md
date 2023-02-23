# 0.6.1

* #116 is a bugfix in PrometheusServer.java to close the server after the delay.
  `prometheus.delay` will now work as expected.

# 0.4.0

* #47 - bugfix so to correctly rename gui elements.\
* #51 - enable listening to assertions in counter type metrics.

# 0.3.0

* #50 - listening to latency
* #49 - listening to idle time
* #48 - listening to connection time

# 0.2.0

* #24 - success ratios to send zeros.
* #46 - code is a label keyword for response code.
* #11 - save jvm stats and allow for configuration.


# 0.2.0-rc3
* #42 - implement counters (success, failure and total)
* listener can now measure response sizes of samples in histogram and summary.
* #41 - enhancement for metric re-use.

# 0.2.0-rc2
Bugfixes for previous version.

* #40
* #34
* undocumented problem copy/paste (serializable issues) of the config


# 0.2.0-rc1
* first real release with only response time functionality in the listener.
