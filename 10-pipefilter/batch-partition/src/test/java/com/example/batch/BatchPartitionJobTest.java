package com.example.batch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
class BatchPartitionJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job partitionJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE person_report");
    }

    @Test
    void job_completesSuccessfully() throws Exception {
        jobLauncherTestUtils.setJob(partitionJob);
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void allPartitions_writeAllRowsToDatabase() throws Exception {
        jobLauncherTestUtils.setJob(partitionJob);
        jobLauncherTestUtils.launchJob();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM person_report", Integer.class);
        assertThat(count).isEqualTo(100_000);
    }
}
