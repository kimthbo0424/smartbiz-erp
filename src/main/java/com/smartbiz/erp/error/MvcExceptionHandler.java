package com.smartbiz.erp.error;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.smartbiz.erp.controller")
public class MvcExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleNotFound(EntityNotFoundException e) {
        return "error/404";
    }

    @ExceptionHandler(Exception.class)
    public String handle500(Exception e) {
        e.printStackTrace();
        return "error/500";
    }
}