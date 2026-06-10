#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

HADOOP_VERSION="${HADOOP_VERSION:-3.5.0}"
WORK_DIR="${WORK_DIR:-/tmp/software-architecture-explained-with-spring-A-hadoop}"
CACHE_DIR="${CACHE_DIR:-${WORK_DIR}/cache}"
RUN_DIR="${RUN_DIR:-${WORK_DIR}/run}"
CONF_DIR="${CONF_DIR:-${WORK_DIR}/conf}"
HADOOP_HOME="${HADOOP_HOME:-${WORK_DIR}/hadoop-${HADOOP_VERSION}}"

export HADOOP_HOME
export HADOOP_COMMON_HOME="${HADOOP_HOME}"
export HADOOP_HDFS_HOME="${HADOOP_HOME}"
export HADOOP_MAPRED_HOME="${HADOOP_HOME}"
export HADOOP_YARN_HOME="${HADOOP_HOME}"
export HADOOP_CONF_DIR="${CONF_DIR}"
export HADOOP_LOG_DIR="${RUN_DIR}/logs"
export HADOOP_PID_DIR="${RUN_DIR}/pids"

info() {
  printf '\033[1;34m==>\033[0m %s\n' "$*"
}

warn() {
  printf '\033[1;33mWARN:\033[0m %s\n' "$*" >&2
}

die() {
  printf '\033[1;31mERROR:\033[0m %s\n' "$*" >&2
  exit 1
}

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || die "command not found: $1"
}

setup_java() {
  require_cmd java

  if [[ -z "${JAVA_HOME:-}" && -x /usr/libexec/java_home ]]; then
    JAVA_HOME="$(/usr/libexec/java_home)"
  fi

  [[ -n "${JAVA_HOME:-}" ]] || die "JAVA_HOME is not set"
  export JAVA_HOME
  info "JAVA_HOME=${JAVA_HOME}"
}

ensure_dirs() {
  mkdir -p "${CACHE_DIR}" "${RUN_DIR}" "${CONF_DIR}" "${HADOOP_LOG_DIR}" "${HADOOP_PID_DIR}"
}

require_hadoop() {
  [[ -x "${HADOOP_HOME}/bin/hdfs" ]] || die "Hadoop not found at ${HADOOP_HOME}. Run bin/00-download.sh first."
}

require_conf() {
  [[ -f "${CONF_DIR}/core-site.xml" ]] || die "Config not found at ${CONF_DIR}. Run bin/01-configure.sh first."
}

hdfs() {
  "${HADOOP_HOME}/bin/hdfs" --config "${CONF_DIR}" "$@"
}

hadoop() {
  "${HADOOP_HOME}/bin/hadoop" --config "${CONF_DIR}" "$@"
}
