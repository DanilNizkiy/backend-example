package com.nizkiyd.receiver.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PaymentCreateDTO {

    private UUID requisitionId;

    private Integer amount;
}
