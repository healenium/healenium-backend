package com.epam.healenium.model.domain;

import com.epam.healenium.converter.RecordWrapperConverter;
import com.epam.healenium.model.wrapper.RecordWrapper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * Represent report about healing during test.
 * Contains information about healed locators and value that was selected as healed.
 */

@Accessors(chain = true)
@Data
@Entity
@Table(name = "report")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class Report {

    @Id
    @GeneratedValue(generator = "report-uuid-generator")
    @GenericGenerator(name = "report-uuid-generator", strategy = "com.epam.healenium.generator.ReportUUIDGenerator")
    @Column(name = "uid")
    private String uid;

    @Column(name = "elements", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    @Convert(converter = RecordWrapperConverter.class)
    private RecordWrapper recordWrapper = new RecordWrapper();

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

}
