package com.epam.healenium.repository;

import com.epam.healenium.model.domain.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, String> {
    Optional<Credential> findByName(String name);
}
