#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

setup_java
require_hadoop
require_conf
ensure_dirs

examples_jar="${HADOOP_HOME}/share/hadoop/mapreduce/hadoop-mapreduce-examples-${HADOOP_VERSION}.jar"
[[ -f "${examples_jar}" ]] || die "Hadoop examples jar not found: ${examples_jar}"

HDFS_USER="${HDFS_USER:-$(whoami)}"
LOCAL_WORDCOUNT_DIR="${RUN_DIR}/wordcount"
LOCAL_INPUT="${LOCAL_WORDCOUNT_DIR}/input.txt"
HDFS_BASE="/user/${HDFS_USER}/wordcount"
HDFS_INPUT="${HDFS_BASE}/input"
HDFS_OUTPUT="${HDFS_BASE}/output"

mkdir -p "${LOCAL_WORDCOUNT_DIR}"
cat > "${LOCAL_INPUT}" <<'EOF'
hadoop hdfs hadoop
architecture hdfs block
block replica namenode datanode
hadoop architecture
EOF

info "Prepare WordCount input in HDFS"
hdfs dfs -mkdir -p "${HDFS_INPUT}"
hdfs dfs -put -f "${LOCAL_INPUT}" "${HDFS_INPUT}/input.txt"
hdfs dfs -rm -r -f "${HDFS_OUTPUT}" >/dev/null 2>&1 || true

info "Run Hadoop WordCount example"
hadoop jar "${examples_jar}" wordcount "${HDFS_INPUT}" "${HDFS_OUTPUT}"

info "List WordCount output"
hdfs dfs -ls "${HDFS_OUTPUT}"

info "Read WordCount result"
hdfs dfs -cat "${HDFS_OUTPUT}/part-r-00000"
