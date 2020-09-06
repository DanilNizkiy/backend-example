package com.nizkiyd.receiver.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@ToString
@Entity
public class Payment {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID requisitionId;

    private Integer amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}
