package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from requisition", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RequisitionServiceTest {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private RequisitionService requisitionService;

    @Test
    public void testGetClientRequisitions() {
        UUID clientId = UUID.randomUUID();

        Requisition r1 = createRequisition(LocalDateTime.of(2030, 1, 1, 1, 30), clientId);
        Requisition r2 = createRequisition(LocalDateTime.of(2030, 1, 1, 1, 10), clientId);
        Requisition r3 = createRequisition(LocalDateTime.of(2030, 1, 1, 1, 40), clientId);
        createRequisition(LocalDateTime.of(2011, 1, 1, 11, 11), clientId);
        createRequisition(LocalDateTime.of(2030, 1, 1, 1, 40), UUID.randomUUID());

        List<RequisitionReadDTO> clientRequisitionsReadDTO = requisitionService.getClientRequisitions(clientId);
        Assertions.assertThat(clientRequisitionsReadDTO).extracting(RequisitionReadDTO::getId)
                .isEqualTo(Arrays.asList(r2.getId(), r1.getId(),  r3.getId()));
        Assert.assertEquals(3, clientRequisitionsReadDTO.size());
    }

    @Test
    public void getRequisitionStatus() {
        Requisition requisition = new Requisition();
        requisition.setId(UUID.randomUUID());
        requisition.setClientId(UUID.randomUUID());
        requisition.setTicketId(UUID.randomUUID());
        requisition.setCost(123);
        requisition.setDeparture(LocalDateTime.of(2020, 1, 9, 11, 30));
        requisition.setRouteNumber("101-A");
        requisition.setStatus(RequisitionStatus.PROCESSING);
        requisition = requisitionRepository.save(requisition);

        RequisitionStatus status = requisitionService.getRequisitionStatus(requisition.getId());
        Assert.assertEquals(requisition.getStatus(), status);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getRequisitionWrongId() {
        requisitionService.getRequisitionStatus(UUID.randomUUID());
    }

    private Requisition createRequisition(LocalDateTime localDateTime, UUID clientId){
        Requisition requisition = new Requisition();
        requisition.setId(UUID.randomUUID());
        requisition.setClientId(clientId);
        requisition.setTicketId(UUID.randomUUID());
        requisition.setCost(123);
        requisition.setDeparture(localDateTime);
        requisition.setRouteNumber("101-A");
        requisition.setStatus(RequisitionStatus.PROCESSING);
        return requisitionRepository.save(requisition);
    }
}