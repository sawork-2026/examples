package com.example.batch;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@SpringBatchTest
class BenchmarkTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier("singleThreadJob")
    private Job singleThreadJob;

    @Autowired
    @Qualifier("partitionJob")
    private Job partitionJob;

    @Autowired
    private BatchPartitionConfig config;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private void reset() {
        jdbcTemplate.execute("TRUNCATE TABLE person_report");
        config.resetWriteCount();
    }

    @Test
    void benchmark() throws Exception {
        // Warm up
        jobLauncherTestUtils.setJob(singleThreadJob);
        jobLauncherTestUtils.launchJob();
        reset();

        // Single-thread Job
        long t0 = System.currentTimeMillis();
        jobLauncherTestUtils.setJob(singleThreadJob);
        JobExecution single = jobLauncherTestUtils.launchJob();
        long singleMs = System.currentTimeMillis() - t0;
        int singleCount = config.getWriteCount();
        reset();

        // Partition Job
        long t1 = System.currentTimeMillis();
        jobLauncherTestUtils.setJob(partitionJob);
        JobExecution partition = jobLauncherTestUtils.launchJob();
        long partitionMs = System.currentTimeMillis() - t1;
        int partitionCount = config.getWriteCount();

        System.out.println();
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Benchmark: 100,000 rows (3 CSV files)");
        System.out.println("═══════════════════════════════════════════════");
        System.out.printf("  Single-thread : %,6d ms  (%s, %,d rows)%n",
                singleMs, single.getStatus(), singleCount);
        System.out.printf("  Partition (x3): %,6d ms  (%s, %,d rows)%n",
                partitionMs, partition.getStatus(), partitionCount);
        System.out.printf("  Speedup       : %.2fx%n",
                (double) singleMs / partitionMs);
        System.out.println("═══════════════════════════════════════════════");
        System.out.println();
    }
}
