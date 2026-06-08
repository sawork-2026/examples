# A-hadoop: 最小 HDFS 实验

这个目录用于配合 `A-Hadoop.md`：在本机启动一个最小的 pseudo-distributed HDFS，看见真实的 `NameNode`、`DataNode` 和 HDFS shell 操作。

默认脚本使用 Apache Hadoop 3.5.0。Apache 官网在 2026-04-02 发布 Hadoop 3.5.0，并称其为 3.5 line 的第一个 stable release。版本可以通过环境变量覆盖。

## 前置条件

- macOS 或 Linux
- JDK
- `curl`
- `python3`
- `tar`

检查 Java：

```bash
java -version
```

## 快速开始

```bash
cd examples/A-hadoop

bash bin/00-download.sh
bash bin/01-configure.sh
bash bin/02-format.sh
bash bin/03-start.sh
bash bin/04-demo.sh
```

打开 NameNode UI：

```text
http://localhost:9870
```

停止 HDFS：

```bash
bash bin/05-stop.sh
```

清理运行数据：

```bash
bash bin/99-clean.sh
```

如果也想删除下载的 Hadoop：

```bash
CLEAN_DOWNLOAD=1 bash bin/99-clean.sh
```

## 目录结构

```text
examples/A-hadoop
├── bin/                 # 下载、配置、格式化、启动、演示、停止脚本
├── conf/                # HDFS 最小配置模板
└── .work/               # 运行时生成：Hadoop、配置、NameNode/DataNode 数据、日志
```

`.work/` 不需要提交，可以随时删掉重来。

## 版本和下载源

默认：

```bash
HADOOP_VERSION=3.5.0
```

Apple Silicon / ARM64 机器默认下载 `hadoop-3.5.0-aarch64.tar.gz`，其他机器默认下载 `hadoop-3.5.0.tar.gz`。

换版本：

```bash
HADOOP_VERSION=3.4.3 bash bin/00-download.sh
```

指定 tarball 名称：

```bash
HADOOP_TARBALL=hadoop-3.5.0.tar.gz bash bin/00-download.sh
```

脚本会优先从 `https://dlcdn.apache.org` 下载，失败后尝试 Apache archive。

## 这个例子启动了什么？

单机伪分布式模式下，所有进程都在本机：

```text
HDFS Client -> NameNode      localhost:9000
            -> DataNode
            -> SecondaryNameNode
```

相关 Web UI：

```text
NameNode UI          http://localhost:9870
DataNode UI          http://localhost:9864
SecondaryNameNode UI http://localhost:9868
```

默认配置：

- `fs.defaultFS = hdfs://127.0.0.1:9000`
- `dfs.replication = 1`
- NameNode metadata 放在 `.work/run/hdfs/name`
- DataNode blocks 放在 `.work/run/hdfs/data`

## 为什么不用 start-dfs.sh？

Apache 官方单机伪分布式教程使用 `start-dfs.sh`，它通常需要配置免密 `ssh localhost`。

为了课堂演示更少踩坑，本例默认不用 SSH，也不使用 Hadoop 的 daemon wrapper，而是直接用 `nohup` 管理三个进程：

```bash
nohup hdfs --config .work/conf namenode &
nohup hdfs --config .work/conf datanode &
nohup hdfs --config .work/conf secondarynamenode &
```

如果你想按官方脚本启动，可以在已经配置好免密 SSH 后运行：

```bash
START_MODE=start-dfs bash bin/03-start.sh
```

## 常见问题

### `JAVA_HOME is not set`

脚本会尽量自动检测 `JAVA_HOME`。如果失败，手动设置：

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

Linux 上可以设置为你的 JDK 目录，例如：

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### DataNode 没起来

最常见原因是之前格式化过 NameNode，但 DataNode 里还留着旧 cluster ID。

课堂环境里可以直接清理后重来：

```bash
bash bin/05-stop.sh
bash bin/99-clean.sh
bash bin/01-configure.sh
bash bin/02-format.sh
bash bin/03-start.sh
```

### 端口冲突

默认使用：

- RPC: `9000`
- NameNode HTTP: `9870`
- DataNode HTTP: `9864`
- SecondaryNameNode HTTP: `9868`

如果端口被占用，修改 `conf/hdfs-site.xml.template` 和 `conf/core-site.xml.template` 后重新运行：

```bash
bash bin/01-configure.sh
```
