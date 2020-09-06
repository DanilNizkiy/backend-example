package com.nizkiyd.receiver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
import com.nizkiyd.receiver.service.PaymentService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = PaymentController.class)
public class PaymentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    public void testPaymentProcess() throws Exception {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setRequisitionId(UUID.randomUUID());
        paymentCreateDTO.setAmount(123);

        PaymentStatus paymentStatus = PaymentStatus.APPROVED;

        Mockito.when(paymentService.paymentProcess(paymentCreateDTO)).thenReturn(paymentStatus);
        String resultJson = mvc.perform(post("/api/v1/payment-gateway/payment-process")
                .content(objectMapper.writeValueAsString(paymentCreateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PaymentStatus actualPaymentStatus = objectMapper.readValue(resultJson, PaymentStatus.class);
        Assert.assertEquals(paymentStatus, actualPaymentStatus);
    }

    @Test
    public void testGetPaymentStatusByRequisitionId() throws Exception {
        PaymentStatus paymentStatus = PaymentStatus.APPROVED;
        UUID requisitionId = UUID.randomUUID();

        Mockito.when(paymentService.getPaymentStatusByRequisitionId(requisitionId)).thenReturn(paymentStatus);

        String resultJson = mvc.perform(get(
                "/api/v1/payment-gateway/payment/requisitions/{requisitionId}", requisitionId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PaymentStatus actualPaymentStatus = objectMapper.readValue(resultJson, PaymentStatus.class);
        Assert.assertEquals(paymentStatus, actualPaymentStatus);
        Mockito.verify(paymentService).getPaymentStatusByRequisitionId(requisitionId);
    }

    @Test
    public void testGetPaymentStatusByRequisitionIdWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Requisition.class, wrongId);

        Mockito.when(paymentService.getPaymentStatusByRequisitionId(wrongId)).thenThrow(exception);

        String resultJson = mvc.perform(get(
                "/api/v1/payment-gateway/payment/requisitions/{requisitionId}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }
}