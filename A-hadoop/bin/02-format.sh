#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_java
require_hadoop_home
require_rendered_conf
ensure_dirs

name_current="${RUN_DIR}/hdfs/name/current"

if [[ -d "${name_current}" && "${FORMAT_FORCE:-0}" != "1" ]]; then
  die "NameNode already appears formatted at ${name_current}. Use FORMAT_FORCE=1 bash bin/02-format.sh to reformat."
fi

if [[ "${FORMAT_FORCE:-0}" == "1" ]]; then
  warn "Removing old HDFS metadata/data under ${RUN_DIR}/hdfs"
  rm -rf "${RUN_DIR}/hdfs/name" "${RUN_DIR}/hdfs/data"
  mkdir -p "${RUN_DIR}/hdfs/name" "${RUN_DIR}/hdfs/data"
fi

info "Formatting NameNode"
hdfs_cmd namenode -format -nonInteractive

