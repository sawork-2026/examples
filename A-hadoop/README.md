# A-hadoop: 最小 HDFS 实验

这个目录用于配合 `A-Hadoop.md`：在本机启动一个最小的 pseudo-distributed HDFS，看见真实的 `NameNode`、`DataNode` 和 HDFS shell 操作。

默认脚本使用 Apache Hadoop 3.5.0。Apache 官网在 2026-04-02 发布 Hadoop 3.5.0，并称其为 3.5 line 的第一个 stable release。版本可以通过环境变量覆盖。

## 前置条件

- macOS 或 Linux
- JDK
- `curl`
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
bash bin/06-wordcount.sh
```

这些脚本按顺序对应：

- `00-download.sh`: 下载并解压 Hadoop。
- `01-configure.sh`: 生成本机运行用的 Hadoop 配置。
- `02-format.sh`: 第一次启动前格式化 NameNode。
- `03-start.sh`: 直接启动 NameNode、DataNode、SecondaryNameNode，并等待 HDFS 可以写入。
- `04-demo.sh`: 上传和读取一个 CSV 文件，验证 HDFS shell。
- `06-wordcount.sh`: 运行 Hadoop 自带 WordCount 示例。
- `05-stop.sh`: 停止本例启动的 HDFS 进程。

WordCount 示例源码已经放在本目录，便于课堂上直接打开阅读：

```text
src/main/java/org/apache/hadoop/examples/WordCount.java
```

打开 NameNode UI：

```text
http://localhost:9870
```

也可以手工查看 HDFS 目录：

```bash
/tmp/software-architecture-explained-with-spring-A-hadoop/hadoop-3.5.0/bin/hdfs \
  --config /tmp/software-architecture-explained-with-spring-A-hadoop/conf \
  dfs -ls "/user/$USER/"
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

## 手工安装和运行 Hadoop

上面的脚本只是把这一节的命令自动化。想看清 Hadoop 伪分布式模式到底做了什么，可以按下面步骤手工执行。

### 1. 准备目录和环境变量

```bash
cd examples/A-hadoop

export HADOOP_VERSION=3.5.0
export WORK_DIR=/tmp/software-architecture-explained-with-spring-A-hadoop
export HADOOP_HOME="$WORK_DIR/hadoop-$HADOOP_VERSION"
export HADOOP_CONF_DIR="$WORK_DIR/conf"
export HADOOP_LOG_DIR="$WORK_DIR/run/logs"
export HADOOP_PID_DIR="$WORK_DIR/run/pids"
export PATH="$HADOOP_HOME/bin:$PATH"

mkdir -p "$WORK_DIR/cache" "$HADOOP_CONF_DIR" "$HADOOP_LOG_DIR" "$HADOOP_PID_DIR"
```

设置 `JAVA_HOME`。macOS 可以这样：

```bash
export JAVA_HOME=$(/usr/libexec/java_home)
```

Linux 按实际 JDK 路径设置，例如：

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
```

### 2. 下载并解压 Hadoop

Apple Silicon / ARM64 机器使用 aarch64 包：

```bash
curl -fL \
  "https://dlcdn.apache.org/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION-aarch64.tar.gz" \
  -o "$WORK_DIR/cache/hadoop-$HADOOP_VERSION-aarch64.tar.gz"

tar -xzf "$WORK_DIR/cache/hadoop-$HADOOP_VERSION-aarch64.tar.gz" -C "$WORK_DIR"
mv "$WORK_DIR/hadoop-$HADOOP_VERSION-aarch64" "$HADOOP_HOME"
```

Intel macOS 或 Linux 通常使用普通包：

```bash
curl -fL \
  "https://dlcdn.apache.org/hadoop/common/hadoop-$HADOOP_VERSION/hadoop-$HADOOP_VERSION.tar.gz" \
  -o "$WORK_DIR/cache/hadoop-$HADOOP_VERSION.tar.gz"

tar -xzf "$WORK_DIR/cache/hadoop-$HADOOP_VERSION.tar.gz" -C "$WORK_DIR"
```

如果 Apache 主站下载慢，可以把 URL 换成镜像源，例如：

```text
https://mirrors.tuna.tsinghua.edu.cn/apache/hadoop/common/hadoop-3.5.0/...
```

检查版本：

```bash
hdfs version
```

### 3. 写 Hadoop 配置

创建运行目录：

```bash
mkdir -p "$WORK_DIR/run/hdfs/name" "$WORK_DIR/run/hdfs/data" "$WORK_DIR/run/tmp"
```

写 `core-site.xml`：

```bash
cat > "$HADOOP_CONF_DIR/core-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <property>
    <name>fs.defaultFS</name>
    <value>hdfs://127.0.0.1:9000</value>
  </property>

  <property>
    <name>hadoop.tmp.dir</name>
    <value>file://$WORK_DIR/run/tmp</value>
  </property>
