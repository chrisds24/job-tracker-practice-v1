package com.chrisds24.job_tracker_practice_v1.job;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


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
    // @RequestParam(name = "memberId", required = false)
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
    // - NOTE: .body() finishes constructing a ResponseEntity with a body
    //   -- Without it or .build(), we only have a builder with the
    //      configurations we specified
    //
    // NOTE: There's also @ModelAttribute which allows us to put all filters
    //   into one object.
    // - However, this binds request parameters, form data, and path variables
    //   to that object and there's no way to distinguish which one comes from
    //   which.
    // - If we care about making the source explicit, need to separate them at
    //   the controller (Ex. Make path variables into @PathVariable in the
    //   controller and leave)
    // - I went with @RequestParam since it makes it explicit in code that the
    //   params are query params and not path params, form data, etc.
    @GetMapping()
    public ResponseEntity<List<JobResponseDto>> getJobs(
        @RequestParam(name = "memberId", required = false) UUID memberId,

        @RequestParam(name = "title", required = false)
        @Size(max = 255)
        String title,

        @RequestParam(name = "company", required = false)
        @Size(max = 255)
        String company,

        @RequestParam(name = "dateSavedFrom", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dateSavedFrom,

        @RequestParam(name = "dateSavedTo", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        Instant dateSavedTo,

        @RequestParam(name = "status", required = false)
        String status,

        @RequestParam(name = "salaryMin", required = false)
        @PositiveOrZero
        Integer salaryMin,

        @RequestParam(name = "salaryMax", required = false)
        @PositiveOrZero
        Integer salaryMax
    ) {
        // Constructor-style
        // return new ResponseEntity(
        //     jobService.getMultipleByUser(memberId),
        //     200
        // );

        // Builder-style
        // - Preferred since it's more readable
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(jobService.getJobs(
                memberId,
                title,
                company,
                dateSavedFrom,
                dateSavedTo,
                status,
                salaryMin,
                salaryMax
            ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponseDto> getJob(
        @PathVariable UUID id
    ) {
        JobResponseDto job = jobService.getJob(id);

        // OPTION 1: Return a null body with 404 Not Found status if job is
        //   not found
        // return ResponseEntity
        //     .status((job != null) ? HttpStatus.OK : HttpStatus.NOT_FOUND)
        //     .body(job);

        // OPTION 2: Return a response without a body and has status 404 if job
        //   isn't found, or else return normally
        // NOTE: The controller method still works even if we don't have a
        //   response body
        if (job == null) {
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .build();
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(job);
        
        // OPTION 3: (TODO) Switch to this later !!!
        // - Have a @RestControllerAdvice that can return error values
        // - So here, we're making the case where the job isn't found an
        //   exception.
        //   -- Notice the contrast to finding multiple jobs, which
        //      returns an empty list if no jobs are found
    }

    // We want to return the created job since it has details such as
    //   the database generated id and dateSaved
    //
    // @Valid tells Spring to validate the object using the validation
    //   annotations on its fields.
    // - This validation happens before the controller method executes, so
    //   Spring can return a 400 Bad Request if validation fails
    //
    // ***** IMPORTANT:
    // - Notice the lack of a "if (job != null)" condition. Here, there
    //   typically would be some kind of error that happened if the job wasn't
    //   successfully created, so that error should throw an execption instead
    @PostMapping()
    public ResponseEntity<JobResponseDto> createJob(
        @Valid @RequestBody CreateJobRequestDto newJob
    ) {
        // NOTE: memberId should come from the authenticated user in the
        //   request, not sent by the client as part of the request body (which
        //   a client could easily fake)
        // - Using a fake id for now
        // - NOTE: I won't add the fakeMemberId for the other operations for
        //   for now since they can operate normally without adding a memberId,
        //   meanwhile this one needs it
        UUID fakeMemberId = UUID.fromString("1e76b303-5e77-463e-8bd6-c01ec8342164"); 

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(jobService.createJob(fakeMemberId, newJob));
    }

    // For simplicity, I won't be returning a response body since the frontend
    //   already has all the info it needs (it already has the updated info
    //   from what it sent).
    // - There's currently no lastUpdated column, which would be generated from
    //   the backend. If there was, I definitely should just return the whole
    //   updated job    
    //
    // There's no inherited repository method from JpaRepository to update a
    //   resource. We simply load the resource, then the flush automatically
    //   executes an UPDATE query that DOES NOT return how many entities are
    //   updated.
    // - If I wanted to return how many entities are updated, I'd need to use
    //   an explicit @Modifying query
    //
    // IMPORTANT: PUT vs PATCH
    // - PATCH means only editing an existing resource's fields
    // - PUT means replacing the full resource with something else, not just
    //   editing the existing resource's fields
    @PatchMapping("/{id}")
    public ResponseEntity<Void> editJob(
        @PathVariable UUID id,
        @Valid @RequestBody EditJobRequestDto editJob
    ) {
        // OPTION 1: Have editJob return number of entities updated then
        //   return a response with 404 status if no job was found
        // - NOTE: Need to use an explicit @Modifying query if I want to return
        //   the number of entities updated
        // int updatedJobsCount = jobService.editJob(id, editJob);
        // if (updatedJobsCount == 0) {
        //     return ResponseEntity
        //         .status(HttpStatus.NOT_FOUND)
        //         .build();
        // }

        // OPTION 2: Have the service method return null if the job can't be
        //   found. Then return the status code conditionally
        JobResponseDto job = jobService.editJob(id, editJob);
        return ResponseEntity
            .status((job != null) ? HttpStatus.NO_CONTENT: HttpStatus.NOT_FOUND)
            .build();

        // OPTION 3: Have a @RestController advice which can handle
        //   a JobNotFoundException, which I throw from the service method
        //   if the repository method findById returns no job
        // - If the update error happens during/after flush (Ex. the job
        //   gets deleted before this transaction commits), then Hibernate
        //   will throw an exception.
        //   -- For simplicity right now, I won't try to handle this myself
    }

    // NOTE: Do I return a 404 and inform the user if the job is already
    //   delete? OR is not doing so fine?
    // - Both designs are valid
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(
        @PathVariable UUID id
    ) {

        jobService.deleteJob(id);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
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
// - After all, we should be getting the memberId to filter by that user's jobs
//   from the authenticated user instead of @RequestParam("memberId") UUID memberId
//   since a malicious user can just edit the memberId in the URL
//   -- We can get it from the authenticated user via:
//      + OPTION 1: As a manually set request attribute in a Filter upon
//        successful authentication, which we can then access in the controller
//        later (via @RequestAttribute)
//      + OPTION 2: From the authenticated principal when using Spring Security
//   -- Even if we can just compare the query param memberId to the one obtained
//      from the authenticated user to deal with malicious requests, having a
//      memberId query param is redundant since our source of truth is still just
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
//           rejected if they supply a memberId query param
//         + Meanwhile, admins aren't limited and can supply a memberId
//      -- Both options are RESTful
//    
// Throw an exception or return an error status code with no body?
// - In the getJob (get one job) method above, I opted for just returning a
//   404 status with no response body
// - Other options:
//   -- OPTION 1: 404 status with a null response body
//   -- OPTION 2: Throw an exception, then a have a global exception handler
//      such as a @RestControllerAdvice handle returning the appropriate
//      response
// - ***** In software engineering, a widely accepted principle is that
//   exceptions should only be used for exceptional/unexpected circumstances
//   and not for standard control flow.
//   -- Which is the reason why I opted not to use OPTION 2, even though
//      ChatGPT suggested that it's common in production Spring apps
//   -- HOWEVER, look at the use case below where it's preferrable to return
//      an error value rather than just a 404 without a response body.
//      + Since Java doesn't have a union type like in TypeScript for the
//        controller method to use, we'll have to resort to either
//        ResponseEntity<?> or a global exception handler that could then
//        return an error type
//
// Return 404 with no body or an error value that contains status, error type,
//   and message?
// - It's generally preferrable to return an error type, which is where having a
//   @RestControllerAdvice comes in
//   -- Ex.
            // {
            //     "status": 404,
            //     "error": "Not Found",
            //     "message": "Job not found"
            // }
//      + NOTE: When doing this, also set the actual HTTP status code to 404
//   -- Otherwise the controller method could have an issue regarding what type
//      it should return: the ResponseEntity or the ErrorResponse
//      + Changing the type to ResponseEntity<?> actually works
//      + But in my opinion, wouldn't having ResponseEntity<?> be bad practice
//        since it doesn't clearly state that the method should only return a
//        ResponseEntity<JobResponseDto> or a ResponseEntity<ErrorResponse>?
//        * This is especially problematic since Java doesn't have a union type
//          like TypeScript does
//
// @PostMapping(
//     consumes = "application/json",
//     produces = "application/json"
// )
// - By convention, consumes and produces isn't needed for regular REST
//   endpoints unless the media type is significant to the endpoint
//   (Ex. an endpoint that consumes MediaType.MULTIPART_FORM_DATA_VALUE only
//   to restrict file uploads)
//
// @Valid tells Spring to validate the object using the validation
//   annotations on its fields.
// - This validation happens before the controller method executes, so
//   Spring can return a 400 Bad Request if validation fails
//   -- TODO: How will this work with a @RestControllerAdvice? Can I catch
//      this validation error and have my @RestControllerAdvice handle it?
//
// EDITING A JOB: Is there a point to finding the job first via a query, then
//   updating it via another query?
// - When working directly with the database via writing a PostgreSQL query
//   yourself without using JPA/HIbernate, there's no point in doing a SELECT
//   first, since an UPDATE ... WHERE id := jobId already finds the job for you
// - When using JPA/Hibernate without a JPQL batch update, Hibernate has to
//   load the job first into the persistence context (one query).
//   -- After that, you can edit the job entity in the persistence context,
//      in which the the actual UPDATE query would be performed during
//      flush (this is another query in addition to the first SELECT)
//   -- ***** Therefore, unless using you're a JPQL batch update, there's no
//      avoiding SELECTing first in Hibernate
// - When using a JPA/Hibernate JPQL batch update, it executes the query
//   directly on the database. There's also no need to SELECT first since
//   we can just use a WHERE just like normal PostgreSQL
//   -- NOTE: This causes the entity in the persistence context to be outdated
//      since the update happens directly on the database
// - ***** Also, it's conventional to just have the editResource (Ex. editJob)
//   service method do the existence check rather than first doing the check
//   by having the controller call a getResource service method and then
//   calling the editResource method after existence is confirmed
//   -- Also, consider the fact that just because the resource existed during
//      getResource doesn't mean it still exists during editResource
//
// @ModelAttribute vs. @RequestParam
// - @ModelAttribute allows us to put all filters into one object.
// - However, this binds request parameters, form data, and path variables
//   to that object and there's no way to distinguish which one comes from
//   which.
// - If we care about making the source explicit, need to separate them at
//   the controller (Ex. Make path variables into @PathVariable in the
//   controller and leave)

