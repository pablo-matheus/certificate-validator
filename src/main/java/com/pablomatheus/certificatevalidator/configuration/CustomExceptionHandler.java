package com.pablomatheus.certificatevalidator.configuration;

import com.pablomatheus.certificatevalidator.domain.builder.ErrorResponseBuilder;
import com.pablomatheus.certificatevalidator.domain.exception.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(BusinessException exception, WebRequest request) {
        return handleExceptionInternal(
                exception, ErrorResponseBuilder.build(exception), new HttpHeaders(), exception.getHttpStatus(), request);
    }

}
