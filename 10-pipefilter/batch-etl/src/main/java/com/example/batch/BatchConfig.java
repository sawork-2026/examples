package com.example.batch;

import com.example.batch.model.Person;
import com.example.batch.model.PersonReport;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfig {

    // Data Source: 从 CSV 读取原始 Person 记录
    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
                .name("personItemReader")
                .resource(new ClassPathResource("sample-data.csv"))
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
    public JdbcBatchItemWriter<PersonReport> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<PersonReport>()
                .sql("INSERT INTO person_report (full_name, department, salary, bonus) " +
                     "VALUES (:fullName, :department, :salary, :bonus)")
                .dataSource(dataSource)
                .beanMapped()
                .build();
    }

    // Step = 一条完整的 Pipe-Filter 流水线
    @Bean
    public Step etlStep(JobRepository jobRepository,
                        DataSourceTransactionManager transactionManager,
                        FlatFileItemReader<Person> reader,
                        PersonItemProcessor processor,
                        JdbcBatchItemWriter<PersonReport> writer) {
        return new StepBuilder("etlStep", jobRepository)
                .<Person, PersonReport>chunk(3, transactionManager) // Pipe: 每次传递 3 条
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    // Job = 整条流水线的入口
    @Bean
    public Job etlJob(JobRepository jobRepository, Step etlStep) {
        return new JobBuilder("etlJob", jobRepository)
                .start(etlStep)
                .build();
    }
}
