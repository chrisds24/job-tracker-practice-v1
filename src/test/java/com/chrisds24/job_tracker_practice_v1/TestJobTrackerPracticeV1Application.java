package com.chrisds24.job_tracker_practice_v1;

import org.springframework.boot.SpringApplication;

public class TestJobTrackerPracticeV1Application {

	public static void main(String[] args) {
		SpringApplication.from(JobTrackerPracticeV1Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
