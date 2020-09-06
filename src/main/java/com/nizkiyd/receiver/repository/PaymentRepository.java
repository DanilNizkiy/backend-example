package com.nizkiyd.receiver.repository;

import com.nizkiyd.receiver.domain.Payment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends CrudRepository<Payment, UUID> {

    Optional<Payment> findByRequisitionId(UUID requisitionId);
}