</configuration>
EOF
```

写 `hdfs-site.xml`：

```bash
cat > "$HADOOP_CONF_DIR/hdfs-site.xml" <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <property>
    <name>dfs.replication</name>
    <value>1</value>
  </property>

  <property>
    <name>dfs.namenode.name.dir</name>
    <value>file://$WORK_DIR/run/hdfs/name</value>
  </property>

  <property>
    <name>dfs.datanode.data.dir</name>
    <value>file://$WORK_DIR/run/hdfs/data</value>
  </property>

  <property>
    <name>dfs.permissions.enabled</name>
    <value>false</value>
  </property>

  <property>
    <name>dfs.namenode.rpc-address</name>
    <value>127.0.0.1:9000</value>
  </property>

  <property>
    <name>dfs.namenode.http-address</name>
    <value>127.0.0.1:9870</value>
  </property>

  <property>
    <name>dfs.namenode.secondary.http-address</name>
    <value>127.0.0.1:9868</value>
  </property>

  <property>
    <name>dfs.datanode.address</name>
    <value>127.0.0.1:9866</value>
  </property>

  <property>
    <name>dfs.datanode.http.address</name>
    <value>127.0.0.1:9864</value>
  </property>

  <property>
    <name>dfs.datanode.ipc.address</name>
    <value>127.0.0.1:9867</value>
  </property>
</configuration>
EOF
```

写 `hadoop-env.sh`：

```bash
cat > "$HADOOP_CONF_DIR/hadoop-env.sh" <<EOF
export JAVA_HOME=$JAVA_HOME
export HADOOP_HOME=$HADOOP_HOME
export HADOOP_LOG_DIR=$HADOOP_LOG_DIR
export HADOOP_PID_DIR=$HADOOP_PID_DIR
export HADOOP_OPTS="-Djava.net.preferIPv4Stack=true"
EOF
```

补上 workers 和日志配置：

```bash
echo localhost > "$HADOOP_CONF_DIR/workers"
cp "$HADOOP_HOME/etc/hadoop/log4j.properties" "$HADOOP_CONF_DIR/log4j.properties"
```

### 4. 格式化 NameNode

第一次启动前必须格式化 NameNode：

```bash
hdfs --config "$HADOOP_CONF_DIR" namenode -format -nonInteractive
```

格式化会初始化：

```text
$WORK_DIR/run/hdfs/name/current
```

如果要重新格式化课堂环境，先停止 HDFS，再删除 `$WORK_DIR/run/hdfs/name` 和 `$WORK_DIR/run/hdfs/data`。

### 5. 启动 HDFS

为了不依赖免密 `ssh localhost`，可以直接启动三个 daemon：

```bash
nohup hdfs --config "$HADOOP_CONF_DIR" namenode > "$HADOOP_LOG_DIR/namenode.out" 2>&1 &
echo $! > "$HADOOP_PID_DIR/namenode.pid"

nohup hdfs --config "$HADOOP_CONF_DIR" datanode > "$HADOOP_LOG_DIR/datanode.out" 2>&1 &
echo $! > "$HADOOP_PID_DIR/datanode.pid"

