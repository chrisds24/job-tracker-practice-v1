package com.chrisds24.job;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// NOTE: Name the file JobController instead of just Controller even if the
// folder structure is src/main/java/com/chrisds24/JobController.java
// - It makes it clearer when working with the class:
//      private final JobService jobService;
//      private final Service service; // Unclear
// - Also, notice what happens when we import two different services:
//      import com.chrisds24.jobtracker.job.JobService;
//      import com.chrisds24.jobtracker.member.MemberService;
@RestController
@RequestMapping("/job")
public class JobController {

    private final JobService jobService;
    
    public JobController(@Autowired JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping()
    public ResponseEntity<List<JobResponseDto>> getMultipleByUser(
        @RequestParam("userId") UUID userId
    ) {
        // id of user is in the query param
        return new ResponseEntity(
            jobService.getMultipleByUser(userId),
            200
        );
    }
}
