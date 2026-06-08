#!/usr/bin/env bash

set -o errexit
set -o errtrace
set -o nounset
set -o pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/common.sh"

require_java
require_hadoop_home
ensure_dirs

info "Rendering Hadoop config into ${RENDERED_CONF_DIR}"

sed \
  -e "s#__RUN_DIR__#${RUN_DIR}#g" \
  -e "s#__PROJECT_DIR__#${PROJECT_DIR}#g" \
  "${PROJECT_DIR}/conf/core-site.xml.template" > "${RENDERED_CONF_DIR}/core-site.xml"

sed \
  -e "s#__RUN_DIR__#${RUN_DIR}#g" \
  -e "s#__PROJECT_DIR__#${PROJECT_DIR}#g" \
  "${PROJECT_DIR}/conf/hdfs-site.xml.template" > "${RENDERED_CONF_DIR}/hdfs-site.xml"

sed \
  -e "s#__JAVA_HOME__#${JAVA_HOME}#g" \
  -e "s#__HADOOP_HOME__#${HADOOP_HOME}#g" \
  -e "s#__RUN_DIR__#${RUN_DIR}#g" \
  "${PROJECT_DIR}/conf/hadoop-env.sh.template" > "${RENDERED_CONF_DIR}/hadoop-env.sh"

cp "${PROJECT_DIR}/conf/workers" "${RENDERED_CONF_DIR}/workers"
cp "${HADOOP_HOME}/etc/hadoop/log4j.properties" "${RENDERED_CONF_DIR}/log4j.properties"

mkdir -p "${RUN_DIR}/hdfs/name" "${RUN_DIR}/hdfs/data" "${HADOOP_LOG_DIR}" "${HADOOP_PID_DIR}"

info "Config files:"
find "${RENDERED_CONF_DIR}" -maxdepth 1 -type f -print | sort
