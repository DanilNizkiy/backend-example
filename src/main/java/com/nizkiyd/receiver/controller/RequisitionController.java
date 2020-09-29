package com.nizkiyd.receiver.controller;

import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.service.RequisitionService;
import com.nizkiyd.receiver.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.nizkiyd.receiver.config.RabbitConfiguration.*;


@Slf4j
@RestController
@RequestMapping("/api/v1")
public class RequisitionController {

    @Autowired
    private RequisitionService service;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RabbitTemplate template;

    @Value("${rabbitMQ.CREATE_REQUISITION_QUEUE}")
    private String queueName;

    @PostMapping("/requisitions")
    public UUID deadLetterCreateRequisition(@RequestBody RequisitionCreateDTO createDTO) {
        log.info(String.format(
                LocalTime.now().toString() + " Received a request to create requisition from a clientId %s",
                createDTO.getClientId()));
        RequisitionListenerDTO requisitionListenerDTO = translationService.toListenerDTO(createDTO);
        UUID requisitionId = UUID.randomUUID();
        requisitionListenerDTO.setId(requisitionId);

        template.convertAndSend(CREATE_REQUISITION_EXCHANGE, queueName, requisitionListenerDTO);
        return requisitionId;
    }

    @PostMapping("/double-requisitions")
    public String fanoutCreateRequisition(@RequestBody RequisitionCreateDTO createDTO) {
        log.info(String.format("Received a request to create requisition from a clientId %s", createDTO.getClientId()));
        RequisitionListenerDTO requisitionListenerDTO = translationService.toListenerDTO(createDTO);
        UUID requisitionId1 = UUID.randomUUID();
        UUID requisitionId2 = UUID.randomUUID();
        requisitionListenerDTO.setId(requisitionId1);
        requisitionListenerDTO.setAdditionalId(requisitionId2);

        template.setExchange(CREATE_DOUBLE_REQUISITION_EXCHANGE);
        template.convertAndSend(requisitionListenerDTO);

        return requisitionId1.toString() + " and " + requisitionId2.toString();
    }

    @GetMapping("/clients/{clientId}/requisitions")
    public List<RequisitionReadDTO> getClientRequisitions(@PathVariable UUID clientId) {
        log.info(String.format("Client %s requests all current requisition", clientId));
        return service.getClientRequisitions(clientId);
    }

    @GetMapping("/requisitions/{id}")
    public RequisitionStatus getRequisitionStatus(@PathVariable UUID id) {
        log.info(String.format("Received a request to obtain status for requisition ID %s", id));
        return service.getRequisitionStatus(id);
    }
}
