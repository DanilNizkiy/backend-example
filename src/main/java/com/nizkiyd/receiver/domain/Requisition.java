package com.nizkiyd.receiver.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Entity
public class Requisition {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID clientId;

    private UUID ticketId;

    private Integer cost;

    private LocalDateTime departure;

    private String routeNumber;

    @Enumerated(EnumType.STRING)
    private RequisitionStatus status;
}
