#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

if [[ -x "${HADOOP_HOME}/bin/hdfs" ]]; then
  bash "${PROJECT_DIR}/bin/05-stop.sh" || true
fi

info "Removing runtime data and rendered config"
rm -rf "${RUN_DIR}" "${CONF_DIR}"

if [[ "${CLEAN_DOWNLOAD:-0}" == "1" ]]; then
  info "Removing downloaded Hadoop and cache"
  rm -rf "${HADOOP_HOME}" "${CACHE_DIR}"
fi

info "Clean complete"
