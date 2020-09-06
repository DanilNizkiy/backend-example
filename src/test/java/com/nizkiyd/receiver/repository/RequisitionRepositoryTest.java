package com.nizkiyd.receiver.repository;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from requisition", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RequisitionRepositoryTest {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Test
    @Transactional
    public void testFindByRequisitionStatus() {
        createRequisition(RequisitionStatus.STARTED_TO_PROCESS);
        createRequisition(RequisitionStatus.PROCESSING);
        createRequisition(RequisitionStatus.PROCESSING);

        List<Requisition> requisitions = requisitionRepository.findByRequisitionStatus(RequisitionStatus.STARTED_TO_PROCESS, RequisitionStatus.PROCESSING);
        Assert.assertEquals(3, requisitions.size());
    }

    private Requisition createRequisition(RequisitionStatus status) {
        Requisition requisition = new Requisition();
        requisition.setRouteNumber("101-A");
        requisition.setClientId(UUID.randomUUID());
        requisition.setTicketId(UUID.randomUUID());
        requisition.setDeparture(LocalDateTime.now());
        requisition.setStatus(status);
        requisition.setCost(123);
        requisitionRepository.save(requisition);
        return requisition;
    }

}