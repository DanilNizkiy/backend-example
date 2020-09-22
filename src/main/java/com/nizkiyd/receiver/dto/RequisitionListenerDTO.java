package com.nizkiyd.receiver.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequisitionListenerDTO implements Serializable {

    private UUID id;

    private UUID additionalId;

    private UUID clientId;

    private UUID ticketId;

    private String routeNumber;

    private LocalDateTime departure;
}
