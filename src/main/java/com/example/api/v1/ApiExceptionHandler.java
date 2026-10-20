package com.example.api.v1;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(basePackages = "com.example.api.v1")
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = Logger.getLogger(ApiExceptionHandler.class.getName());

    @ExceptionHandler(DuplicateUsernameException.class)
    ResponseEntity<Object> duplicateUsername(DuplicateUsernameException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Username already taken");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> unexpected(Exception ex) {
        // Full detail goes to the server log only -- the response body never carries
        // ex.getMessage(), same reasoning as the format-adapter fix for a bad upload.
        logger.log(Level.SEVERE, "Unhandled exception from an api/v1 request", ex);
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong.");
        return ResponseEntity.internalServerError().body(problem);
    }
}
