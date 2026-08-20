package com.example.audit.repository;

import com.example.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<AuditLog> findTopByOrderByIdDesc();

    List<AuditLog> findAllByOrderByIdAsc();
    List<AuditLog> findByTimestampBeforeAndIsArchivedFalse(Instant beforeDate);
    List<AuditLog> findByActorIdOrderByIdAsc(String actorId);
    List<AuditLog> findByResourceIdOrderByIdAsc(String resourceId);

    int countByActorIdAndTimestampAfter(String actorId, Instant timestamp);

    // FIXED ARCH-06: Database-level aggregation to prevent memory exhaustion
    @Query("SELECT a.eventType as eventType, COUNT(a) as total FROM AuditLog a WHERE a.timestamp BETWEEN :start AND :end AND a.isArchived = false GROUP BY a.eventType")
    List<Object[]> countEventsByType(@Param("start") Instant start, @Param("end") Instant end);
}