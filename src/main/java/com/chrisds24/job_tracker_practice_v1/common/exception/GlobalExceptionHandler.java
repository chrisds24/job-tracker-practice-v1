package com.chrisds24.job_tracker_practice_v1.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.chrisds24.job_tracker_practice_v1.job.exception.InvalidSalaryRangeException;
import com.chrisds24.job_tracker_practice_v1.job.exception.JobNotFoundException;
import com.chrisds24.job_tracker_practice_v1.member.exception.EmailAlreadyExistsException;
import com.chrisds24.job_tracker_practice_v1.member.SignupResponseDto;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    // What type of exceptions do I need to handle?
    // - Exceptions from services that I throw myself (JobNotFoundException)
    // - Validation exceptions
    // - Exceptions from services that I expect but are thrown by Spring,
    //   Spring Data JPA, Hibernate, etc. instead of by myself
    // - Exceptions that I don't expect (could be thrown by Spring, Hibernate,
    //   etc.)
    
    @ExceptionHandler(JobNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handlejobNotFound(
        JobNotFoundException ex
    ) {
        return new ApiErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "JOB_NOT_FOUND",
            ex.getMessage()
        );
    }

    // 422 Unprocessable Content is for business-rule validation failure
    // Meanwhile, 400 is for malformed/invalid requests
    // However, 400 can be used for 422 cases and is common to do
    @ExceptionHandler(InvalidSalaryRangeException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public ApiErrorResponse handleInvalidSalaryRange(
        InvalidSalaryRangeException ex
    ) {
        return new ApiErrorResponse(
            HttpStatus.UNPROCESSABLE_CONTENT.value(),
            "INVALID_SALARY_RANGE",
            ex.getMessage()
        );
    }

    // The normal status code for an "already exists" issue is to use
    //   a 409 Conflict
    // But for safety reasons, I'll just return a 202 with a message
    //   (SEE comment in signup method of the MemberController)
    // - This returns exactly the same response body and status code as a
    //   successful signup
    //
    @ExceptionHandler(EmailAlreadyExistsException.class)
    // @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseStatus(HttpStatus.ACCEPTED)
    // public ApiErrorResponse handleEmailAlreadyExists(
    public SignupResponseDto handleEmailAlreadyExists(
        EmailAlreadyExistsException ex
    ) {
        // Instead of ex.getMessage() being sent as part of an
        //   ApiErrorResponse, I can just log it.

        return new SignupResponseDto(
            "If this email can be registered, you'll receive further instructions."
        );
    }

    // IMPORTANT: Notice how I'm not using ex.getMessage() here since it
    //   exposes a lot of Spring-related details. ex.getMessage() is useful
    //   for custom messages I've passed when creating the exception
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleMethodArgumentNotValid(
        MethodArgumentNotValidException ex
    ) {
        return new ApiErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "REQUEST_VALIDATION_FAILED",
            "Request validation failed"
        );
    }

    // Catch Exception at the final fallback handler, not RuntimeException
    // ABSOLUTELY DO NOT return ex.getMessage() as the message here! It's
    //   a gigantic security risk!
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnexpectedException(
        Exception ex
    ) {
        return new ApiErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "Unexpected error"
        );
    }
}
