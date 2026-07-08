package com.epam.healenium.model.domain;
import com.epam.healenium.tenant.TenantAwareEntity;
import com.epam.healenium.tenant.FreeTenantEntityListener;
import com.epam.healenium.tenant.ProTenantEntityListener;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Accessors(chain = true)
@Data
@Entity
@EntityListeners({FreeTenantEntityListener.class, ProTenantEntityListener.class})
@Table(name = "llm")
public class Llm implements TenantAwareEntity {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "name")
    private String name;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
