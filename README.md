[![Build Status](https://travis-ci.org/johrstrom/jmeter-prometheus-plugin.png?branch=master)](travis)
[![Current Version](https://img.shields.io/maven-central/v/com.github.johrstrom/jmeter-prometheus-plugin.svg)](maven-central)

# Prometheus Listener for Jmeter

# Overview
This JMeter plugin is highly configurable listener (and config element) to allow users define they're own metrics (names, types etc.) and expose them through a Prometheus /metrics API to be scraped by a Prometheus server.

# Documentation
More documentation can be found on [this project's wiki](https://github.com/johrstrom/jmeter-prometheus-plugin/wiki).

# Listener QuickDoc
Here's a simple example to get us started.  This example [can be found here](/docs/examples/simple_prometheus_example.jmx).  All the documentation on this README is from this jmx file.

![JMeter testplan](/docs/imgs/simple_testplan.png?raw=true)

If we look closer at the very first Prometheus listener, it looks like the image below.

![JMeter testplan](/docs/imgs/listener_full.png?raw=true)

Let's go through all the columns one by one.

- **Name**: the name of the metric.
- **Help**: The help message of the metric.
- **Labels**: A comma seperated list of labels you want to apply to the metric.
  - `label` is a keyword. In JMeter it means the *name* of the sampler.
  - `code` is a keyword. It's the response code of the result.
  - JMeter variables can be used here. See the section [below](#Using-JMeter-variables-as-labels).
- **Type**: The type of metric you're creating.
  - See [Prometheus documentation](https://prometheus.io/docs/concepts/metric_types/) about metric types.
  - [Success Ratio](#Success-Ratio) is something specific to this plugin and you can see the documentation below.
- **Buckets of Quantiles**:
  - Buckets are comma seperated list of numbers. Can be integers or decimals.
  - Quantiles are comma `,` separated pair of decimals separated by a vertical bar `|`. The first decimal being the quantile and the second being the error rating. Optionally, after a `;` separator the lenght of the window used to calculate the quantile can be specified. Sample: `0.8,0.01|0.9,0.01|0.95,0.005|0.99,0.001;60`
- **Listen To**: Dropdown to listen to samples or assertions. This only applies to Counters and SuccessRatio type metrics.
- **Measuring**: Dropdown menu of all the things you can measure
  - See the [Type and Measuring compatibility matrix](#Type-and-Measuring-compatibility-matrix) section below.

### Using JMeter variables as labels

Notice in the image above `jsr223_rt_as_summary` (the 2nd down) has `category,label` in it's labels column.

This plugin allows you to use variables in the test plan as label values for a given metric.  You can see here in the above image, I simply generated a random string and assigned it to the `category` jmeter variable, and this plugin exposed it as a label.

As you can see below, it produced that metric with the `category="[A,B,C]"`. Again, this example [can be found here](/docs/examples/simple_prometheus_example.jmx).

![JMeter testplan](/docs/imgs/category_variable.png?raw=true)


### Listener output

![JMeter testplan](/docs/imgs/rt_as_sum.png?raw=true)

### Success Ratio

Success ratio is a concept specific to this plugin library.  Often we want measure success rates of samplers and it's difficult to do so when the failure for a given metric or labelset has never occurred.  It's difficult because it involves computations with NaNs.

So, we want to measure success ratios and be sure to emit zeros for both failures and success when the other is created for the first time.  That way you can always safely run computations like rate() and so on.

[Here's prometheus](https://www.robustperception.io/existential-issues-with-metrics) documentation on why it's important.  [Here](https://github.com/johrstrom/jmeter-prometheus-plugin/issues/24) is this repositories issue for this feature.

Here you can see from the example `jsr223_can_fail` turned into 3 metrics.  The names have appeneded `_success`, `_failure` and `_total`.  They're all counter type metrics that increment on success failure and total, respectively.  

From this example you can see that `4 success + 2 failures = 6 total`.  Again, this metric guarantees a zero, for success or failure, for a metric that has a total of one or more.

![JMeter testplan](/docs/imgs/success_ratio_output.png?raw=true)

### Type and Measuring compatibility matrix

Does it make sense to have a Counter measuring Response Time? No. Does it make sense to have a Histogram of total successes? No.  

This is a matrix of what metric types can measure what metrics.  If you configure, say a histogram to measure count total, the plugin will likely do nothing to update that metric.

**Bold** types can listen to samples or assertions (not both at the same time).  Note that if you don't use `label` when listening to assertions you may get strange results.  This is because *one* sample can generate many *assertion results* which are then counted. When there's no label to distinguish those counts, they'll be summed together which may or may not be expected.

| | Histogram | Summary | Counter | Guage | Success Ratio |
|:-----:|:------:|:------:|:------:|:------:|:------:|
| Response time  | x | x |   |   |   |
| Response size  | x | x |   |   |   |
| Latency        | x | x |   |   |   |
| Idle time      | x | x |   |   |   |
| Connect time   | x | x |   |   |   |
| Count total    |   |   | **x** |   |   |
| Failure total  |   |   | **x** |   |   |
| Success total  |   |   | **x** |   |   |
| Success Ratio  |   |   |   |   | **x** |

#### What about gauges
I'm not quite sure how Guages make sense in the plugin.  If you have a use case, I'd love to hear it. I wrote them in without actually having one, so you can technically create one, I just don't know how the listener may update it.

# Config QuickDoc

This library provides not only a listener, but a configuration element as well.  This is useful when users have to make some computation for a specific use case and then want to expose that metric in Prometheus.

These use cases for this are mostly functional, where say you're validating something about a response that's specific the thing under test.  

It works like this.  Define a metric with this configuration element, and at test run-time this library will place that object in the jmeter variables from which you can access it.

Let's consider this use case.  You, have an API that tells you the current number animals categorized by `color` `size`and `mammal` - perhaps running down your street.  You may create a counter like this as you watch them go by.
![JMeter testplan](/docs/imgs/prometheus_cfg.png?raw=true)


So I then access the object I've created above like so.  This is an absolutely trivial case of randomly generating the variables but the example is only trying to convey how one may interact with the objects created by the configuration element.  However you extract the data, you can access and interact with the Prometheus metric you've created.  

See the [Prometheus Javadocs](https://prometheus.github.io/client_java/) for more information on their API.

![JMeter testplan](/docs/imgs/jsr223_use_prometheus_cfg.png?raw=true)

Which will expose a counter with all the appropriate labels.

![JMeter testplan](/docs/imgs/prom_cfg_output.png?raw=true)

# Usage Tips

### Skipping samplers or other elements

You can re-use metrics in multiple listeners so long as they're defined in the **exact** same way. There will be undefined behavior if two or more listeners have the same metric (the same metric name) with different configurations.


Here you see `first_random_sampler` and `second_random_sampler` in the labels of this metric, but you do not see `want_to_skip`, the thing that we're trying to skip.  Note the composition of the test plan as shown [at the top of this page](#Listener-QuickDoc)

![JMeter testplan](/docs/imgs/rt_as_hist.png?raw=true)

### Visualization

This plugin has limited "out of the box" functionality because it gives you, the user, total control over what the metric names may be.  That said, here's a sample dashboard given [here in examples](/docs/examples/grafana_jsr223_test.json) such that if you have a local prometheus/grafana stack you can a dashboard that looks something like this.

![JMeter testplan](/docs/imgs/grafana_jsr223_test.png?raw=true)


# Properties you can override
To overrider properties, add the Properties in the jmeter.properties file (JMETER_HOME/bin folder) and restart Jmeter to take effect

|Property | default | description|
|:----------:|:-----------:|:-------------------------------:|
|prometheus.port|9270|The port the http server will bind to |
|prometheus.ip|127.0.0.1|The ip the http server will bind to. Containers may need `0.0.0.0`|
|prometheus.delay|0|The delay (in seconds) the http server will wait before being destroyed|
|prometheus.save.threads|true|True or false value to save and collect jmeter thread metrics|
|prometheus.save.threads.name|jmeter\_threads|The name of the metric describing jmeter threads|
|prometheus.save.jvm|true|Collect metrics from the JVM|

# Download

## Maven Dependency

We're now hosted on maven central! If you want to download this jar in a maven style project, simply add this dependency:

```xml
    <!-- you'll have to specify jmeter-prometheus-plugin.version here -->
    <dependency>
      <groupId>com.github.johrstrom</groupId>
      <artifactId>jmeter-prometheus-plugin</artifactId>
      <version>${jmeter-prometheus-plugin.version}</version>
    </dependency>
```

## Programatically

This URL below seems to be the only way to download jars from maven through `curl` or `wget`.  Again, replace `0.6.0` here with the
current version, which can be viewed at the top of this README.

`https://search.maven.org/remotecontent?filepath=com/github/johrstrom/jmeter-prometheus-plugin/0.6.0/jmeter-prometheus-plugin-0.6.0.jar`

## Web Browser

Search [maven central](https://search.maven.org/search?q=a:jmeter-prometheus-plugin) to get the latest version.

This project is hosted [here](https://oss.sonatype.org/content/groups/public/com/github/johrstrom/jmeter-prometheus-plugin/) on 
[OSS sonatype org](https://oss.sonatype.org).

## Verifying

I sign these release jars so you can verify with this method (of course the version is going to change):

```bash
gpg --verify jmeter-prometheus-plugin-0.5.0.jar.asc
```

You should see output similar to this.

```bash
gpg: assuming signed data in 'jmeter-prometheus-plugin-0.5.0.jar'
gpg: Signature made Thu 22 Aug 2019 09:35:15 PM EDT
gpg:                using RSA key 6F5EAC674B279301932EC1FEAC2AEC6C76D4AF12
gpg: Good signature from "Jeff Ohrstrom (Jeff Ohrstrom's personal key) <johrstrom@hotmail.com>" [ultimate]
```

# Building

To build, simply maven package:
```
mvn clean package
```
This creates 2 jars, a shaded jar that has all the dependencies within it (this is the one you want) and the original jar. Both are in the target directory.  Simply move the jar to your $JMETER\_HOME/lib/ext directory as with any JMeter plugin and you're ready to go!

## Feedback

Feel free to open issues against this project, even to ask questions.
