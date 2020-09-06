package com.nizkiyd.receiver.job;

import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.repository.PaymentRepository;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"test", "integration-test"})
@Sql(statements = {"delete from requisition", "delete from payment"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RequisitionsExecutionJobTest {

    @Autowired
    private RequisitionsExecutionJob requisitionsExecutionJob;

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Test
    public void testRequisitionsExecution() {
        Requisition r1 = createRequisition(RequisitionStatus.PROCESSING);
        Requisition r2 = createRequisition(RequisitionStatus.PROCESSING);
        Requisition r3 = createRequisition(RequisitionStatus.PROCESSING);
        Requisition r4 = createRequisition(RequisitionStatus.PROCESSING);

        requisitionsExecutionJob.requisitionsExecution();

        Requisition actualR1 = requisitionRepository.findById(r1.getId()).get();
        Requisition actualR2 = requisitionRepository.findById(r2.getId()).get();
        Requisition actualR3 = requisitionRepository.findById(r3.getId()).get();
        Requisition actualR4 = requisitionRepository.findById(r4.getId()).get();

        assertTrue(Stream.of(actualR1.getStatus(), actualR2.getStatus(), actualR3.getStatus(), actualR4.getStatus())
                .anyMatch(i -> i != RequisitionStatus.PROCESSING));
    }

    @Test
    public void testRequisitionsExecutionCrashAfterPaymentGateway() {
        Requisition requisition = createRequisition(RequisitionStatus.STARTED_TO_PROCESS);

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/api/v1")
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();

        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setRequisitionId(requisition.getId());
        paymentCreateDTO.setAmount(requisition.getCost());

        PaymentStatus paymentStatus = webClient
                .method(HttpMethod.POST)
                .uri("/payment-gateway/payment-process")
                .body(BodyInserters.fromPublisher(Mono.just(paymentCreateDTO), PaymentCreateDTO.class))
                .retrieve()
                .bodyToMono(PaymentStatus.class)
                .block();
        RequisitionStatus requisitionStatus = RequisitionStatus.valueOf(paymentStatus.toString());

        requisitionsExecutionJob.requisitionsExecution();

        Requisition actualRequisition = requisitionRepository.findById(requisition.getId()).get();
        Assert.assertEquals(requisitionStatus, actualRequisition.getStatus());
    }

    @Test
    public void testRequisitionsExecutionCrashBeforePaymentGateway() {
        Requisition requisition = createRequisition(RequisitionStatus.STARTED_TO_PROCESS);

        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/api/v1")
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();

        HttpStatus httpStatus = webClient
                .method(HttpMethod.GET)
                .uri("/payment-gateway/payment/requisitions/{requisitionId}", requisition.getId())
                .exchange()
                .block()
                .statusCode();
        Assert.assertEquals(HttpStatus.NOT_FOUND, httpStatus);

        requisitionsExecutionJob.requisitionsExecution();

        Requisition actualRequisition = requisitionRepository.findById(requisition.getId()).get();
        Assert.assertNotEquals(RequisitionStatus.STARTED_TO_PROCESS, actualRequisition.getStatus());

        PaymentStatus paymentStatusAfterChange = webClient
                .method(HttpMethod.GET)
                .uri("/payment-gateway/payment/requisitions/{requisitionId}", requisition.getId())
                .exchange()
                .flatMap(response -> response.bodyToMono(PaymentStatus.class))
                .block();
        Assert.assertEquals(RequisitionStatus.valueOf(paymentStatusAfterChange.toString()), actualRequisition.getStatus());

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