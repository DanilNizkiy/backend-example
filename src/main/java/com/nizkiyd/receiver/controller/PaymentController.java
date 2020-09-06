package com.nizkiyd.receiver.controller;

import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/payment-gateway")
public class PaymentController {

    @Autowired
    private PaymentService service;

    @PostMapping("/payment-process")
    public PaymentStatus paymentProcess(@RequestBody PaymentCreateDTO paymentCreateDTO) {
        log.info(String.format("Received a request to pay for the requisition with id %s",
                paymentCreateDTO.getRequisitionId()));
        return service.paymentProcess(paymentCreateDTO);
    }

    @GetMapping("/payment/requisitions/{requisitionId}")
    public PaymentStatus getPaymentStatusByRequisitionId(@PathVariable UUID requisitionId) {
        log.info(String.format("Request received to get payment status by requisition id %s", requisitionId));
        return service.getPaymentStatusByRequisitionId(requisitionId);
    }
}
