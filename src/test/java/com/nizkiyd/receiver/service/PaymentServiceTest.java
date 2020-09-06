package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Payment;
import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
import com.nizkiyd.receiver.repository.PaymentRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from requisition", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    public void testPaymentProcessCreatePayment() {
        UUID requisitionId = UUID.randomUUID();

        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setRequisitionId(requisitionId);
        paymentCreateDTO.setAmount(123);

        PaymentStatus paymentStatus = paymentService.paymentProcess(paymentCreateDTO);
        Assert.assertNotNull(paymentStatus);

        Payment payment = paymentRepository.findByRequisitionId(requisitionId).get();
        Assertions.assertThat(paymentCreateDTO).isEqualToComparingFieldByField(payment);
    }

    @Test
    public void testPaymentProcessRandomStatus() {
        PaymentCreateDTO r1 = createPaymentCreateDTO();
        PaymentCreateDTO r2 = createPaymentCreateDTO();
        PaymentCreateDTO r3 = createPaymentCreateDTO();
        PaymentCreateDTO r4 = createPaymentCreateDTO();

        PaymentStatus statusForR1 = paymentService.paymentProcess(r1);
        PaymentStatus statusForR2 = paymentService.paymentProcess(r2);
        PaymentStatus statusForR3 = paymentService.paymentProcess(r3);
        PaymentStatus statusForR4 = paymentService.paymentProcess(r4);

        assertTrue(Stream.of(statusForR1, statusForR2, statusForR3, statusForR4)
                .anyMatch(i -> i != PaymentStatus.PROCESSING));
    }

    @Test
    public void testGetPaymentStatusByRequisitionId() {
        UUID requisitionId = UUID.randomUUID();

        Payment payment = new Payment();
        payment.setRequisitionId(requisitionId);
        payment.setAmount(123);
        payment.setStatus(PaymentStatus.APPROVED);
        paymentRepository.save(payment);

        PaymentStatus paymentStatus = paymentService.getPaymentStatusByRequisitionId(requisitionId);
        Assert.assertEquals(payment.getStatus(), paymentStatus);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetPaymentStatusByRequisitionIdWrongId() {
        paymentService.getPaymentStatusByRequisitionId(UUID.randomUUID());
    }

    private PaymentCreateDTO createPaymentCreateDTO() {
        PaymentCreateDTO createDTO = new PaymentCreateDTO();
        createDTO.setRequisitionId(UUID.randomUUID());
        createDTO.setAmount((int) (Math.random() * 1000));
        return createDTO;
    }
}