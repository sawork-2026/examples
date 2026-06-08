#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_java
require_cmd python3
require_hadoop_home
require_rendered_conf
ensure_dirs

[[ -d "${RUN_DIR}/hdfs/name/current" ]] || die "NameNode is not formatted. Run bin/02-format.sh first."

START_MODE="${START_MODE:-nohup}"

if [[ "${START_MODE}" == "start-dfs" ]]; then
  info "Starting HDFS with start-dfs.sh. This requires passphraseless ssh localhost."
  "${HADOOP_HOME}/sbin/start-dfs.sh" --config "${RENDERED_CONF_DIR}"
else
  info "Starting HDFS daemons without ssh"
  start_hdfs_service namenode
  wait_for_tcp 127.0.0.1 9000 "NameNode RPC"
  start_hdfs_service datanode
  wait_for_tcp 127.0.0.1 9864 "DataNode HTTP"
  start_hdfs_service secondarynamenode
  wait_for_tcp 127.0.0.1 9868 "SecondaryNameNode HTTP"
fi

info "Java processes"
jps || true

cat <<EOF

HDFS is starting.

NameNode UI:
  http://localhost:9870

Try:
  bash bin/04-demo.sh

Logs:
  ${HADOOP_LOG_DIR}
EOF
