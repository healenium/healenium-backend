package com.epam.healenium.model.domain;

import com.epam.healenium.converter.NodeConverter;
import com.epam.healenium.model.Locator;
import com.epam.healenium.model.wrapper.NodePathWrapper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Contains selector information.
 * Speaking general, this is locator used if specific test class method and it's last successful position on page .
 */

@Accessors(chain = true)
@Data
@Entity
@Table(name = "selector")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Selector {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "name")
    private String name;

    @Column(name = "class_name")
    private String className;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "locator", columnDefinition = "json")
    @Type(type = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @ColumnTransformer(read = "locator::json")
    private Locator locator;

    @Column(name = "node_path")
    @ToString.Exclude
    @Convert(converter = NodeConverter.class)
    private NodePathWrapper nodePathWrapper;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

}