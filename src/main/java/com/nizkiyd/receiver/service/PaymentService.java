package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Payment;
import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
import com.nizkiyd.receiver.repository.PaymentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public PaymentStatus paymentProcess(PaymentCreateDTO paymentCreateDTO) {
        List<PaymentStatus> allStatuses = Arrays.asList(
                PaymentStatus.APPROVED, PaymentStatus.CANCELED, PaymentStatus.PROCESSING);
        PaymentStatus randomStatus = allStatuses.get((int) (Math.random() * 3));

        Payment payment = new Payment();
        payment.setRequisitionId(paymentCreateDTO.getRequisitionId());
        payment.setAmount(paymentCreateDTO.getAmount());
        payment.setStatus(randomStatus);
        paymentRepository.save(payment);
        log.info(String.format("Payment created with ID %s and status %s",
                payment.getId(), payment.getStatus().toString()));
        return randomStatus;
    }

    public PaymentStatus getPaymentStatusByRequisitionId(UUID requisitionId) {
        Payment payment = paymentRepository.findByRequisitionId(requisitionId).orElseThrow(() -> {
            throw new EntityNotFoundException(String.format("Payment with requisition ID %s not found", requisitionId));
        });
        log.info(String.format("Status %s for requisition %s successfully received", payment.getStatus(), payment.getId()));
        return payment.getStatus();
    }
}
