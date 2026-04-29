package com.example.batch;

import com.example.batch.model.Person;
import com.example.batch.model.PersonReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.MultiResourcePartitioner;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class BatchPartitionConfig {

    private static final Logger log = LoggerFactory.getLogger(BatchPartitionConfig.class);

    // ── Partitioner: 按 CSV 文件拆分分区 ──────────────────────────────

    @Bean
    public Partitioner partitioner() throws IOException {
        MultiResourcePartitioner partitioner = new MultiResourcePartitioner();
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:data-*.csv");
        log.info("Partitioner found {} files", resources.length);
        partitioner.setResources(resources);
        partitioner.setKeyName("file");
        return partitioner;
    }

    // ── Slave Step 的组件 ─────────────────────────────────────────────

    // Data Source: 每个分区从各自的 CSV 文件读取（@StepScope 实现延迟绑定）
    @Bean
    @StepScope
    public FlatFileItemReader<Person> reader(
            @Value("#{stepExecutionContext['file']}") Resource resource) {
        log.info("[{}] Opening partition file: {}",
                Thread.currentThread().getName(), resource.getFilename());
        return new FlatFileItemReaderBuilder<Person>()
                .name("partitionedPersonReader")
                .resource(resource)
                .delimited()
                .names("firstName", "lastName", "department", "salary")
                .targetType(Person.class)
                .build();
    }

    // Filter: 转换 Person → PersonReport，计算奖金
    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    // Data Sink: 将 PersonReport 批量写入数据库
    @Bean
    public JdbcBatchItemWriter<PersonReport> dbWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PersonReport>()
                .sql("INSERT INTO person_report (full_name, department, salary, bonus) " +
                     "VALUES (:fullName, :department, :salary, :bonus)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    // 计数包装：写入数据库的同时统计行数（用于 benchmark）
    private final AtomicInteger writeCount = new AtomicInteger();

    @Bean
    public ItemWriter<PersonReport> writer(JdbcBatchItemWriter<PersonReport> dbWriter) {
        return items -> {
            dbWriter.write(items);
            writeCount.addAndGet(items.size());
        };
    }

    public int getWriteCount() {
        return writeCount.get();
    }

    public void resetWriteCount() {
        writeCount.set(0);
    }

    // ── TaskExecutor: 并行执行分区 ───────────────────────────────────

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("partition-");
    }

    // ── Slave Step: 每个分区独立运行的 Pipe-Filter 流水线 ─────────────

    @Bean
    public Step slaveStep(JobRepository jobRepository,
                          DataSourceTransactionManager transactionManager,
                          FlatFileItemReader<Person> reader,
                          PersonItemProcessor processor,
                          ItemWriter<PersonReport> writer) {
        return new StepBuilder("slaveStep", jobRepository)
                .<Person, PersonReport>chunk(500, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // ── Master Step: 分区调度器，将工作分发给多个 Slave Step ──────────

    @Bean
    public Step masterStep(JobRepository jobRepository,
                           Partitioner partitioner,
                           Step slaveStep,
                           TaskExecutor taskExecutor) {
        return new StepBuilder("masterStep", jobRepository)
                .partitioner("slaveStep", partitioner)
                .step(slaveStep)
                .gridSize(3)
                .taskExecutor(taskExecutor)
                .build();
    }

    // ── Job: 整条并行流水线的入口 ────────────────────────────────────

    @Bean
    public Job partitionJob(JobRepository jobRepository, Step masterStep) {
        return new JobBuilder("partitionJob", jobRepository)
                .start(masterStep)
                .build();
    }

    // ── 单线程 Job（对照组）────────────────────────────────────────────

    @Bean
    public FlatFileItemReader<Person> singleFileReader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("singleFileReader")
                .delimited()
                .names("firstName", "lastName", "department", "salary")
                .targetType(Person.class)
                .build();
    }

    @Bean
    public MultiResourceItemReader<Person> multiReader() throws IOException {
        Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:data-*.csv");
        MultiResourceItemReader<Person> reader = new MultiResourceItemReader<>();
        reader.setResources(resources);
        reader.setDelegate(singleFileReader());
        return reader;
    }

    @Bean
    public Step singleThreadStep(JobRepository jobRepository,
                                 DataSourceTransactionManager transactionManager,
                                 MultiResourceItemReader<Person> multiReader,
                                 PersonItemProcessor processor,
                                 ItemWriter<PersonReport> writer) {
        return new StepBuilder("singleThreadStep", jobRepository)
                .<Person, PersonReport>chunk(500, transactionManager)
                .reader(multiReader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job singleThreadJob(JobRepository jobRepository, Step singleThreadStep) {
        return new JobBuilder("singleThreadJob", jobRepository)
                .start(singleThreadStep)
                .build();
    }
}
