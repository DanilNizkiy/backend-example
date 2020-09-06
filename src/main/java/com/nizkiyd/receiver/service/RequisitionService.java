package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionReadDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.EntityNotFoundException;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RequisitionService {

    @Autowired
    private RequisitionRepository repository;

    public UUID createRequisition(RequisitionCreateDTO createDTO) {
        UUID ticketId = createDTO.getTicketId();
        if (repository.findByTicketId(ticketId).isPresent())
            throw new DuplicateRequisitionException(ticketId);

        Requisition requisition = new Requisition();
        requisition.setClientId(createDTO.getClientId());
        requisition.setTicketId(createDTO.getTicketId());
        requisition.setDeparture(createDTO.getDeparture());
        requisition.setRouteNumber(createDTO.getRouteNumber());
        requisition.setStatus(RequisitionStatus.PROCESSING);
        requisition.setCost(costCalculation(requisition));//simulation
        requisition = repository.save(requisition);
        log.info(String.format("A requisition has been created with id %s and default status", requisition.getId()));
        return requisition.getId();
    }

    public List<RequisitionReadDTO> getClientRequisitions(UUID clientId) {
        List<Requisition> allRequisitions = new ArrayList<>();
        repository.findAll().forEach(allRequisitions::add);

        List<Requisition> clientRequisitions = allRequisitions.stream()
                .filter(r -> r.getClientId().equals(clientId))
                .filter(r -> r.getDeparture().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Requisition::getDeparture))
                .collect(Collectors.toList());
        log.info(String.format("Requisitions for user %s successfully received", clientId));


        return clientRequisitions.stream().map(this::toRead).collect(Collectors.toList());
    }

    public RequisitionStatus getRequisitionStatus(UUID id) {
        Requisition requisition = repository.findById(id).orElseThrow(() -> {
            throw new EntityNotFoundException(Requisition.class, id);
        });
        log.info(String.format("Status of requisition with ID %s received", id));
        return requisition.getStatus();
    }

    //simulation
    private Integer costCalculation(Requisition requisition) {
        return (int) (Math.random() * 10000);
    }

    private RequisitionReadDTO toRead(Requisition requisition) {
        RequisitionReadDTO readDTO = new RequisitionReadDTO();
        readDTO.setId(requisition.getId());
        readDTO.setClientId(requisition.getClientId());
        readDTO.setTicketId(requisition.getTicketId());
        readDTO.setCost(requisition.getCost());
        readDTO.setDeparture(requisition.getDeparture());
        readDTO.setRouteNumber(requisition.getRouteNumber());
        readDTO.setStatus(requisition.getStatus());
        return readDTO;
    }
}
