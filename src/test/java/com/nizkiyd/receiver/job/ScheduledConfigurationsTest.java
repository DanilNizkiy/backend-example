package com.nizkiyd.receiver.job;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Import(ScheduledConfigurationsTest.ScheduledTestConfig.class)
public class ScheduledConfigurationsTest {

    @Test
    public void testSpringContextUpAndRunning() {
        log.info("@Scheduled configurations are OK");
    }

    @EnableScheduling
    static class ScheduledTestConfig{
    }
}
