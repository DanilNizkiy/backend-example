package com.nizkiyd.receiver.controller;

import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.service.RequisitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class RequisitionController {

    @Autowired
    private RequisitionService service;

    @PostMapping("/requisitions")
    public UUID createRequisition(@RequestBody RequisitionCreateDTO createDTO) {
        log.info(String.format("Received a request to create requisition from a customer with ID %s",
                createDTO.getClientId()));
        return service.createRequisition(createDTO);
    }

    @GetMapping("/clients/{clientId}/requisitions")
    public List<RequisitionReadDTO> getClientRequisitions(@PathVariable UUID clientId) {
        log.info(String.format(
                "Client %s requests all current requisition", clientId));
        return service.getClientRequisitions(clientId);
    }

    @GetMapping("/requisitions/{id}")
    public RequisitionStatus getRequisitionStatus(@PathVariable UUID id) {
        log.info(String.format("Received a request to obtain status for requisition ID %s", id));
        return service.getRequisitionStatus(id);
    }
}
