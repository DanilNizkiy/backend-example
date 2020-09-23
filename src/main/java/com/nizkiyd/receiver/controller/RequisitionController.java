package com.nizkiyd.receiver.controller;

import com.google.gson.Gson;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.service.RequisitionService;
import com.nizkiyd.receiver.service.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;


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

    @PostMapping("/requisitions/dead-letter")
    public UUID deadLetterCreateRequisition(@RequestBody RequisitionCreateDTO createDTO) throws Exception{
        log.info(String.format(
                LocalTime.now().toString() + " Received a request to create requisition from a clientId %s",
                createDTO.getClientId()));
        RequisitionListenerDTO requisitionListenerDTO = translationService.toListenerDTO(createDTO);
        UUID requisitionId = UUID.randomUUID();
        requisitionListenerDTO.setId(requisitionId);

        template.convertAndSend("tutorial-exchange", "primaryRoutingKey", requisitionListenerDTO.toString());
        return requisitionId;
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
