# Prometheus Listener for Jmeter

# Overview
This JMeter plugin is highly configurable listener (and config element) to allow users define they're own metrics (names, types etc.) and expose them through a Prometheus /metrics API to be scraped by a Prometheus server.

# Documentation
More documentation can be found on [this project's wiki](https://github.com/johrstrom/jmeter-prometheus-plugin/wiki).

# Listener QuickDoc
Here's a simple example to get us started.  This example [can be found here](https://github.com/johrstrom/jmeter-prometheus-plugin/blob/master/docs/examples/simple_prometheus_example.jmx).  All the documentation on this README is from this jmx file.

![JMeter testplan](/docs/imgs/simple_testplan.png?raw=true)

If we look closer at the very first Prometheus listener, it looks like this.  As you can see the current features are:
* Histograms and Summaries measuring response times (in seconds) or response sizes (in bytes)
* Counters measuring totals, successes, or failures.


![JMeter testplan](/docs/imgs/response_time_listener.png?raw=true)

Which will generate metrics like the image below.  Two things to note about the labels here in this example.  First is that `label` is a keyword so the label value `label="can_fail_sampler"` in the jsr223_rt_as_hist metric is from the testplan above.  It's the **name** of the actual sampler.

![JMeter testplan](/docs/imgs/category_variable.png?raw=true)

 The second thing of note is this label `category` in the jsr223_rt_as_summary metric which produced `category="[A,B,C]"`.  This plugin allows you to use variables in the test plan as label values for a given metric.  You can see here in the above image, I simply generated a random string and assigned it to the `category` jmeter variable, and this plugin exposed it as a label.

![JMeter testplan](/docs/imgs/rt_as_sum.png?raw=true)

# Config QuickDoc

This library provides not only a listener, but a configuration element as well.  This is useful when users have to make some computation for a specific and they want to expose that metric in Prometheus.

The use cases for this are mostly functional, where say you're validating something about a response that's specific the thing under test.

It works like this.  Define a metric with this configuration element, and at test run-time this library will place that object in the jmeter variables from which you can access it.

This example [can be found here](https://github.com/johrstrom/jmeter-prometheus-plugin/), but looks look at it. Here I define a metric where I'm counting all the animals of different colors, sizes and whether they're mammals or not.  

![JMeter testplan](/docs/imgs/prometheus_cfg.png?raw=true)

So I then access the object I've created above like so.  This is an absolutely trivial case of randomly generating the variables `color` `size`and `mammal`, but the example is only trying to convey how one may interact with the objects created by the configuration element.

![JMeter testplan](/docs/imgs/jsr223_use_prometheus_cfg.png?raw=true)

Which will expose a counter with all the appropriate labels.

![JMeter testplan](/docs/imgs/prom_cfg_output.png?raw=true)

# Usage Tips

### Skipping samplers or other elements

You can re-use metrics in multiple listeners so long as they're defined in the **exact** same way. There will be undefined behavior if two or more listeners have the same metric (the same metric name) with different configurations.


Here you see `first_random_sampler` and `second_random_sampler` in the labels of this metric, but you do not see `want_to_skip`, the thing that we're trying to skip.  Note the composition of the test plan as shown [at the top of this page](#Listener-QuickDoc)

![JMeter testplan](/docs/imgs/rt_as_hist.png?raw=true)

### Visualization

This plugin has limited "out of the box" functionality because it gives you, the user, total control over what the metric names may be.  That said, here's a sample dashboard given [here in examples](/examples/grafana_jsr223_test.json) such that if you have a local prometheus/grafana stack you can a dashboard that looks something like this.

![JMeter testplan](/docs/imgs/grafana_jsr223_test.png?raw=true)


# Properties you can override

|Property | default | description|
|:----------:|:-----------:|:-------------------------------:|
|prometheus.port|9270|The port the http server will bind to |
|prometheus.delay|0|The delay (in seconds) the http server will wait before being destroyed|
|prometheus.save.threads|true|True or false value to save and collect jmeter thread metrics|
|prometheus.save.threads.name|jmeter_threads|The name of the metric describing jmeter threads|

# Building

To build, simply maven package:
```
mvn clean package
```
This creates 2 jars, a shaded jar that has all the dependencies within it (this is the one you want) and the original jar. Both are in the target directory.  Simply move the jar to your $JMETER\_HOME/lib directory as with any JMeter plugin and you're ready to go!

## Feedback

Feel free to open issues against this project, even to ask questions.
