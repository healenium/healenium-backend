package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import com.epam.healenium.tenant.TenantAwareEntity;
import com.epam.healenium.tenant.TenantEntityListener;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@lombok.experimental.Accessors(chain = true)
@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name = "healing_result")
public class HealingResult implements TenantAwareEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "healing_result_seq")
    @SequenceGenerator(name = "healing_result_seq", sequenceName = "healing_result_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "locator", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    private Locator locator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "healing_id", referencedColumnName = "uid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Healing healing;

    @Column(name = "score")
    private Double score;

    @Column(name = "success_healing")
    private boolean successHealing = true;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createDate;

}