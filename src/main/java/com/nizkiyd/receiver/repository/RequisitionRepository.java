package com.nizkiyd.receiver.repository;

import com.nizkiyd.receiver.domain.Requisition;
import com.nizkiyd.receiver.domain.RequisitionStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RequisitionRepository extends CrudRepository<Requisition, UUID> {

    Optional<UUID> findByTicketId(UUID ticketId);

    @Query("select r from Requisition r where r.status = :s1 or r.status = :s2")
    List<Requisition> findByRequisitionStatus(@Param("s1") RequisitionStatus s1, @Param("s2") RequisitionStatus s2);


}
