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
class BatchEtlJobTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private Job etlJob;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.execute("TRUNCATE TABLE person_report");
    }

    @Test
    void job_completesSuccessfully() throws Exception {
        jobLauncherTestUtils.setJob(etlJob);
        JobExecution execution = jobLauncherTestUtils.launchJob();
        assertThat(execution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
    }

    @Test
    void allRows_areWrittenToDatabase() throws Exception {
        jobLauncherTestUtils.setJob(etlJob);
        jobLauncherTestUtils.launchJob();

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM person_report", Integer.class);
        assertThat(count).isEqualTo(6);
    }

    @Test
    void engineeringEmployee_hasCorrectBonus() throws Exception {
        jobLauncherTestUtils.setJob(etlJob);
        jobLauncherTestUtils.launchJob();

        Integer bonus = jdbcTemplate.queryForObject(
                "SELECT bonus FROM person_report WHERE full_name = 'JILL DOE'",
                Integer.class);
        assertThat(bonus).isEqualTo(16000); // 80000 * 20%
    }
}
