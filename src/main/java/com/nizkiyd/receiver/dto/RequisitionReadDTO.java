package com.nizkiyd.receiver.dto;

import com.nizkiyd.receiver.domain.RequisitionStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RequisitionReadDTO {

    private UUID id;

    private UUID clientId;

    private UUID ticketId;

    private Integer cost;

    private LocalDateTime departure;

    private String routeNumber;

    private RequisitionStatus status;
}
