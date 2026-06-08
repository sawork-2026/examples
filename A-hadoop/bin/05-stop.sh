#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if [[ ! -x "${HADOOP_HOME}/bin/hdfs" ]]; then
  warn "Hadoop not found at ${HADOOP_HOME}; nothing to stop"
  exit 0
fi

if [[ -f "$(service_pid_file secondarynamenode)" || -f "$(service_pid_file datanode)" || -f "$(service_pid_file namenode)" ]]; then
  info "Stopping HDFS daemons"
  stop_hdfs_service secondarynamenode
  stop_hdfs_service datanode
  stop_hdfs_service namenode
elif [[ -f "${RENDERED_CONF_DIR}/core-site.xml" ]]; then
  warn "No local pid files found; trying Hadoop daemon stop"
  hdfs_cmd --daemon stop secondarynamenode || true
  hdfs_cmd --daemon stop datanode || true
  hdfs_cmd --daemon stop namenode || true
else
  warn "Rendered config not found; trying default Hadoop stop"
  "${HADOOP_HOME}/bin/hdfs" --daemon stop secondarynamenode || true
  "${HADOOP_HOME}/bin/hdfs" --daemon stop datanode || true
  "${HADOOP_HOME}/bin/hdfs" --daemon stop namenode || true
fi

info "Remaining Java processes"
jps || true
