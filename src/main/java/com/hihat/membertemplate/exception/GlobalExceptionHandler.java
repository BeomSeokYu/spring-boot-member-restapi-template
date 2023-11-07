package com.hihat.membertemplate.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public void handleCustomException(HttpServletRequest request,
                                      HttpServletResponse response,
                                      CustomException ex) throws IOException {
        response.sendError(ex.getHttpStatus().value(), ex.getMessage());
    }

}
