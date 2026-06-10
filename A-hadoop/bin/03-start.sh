#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

setup_java
require_hadoop
require_conf
ensure_dirs

[[ -d "${RUN_DIR}/hdfs/name/current" ]] || die "NameNode is not formatted. Run bin/02-format.sh first."

wait_for_port() {
  local port="$1"
  local name="$2"

  for _ in {1..30}; do
    if (: </dev/tcp/127.0.0.1/"${port}") >/dev/null 2>&1; then
      info "${name} is listening on 127.0.0.1:${port}"
      return
    fi
    sleep 1
  done

  die "${name} did not start listening on 127.0.0.1:${port}"
}

wait_for_hdfs() {
  for _ in {1..60}; do
    if hdfs dfsadmin -safemode get 2>/dev/null | grep -q 'Safe mode is OFF'; then
      info "HDFS is writable"
      return
    fi
    sleep 1
  done

  die "HDFS is still in safe mode"
}

start_one() {
  local service="$1"
  local pid_file="${HADOOP_PID_DIR}/${service}.pid"
  local log_file="${HADOOP_LOG_DIR}/${service}.out"

  if [[ -f "${pid_file}" ]] && kill -0 "$(cat "${pid_file}")" >/dev/null 2>&1; then
    info "${service} already running, pid $(cat "${pid_file}")"
    return
  fi

  rm -f "${pid_file}"
  info "Starting ${service}"
  nohup "${HADOOP_HOME}/bin/hdfs" --config "${CONF_DIR}" "${service}" > "${log_file}" 2>&1 &
  echo "$!" > "${pid_file}"
  sleep 1

  if ! kill -0 "$(cat "${pid_file}")" >/dev/null 2>&1; then
    warn "${service} exited during startup. Last log lines:"
    tail -n 80 "${log_file}" >&2 || true
    exit 1
  fi
}

info "Starting HDFS daemons without ssh"
start_one namenode
wait_for_port 9000 "NameNode RPC"
start_one datanode
wait_for_port 9864 "DataNode HTTP"
start_one secondarynamenode
wait_for_port 9868 "SecondaryNameNode HTTP"
wait_for_hdfs

cat <<EOF

HDFS is ready.

NameNode UI:
  http://localhost:9870

Try:
  bash bin/04-demo.sh
  bash bin/06-wordcount.sh

Logs:
  ${HADOOP_LOG_DIR}
EOF
