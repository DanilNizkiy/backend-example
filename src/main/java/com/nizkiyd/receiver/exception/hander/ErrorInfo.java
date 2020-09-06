package com.nizkiyd.receiver.exception.hander;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorInfo {

    private HttpStatus status;
    private Class exceptionClass;
    private String message;
}
