package com.epam.healenium.model.domain;

import com.epam.healenium.converter.NodeConverter;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

/**
 * Contains selector information.
 * Speaking general, this is locator used if specific test class method and it's last successful position on page .
 */

@Accessors(chain = true)
@Data
@Entity
@Table(name = "selector")
public class Selector {

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

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "enable_healing")
    private Boolean enableHealing;

}