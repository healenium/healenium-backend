package com.epam.healenium.model.domain;

import com.epam.healenium.tenant.TenantAwareEntity;
import com.epam.healenium.tenant.TenantEntityListener;

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
@EntityListeners(TenantEntityListener.class)
@Table(name = "vcs")
public class Vcs implements TenantAwareEntity {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "name")
    private String name;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "repository")
    private String repository;

    @Column(name = "branch")
    private String branch;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
