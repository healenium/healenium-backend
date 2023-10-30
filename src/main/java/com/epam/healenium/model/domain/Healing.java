package com.epam.healenium.model.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represent record about known element healing attempt in specific context.
 * If healing finish successfully, then it must have at least one {@link HealingResult}
 */

@Accessors(chain = true)
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "healing")
public class Healing {

    @Id
    @Column(name = "uid")
    private String uid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selector_id", referencedColumnName = "uid", nullable = false)
    private Selector selector;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "healing")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<HealingResult> results;

    @Column(name = "page_content")
    @ToString.Exclude
    private String pageContent;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;

    public Healing (String uid, Selector selector, String pageContent) {
        this.uid = uid;
        this.selector = selector;
        this.pageContent = pageContent;
    }
}