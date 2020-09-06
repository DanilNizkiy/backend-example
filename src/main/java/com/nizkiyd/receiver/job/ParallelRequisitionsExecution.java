package com.nizkiyd.receiver.job;

import com.nizkiyd.receiver.domain.PaymentStatus;
import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.PaymentCreateDTO;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.RecursiveAction;

@Slf4j
@Service
public class ParallelRequisitionsExecution extends RecursiveAction {

    private RequisitionRepository requisitionRepository;
    private int start;
    private int end;
    private int threshold;
    private List<Requisition> requisitions;
    private WebClient webClient;
    private PaymentStatus paymentStatus;

    public ParallelRequisitionsExecution(int start, int end, int threshold,
                                         List<Requisition> requisitions, WebClient webClient,
                                         RequisitionRepository requisitionRepository) {
        this.start = start;
        this.end = end;
        this.threshold = threshold;
        this.requisitions = requisitions;
        this.webClient = webClient;
        this.requisitionRepository = requisitionRepository;
    }

    public ParallelRequisitionsExecution() {
    }

    @Override
    protected void compute() {
        if ((end - start) < threshold) {
            for (int i = start; i < end; i++) {
                if (requisitions.get(i).getStatus() == RequisitionStatus.STARTED_TO_PROCESS) {
                    Mono<ClientResponse> verificationResult = paymentVerificationByRequisitionId(
                            requisitions.get(i).getId(), webClient);

                    HttpStatus httpStatus = verificationResult.block().statusCode();
                    if (httpStatus == HttpStatus.NOT_FOUND) {
                        paymentStatus = payment(requisitions.get(i), webClient);
                    } else {
                        paymentStatus = verificationResult
                                .flatMap(response -> response.bodyToMono(PaymentStatus.class))
                                .block();
                    }
                } else {
                    requisitions.get(i).setStatus(RequisitionStatus.STARTED_TO_PROCESS);
                    requisitionRepository.save(requisitions.get(i));

                    paymentStatus = payment(requisitions.get(i), webClient);
                }
                RequisitionStatus requisitionStatus = RequisitionStatus.valueOf(paymentStatus.toString());
                requisitions.get(i).setStatus(requisitionStatus);
                requisitionRepository.save(requisitions.get(i));
                log.info(String.format("Requisition with id %s received status %s",
                        requisitions.get(i).getId(), requisitionStatus.toString()));
            }
        } else {
            int middle = (start + end) / 2;
            invokeAll(
                    new ParallelRequisitionsExecution(start, middle, threshold, requisitions, webClient, requisitionRepository),
                    new ParallelRequisitionsExecution(middle, end, threshold, requisitions, webClient, requisitionRepository));
        }
    }


    private PaymentStatus payment(Requisition requisition, WebClient webClient) {
        PaymentCreateDTO paymentCreateDTO = new PaymentCreateDTO();
        paymentCreateDTO.setRequisitionId(requisition.getId());
        paymentCreateDTO.setAmount(requisition.getCost());

        paymentStatus = webClient
                .method(HttpMethod.POST)
                .uri("/payment-gateway/payment-process")
                .body(BodyInserters.fromPublisher(Mono.just(paymentCreateDTO), PaymentCreateDTO.class))
                .retrieve()
                .bodyToMono(PaymentStatus.class)
                .block();
        return paymentStatus;
    }

    private Mono<ClientResponse> paymentVerificationByRequisitionId(UUID requisitionId, WebClient webClient) {
        return webClient
                .method(HttpMethod.GET)
                .uri("/payment-gateway/payment/requisitions/{requisitionId}", requisitionId)
                .exchange();
    }


}
