package com.epam.healenium.model.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

/**
 * Represent record about known element healing attempt in specific context.
 * If healing finish successfully, then it must have at least one {@link HealingResult}
 */

@Accessors(chain = true)
@Data
@NoArgsConstructor
@Document(collection = "healing_document")
public class Healing {

    @Id
    private String uid;
    @DBRef
    private Selector selector;
    @DBRef
    private Set<HealingResult> results;
    @ToString.Exclude
    private String pageContent;

    public Healing (String uid, Selector selector, String pageContent) {
        this.uid = uid;
        this.selector = selector;
        this.pageContent = pageContent;
    }
}