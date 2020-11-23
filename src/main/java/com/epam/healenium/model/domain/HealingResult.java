package com.epam.healenium.model.domain;

import com.epam.healenium.model.Locator;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
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

    @Column(name = "success_healing")
    private boolean successHealing = true;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createDate;

}