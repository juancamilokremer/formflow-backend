package com.kodelabs.formflow.modules.auth.infrastructure.persistence.repository;

import com.kodelabs.formflow.modules.auth.domain.model.EmailTokenType;
import com.kodelabs.formflow.modules.auth.infrastructure.persistence.entity.EmailTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface EmailTokenJpaRepository extends JpaRepository<EmailTokenJpaEntity, UUID> {

    Optional<EmailTokenJpaEntity> findByTokenHashAndType(String tokenHash, EmailTokenType type);

    @Modifying
    @Query("UPDATE EmailTokenJpaEntity et SET et.usedAt = :now " +
           "WHERE et.userId = :userId AND et.type = :type AND et.usedAt IS NULL")
    void markAllUsedByUserIdAndType(@Param("userId") UUID userId,
                                    @Param("type") EmailTokenType type,
                                    @Param("now") Instant now);
}
