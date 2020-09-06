package com.nizkiyd.receiver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateRequisitionException extends RuntimeException {

    public DuplicateRequisitionException(UUID ticketId) {
        super(String.format("An Requisition for payment for ticket %s has already been created", ticketId));
    }
}
