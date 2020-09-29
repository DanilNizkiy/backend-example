package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import com.nizkiyd.receiver.dto.RequisitionCreateDTO;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TranslationService {

    public RequisitionListenerDTO toListenerDTO(RequisitionCreateDTO createDTO) {
        RequisitionListenerDTO listenerDTO = new RequisitionListenerDTO();
        listenerDTO.setClientId(createDTO.getClientId());
        listenerDTO.setTicketId(createDTO.getTicketId());
        listenerDTO.setRouteNumber(createDTO.getRouteNumber());
        listenerDTO.setDeparture(createDTO.getDeparture());
        return listenerDTO;
    }

    public Requisition toRequisition(RequisitionListenerDTO listenerDTO) {
        Requisition requisition = new Requisition();
        requisition.setId(listenerDTO.getId());
        requisition.setClientId(listenerDTO.getClientId());
        requisition.setTicketId(listenerDTO.getTicketId());
        requisition.setDeparture(listenerDTO.getDeparture());
        requisition.setRouteNumber(listenerDTO.getRouteNumber());
        requisition.setStatus(RequisitionStatus.PROCESSING);
        requisition.setCost(costCalculation(requisition));//simulation
        return requisition;
    }

    public Requisition toRequisition(RequisitionListenerDTO listenerDTO, UUID id) {
        Requisition requisition = new Requisition();
        requisition.setId(id);
        requisition.setClientId(listenerDTO.getClientId());
        requisition.setTicketId(listenerDTO.getTicketId());
        requisition.setDeparture(listenerDTO.getDeparture());
        requisition.setRouteNumber(listenerDTO.getRouteNumber());
        requisition.setStatus(RequisitionStatus.PROCESSING);
        requisition.setCost(costCalculation(requisition));//simulation
        return requisition;
    }

    //simulation
    private Integer costCalculation(Requisition requisition) {
        return (int) (Math.random() * 10000);
    }
}
