package com.nizkiyd.receiver.exception.hander;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> handlerException(Exception ex) {
        ResponseStatus status = AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        HttpStatus httpStatus = status != null ? status.code() : HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorInfo errorInfo = new ErrorInfo(httpStatus, ex.getClass(), ex.getMessage());
        return new ResponseEntity<>(errorInfo, new HttpHeaders(), httpStatus);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                                      WebRequest request) {
        ErrorInfo errorInfo = new ErrorInfo(BAD_REQUEST, ex.getClass(), String.format(
                "The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName()));

        return new ResponseEntity<>(errorInfo, new HttpHeaders(), BAD_REQUEST);
    }
}
