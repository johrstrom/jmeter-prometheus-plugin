#!/bin/sh

mvn clean package

cp "target/prometheus-jmeter-0.0.1-SNAPSHOT.jar" "/opt/jmeter/apache-jmeter-3.0/lib/ext"
