package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.RouteNumberException;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.util.UUID;


@Slf4j
@Component
public class Consumer {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private TranslationService translationService;

    //direct
    @RabbitListener(queues = "queue.direct")
    public void createRequisitionDirectWorker(RequisitionListenerDTO listenerDTO) {
        log.info("createRequisitionDirectWorker started work");
        createRequisition(listenerDTO);
        log.info("createRequisitionDirectWorker finished work");
    }

    //fanout
    @RabbitListener(queues = "queue.fanout.first")
    public void createRequisitionFanoutWorker1(RequisitionListenerDTO listenerDTO) {
        log.info("createRequisitionFanoutWorker1 started work");
        createDoubleRequisition(listenerDTO);
        log.info("createRequisitionFanoutWorker1 finished work");
    }

    @RabbitListener(queues = "queue.fanout.second")
    public void createRequisitionFanoutWorker2(RequisitionListenerDTO listenerDTO) throws InterruptedException {
        Thread.sleep(200);
        log.info("createRequisitionFanoutWorker2 started work");
        createDoubleRequisition(listenerDTO);
        log.info("createRequisitionFanoutWorker2 finished work");
    }

    //dead letter
    @RabbitListener(queues = "primaryWorkerQueue")
    public void createRequisitionDeadLetterWorker(RequisitionListenerDTO listenerDTO) {
        log.info("createRequisitionDeadLetterWorker started work");
        if (listenerDTO.getRouteNumber() == null){
            log.info(String.format("Route number information for ticketId %s is not yet available",
                    listenerDTO.getTicketId()));
            throw new RouteNumberException(listenerDTO.getTicketId());
        }
        createRequisition(listenerDTO);
        log.info("createRequisitionDeadLetterWorker finished work");
    }

    @RabbitListener(queues = "primaryWorkerQueue.parkingLot")
    public void deadLetterHandling(Message in) throws Exception {
        log.info("dead letter handling started work");
        //TODO
        log.info("dead letter handling finished work");
    }


    private void createDoubleRequisition(RequisitionListenerDTO listenerDTO) {
        boolean exist = requisitionRepository.findById(listenerDTO.getId()).isPresent();
        Requisition requisition;
        if (!exist) {
            requisition = translationService.toRequisition(listenerDTO, listenerDTO.getId());
        } else {
            requisition = translationService.toRequisition(listenerDTO, listenerDTO.getAdditionalId());
        }
        requisition = requisitionRepository.save(requisition);
        log.info(String.format("A requisition has been created with id %s and default status", requisition.getId()));
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
