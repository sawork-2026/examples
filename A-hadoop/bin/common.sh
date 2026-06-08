#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"

HADOOP_VERSION="${HADOOP_VERSION:-3.5.0}"
WORK_DIR="${WORK_DIR:-${PROJECT_DIR}/.work}"
CACHE_DIR="${CACHE_DIR:-${WORK_DIR}/cache}"
RUN_DIR="${RUN_DIR:-${WORK_DIR}/run}"
RENDERED_CONF_DIR="${RENDERED_CONF_DIR:-${WORK_DIR}/conf}"
HADOOP_HOME="${HADOOP_HOME:-${WORK_DIR}/hadoop-${HADOOP_VERSION}}"

APACHE_DOWNLOAD_BASE="${APACHE_DOWNLOAD_BASE:-https://dlcdn.apache.org/hadoop/common}"
APACHE_ARCHIVE_BASE="${APACHE_ARCHIVE_BASE:-https://archive.apache.org/dist/hadoop/common}"

export HADOOP_HOME
export HADOOP_CONF_DIR="${RENDERED_CONF_DIR}"
export HADOOP_LOG_DIR="${RUN_DIR}/logs"
export HADOOP_PID_DIR="${RUN_DIR}/pids"

function info {
  printf '\033[1;34m==>\033[0m %s\n' "$*"
}

function warn {
  printf '\033[1;33mWARN:\033[0m %s\n' "$*" >&2
}

function die {
  printf '\033[1;31mERROR:\033[0m %s\n' "$*" >&2
  exit 1
}

function require_cmd {
  command -v "$1" >/dev/null 2>&1 || die "command not found: $1"
}

function detect_java_home {
  if [[ -n "${JAVA_HOME:-}" ]]; then
    printf '%s\n' "${JAVA_HOME}"
    return
  fi

  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    /usr/libexec/java_home
    return
  fi

  if command -v java >/dev/null 2>&1; then
    local java_bin
    java_bin="$(command -v java)"
    if command -v readlink >/dev/null 2>&1; then
      java_bin="$(readlink -f "${java_bin}" 2>/dev/null || printf '%s\n' "${java_bin}")"
    fi
    cd "$(dirname "${java_bin}")/.." && pwd
    return
  fi

  return 1
}

function require_java {
  require_cmd java
  JAVA_HOME="$(detect_java_home)" || die "JAVA_HOME is not set and could not be detected"
  export JAVA_HOME
  info "JAVA_HOME=${JAVA_HOME}"
}

function require_hadoop_home {
  [[ -x "${HADOOP_HOME}/bin/hdfs" ]] || die "Hadoop not found at ${HADOOP_HOME}. Run bin/00-download.sh first."
}

function require_rendered_conf {
  [[ -f "${RENDERED_CONF_DIR}/core-site.xml" ]] || die "Rendered config not found. Run bin/01-configure.sh first."
}

function hdfs_cmd {
  require_hadoop_home
  require_rendered_conf
  "${HADOOP_HOME}/bin/hdfs" --config "${RENDERED_CONF_DIR}" "$@"
}

function hadoop_cmd {
  require_hadoop_home
  require_rendered_conf
  "${HADOOP_HOME}/bin/hadoop" --config "${RENDERED_CONF_DIR}" "$@"
}

function ensure_dirs {
  mkdir -p "${CACHE_DIR}" "${RUN_DIR}" "${RENDERED_CONF_DIR}" "${HADOOP_LOG_DIR}" "${HADOOP_PID_DIR}"
}

function hadoop_tarball_name {
  if [[ -n "${HADOOP_TARBALL:-}" ]]; then
    printf '%s\n' "${HADOOP_TARBALL}"
    return
  fi

  case "$(uname -m)" in
    arm64|aarch64)
      printf 'hadoop-%s-aarch64.tar.gz\n' "${HADOOP_VERSION}"
      ;;
    *)
      printf 'hadoop-%s.tar.gz\n' "${HADOOP_VERSION}"
      ;;
  esac
}

function service_pid_file {
  local service="$1"
  printf '%s/%s.pid\n' "${HADOOP_PID_DIR}" "${service}"
}

function service_log_file {
  local service="$1"
  printf '%s/%s.out\n' "${HADOOP_LOG_DIR}" "${service}"
}

function is_pid_running {
  local pid="$1"
  [[ -n "${pid}" ]] && kill -0 "${pid}" >/dev/null 2>&1
}

function start_hdfs_service {
  local service="$1"
  local pid_file
  local log_file
  pid_file="$(service_pid_file "${service}")"
  log_file="$(service_log_file "${service}")"

  if [[ -f "${pid_file}" ]] && is_pid_running "$(cat "${pid_file}")"; then
    info "${service} already running, pid $(cat "${pid_file}")"
    return
  fi

  rm -f "${pid_file}"
  info "Starting ${service}"
  nohup "${HADOOP_HOME}/bin/hdfs" --config "${RENDERED_CONF_DIR}" "${service}" > "${log_file}" 2>&1 &
  echo "$!" > "${pid_file}"
  sleep 1

  if ! is_pid_running "$(cat "${pid_file}")"; then
    warn "${service} exited during startup. Last log lines:"
    tail -n 80 "${log_file}" >&2 || true
    return 1
  fi
}

function stop_hdfs_service {
  local service="$1"
  local pid_file
  pid_file="$(service_pid_file "${service}")"

  if [[ ! -f "${pid_file}" ]]; then
    warn "${service} pid file not found"
    return 0
  fi

  local pid
  pid="$(cat "${pid_file}")"
  if ! is_pid_running "${pid}"; then
    warn "${service} pid ${pid} is not running"
    rm -f "${pid_file}"
    return 0
  fi

  info "Stopping ${service}, pid ${pid}"
  kill "${pid}" >/dev/null 2>&1 || true

  local i
  for i in {1..20}; do
    if ! is_pid_running "${pid}"; then
      rm -f "${pid_file}"
      return 0
    fi
    sleep 1
  done

  warn "${service} did not stop after 20s; sending SIGKILL"
  kill -9 "${pid}" >/dev/null 2>&1 || true
  rm -f "${pid_file}"
}

function wait_for_tcp {
  local host="$1"
  local port="$2"
  local label="$3"
  local i
  for i in {1..30}; do
    if python3 - "${host}" "${port}" <<'PY' >/dev/null 2>&1
import socket
import sys
host, port = sys.argv[1], int(sys.argv[2])
with socket.create_connection((host, port), timeout=1):
    pass
PY
    then
      info "${label} is listening on ${host}:${port}"
      return 0
    fi
    sleep 1
  done
  die "${label} did not start listening on ${host}:${port}"
}
