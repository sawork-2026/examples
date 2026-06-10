#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

setup_java
require_hadoop
require_conf
ensure_dirs

HDFS_USER="${HDFS_USER:-$(whoami)}"
LOCAL_DEMO_DIR="${RUN_DIR}/demo"
LOCAL_FILE="${LOCAL_DEMO_DIR}/orders.csv"
HDFS_DIR="/user/${HDFS_USER}/input"
HDFS_FILE="${HDFS_DIR}/orders.csv"

mkdir -p "${LOCAL_DEMO_DIR}"
cat > "${LOCAL_FILE}" <<'EOF'
order_id,user_id,total
1001,u-001,42.50
1002,u-002,18.00
1003,u-001,128.90
EOF

info "Create HDFS directory"
hdfs dfs -mkdir -p "${HDFS_DIR}"

info "Upload local file to HDFS"
hdfs dfs -put -f "${LOCAL_FILE}" "${HDFS_FILE}"

info "List HDFS directory"
hdfs dfs -ls "${HDFS_DIR}"

info "Read HDFS file"
hdfs dfs -cat "${HDFS_FILE}"

info "Show blocks and locations"
hdfs fsck "${HDFS_FILE}" -files -blocks -locations
