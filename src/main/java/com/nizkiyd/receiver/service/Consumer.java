package com.nizkiyd.receiver.service;

import com.google.gson.Gson;
import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.nizkiyd.receiver.config.RabbitConfiguration.*;


@Slf4j
@Component
public class Consumer {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = PRIMARY_QUEUE)
    public void primary(Message in) throws Exception {
        RequisitionListenerDTO listenerDTO = new Gson().fromJson(
                "{" + new String(in.getBody(), StandardCharsets.UTF_8) + "}", RequisitionListenerDTO.class);

        if (listenerDTO.getRouteNumber() == null) {
            List<Map<String, ?>> xDeathHeader = in.getMessageProperties().getXDeathHeader();
            log.info(String.format("Route number information for ticketId %s is not yet available",
                    listenerDTO.getTicketId()));
            Long count = (Long) xDeathHeader.get(0).get("count");
            if (!(count >= 3)) throw new Exception("There was an error");
        } else {
            createRequisition(listenerDTO);
        }
        log.info("createRequisitionDeadLetterWorker finished work");
    }

    private void createRequisition(RequisitionListenerDTO listenerDTO) {
        UUID ticketId = listenerDTO.getTicketId();
        if (requisitionRepository.findByTicketId(ticketId).isPresent())
            throw new DuplicateRequisitionException(ticketId);

        Requisition requisition = translationService.toRequisition(listenerDTO);
        requisition = requisitionRepository.save(requisition);

        log.info(String.format("A requisition has been created with id %s and default status", requisition.getId()));
    }
}
