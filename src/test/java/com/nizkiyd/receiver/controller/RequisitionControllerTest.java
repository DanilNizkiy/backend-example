package com.nizkiyd.receiver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
import com.nizkiyd.receiver.exception.hander.ErrorInfo;
import com.nizkiyd.receiver.service.Consumer;
import com.nizkiyd.receiver.service.RequisitionService;
import com.nizkiyd.receiver.service.TranslationService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.JsonAssert.when;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_VALUES;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = RequisitionController.class)
public class RequisitionControllerTest {

//    @Autowired
//    private MockMvc mvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private RequisitionService requisitionService;
//
//    @MockBean
//    private RabbitTemplate rabbitTemplate;
//
//    @MockBean
//    private TranslationService translationService;
//
//    @Test
//    public void testGetClientRequisitions() throws Exception {
//        UUID clientId = UUID.randomUUID();
//
//        RequisitionReadDTO r1 = createRequisitionReadDTO(clientId);
//        RequisitionReadDTO r2 = createRequisitionReadDTO(clientId);
//        RequisitionReadDTO r3 = createRequisitionReadDTO(clientId);
//
//        List<RequisitionReadDTO> clientRequisitionsReadDTO = Arrays.asList(r1, r2, r3);
//        Mockito.when(requisitionService.getClientRequisitions(clientId)).thenReturn(clientRequisitionsReadDTO);
//
//        String resultJson = mvc.perform(get("/api/v1/clients/{clientId}/requisitions", clientId))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        List<RequisitionReadDTO> actualClientRequisitionsReadDTO = objectMapper.readValue(
//                resultJson, new TypeReference<>() {
//                });
//        Assert.assertEquals(3, actualClientRequisitionsReadDTO.size());
//        Assertions.assertThat(actualClientRequisitionsReadDTO).extracting("id")
//                .containsExactlyInAnyOrder(r1.getId(), r2.getId(), r3.getId());
//
//        Mockito.verify(requisitionService).getClientRequisitions(clientId);
//    }
//
//    @Test
//    public void testGetRequisitionStatus() throws Exception {
//        RequisitionReadDTO readDTO = createRequisitionReadDTO();
//
//        Mockito.when(requisitionService.getRequisitionStatus(readDTO.getId())).thenReturn(readDTO.getStatus());
//
//        String resultJson = mvc.perform(get("/api/v1/requisitions/{id}", readDTO.getId()))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        RequisitionStatus actualStatus = objectMapper.readValue(resultJson, RequisitionStatus.class);
//        Assertions.assertThat(actualStatus).isEqualTo(readDTO.getStatus());
//
//        Mockito.verify(requisitionService).getRequisitionStatus(readDTO.getId());
//    }
//
//    @Test
//    public void testGetRequisitionStatusWrongId() throws Exception {
//        UUID wrongId = UUID.randomUUID();
//
//        EntityNotFoundException exception = new EntityNotFoundException(Requisition.class, wrongId);
//
//        Mockito.when(requisitionService.getRequisitionStatus(wrongId)).thenThrow(exception);
//
//        String resultJson = mvc.perform(get("/api/v1/requisitions/{id}", wrongId))
//                .andExpect(status().isNotFound())
//                .andReturn().getResponse().getContentAsString();
//
//        assertTrue(resultJson.contains(exception.getMessage()));
//    }
//
//    @Test
//    public void testGetRequisitionStatusWrongIdFormat() throws Exception {
//        String wrongId = "123";
//
//        String resultJson = mvc.perform(get("/api/v1/requisitions/{id}", wrongId))
//                .andExpect(status().isBadRequest())
//                .andReturn().getResponse().getContentAsString();
//
//        ErrorInfo randomError = new ErrorInfo(HttpStatus.BAD_REQUEST, Requisition.class, "error message");
//        String errorJson = objectMapper.writeValueAsString(randomError);
//
//        assertJsonEquals(resultJson, errorJson, when(IGNORING_VALUES));
//    }
//
//    private RequisitionListenerDTO createRequisitionListenerDTO() {
//        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
//
//        listenerDTO.setClientId(UUID.randomUUID());
//        listenerDTO.setTicketId(UUID.randomUUID());
//        listenerDTO.setRouteNumber("101-A");
//        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 1, 11, 11));
//        return listenerDTO;
//    }
//
//    private RequisitionCreateDTO createRequisitionCreateDTO() {
//        RequisitionCreateDTO createDTO = new RequisitionCreateDTO();
//        createDTO.setClientId(UUID.randomUUID());
//        createDTO.setTicketId(UUID.randomUUID());
//        createDTO.setRouteNumber("101-A");
//    //    createDTO.setDeparture();
//        return createDTO;
//    }
//
//
//    private RequisitionListenerDTO createRequisitionListenerDTO(UUID ticketId) {
//        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
//        listenerDTO.setId(UUID.randomUUID());
//        listenerDTO.setClientId(UUID.randomUUID());
//        listenerDTO.setTicketId(ticketId);
//        listenerDTO.setRouteNumber("101-A");
//        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 1, 11, 11));
//        return listenerDTO;
//    }
//
//    private RequisitionReadDTO createRequisitionReadDTO(UUID clientId) {
//        RequisitionReadDTO readDTO = new RequisitionReadDTO();
//        readDTO.setId(UUID.randomUUID());
//        readDTO.setClientId(clientId);
//        readDTO.setTicketId(UUID.randomUUID());
//        readDTO.setCost(123);
//        readDTO.setRouteNumber("101-A");
//        readDTO.setDeparture(LocalDateTime.of(2020, 1, 1, 11, 11));
//        readDTO.setStatus(RequisitionStatus.PROCESSING);
//        return readDTO;
//    }
//
//    private RequisitionReadDTO createRequisitionReadDTO() {
//        RequisitionReadDTO readDTO = new RequisitionReadDTO();
//        readDTO.setId(UUID.randomUUID());
//        readDTO.setClientId(UUID.randomUUID());
//        readDTO.setTicketId(UUID.randomUUID());
//        readDTO.setCost(123);
//        readDTO.setRouteNumber("101-A");
//        readDTO.setDeparture(LocalDateTime.of(2020, 1, 1, 11, 11));
//        readDTO.setStatus(RequisitionStatus.PROCESSING);
//        return readDTO;
//    }
}