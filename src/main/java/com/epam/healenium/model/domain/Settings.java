package com.epam.healenium.model.domain;

import com.epam.healenium.tenant.TenantAwareEntity;
import com.epam.healenium.tenant.TenantEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Accessors(chain = true)
@Data
@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name = "settings")
public class Settings implements TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, updatable = false, unique = true)
    private UUID tenantId;

    @Column(name = "heal_enabled", nullable = false)
    private Boolean healEnabled = true;

    @Column(name = "recovery_tries", nullable = false)
    private Integer recoveryTries = 1;

    @Column(name = "score_cap", nullable = false)
    private Double scoreCap = 0.60;

    @Column(name = "healed_type", nullable = false, length = 20)
    private String healedType = "css";

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
