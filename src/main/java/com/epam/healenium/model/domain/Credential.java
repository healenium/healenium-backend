package com.epam.healenium.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Accessors(chain = true)
@Data
@Entity
@Table(name = "credential")
public class Credential {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "name")
    private String name;

    @Column(name = "token")
    private String token;

    @Column(name = "resource")
    private String resource;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
