#!/usr/bin/env bash

set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_cmd curl
require_cmd tar
setup_java
ensure_dirs

if [[ -x "${HADOOP_HOME}/bin/hdfs" ]]; then
  info "Hadoop already exists: ${HADOOP_HOME}"
  "${HADOOP_HOME}/bin/hdfs" version | sed -n '1,3p'
  exit 0
fi

case "$(uname -m)" in
  arm64|aarch64) default_tarball="hadoop-${HADOOP_VERSION}-aarch64.tar.gz" ;;
  *) default_tarball="hadoop-${HADOOP_VERSION}.tar.gz" ;;
esac

tarball="${HADOOP_TARBALL:-${default_tarball}}"
tarball_path="${CACHE_DIR}/${tarball}"
primary_url="https://dlcdn.apache.org/hadoop/common/hadoop-${HADOOP_VERSION}/${tarball}"
archive_url="https://archive.apache.org/dist/hadoop/common/hadoop-${HADOOP_VERSION}/${tarball}"

if [[ ! -f "${tarball_path}" ]]; then
  info "Downloading ${primary_url}"
  if ! curl -fL "${primary_url}" -o "${tarball_path}"; then
    warn "Primary download failed; trying Apache archive"
    curl -fL "${archive_url}" -o "${tarball_path}"
  fi
else
  info "Using cached ${tarball_path}"
fi

info "Extracting to ${WORK_DIR}"
tar -xzf "${tarball_path}" -C "${WORK_DIR}"

extracted="${WORK_DIR}/${tarball%.tar.gz}"
if [[ "${extracted}" != "${HADOOP_HOME}" && -d "${extracted}" && ! -e "${HADOOP_HOME}" ]]; then
  mv "${extracted}" "${HADOOP_HOME}"
fi

[[ -x "${HADOOP_HOME}/bin/hdfs" ]] || die "Extracted Hadoop does not contain bin/hdfs: ${HADOOP_HOME}"

info "Downloaded Hadoop:"
"${HADOOP_HOME}/bin/hdfs" version | sed -n '1,3p'
