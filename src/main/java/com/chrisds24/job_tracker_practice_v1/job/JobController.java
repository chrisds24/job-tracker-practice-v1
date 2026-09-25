package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


// @RestController: This class handles HTTP requests and returns a response body
// - This annotation is the same as adding both @Controller and @ResponseBody
// @RequestMapping("/jobs") means that /jobs is the API endpoint for this
//   controller
// - Both annotations come from Spring MVC
// - ***** It's convention to use plural nouns for API endpoints unless the
//   resource can only be singular, which is why we use /jobs instead of /job
//   unlike in a database table (which prefers singular names)
@RestController
@RequestMapping("/job")
public class JobController {

    private final JobService jobService;
    
    // @Autowired here is constructor injection
    // - Spring sees that the constructor requires a JobService, so it finds a
    //   JobService bean in its application context then passes that it into
    //   the constructor when creating the JobController
    // - This isn't actually needed, since Spring automatically does this, but
    //   I'm putting the annotation just to be more explicit
    // - I've read that constructor injection is preferred over field
    //   injection (where we have @Autowired on the field) for various
    //   reasons (don't remember what they are exactly)
    public JobController(@Autowired JobService jobService) {
        this.jobService = jobService;
    }

    // @GetMapping: This is a GET request
    //
    // @RequestParam(name = "userId", required = false)
    // - By default, @RequestParam has required = true, so we're setting it to
    //   false since we don't want to require it
    // - name is simply just the name of the query param in the URL
    //
    // The dateSavedFrom and dateSavedTo query params receive a UTC timestamp
    //   as an ISO-8601 string. @DateTimeFormat tells Spring how to parse the
    //   ISO-8601 string
    // - Alternative: Have the types be String, then manually Instant.parse()
    //
    // ResponseEntity lets the controller explicitly control the HTTP status,
    //   headers, and response body
    @GetMapping()
    public ResponseEntity<List<JobResponseDto>> getMultiple(
        @RequestParam(name = "userId", required = false) UUID userId,
        @RequestParam(name = "title", required = false) String title,
        @RequestParam(name = "company", required = false) String company,
        @RequestParam(name = "dateSavedFrom", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dateSavedFrom,
        @RequestParam(name = "dateSavedTo", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dateSavedTo
    ) {
        // Builder-style
        // - Preferred since it's more readable
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jobService.getMultipleByUser(userId));

        // Constructor-style
        // return new ResponseEntity(
        //     jobService.getMultipleByUser(userId),
        //     200
        // );
    }


}

// ********* NOTES **********
// Name the file JobController instead of just Controller even if the
// folder structure is src/main/java/com/chrisds24/JobController.java
// - It makes it clearer when working with the class:
//      private final JobService jobService; // Clear
//      private final Service service; // Unclear
// - Also, notice what happens when we import two different services:
//      // Can't have this
//      import com.chrisds24.jobtracker.job.Service;
//      import com.chrisds24.jobtracker.member.Service;
// 
// Why constructor injection over field injection:
// - Constructor injection makes the dependency required and the controller
//   can't be constructed without providing a JobService
// - It's also easier to unit test, such as creating a JobController with a
//   mock JobService
//
// Should /jobs endpoint only have one method when getting multiple jobs?
// - Ex. Just have getMultiple instead of getMultipleByUser,
//   getMultipleByStatus, etc.?
//   -- YES !!! There would be so many methods otherwise
//   -- Also, you can't have different methods for the same endpoint where
//      they only differ by @RequestParam / query parameters
//
// How about service methods? Should there be different ones depending on
//   the filter applied? Ex. getMultipleByUser, getMultipleByStatus, etc.
// - Once again, NO !!!
// - Just use one with cleanly implemented conditionals where appropriate
//
// Should GET /jobs only return current user's jobs since normal users should
//   only be able to access their own jobs?
// - After all, we should be getting the userId to filter by that user's jobs
//   from the authenticated user instead of @RequestParam("userId") UUID userId
//   since a malicious user can just edit the userId in the URL
//   -- We can get it from the authenticated user via:
//      + OPTION 1: As a manually set request attribute in a Filter upon
//        successful authentication, which we can then access in the controller
//        later
//      + OPTION 2: From the authenticated principal when using Spring Security
//   -- Even if we can just compare the query param userId to the one obtained
//      from the authenticated user to deal with malicious requests, having a
//      userId query param is redundant since our source of truth is still just
//      the authenticated user
// - Then, we can just have filters (Ex. by status, sort order, by city, etc.)
//   as the query params
// - ***** HOWEVER, there's one potential problem: What if we have an admin app
//   that should be able to access all users jobs (setting aside the ethical
//   issues of doing so for now)? Shouldn't GET /jobs then be able return jobs
//   not limited to the current user?
//   -- ***** SOLUTIONS:
//      -- OPTION 1: Just have a /admin/jobs endpoint
//      -- OPTION 2: Just have /jobs, but have different codepaths execute
//         depending on if the user is a normal user or has admin authorization
//         + Normal users can only access their jobs and the request could be
//           rejected if they supply a userId query param
//         + Meanwhile, admins aren't limited and can supply a userId
//      -- Both options are RESTful
//    

