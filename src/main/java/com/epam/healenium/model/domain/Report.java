package com.epam.healenium.model.domain;

import com.epam.healenium.converter.RecordWrapperConverter;
import com.epam.healenium.model.wrapper.RecordWrapper;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

/**
 * Represent report about healing during test.
 * Contains information about healed locators and value that was selected as healed.
 */

@Accessors(chain = true)
@Data
@Entity
@Table(name = "report")
public class Report {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "elements", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @Convert(converter = RecordWrapperConverter.class)
    private RecordWrapper recordWrapper = new RecordWrapper();

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Version
    @Column(name = "version")
    private Long version;

}
