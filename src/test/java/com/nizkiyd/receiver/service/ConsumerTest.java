package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.RouteNumberException;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.nizkiyd.receiver.config.RabbitConfiguration.CREATE_REQUISITION_QUEUE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from requisition", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ConsumerTest {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private Consumer consumer;

    @Test
    public void testCreateRequisitionListener()  {
        RequisitionListenerDTO listenerDTO = createListenerCreateDTO("101-A");
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("count", 0l);
        stringObjectHashMap.put("queue", CREATE_REQUISITION_QUEUE);

        List<HashMap<String, Object>> xDeath = List.of(stringObjectHashMap);
        consumer.createRequisitionListener(listenerDTO, xDeath);

        Requisition requisition = requisitionRepository.findById(listenerDTO.getId()).get();
        Assertions.assertThat(listenerDTO).isEqualToIgnoringGivenFields(
                requisition, "additionalId");
        Assert.assertEquals(RequisitionStatus.PROCESSING, requisition.getStatus());
        Assert.assertNotNull(requisition.getCost());
    }

    @Test(expected = RouteNumberException.class)
    public void testCreateRequisitionListenerAllowedNumberOfRepetitions()  {
        String routeNumber = null;
        RequisitionListenerDTO listenerDTO = createListenerCreateDTO(routeNumber);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("count", 3l);
        stringObjectHashMap.put("queue", CREATE_REQUISITION_QUEUE);

        List<HashMap<String, Object>> xDeath = List.of(stringObjectHashMap);
        consumer.createRequisitionListener(listenerDTO, xDeath);
    }

    @Test
    public void testCreateRequisitionListenerNotAllowedNumberOfRepetitions()  {
        String routeNumber = null;
        RequisitionListenerDTO listenerDTO = createListenerCreateDTO(routeNumber);
        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("count", 51l);
        stringObjectHashMap.put("queue", CREATE_REQUISITION_QUEUE);

        List<HashMap<String, Object>> xDeath = List.of(stringObjectHashMap);
        consumer.createRequisitionListener(listenerDTO, xDeath);

        Boolean b = requisitionRepository.findById(listenerDTO.getId()).isPresent();
        Assert.assertFalse(b);
    }

    @Test(expected = DuplicateRequisitionException.class)
    public void testCreateRequisitionDuplicate() {
        UUID ticketId = UUID.randomUUID();

        HashMap<String, Object> stringObjectHashMap = new HashMap<>();
        stringObjectHashMap.put("count", 0l);
        stringObjectHashMap.put("queue", CREATE_REQUISITION_QUEUE);
        List<HashMap<String, Object>> xDeath = List.of(stringObjectHashMap);

        RequisitionListenerDTO uniqueRequisition = createListenerCreateDTO(ticketId);
        consumer.createRequisitionListener(uniqueRequisition,  xDeath);

        RequisitionListenerDTO duplicateRequisition = createListenerCreateDTO(ticketId);
        consumer.createRequisitionListener(duplicateRequisition,  xDeath);
    }

    private RequisitionListenerDTO createListenerCreateDTO(UUID ticketId) {
        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
        listenerDTO.setId(UUID.randomUUID());
        listenerDTO.setClientId(UUID.randomUUID());
        listenerDTO.setTicketId(ticketId);
        listenerDTO.setRouteNumber("101-A");
        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 9, 11, 30));
        return listenerDTO;
    }

    private RequisitionListenerDTO createListenerCreateDTO(String routeNumber) {
        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
        listenerDTO.setId(UUID.randomUUID());
        listenerDTO.setClientId(UUID.randomUUID());
        listenerDTO.setTicketId(UUID.randomUUID());
        listenerDTO.setRouteNumber(routeNumber);
        listenerDTO.setDeparture(LocalDateTime.of(2020, 1, 9, 11, 30));
        return listenerDTO;
    }
}