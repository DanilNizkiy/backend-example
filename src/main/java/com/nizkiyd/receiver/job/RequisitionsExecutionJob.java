package com.nizkiyd.receiver.job;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@Service
public class RequisitionsExecutionJob {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Value("${number.threads}")
    protected int parallelismLevel;

    @Value("${threshold}")
    protected int threshold;

    @Autowired
    private ParallelRequisitionsExecution parallelRequisitionsExecution;

    @Transactional
    @Scheduled(cron = "${payment.process.job.cron}")
    public void requisitionsExecution() {
        log.info("Job started");
        List<Requisition> requisitions = requisitionRepository.findByRequisitionStatus(
                RequisitionStatus.STARTED_TO_PROCESS, RequisitionStatus.PROCESSING);
        log.info(String.format("Received %s requisitions for execution", requisitions.size()));

        WebClient webClient = webClientBuilder();

        ForkJoinPool pool = new ForkJoinPool(parallelismLevel);
        pool.invoke(
                new ParallelRequisitionsExecution(0, requisitions.size(), threshold, requisitions, webClient, requisitionRepository));
        log.info("Job finished");
    }

    private WebClient webClientBuilder() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080/api/v1")
                .defaultCookie("cookieKey", "cookieValue")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
                .build();
        return webClient;
    }
}