nohup hdfs --config "$HADOOP_CONF_DIR" secondarynamenode > "$HADOOP_LOG_DIR/secondarynamenode.out" 2>&1 &
echo $! > "$HADOOP_PID_DIR/secondarynamenode.pid"
```

检查进程：

```bash
jps
```

应该能看到类似下面这些 Hadoop 进程；如果还有 IDE 或其他工具的 Java 进程，可以忽略：

```text
NameNode
DataNode
SecondaryNameNode
Jps
```

打开 Web UI：

```text
http://localhost:9870
```

刚启动后 NameNode 可能会短暂处于 safe mode。等下面命令输出 `Safe mode is OFF` 后再写入文件：

```bash
hdfs --config "$HADOOP_CONF_DIR" dfsadmin -safemode get
```

### 6. 手工验证 HDFS shell

准备一个本地文件：

```bash
mkdir -p "$WORK_DIR/run/demo"
cat > "$WORK_DIR/run/demo/orders.csv" <<EOF
order_id,user_id,total
1001,u-001,42.50
1002,u-002,18.00
1003,u-001,128.90
EOF
```

上传到 HDFS 并读取：

```bash
hdfs --config "$HADOOP_CONF_DIR" dfs -mkdir -p "/user/$USER/input"
hdfs --config "$HADOOP_CONF_DIR" dfs -put -f "$WORK_DIR/run/demo/orders.csv" "/user/$USER/input/orders.csv"
hdfs --config "$HADOOP_CONF_DIR" dfs -ls "/user/$USER/input"
hdfs --config "$HADOOP_CONF_DIR" dfs -cat "/user/$USER/input/orders.csv"
```

查看 block 和位置：

```bash
hdfs --config "$HADOOP_CONF_DIR" fsck "/user/$USER/input/orders.csv" -files -blocks -locations
```

看到 `Status: HEALTHY` 和 `Live_repl=1`，说明这个最小 HDFS 已经能工作。

### 7. 手工运行 WordCount

准备输入：

```bash
mkdir -p "$WORK_DIR/run/wordcount"
cat > "$WORK_DIR/run/wordcount/input.txt" <<EOF
hadoop hdfs hadoop
architecture hdfs block
block replica namenode datanode
hadoop architecture
EOF
```

上传输入并清理旧输出：

```bash
hdfs --config "$HADOOP_CONF_DIR" dfs -mkdir -p "/user/$USER/wordcount/input"
hdfs --config "$HADOOP_CONF_DIR" dfs -put -f "$WORK_DIR/run/wordcount/input.txt" "/user/$USER/wordcount/input/input.txt"
hdfs --config "$HADOOP_CONF_DIR" dfs -rm -r -f "/user/$USER/wordcount/output"
```

运行 Hadoop 自带 WordCount：

```bash
hadoop --config "$HADOOP_CONF_DIR" jar \
  "$HADOOP_HOME/share/hadoop/mapreduce/hadoop-mapreduce-examples-$HADOOP_VERSION.jar" \
  wordcount \
  "/user/$USER/wordcount/input" \
  "/user/$USER/wordcount/output"
```

查看结果：

```bash
hdfs --config "$HADOOP_CONF_DIR" dfs -cat "/user/$USER/wordcount/output/part-r-00000"
```

期望输出：

```text
architecture	2
block	2
datanode	1
hadoop	3
hdfs	2
namenode	1
replica	1
```

### 8. 停止 HDFS

如果前面用 `nohup` 手工启动，可以按 PID 停止：

```bash
kill "$(cat "$HADOOP_PID_DIR/secondarynamenode.pid")" 2>/dev/null || true
kill "$(cat "$HADOOP_PID_DIR/datanode.pid")" 2>/dev/null || true
kill "$(cat "$HADOOP_PID_DIR/namenode.pid")" 2>/dev/null || true
```

再次确认：

```bash
jps
```

如果你用的是本目录脚本，也可以直接：

```bash
bash bin/05-stop.sh
```

## 目录结构

```text
examples/A-hadoop
├── bin/                 # 下载、配置、格式化、启动、演示、停止脚本
├── conf/                # HDFS 最小配置模板
```

默认运行目录在仓库外：

```text
/tmp/software-architecture-explained-with-spring-A-hadoop
```

它保存 Hadoop、渲染后的配置、NameNode/DataNode 数据和日志，可以随时删掉重来。放在 `/tmp` 是为了避免 VS Code、Slidev、Java language server 扫描仓库内的大量 Hadoop 文件。

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
- NameNode metadata 放在 `/tmp/software-architecture-explained-with-spring-A-hadoop/run/hdfs/name`
- DataNode blocks 放在 `/tmp/software-architecture-explained-with-spring-A-hadoop/run/hdfs/data`

## WordCount 验证

`bin/06-wordcount.sh` 使用 Hadoop 自带的 `hadoop-mapreduce-examples` jar 运行经典 WordCount：

```bash
bash bin/06-wordcount.sh
```

它会把输入文本上传到 HDFS：

```text
/user/$USER/wordcount/input/input.txt
```

然后把统计结果写到：

```text
/user/$USER/wordcount/output/part-r-00000
```

## 为什么不用 start-dfs.sh？

Apache 官方单机伪分布式教程使用 `start-dfs.sh`，它通常需要配置免密 `ssh localhost`。

为了课堂演示更少踩坑，本例默认不用 SSH，也不使用 Hadoop 的 daemon wrapper，而是直接用 `nohup` 管理三个进程：

```bash
nohup hdfs --config "$HADOOP_CONF_DIR" namenode &
nohup hdfs --config "$HADOOP_CONF_DIR" datanode &
nohup hdfs --config "$HADOOP_CONF_DIR" secondarynamenode &
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
