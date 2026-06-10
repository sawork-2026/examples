#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

stop_one() {
  local service="$1"
  local pid_file="${HADOOP_PID_DIR}/${service}.pid"

  if [[ ! -f "${pid_file}" ]]; then
    return
  fi

  local pid
  pid="$(cat "${pid_file}")"

  if kill -0 "${pid}" >/dev/null 2>&1; then
    info "Stopping ${service}, pid ${pid}"
    kill "${pid}" >/dev/null 2>&1 || true
  fi

  rm -f "${pid_file}"
}

if [[ ! -d "${HADOOP_PID_DIR}" ]]; then
  info "No HDFS pid directory found; nothing to stop"
  exit 0
fi

stop_one secondarynamenode
stop_one datanode
stop_one namenode

info "Stop command finished"
