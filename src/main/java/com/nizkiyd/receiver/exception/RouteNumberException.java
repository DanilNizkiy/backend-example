package com.nizkiyd.receiver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RouteNumberException extends RuntimeException {

    public RouteNumberException(UUID ticketId) {
        super("Route number not set for ticket with ID " + ticketId);
    }
}
