package com.example.audit.repository;

import com.example.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Optional<AuditLog> findTopByOrderByIdDesc();

    List<AuditLog> findAllByOrderByIdAsc();

    List<AuditLog> findByTimestampBeforeAndIsArchivedFalse(Instant beforeDate);

    List<AuditLog> findByActorIdOrderByIdAsc(String actorId);

    List<AuditLog> findByResourceIdOrderByIdAsc(String resourceId);

    // Scenario C additions
    List<AuditLog> findByTimestampBetweenAndIsArchivedFalse(Instant from, Instant to);

    long countByActorIdAndTimestampAfter(String actorId, Instant timestamp);
}