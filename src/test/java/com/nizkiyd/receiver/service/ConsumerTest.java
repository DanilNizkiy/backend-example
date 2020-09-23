package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from requisition", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ConsumerTest {
//
//    @Autowired
//    private RequisitionRepository requisitionRepository;
//
//    @Autowired
//    private Consumer consumer;
//
////    @Test
////    public void testCreateRequisition()  {
////        RequisitionListenerDTO listenerDTO = createListenerCreateDTO();
////        consumer.createRequisitionDirectWorker(listenerDTO);
////
////        Requisition requisition = requisitionRepository.findById(listenerDTO.getId()).get();
////        Assertions.assertThat(listenerDTO).isEqualToIgnoringGivenFields(
////                requisition, "additionalId");
////        Assert.assertEquals(RequisitionStatus.PROCESSING, requisition.getStatus());
////        Assert.assertNotNull(requisition.getCost());
////    }
////
////    @Test(expected = DuplicateRequisitionException.class)
////    public void testCreateRequisitionDuplicate() {
////        UUID ticketId = UUID.randomUUID();
////
////        RequisitionListenerDTO uniqueRequisition = createListenerCreateDTO(ticketId);
////        consumer.createRequisitionDirectWorker(uniqueRequisition);
////
////        RequisitionListenerDTO duplicateRequisition = createListenerCreateDTO(ticketId);
////        consumer.createRequisitionDirectWorker(duplicateRequisition);
////    }
//
//    private RequisitionListenerDTO createListenerCreateDTO(UUID ticketId) {
//        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
//        listenerDTO.setId(UUID.randomUUID());
//        listenerDTO.setClientId(UUID.randomUUID());
//        listenerDTO.setTicketId(ticketId);
//        listenerDTO.setRouteNumber("101-A");
//        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 9, 11, 30));
//        return listenerDTO;
//    }
//
//    private RequisitionListenerDTO createListenerCreateDTO() {
//        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
//        listenerDTO.setId(UUID.randomUUID());
//        listenerDTO.setClientId(UUID.randomUUID());
//        listenerDTO.setTicketId(UUID.randomUUID());
//        listenerDTO.setRouteNumber("101-A");
//        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 9, 11, 30));
//        return listenerDTO;
//    }
}