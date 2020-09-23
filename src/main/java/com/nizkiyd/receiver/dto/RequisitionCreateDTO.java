package com.nizkiyd.receiver.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequisitionCreateDTO {

    private UUID clientId;

    private UUID ticketId;

    private String routeNumber;

    private String departure;
}
