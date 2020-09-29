package com.nizkiyd.receiver.service;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.dto.RequisitionListenerDTO;
import com.nizkiyd.receiver.exception.DuplicateRequisitionException;
import com.nizkiyd.receiver.exception.RouteNumberException;
import com.nizkiyd.receiver.repository.RequisitionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.nizkiyd.receiver.config.RabbitConfiguration.*;


@Slf4j
@Component
public class Consumer {

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private TranslationService translationService;

    //dead letter
    @RabbitListener(queues = CREATE_REQUISITION_QUEUE)
    public void createRequisitionListener(RequisitionListenerDTO listenerDTO, @Header(value = "x-death", required = false) List<HashMap<String, Object>> xDeath) {
        log.info("createRequisitionListener started to work");
        if (listenerDTO.getRouteNumber() == null) {
            if (hasExceededRetryCount(xDeath, CREATE_REQUISITION_QUEUE, (long) 50)){
                log.info("The number of attempts to create an entity has been exceeded, the task will " +
                        "be removed from the queue");
                return;
            };
            throw new RouteNumberException(listenerDTO.getTicketId());
        }
        createRequisition(listenerDTO);
        log.info("createRequisitionListener finished work");
    }

    //fanout
    @RabbitListener(queues = CREATE_DOUBLE_REQUISITION_QUEUE1)
    public void createDoubleRequisitionWorker1(RequisitionListenerDTO listenerDTO, @Header(value = "x-death", required = false) List<HashMap<String, Object>> xDeath) {
        log.info("createDoubleRequisitionWorker1 started work");
        if (listenerDTO.getRouteNumber() == null) {
            if (hasExceededRetryCount(xDeath, CREATE_DOUBLE_REQUISITION_QUEUE1, (long) 50)){
                log.info("The number of attempts to create an entity has been exceeded, the task will " +
                        "be removed from the queue");
                return;
            };
            throw new RouteNumberException(listenerDTO.getTicketId());
        }
        createDoubleRequisition(listenerDTO);
        log.info("createDoubleRequisitionWorker1 finished work");
    }

    @RabbitListener(queues = CREATE_DOUBLE_REQUISITION_QUEUE2)
    public void createDoubleRequisitionWorker2(RequisitionListenerDTO listenerDTO, @Header(value = "x-death", required = false) List<HashMap<String, Object>> xDeath) throws InterruptedException {
        Thread.sleep(200);
        log.info("createDoubleRequisitionWorker2 started work");
        if (listenerDTO.getRouteNumber() == null) {
            if (hasExceededRetryCount(xDeath, CREATE_DOUBLE_REQUISITION_QUEUE2, (long) 50)){
                log.info("The number of attempts to create an entity has been exceeded, the task will " +
                        "be removed from the queue");
                return;
            };
            throw new RouteNumberException(listenerDTO.getTicketId());
        }
        createDoubleRequisition(listenerDTO);
        log.info("createDoubleRequisitionWorker2 finished work");
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

    private boolean hasExceededRetryCount(List<HashMap<String, Object>> xDeath, String queueName, Long retryCount) {
        if (xDeath != null && xDeath.size() >= 1) {
            final Long[] count = new Long[1];
            xDeath.stream()
                    .filter(i -> i.get("queue").equals(queueName))
                    .forEach(i -> count[0] = (Long) i.get("count"));
            return count[0] >= retryCount;
        }
        return false;
    }
}
