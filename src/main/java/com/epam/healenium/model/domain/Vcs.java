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
@Table(name = "vcs")
public class Vcs {

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "name")
    private String name;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "repository")
    private String repository;

    @Column(name = "branch")
    private String branch;

    @Column(name = "create_date")
    @CreationTimestamp
    private LocalDateTime createdDate;
}
