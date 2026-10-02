package com.chrisds24.job_tracker_practice_v1.job.exception;

public class JobNotFoundException extends RuntimeException {
    public JobNotFoundException(String message) {
        super(message);
    }

    // This is not needed, since JobNotFoundException isn't wrapping
    //   another exception that caused me to throw JobNotFoundException
    // Here, Throwable allows us to preserve the original exception as the
    //   cause
    public JobNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
