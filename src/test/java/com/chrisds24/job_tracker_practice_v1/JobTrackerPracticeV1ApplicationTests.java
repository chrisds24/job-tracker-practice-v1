package com.chrisds24.job_tracker_practice_v1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JobTrackerPracticeV1ApplicationTests {

	@Test
	void contextLoads() {
	}

}
