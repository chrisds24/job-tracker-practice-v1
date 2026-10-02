package com.chrisds24.job_tracker_practice_v1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JobTrackerPracticeV1Application {

	public static void main(String[] args) {
		SpringApplication.run(JobTrackerPracticeV1Application.class, args);
	}

}

// Folder Organization
// - Package by layer
//     controller/
//     service/
//     repository/
//
// - Package by feature
//     job/
//     member/
//     auth/
//
// Package-by-layer is very easy to understand and is common in
//   tutorials/smaller Spring projects. Package-by-feature can become easier
//   to navigate as an application grows