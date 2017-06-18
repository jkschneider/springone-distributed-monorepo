#!/bin/bash

# This init script installs Netflix Atlas on the master node of a Cloud
# Dataproc cluster. It also installs and runs a spring-metrics enabled
# Spring Boot application that publishes timer metrics to Atlas.

set -x -e

# Only run on the master node
ROLE="$(/usr/share/google/get_metadata_value attributes/dataproc-role)"

if [[ "${ROLE}" == 'Master' ]]; then
  echo "Installing Atlas daemon"

  curl -Lo /etc/init.d/atlas https://raw.githubusercontent.com/jkschneider/gradle-summit-2017/master/atlas-dataproc-init/atlas
  chmod 755 /etc/init.d/atlas
  chown root:root /etc/init.d/atlas

  /etc/init.d/atlas start

  # Start a Spring Boot service that keeps track of timers on behalf of Spark workers
  echo "Installing Atlas collector"
  curl -Lo /opt/atlas-collector.jar 'https://dl.bintray.com/jkschneider/maven/io/jschneider/atlas-collector/0.1.0/atlas-collector-0.1.0.jar'
  nohup java -jar /opt/atlas-collector.jar > /dev/null 2>&1&
fi
