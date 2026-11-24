package com.epam.healenium.model.domain;

import com.epam.healenium.converter.NodeConverter;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.epam.healenium.tenant.TenantAwareEntity;
import com.epam.healenium.tenant.TenantEntityListener;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Contains selector information.
 * Speaking general, this is locator used if specific test class method and it's last successful position on page .
 */

@Accessors(chain = true)
@Data
@Entity
@EntityListeners(TenantEntityListener.class)
@Table(name = "selector")
public class Selector implements TenantAwareEntity {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "url")
    private String url;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "locator", columnDefinition = "json")
    @JdbcTypeCode(SqlTypes.JSON)
    @Basic(fetch = FetchType.LAZY)
    @ColumnTransformer(read = "locator::json")
    private Locator locator;

    @Column(name = "command")
    private String command;

    @Column(name = "node_path")
    @ToString.Exclude
    @Convert(converter = NodeConverter.class)
    private NodePathWrapper nodePathWrapper;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "enable_healing")
    private Boolean enableHealing;

}