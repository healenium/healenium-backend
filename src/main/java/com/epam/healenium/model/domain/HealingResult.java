package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "healing_result")
@TypeDefs({
        @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class HealingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "healing_result_seq")
    @SequenceGenerator(name = "healing_result_seq", sequenceName = "healing_result_seq", allocationSize = 1)
    private Integer id;

    @Column(name = "locator", columnDefinition = "jsonb")
    @Type(type = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private Locator locator;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "healing_id", referencedColumnName = "uid")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Healing healing;

    @Column(name = "score")
    private Double score;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createDate;

}