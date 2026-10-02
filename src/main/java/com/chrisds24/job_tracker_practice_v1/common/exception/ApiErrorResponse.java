package com.chrisds24.job_tracker_practice_v1.common.exception;

record ApiErrorResponse(
    int status,
    String code,
    String message
) {}
