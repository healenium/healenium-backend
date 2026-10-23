package com.epam.healenium.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Entity
@Table(name = "llm_model")
public class LlmModel {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "name", nullable = false, length = 264)
    private String name;

    @Column(name = "protocol", nullable = false, length = 16)
    private String protocol;

    @Column(name = "base_url", nullable = false, length = 512)
    private String baseUrl;
}
